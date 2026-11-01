# Kitchensink modernization charter

This file is the reference for the rewrite. Follow it before adding code. When a later decision conflicts with this document, update this document first.

## Intent

Replace the Jakarta EE / JBoss EAP quickstart with a **Spring Boot** application structured as **hexagonal architecture** and **DDD** (bounded context, domain model, application use cases, adapters). Preserve the **business behavior** of member registration; do not preserve EAP, JSF, EJB, or JPA-on-the-entity design.

## 1. Source of truth vs. working tree

| Location | Role |
|---|---|
| `~/wspace/vibe/jboss-eap-quickstarts/kitchensink` | **Read-only knowledge base.** Behavior, validation rules, REST shapes, localization, and tests live there as history. Do not modify it for this rewrite. |
| `~/wspace/vibe/kitchensink-sb` | **New product.** Spring Boot, hexagonal modules, new contracts. This tree is the only place we implement. |

Rules:

- Consult the old repo to answer “what did the product do?”
- Do not copy EAP types, XML descriptors, JSF pages, or `Member` as a JPA entity into the new domain.
- If behavior is ambiguous, prefer the old integration tests and REST resource over README prose.
- Do not dual-write: once a use case exists here, change it here only.

### Behavior to preserve (from the knowledge base)

- Register a member with **name**, **email**, **phone number**.
- List members (ordered by name).
- Look up a member by id.
- Name: required, 1–25 characters, no digits.
- Email: required, valid email, **unique**.
- Phone: required, 10–12 digits.
- Duplicate email is a **conflict**, not a generic failure.
- Validation failures return **field-level** messages.
- Missing member by id is **not found**.
- UI language (en / es / fr) is a later adapter concern; domain language stays English identifiers.

## 2. Target stack and architecture

- **Runtime:** Spring Boot (Java 17+), executable application (no EAP provisioned server).
- **Style:** Hexagonal (ports and adapters) + DDD tactical patterns (aggregate, value objects, domain events, domain exceptions).
- **One bounded context for v1:** **Membership** (register and query members). Do not invent extra contexts until a real boundary appears.

```
                    ┌──────────── api ────────────┐
                    │ HTTP / OpenAPI / DTOs       │
                    └──────────────┬──────────────┘
                                   │ depends on
                    ┌──────────── app ────────────┐
                    │ use cases, inbound ports    │
                    └──────────────┬──────────────┘
                                   │ depends on
                    ┌────────── domain ───────────┐
                    │ model, rules, outbound ports│
                    └──────────────▲──────────────┘
                                   │ implements
                    ┌────────── infra ────────────┐
                    │ persistence, messaging,     │
                    │ config, clock, ids          │
                    └─────────────────────────────┘
```

Dependency rule (never invert):

```
api → app → domain ← infra
```

- `domain` depends on **nothing** in this project (no Spring, no JPA, no HTTP).
- `app` depends only on `domain`.
- `api` depends on `app` (and may use `domain` types only if they are stable value objects; prefer API DTOs).
- `infra` depends on `domain` and `app` (implements ports). It must not be imported by `domain` or `app`.
- The Spring Boot application module (or `infra` + a thin `bootstrap`) wires adapters at startup.

## 3. Modules

Maven multi-module (artifact names may use a `kitchensink-` prefix). Packages follow `org.kitchensink.<module>...`.

### `domain`

Owns the Membership model and rules.

- Aggregate: `Member` (identity, name, email, phone).
- Value objects for email and phone (invariants in constructors / factories).
- Domain exceptions: e.g. `EmailAlreadyRegistered`, `MemberNotFound`, `InvalidMember`.
- Outbound ports (interfaces): e.g. `MemberRepository`, `MemberEventPublisher`.
- Domain events: e.g. `MemberRegistered`.
- No REST annotations, no persistence annotations, no Spring stereotypes.

### `app`

Owns use cases (application services).

- Inbound ports: `RegisterMember`, `ListMembers`, `GetMember`.
- Commands / queries as explicit types (`RegisterMemberCommand`, etc.).
- Orchestrates domain + outbound ports. Transaction boundary is declared here (port or interceptor implemented in infra), not in controllers.
- Maps domain exceptions to application outcomes; does not build HTTP responses.

### `api`

Owns HTTP contracts and inbound adapters.

- REST controllers, request/response DTOs, OpenAPI.
- Bean Validation on **API DTOs only** (transport checks). Uniqueness and “email taken” stay in domain/app.
- Exception handlers that translate application/domain errors into the error envelope (below).
- No `EntityManager`, no JPA entities, no direct repository implementations.

### `infra`

Owns outbound adapters and technical wiring.

