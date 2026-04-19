package com.misanthropy.linggango.linggango_tweaks.chaos;

import net.minecraftforge.fml.loading.FMLPaths;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class GlobalProgressionHandler {
    private static final Path PROGRESSION_PATH = FMLPaths.CONFIGDIR.get().resolve("linggango/progression.dat");

    public static boolean isChaosUnlocked() {return Files.exists(PROGRESSION_PATH);
    }

    public static void unlockChaos() {
        try {
            if (!Files.exists(PROGRESSION_PATH.getParent())) {Files.createDirectories(PROGRESSION_PATH.getParent());}if (!Files.exists(PROGRESSION_PATH)) {Files.createFile(PROGRESSION_PATH);}
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean resetProgression() {try {
            return Files.deleteIfExists(PROGRESSION_PATH);
        } catch (IOException e) {
            e.printStackTrace();return false;
        }
    }
}