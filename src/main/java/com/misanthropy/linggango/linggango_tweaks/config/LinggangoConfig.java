package com.misanthropy.linggango.linggango_tweaks.config; // keeping this separate

import net.minecraft.client.Minecraft;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class LinggangoConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger("LinggangoConfig");
    private static final Path CONFIG_PATH = Minecraft.getInstance().gameDirectory.toPath().resolve("config/linggango_extras.txt");
    private static boolean cachedEnabled = false;
    private static boolean initialized = false;

    public static boolean isEnabled() {
        if (!initialized) {
            loadFromDisk();
        }
        return cachedEnabled;
    }

    public static void setEnabled(boolean enabled) {
        cachedEnabled = enabled;
        initialized = true;

        try {
            Path parent = CONFIG_PATH.getParent();
            if (parent != null && !Files.exists(parent)) {
                Files.createDirectories(parent);
            }

            Files.writeString(
                    CONFIG_PATH,
                    String.valueOf(enabled),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING
            );
        } catch (Exception e) {
            LOGGER.error("Linggango config did not save", e);
        }
    }

    private static void loadFromDisk() {
        initialized = true;
        if (!Files.exists(CONFIG_PATH)) {
            cachedEnabled = false;
            return;
        }
        try {
            cachedEnabled = Files.readString(CONFIG_PATH).trim().equalsIgnoreCase("true");
        } catch (Exception e) {
            LOGGER.error("Failed to read Linggango config", e);
            cachedEnabled = false;
        }
    }
}