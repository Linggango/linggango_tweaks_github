package com.misanthropy.linggango.linggango_tweaks.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

// filthy .json system
public class AtmosphereConfigManager {

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

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_FILE = FMLPaths.CONFIGDIR.get().resolve("linggango_atmosphere.json").toFile();
    public static final Map<String, AtmosphereSettings> ATMOSPHERES = new HashMap<>();

    public static void load() {
        if (CONFIG_FILE.exists()) {
            try (FileReader reader = new FileReader(CONFIG_FILE)) {
                Type type = new TypeToken<Map<String, AtmosphereSettings>>(){}.getType();
                Map<String, AtmosphereSettings> loaded = GSON.fromJson(reader, type);

                ATMOSPHERES.clear();
                if (loaded != null) {
                    ATMOSPHERES.putAll(loaded);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            save();
        }
    }

    public static void save() {
        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            GSON.toJson(ATMOSPHERES, writer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}