package com.misanthropy.linggango.linggango_tweaks.fixes;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.Locale;

public class LanguageRelatedCrashFixes {
    private static final Logger LOGGER = LogManager.getLogger("LinggangoTweaks/LocaleFix");

    public static void fixLocale() {
        Locale currentLocale = Locale.getDefault();
        String language = currentLocale.getLanguage();
        if ("tr".equals(language) || "az".equals(language)) {
            Locale.setDefault(Locale.ROOT);
            LOGGER.warn("Seems like you are running either Turkish or Azerbaijani language. I highly suggest using English to avoid crashes.");
        }
    }
}