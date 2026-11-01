package org.jboss.as.quickstarts.kitchensink.util;

import java.util.Locale;
import java.io.Serializable;
import javax.annotation.processing.Generated;

/**
 * Warning this class consists of generated code.
 */
@Generated(value = "org.jboss.logging.processor.generator.model.MessageBundleTranslator", date = "2026-11-01T11:35:19-0300")
public class KitchensinkMessages_$bundle_fr extends KitchensinkMessages_$bundle implements KitchensinkMessages, Serializable {
    private static final long serialVersionUID = 1L;
    protected KitchensinkMessages_$bundle_fr() {
        super();
    }
    public static final KitchensinkMessages_$bundle_fr INSTANCE = new KitchensinkMessages_$bundle_fr();
    @Override
    protected Object readResolve() {
        return INSTANCE;
    }
    private static final Locale LOCALE = Locale.FRENCH;
    @Override
    protected Locale getLoggingLocale() {
        return LOCALE;
    }
    @Override
    protected String registeredMessage$str() {
        return "Enregistré!";
    }
    @Override
    protected String registerSuccessfulMessage$str() {
        return "enregistré avec succès!";
    }
    @Override
    protected String registerFailMessage$str() {
        return "Echec de l'enregistrement:";
    }
    @Override
    protected String defaultErrorMessage$str() {
        return "Echec de l'enregistrement. Voir journal du serveur pour plus d'informations.";
    }
}
