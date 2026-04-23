package com.misanthropy.linggango.linggango_tweaks.tweaks;

import com.mojang.blaze3d.shaders.FogShape;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.material.FogType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ViewportEvent;
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
public class DynamicFogHandler {

    public static double fogStartMultiplier = 0.15;
    public static boolean dynamicFogEnabled = true;
    public static boolean voidFogEnabled = true;
    public static double rainFogDensity = 0.05;
    private static boolean loaded = false;

    private static final AtmosphericNoiseGenerator noiseGen = new AtmosphericNoiseGenerator(89234L);

    private static float currentNearPlane = -1f;
    private static float currentFarPlane = -1f;
    private static float currentR = -1f;
    private static float currentG = -1f;
    private static float currentB = -1f;

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
                voidFogEnabled = Boolean.parseBoolean(props.getProperty("voidFogEnabled", "true"));
                rainFogDensity = Double.parseDouble(props.getProperty("rainFogDensity", "0.05"));
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
            props.setProperty("voidFogEnabled", String.valueOf(voidFogEnabled));
            props.setProperty("rainFogDensity", String.valueOf(rainFogDensity));
            try (Writer writer = Files.newBufferedWriter(configPath)) {
                props.store(writer, "Linggango Dynamic Fog Settings");
            }
        } catch (Exception ignored) {
        }
    }

    private static class AtmosphericNoiseGenerator {
        private final int[] permutation = new int[512];

        public AtmosphericNoiseGenerator(long seed) {
            RandomSource rand = RandomSource.create(seed);
            for (int i = 0; i < 256; i++) {
                permutation[i] = i;
            }
            for (int i = 0; i < 256; i++) {
                int j = rand.nextInt(256);
                int temp = permutation[i];
                permutation[i] = permutation[j];
                permutation[j] = temp;
                permutation[i + 256] = permutation[i];
            }
        }

        public float evaluate(float x) {
            int xi = (int) Math.floor(x) & 255;
            float xf = x - (float) Math.floor(x);
            float u = xf * xf * (3.0f - 2.0f * xf);
            int a = permutation[xi];
            int b = permutation[xi + 1];
            float res = Mth.lerp(u, gradient(permutation[a], xf), gradient(permutation[b], xf - 1.0f));
            return (res + 1.0f) / 2.0f;
        }

        private float gradient(int hash, float x) {
            return (hash & 1) == 0 ? x : -x;
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (event.player != Minecraft.getInstance().player) return;
        if (!voidFogEnabled || !dynamicFogEnabled) return;
        if (event.player.hasEffect(MobEffects.NIGHT_VISION)) return;

        int minHeight = event.player.level().getMinBuildHeight();
        float voidStart = minHeight + 32.0f;
        float playerY = (float) event.player.getY();

        if (playerY < voidStart) {
            float depthFactor = Mth.clamp((voidStart - playerY) / 32.0f, 0.0f, 1.0f);
            float smoothDepth = depthFactor * depthFactor * (3.0f - 2.0f * depthFactor);

            RandomSource random = event.player.getRandom();
            int particleCount = (int) (120.0f * smoothDepth);

            for (int l = 0; l < particleCount; ++l) {
                int x = event.player.blockPosition().getX() + random.nextInt(16) - random.nextInt(16);
                int y = event.player.blockPosition().getY() + random.nextInt(16) - random.nextInt(16);
                int z = event.player.blockPosition().getZ() + random.nextInt(16) - random.nextInt(16);

                BlockPos pos = new BlockPos(x, y, z);
                if (event.player.level().getBlockState(pos).isAir() && random.nextFloat() < smoothDepth) {
                    event.player.level().addParticle(ParticleTypes.ASH, x + random.nextFloat(), y + random.nextFloat(), z + random.nextFloat(), 0.0D, 0.0D, 0.0D);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onRenderFog(ViewportEvent.@NonNull RenderFog event) {
        if (!loaded) load();
        if (!dynamicFogEnabled) return;
        if (event.getCamera().getFluidInCamera() != FogType.NONE) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;

        Camera camera = event.getCamera();
        float originalEnd = event.getFarPlaneDistance();

        float targetFar = originalEnd * 1.60f;
        float targetNear = originalEnd * 0.05f;

        float timeCycle = mc.level.getGameTime() / 24000.0f;
        float rollingAtmosphere = noiseGen.evaluate(timeCycle * 2.5f);

        targetNear = Mth.lerp(rollingAtmosphere, targetNear, originalEnd * 0.01f);
        targetFar = Mth.lerp(rollingAtmosphere, targetFar, originalEnd * 1.35f);

        float playerY = (float) camera.getPosition().y;
        float minHeight = mc.level.getMinBuildHeight();
        float altitudeFactor = Mth.clamp((playerY - minHeight) / 140.0f, 0.0f, 1.0f);
        float valleyMist = 1.0f - (altitudeFactor * altitudeFactor * (3.0f - 2.0f * altitudeFactor));

        targetNear = Mth.lerp(valleyMist, targetNear, originalEnd * 0.02f);

        float rainLevel = mc.level.getRainLevel(1.0f);
        float thunderLevel = mc.level.getThunderLevel(1.0f);

        if (rainLevel > 0.0f) {
            float rainNear = originalEnd * (float) rainFogDensity;
            float rainFar = originalEnd * 0.85f;

            if (thunderLevel > 0.0f) {
                float thunderNear = originalEnd * 0.005f;
                float thunderFar = originalEnd * 0.35f;

                float thunderSmooth = thunderLevel * thunderLevel * (3.0f - 2.0f * thunderLevel);
                rainNear = Mth.lerp(thunderSmooth, rainNear, thunderNear);
                rainFar = Mth.lerp(thunderSmooth, rainFar, thunderFar);
            }

            float rainSmooth = rainLevel * rainLevel * (3.0f - 2.0f * rainLevel);
            targetNear = Mth.lerp(rainSmooth, targetNear, rainNear);
            targetFar = Mth.lerp(rainSmooth, targetFar, rainFar);
        }

        if (voidFogEnabled && !mc.player.hasEffect(MobEffects.NIGHT_VISION)) {
            float voidStart = minHeight + 32.0f;
            if (playerY < voidStart) {
                float voidFactor = Mth.clamp((voidStart - playerY) / 32.0f, 0.0f, 1.0f);
                float smoothVoid = voidFactor * voidFactor * (3.0f - 2.0f * voidFactor);
                targetNear = Mth.lerp(smoothVoid, targetNear, 0.0f);
                targetFar = Mth.lerp(smoothVoid, targetFar, 14.0f);
            }
        }

        if (currentNearPlane < 0f) {
            currentNearPlane = targetNear;
            currentFarPlane = targetFar;
        } else {
            currentNearPlane = Mth.lerp(0.03f, currentNearPlane, targetNear);
            currentFarPlane = Mth.lerp(0.03f, currentFarPlane, targetFar);
        }

        event.setNearPlaneDistance(currentNearPlane);
        event.setFarPlaneDistance(currentFarPlane);
        event.setFogShape(FogShape.SPHERE);
        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onComputeFogColor(ViewportEvent.@NonNull ComputeFogColor event) {
        if (!loaded) load();
        if (!dynamicFogEnabled) return;
        if (event.getCamera().getFluidInCamera() != FogType.NONE) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;

        Camera camera = event.getCamera();
        BlockPos cameraPos = BlockPos.containing(camera.getPosition());
        Holder<Biome> biomeHolder = mc.level.getBiome(cameraPos);
        int biomeColor = biomeHolder.value().getFogColor();

        float biomeR = (biomeColor >> 16 & 255) / 255.0f;
        float biomeG = (biomeColor >> 8 & 255) / 255.0f;
        float biomeB = (biomeColor & 255) / 255.0f;

        float engineR = event.getRed();
        float engineG = event.getGreen();
        float engineB = event.getBlue();

        float r = Mth.lerp(0.4f, engineR, biomeR);
        float g = Mth.lerp(0.4f, engineG, biomeG);
        float b = Mth.lerp(0.4f, engineB, biomeB);

        float timeOfDay = mc.level.getTimeOfDay((float) event.getPartialTick());
        float cosAngle = Mth.cos(timeOfDay * ((float) Math.PI * 2f));

        float sunsetGlow = Mth.clamp((0.2f - Math.abs(cosAngle)) * 5.0f, 0.0f, 1.0f);
        float sunsetSmooth = sunsetGlow * sunsetGlow * (3.0f - 2.0f * sunsetGlow);
        r += sunsetSmooth * 0.18f;
        g += sunsetSmooth * 0.06f;

        float nightFactor = Mth.clamp(-cosAngle * 1.5f, 0.0f, 1.0f);
        float nightSmooth = nightFactor * nightFactor * (3.0f - 2.0f * nightFactor);

        r = Mth.lerp(nightSmooth, r, r * 0.15f + 0.01f);
        g = Mth.lerp(nightSmooth, g, g * 0.20f + 0.03f);
        b = Mth.lerp(nightSmooth, b, b * 0.35f + 0.09f);

        float rainLevel = mc.level.getRainLevel((float) event.getPartialTick());
        float thunderLevel = mc.level.getThunderLevel((float) event.getPartialTick());

        if (rainLevel > 0.0f) {
            float daylight = Mth.clamp(cosAngle + 0.5f, 0.08f, 1.0f);
            float luminance = 0.299f * r + 0.587f * g + 0.114f * b;

            float rainR = luminance * 0.75f * daylight;
            float rainG = luminance * 0.82f * daylight;
            float rainB = luminance * 0.90f * daylight;

            if (thunderLevel > 0.0f) {
                float thunderLuminance = luminance * 0.25f * daylight;
                float thunderSmooth = thunderLevel * thunderLevel * (3.0f - 2.0f * thunderLevel);

                rainR = Mth.lerp(thunderSmooth, rainR, thunderLuminance);
                rainG = Mth.lerp(thunderSmooth, rainG, thunderLuminance);
                rainB = Mth.lerp(thunderSmooth, rainB, thunderLuminance * 1.15f);
            }

            float rainSmooth = rainLevel * rainLevel * (3.0f - 2.0f * rainLevel);
            r = Mth.lerp(rainSmooth, r, rainR);
            g = Mth.lerp(rainSmooth, g, rainG);
            b = Mth.lerp(rainSmooth, b, rainB);
        }

        if (voidFogEnabled) {
            float playerY = (float) camera.getPosition().y;
            float minHeight = mc.level.getMinBuildHeight();
            float voidStart = minHeight + 32.0f;

            if (playerY < voidStart) {
                float depthFactor = Mth.clamp((voidStart - playerY) / 32.0f, 0.0f, 1.0f);
                float darken = 1.0f - (depthFactor * depthFactor * (3.0f - 2.0f * depthFactor));
                darken = Math.max(darken, 0.02f);
                r *= darken;
                g *= darken;
                b *= darken;
            }
        }

        if (currentR < 0f) {
            currentR = r;
            currentG = g;
            currentB = b;
        } else {
            currentR = Mth.lerp(0.04f, currentR, r);
            currentG = Mth.lerp(0.04f, currentG, g);
            currentB = Mth.lerp(0.04f, currentB, b);
        }

        event.setRed(currentR);
        event.setGreen(currentG);
        event.setBlue(currentB);
    }
}