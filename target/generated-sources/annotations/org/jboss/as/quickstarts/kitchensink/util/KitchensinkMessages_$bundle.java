package org.jboss.as.quickstarts.kitchensink.util;

import java.util.Locale;
import java.io.Serializable;
import javax.annotation.processing.Generated;
import java.lang.String;

/**
 * Warning this class consists of generated code.
 */
@Generated(value = "org.jboss.logging.processor.generator.model.MessageBundleImplementor", date = "2026-11-01T11:35:19-0300")
public class KitchensinkMessages_$bundle implements KitchensinkMessages, Serializable {
    private static final long serialVersionUID = 1L;
    protected KitchensinkMessages_$bundle() {}
    public static final KitchensinkMessages_$bundle INSTANCE = new KitchensinkMessages_$bundle();
    protected Object readResolve() {
        return INSTANCE;
    }
    private static final Locale LOCALE = Locale.ROOT;
    protected Locale getLoggingLocale() {
        return LOCALE;
    }
    protected String registeredMessage$str() {
        return "Registered!";
    }
    @Override
    public final String registeredMessage() {
        return String.format(getLoggingLocale(), registeredMessage$str());
    }
    protected String registerSuccessfulMessage$str() {
        return "Successfully registered!";
    }
    @Override
    public final String registerSuccessfulMessage() {
        return String.format(getLoggingLocale(), registerSuccessfulMessage$str());
    }
    protected String registerFailMessage$str() {
        return "Registration failed:";
    }
    @Override
    public final String registerFailMessage() {
        return String.format(getLoggingLocale(), registerFailMessage$str());
    }
    protected String defaultErrorMessage$str() {
        return "Registration failed. See server log for more information.";
    }
    @Override
    public final String defaultErrorMessage() {
        return String.format(getLoggingLocale(), defaultErrorMessage$str());
    }
}
