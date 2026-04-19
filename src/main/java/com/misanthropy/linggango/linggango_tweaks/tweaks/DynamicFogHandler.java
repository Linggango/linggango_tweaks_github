package com.misanthropy.linggango.linggango_tweaks.tweaks;

import net.minecraft.client.Minecraft;
import net.minecraft.world.level.material.FogType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLPaths;
import org.jspecify.annotations.NonNull;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

@Mod.EventBusSubscriber(modid = "linggango_tweaks", value = Dist.CLIENT)
public class DynamicFogHandler {

    public static double fogStartMultiplier = 0.15;
    public static boolean dynamicFogEnabled = true;
    private static boolean loaded = false;

    public static void load() {
        try {
            Path configPath = FMLPaths.CONFIGDIR.get().resolve("linggango_fog.properties");
            if (Files.exists(configPath)) {
                Properties props = new Properties();
                try (Reader reader = Files.newBufferedReader(configPath)) {
                    props.load(reader);
                }
                fogStartMultiplier = Double.parseDouble(props.getProperty("fogStartMultiplier", "0.15"));
                dynamicFogEnabled = Boolean.parseBoolean(props.getProperty("dynamicFogEnabled", "true"));
            }
        } catch (Exception ignored) {
        }
        loaded = true;
    }

    public static void save() {
        try {
            Path configPath = FMLPaths.CONFIGDIR.get().resolve("linggango_fog.properties");
            Properties props = new Properties();
            props.setProperty("fogStartMultiplier", String.valueOf(fogStartMultiplier));
            props.setProperty("dynamicFogEnabled", String.valueOf(dynamicFogEnabled));
            try (Writer writer = Files.newBufferedWriter(configPath)) {
                props.store(writer, "Linggango Dynamic Fog Settings");
            }
        } catch (Exception ignored) {
        }
    }

    @SubscribeEvent
    public static void onRenderFog(ViewportEvent.@NonNull RenderFog event) {
        if (!loaded) load();
        if (!dynamicFogEnabled) return;
        if (event.getCamera().getFluidInCamera() != FogType.NONE) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;

        float renderDistBlocks = mc.options.getEffectiveRenderDistance() * 16.0F;
        float fogStart = renderDistBlocks * (float) fogStartMultiplier;
        float fogEnd = renderDistBlocks;

        event.setNearPlaneDistance(fogStart);
        event.setFarPlaneDistance(fogEnd);
        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onComputeFogColor(ViewportEvent.@NonNull ComputeFogColor event) {
        if (!loaded) load();
        if (!dynamicFogEnabled) return;
        if (event.getCamera().getFluidInCamera() != FogType.NONE) return;

        float r = event.getRed();
        float g = event.getGreen();
        float b = event.getBlue();

        r = r * 0.70F;
        g = g * 0.75F;
        b = Math.min(1.0F, b * 1.40F + 0.15F);

        event.setRed(r);
        event.setGreen(g);
        event.setBlue(b);
    }
}