package com.misanthropy.linggango.linggango_tweaks.util; // keeping this seperate

import net.minecraft.client.Minecraft;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

public class LinggangoConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger("LinggangoConfig");
    private static final File CONFIG_FILE = new File(Minecraft.getInstance().gameDirectory, "config/linggango_extras.txt");

    public static boolean isEnabled() {
        if (!CONFIG_FILE.exists()) return false;
        try {
            return Files.readString(CONFIG_FILE.toPath()).trim().equalsIgnoreCase("true");
        } catch (Exception e) {
            LOGGER.error("Failed to read Linggango config", e);
            return false;
        }
    }

    public static void setEnabled(boolean enabled) {
        try {
            File parent = CONFIG_FILE.getParentFile();
            if (parent != null && !parent.exists()) {
                if (!parent.mkdirs()) {
                    LOGGER.error("Failed to create config directory {}", parent.getAbsolutePath());
                    return;
                }
            }

            Files.writeString(
                    CONFIG_FILE.toPath(),
                    String.valueOf(enabled),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING
            );
        } catch (Exception e) {
            LOGGER.error("Linggango config did not save", e);
        }
    }
}