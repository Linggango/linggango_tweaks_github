package com.misanthropy.linggango.linggango_tweaks.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class AtmosphereConfigManager {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_FILE = FMLPaths.CONFIGDIR.get().resolve("linggango_atmosphere.json").toFile();
    public static final Map<String, AtmosphereSettings> ATMOSPHERES = new HashMap<>();
    public static class AtmosphereSettings {
        public int fogHex, skyHex;
        public float fogStart, fogEnd;

        public AtmosphereSettings(int fogHex, int skyHex, float fogStart, float fogEnd) {
            this.fogHex = fogHex;
            this.skyHex = skyHex;
            this.fogStart = fogStart;
            this.fogEnd = fogEnd;
        }
    }

    public static void load() {
        if (CONFIG_FILE.exists()) {
            try (FileReader reader = new FileReader(CONFIG_FILE)) {
                Type type = new TypeToken<Map<String, AtmosphereSettings>>(){}.getType();
                Map<String, AtmosphereSettings> loaded = GSON.fromJson(reader, type);
                ATMOSPHERES.clear();
                if (loaded != null) {
                    ATMOSPHERES.putAll(loaded);
                }
                LOGGER.info("Atmosphere config was found and loaded successfully.");
            } catch (Exception e) {
                LOGGER.error("Failed to load atmosphere config file: {}", CONFIG_FILE.getPath(), e);
            }
        } else {
            LOGGER.info("No atmosphere config found, creating default.");
            save();
        }
    }

    public static void save() {
        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            GSON.toJson(ATMOSPHERES, writer);
            LOGGER.debug("Atmosphere configuration saved successfully.");
        } catch (Exception e) {
            LOGGER.error("Failed to save atmosphere config file: {}", CONFIG_FILE.getPath(), e);
        }
    }
}