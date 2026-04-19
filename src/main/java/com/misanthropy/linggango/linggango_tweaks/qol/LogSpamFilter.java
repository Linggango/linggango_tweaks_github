package com.misanthropy.linggango.linggango_tweaks.qol; // not used yet for debugging purposes

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.filter.AbstractFilter;
import java.util.List;

public class LogSpamFilter extends AbstractFilter {

    private static final List<String> SPAM_MESSAGES = List.of(
            "fart"
    );

    @Override
    public Result filter(LogEvent event) {
        String message = event.getMessage() != null ? event.getMessage().getFormattedMessage() : null;

        if (message != null) {
            if (event.getLevel() == Level.WARN && message.contains("Mixin apply failed")) {
                return Result.DENY;
            }

            if (message.contains("Recipe") && message.contains("not found")) {
                return Result.DENY;
            }
            for (String spam : SPAM_MESSAGES) {
                if (message.contains(spam)) {
                    return Result.DENY;
                }
            }
        }
        Throwable thrown = event.getThrown();
        if (thrown != null) {
            Throwable current = thrown;
            while (current != null) {
                String errorMsg = current.getMessage();
                if (errorMsg != null && (
                        errorMsg.contains("fabric-data-generation-api-v1") ||
                                errorMsg.contains("End of input at line 1 column 1") ||
                                errorMsg.contains("No key id in MapLike")
                )) {
                    return Result.DENY;
                }
                current = current.getCause();
            }
        }

        return Result.NEUTRAL;
    }

    public static void register() {
        LoggerContext context = (LoggerContext) LogManager.getContext(false);
        Configuration config = context.getConfiguration();

        LogSpamFilter filter = new LogSpamFilter();
        filter.start();

        config.getRootLogger().addFilter(filter);
        context.updateLoggers();
    }
}