- Persistence (Spring Data / JPA **entities separate from domain** `Member`).
- Mapping: domain ↔ persistence.
- Logging, metrics, i18n message sources, clock, id generation.
- Spring configuration, datasource, profiles (`local`, `test`).

Do not add a fifth business module without updating this file.

## 4. API contracts

Public HTTP is a **versioned contract**, independent of the domain model.

### Resources (v1, aligned with the old `/rest/members` behavior)

| Method | Path | Success | Notes |
|---|---|---|---|
| `GET` | `/api/v1/members` | `200` list | Ordered by name |
| `GET` | `/api/v1/members/{id}` | `200` member | `404` if missing |
| `POST` | `/api/v1/members` | `201` created | Body: name, email, phoneNumber |

- JSON field names: `id`, `name`, `email`, `phoneNumber`.
- Controllers return DTOs, never JPA entities or the domain aggregate directly if it would leak internals.
- Breaking changes require a new version prefix (`/api/v2/...`). Additive fields are allowed in v1 only if clients can ignore them.
- OpenAPI is generated from the `api` module and kept in sync with controllers.

### Error envelope (all 4xx/5xx JSON)

```json
{
  "code": "EMAIL_ALREADY_REGISTERED",
  "message": "Email taken",
  "fields": {
    "email": "Email taken"
  },
  "correlationId": "..."
}
```

| Situation | HTTP | `code` |
|---|---|---|
| Transport / field validation | `400` | `VALIDATION_FAILED` |
| Email already registered | `409` | `EMAIL_ALREADY_REGISTERED` |
| Member id not found | `404` | `MEMBER_NOT_FOUND` |
| Unexpected failure | `500` | `INTERNAL_ERROR` |

`fields` is present for validation and conflict-on-a-field. Do not put stack traces in the body.

## 5. Domain boundaries

- **Membership** is the only bounded context in v1.
- Persistence models, HTTP DTOs, and the domain `Member` are **three types**. Mapping is explicit.
- Uniqueness of email is a **domain rule**, enforced via a port (query + persist), not only a DB unique index. The unique index remains as a safety net in infra.
- Domain events (`MemberRegistered`) are published through a port. The old CDI `Event<Member>` is the knowledge-base equivalent; do not use CDI.
- Localization is an **api/infra** concern. Domain messages are stable codes; `api` or `infra` resolve them for the UI/API locale.
- No shared “god” `Member` used as entity, DTO, and form backing object (that is the old design; do not repeat it).

## 6. Coding conventions

### Error handling

- Throw **domain exceptions** for rule violations; never `RuntimeException` with a free-text string as the contract.
- Catch at the **api** adapter (or a global handler in `api`). `app` and `domain` do not catch-and-map to HTTP.
- Do not swallow exceptions. Infra may wrap driver errors into an infra exception, then let the handler map `500`.
- Validation: fail fast; return **all** field errors for a request, not only the first.

### Logging

- Use SLF4J. One logger per class (`LoggerFactory.getLogger(getClass())`).
- **Info:** successful register (member id, not PII dumps).
- **Warn:** conflicts and not-found that are client-expected (optional; prefer metrics).
- **Error:** unexpected failures, with stack trace.
- Never log passwords. Treat email and phone as sensitive: log member **id** after persistence; avoid full email/phone at info level.
- Include `correlationId` (MDC) on every request log line.
- No `System.out` / `java.util.logging` in new code.

### Naming and structure

- Use cases named as verbs: `RegisterMemberService`.
- Ports named after the domain capability, not the vendor (`MemberRepository`, not `JpaMemberDao` in `domain`).
- Package by module then by bounded context, e.g. `org.kitchensink.domain.membership`.

### Testing

- `domain`: unit tests, no Spring.
- `app`: unit tests with mocked ports.
- `api`: web slice or contract tests against the error envelope.
- `infra`: persistence tests against the repository adapter.
- Keep an end-to-end test that matches old behavior: register, list, get, duplicate email, validation.

### Local run

- Spring Boot on the host JVM (WSL). Default port `8080`.
- Local profile uses an embedded database until we choose otherwise.
- No JBoss / provisioned EAP in this project.

## 7. What we will not carry over

- JSF UI as the primary contract (API-first; a UI may come later as another adapter).
- `*-ds.xml`, EAP feature packs, Helm-on-EAP as the default runtime.
- Bean Validation on the domain entity as the only design (invariants belong in value objects / aggregate; HTTP validation is extra).
- Returning the persistence model from REST.

## 8. Working agreement

1. Read this file and the old repo (read-only) before implementing a use case.
2. Add or change a use case in `app` + `domain` first, then adapters.
3. If a module dependency would point the wrong way, stop and introduce a port.
4. Update this charter when modules, URLs, or error codes change.
