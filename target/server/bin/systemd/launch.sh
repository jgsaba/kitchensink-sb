#!/bin/sh
if [ "${JBOSS_EAP_SYSTEMD_DEBUG}" == "true" ]; then
    set -x
fi

echo "INFO: systemd unit file server launch script"

# Disable color output for the standard output
export PREPEND_JAVA_OPTS="${PREPEND_JAVA_OPTS} -Dorg.jboss.logmanager.nocolor=true"
export PATH="${JAVA_HOME}/bin:${PATH}"

logDir=$(dirname "${JBOSS_EAP_CONSOLE_LOG}")
if [ ! -d "${logDir}" ]; then
    mkdir -p "${logDir}"
fi

serverOptions="${JBOSS_EAP_OPTS}"
if [ -n "${JBOSS_EAP_SUSPEND_TIMEOUT}" ]; then
   serverOptions="-Dorg.wildfly.sigterm.suspend.timeout=${JBOSS_EAP_SUSPEND_TIMEOUT} ${serverOptions}"
fi

if [ -z "${JBOSS_EAP_HOST_CONFIG}" ]; then
   "${JBOSS_EAP_SH}" -c "${JBOSS_EAP_SERVER_CONFIG}" -b "${JBOSS_EAP_BIND}" ${serverOptions} > "${JBOSS_EAP_CONSOLE_LOG}" 2>&1
else
   "${JBOSS_EAP_SH}" --host-config="${JBOSS_EAP_HOST_CONFIG}" -c "${JBOSS_EAP_SERVER_CONFIG}" -b "${JBOSS_EAP_BIND}" ${serverOptions} > "${JBOSS_EAP_CONSOLE_LOG}" 2>&1
fi
