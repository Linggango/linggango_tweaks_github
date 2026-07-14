package com.misanthropy.linggango.linggango_tweaks.tweaks.minecraft;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
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
public class RenderCullingHandler {

    public static float xStretch = 1.0F;
    public static float yStretch = 1.0F;
    public static float cosAngle = 1.0F;
    public static float sinAngle = 1.0F;
    public static double maxSqDist = 0;

    public static double horizontalStretch = 2.0;
    public static double verticalStretch = 3.0;

    private static boolean loaded = false;

    public static void load() {
        try {
            Path configPath = FMLPaths.CONFIGDIR.get().resolve("linggango_culling.properties");
            if (Files.exists(configPath)) {
                Properties props = new Properties();
                try (Reader reader = Files.newBufferedReader(configPath)) {
                    props.load(reader);
                }
                horizontalStretch = Double.parseDouble(props.getProperty("horizontalStretch", "2.0"));
                verticalStretch = Double.parseDouble(props.getProperty("verticalStretch", "3.0"));
            }
        } catch (Exception ignored) {
        }
        loaded = true;
    }

    public static void save() {
        try {
            Path configPath = FMLPaths.CONFIGDIR.get().resolve("linggango_culling.properties");
            Properties props = new Properties();
            props.setProperty("horizontalStretch", String.valueOf(horizontalStretch));
            props.setProperty("verticalStretch", String.valueOf(verticalStretch));
            try (Writer writer = Files.newBufferedWriter(configPath)) {
                props.store(writer, "Linggango Culling Settings");
            }
        } catch (Exception ignored) {
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.@NonNull ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        if (!loaded) load();

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            xStretch = 1.0F;
            yStretch = 1.0F;
            return;
        }

        float yRot = mc.player.getYRot();
        cosAngle = (float) Math.cos(-yRot * (Math.PI / 180D));
        sinAngle = (float) Math.sin(-yRot * (Math.PI / 180D));

        xStretch = (float) (horizontalStretch * horizontalStretch);
        yStretch = (float) (verticalStretch * verticalStretch);

        int renderDist = mc.options.getEffectiveRenderDistance();
        double distance = (renderDist * 16) + 24.0;
        maxSqDist = distance * distance;
    }

    public static double getAdjustedDistance(double x1, double y1, double z1, double x2, double y2, double z2) {
        double dx = x2 - x1;
        double dy = y1 - y2;
        double dz = z2 - z1;

        double d0 = dz * sinAngle - dx * cosAngle;
        double d2 = dx * sinAngle + dz * cosAngle;

        return (double) xStretch * d0 * d0 + (double) yStretch * dy * dy + d2 * d2;
    }
}