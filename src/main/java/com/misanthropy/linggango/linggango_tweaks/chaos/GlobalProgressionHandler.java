package com.misanthropy.linggango.linggango_tweaks.chaos;

import com.mojang.logging.LogUtils;
import net.minecraftforge.fml.loading.FMLPaths;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@SuppressWarnings("unused")
public class GlobalProgressionHandler {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Path PROGRESSION_PATH = FMLPaths.CONFIGDIR.get().resolve("linggango/progression.dat");
    private static Boolean isUnlockedCache = null;

    public static boolean isChaosUnlocked() {
        if (isUnlockedCache == null) {
            isUnlockedCache = Files.exists(PROGRESSION_PATH);
        }
        return isUnlockedCache;
    }

    public static void unlockChaos() {
        try {
            if (!Files.exists(PROGRESSION_PATH.getParent())) {
                Files.createDirectories(PROGRESSION_PATH.getParent());
            }
            if (!Files.exists(PROGRESSION_PATH)) {
                Files.createFile(PROGRESSION_PATH);
            }
            isUnlockedCache = true;
        } catch (IOException e) {
            LOGGER.error("Failed to unlock chaos difficulty", e);
        }
    }

    public static boolean resetProgression() {
        try {
            boolean deleted = Files.deleteIfExists(PROGRESSION_PATH);
            if (deleted) {
                isUnlockedCache = false;
            }
            return deleted;
        } catch (IOException e) {
            LOGGER.error("Failed to reset progression", e);
            return false;
        }
    }
}