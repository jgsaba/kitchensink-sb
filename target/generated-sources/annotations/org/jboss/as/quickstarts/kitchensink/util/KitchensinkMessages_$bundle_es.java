package org.jboss.as.quickstarts.kitchensink.util;

import java.util.Locale;
import java.io.Serializable;
import javax.annotation.processing.Generated;

/**
 * Warning this class consists of generated code.
 */
@Generated(value = "org.jboss.logging.processor.generator.model.MessageBundleTranslator", date = "2026-11-01T11:35:19-0300")
public class KitchensinkMessages_$bundle_es extends KitchensinkMessages_$bundle implements KitchensinkMessages, Serializable {
    private static final long serialVersionUID = 1L;
    protected KitchensinkMessages_$bundle_es() {
        super();
    }
    public static final KitchensinkMessages_$bundle_es INSTANCE = new KitchensinkMessages_$bundle_es();
    @Override
    protected Object readResolve() {
        return INSTANCE;
    }
    private static final Locale LOCALE = new Locale("es");
    @Override
    protected Locale getLoggingLocale() {
        return LOCALE;
    }
    @Override
    protected String registeredMessage$str() {
        return "De registro!";
    }
    @Override
    protected String registerSuccessfulMessage$str() {
        return "Registrado con éxito!";
    }
    @Override
    protected String registerFailMessage$str() {
        return "Registro no se ha:";
    }
    @Override
    protected String defaultErrorMessage$str() {
        return "Error de registro. Vea el registro del servidor para obtener más información.";
    }
}
