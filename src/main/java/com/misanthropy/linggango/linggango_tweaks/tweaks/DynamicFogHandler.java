package com.misanthropy.linggango.linggango_tweaks.tweaks;

import com.misanthropy.linggango.linggango_tweaks.client.atmosphere.AtmosphereEditorScreen;
import com.misanthropy.linggango.linggango_tweaks.config.AtmosphereConfigManager;
import com.mojang.blaze3d.shaders.FogShape;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.phys.Vec3;
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
    public static double biomeTintStrength = 0.72;
    public static double skyFogBlendStrength = 0.36;
    public static double chunkBorderFogSoftness = 0.14;

    private static final String CONFIG_FILE = "linggango_fog.properties";
    private static final String KEY_FOG_START = "fogStartMultiplier";
    private static final String KEY_DYNAMIC_FOG = "dynamicFogEnabled";
    private static final String KEY_VOID_FOG = "voidFogEnabled";
    private static final String KEY_RAIN_DENSITY = "rainFogDensity";
    private static final String KEY_BIOME_TINT = "biomeTintStrength";
    private static final String KEY_SKY_BLEND = "skyFogBlendStrength";
    private static final String KEY_CHUNK_SOFTNESS = "chunkBorderFogSoftness";
    private static final float COLOR_EPSILON = 0.0005F;
    private static final int COLOR_SAMPLE_INTERVAL_TICKS = 5;
    private static final int COLOR_SAMPLE_REUSE_DISTANCE_SQR = 16;
    private static final int COLOR_SAMPLE_RADIUS = 6;
    private static final int SKY_OPENNESS_SAMPLE_HEIGHT = 6;
    private static final int[] SAMPLE_X = {0, COLOR_SAMPLE_RADIUS, -COLOR_SAMPLE_RADIUS, 0, 0, COLOR_SAMPLE_RADIUS, COLOR_SAMPLE_RADIUS, -COLOR_SAMPLE_RADIUS, -COLOR_SAMPLE_RADIUS};
    private static final int[] SAMPLE_Z = {0, 0, 0, COLOR_SAMPLE_RADIUS, -COLOR_SAMPLE_RADIUS, COLOR_SAMPLE_RADIUS, -COLOR_SAMPLE_RADIUS, COLOR_SAMPLE_RADIUS, -COLOR_SAMPLE_RADIUS};
    private static final float[] SAMPLE_WEIGHT = {4.0F, 2.0F, 2.0F, 2.0F, 2.0F, 1.0F, 1.0F, 1.0F, 1.0F};
    private static final ThreadLocal<BlockPos.MutableBlockPos> SAMPLE_CURSOR = ThreadLocal.withInitial(BlockPos.MutableBlockPos::new);

    private static boolean loaded = false;
    private static final AtmosphericNoise noiseGen = new AtmosphericNoise(89234L);

    private static float currentNearPlane = -1.0F;
    private static float currentFarPlane = -1.0F;
    private static float currentR = -1.0F;
    private static float currentG = -1.0F;
    private static float currentB = -1.0F;

    private static long lastSampleTick = Long.MIN_VALUE;
    private static int lastSampleX = Integer.MIN_VALUE;
    private static int lastSampleY = Integer.MIN_VALUE;
    private static int lastSampleZ = Integer.MIN_VALUE;
    private static float sampledBiomeR = 0.5F;
    private static float sampledBiomeG = 0.6F;
    private static float sampledBiomeB = 0.7F;
    private static float sampledSkyR = 0.6F;
    private static float sampledSkyG = 0.7F;
    private static float sampledSkyB = 0.9F;
    private static float sampledSkyOpenness = 1.0F;
    private static float sampledSkyVisibility = 1.0F;
    private static float sampledValleyMist = 0.0F;

    public static void load() {
        try {
            Path path = FMLPaths.CONFIGDIR.get().resolve(CONFIG_FILE);
            if (Files.exists(path)) {
                Properties props = new Properties();
                try (Reader reader = Files.newBufferedReader(path)) {
                    props.load(reader);
                }

                fogStartMultiplier = linggango_tweaks$parseDouble(props, KEY_FOG_START, 0.15D, 0.02D, 0.65D);
                dynamicFogEnabled = Boolean.parseBoolean(props.getProperty(KEY_DYNAMIC_FOG, "true"));
                voidFogEnabled = Boolean.parseBoolean(props.getProperty(KEY_VOID_FOG, "true"));
                rainFogDensity = linggango_tweaks$parseDouble(props, KEY_RAIN_DENSITY, 0.05D, 0.01D, 0.50D);
                biomeTintStrength = linggango_tweaks$parseDouble(props, KEY_BIOME_TINT, 0.72D, 0.0D, 1.0D);
                skyFogBlendStrength = linggango_tweaks$parseDouble(props, KEY_SKY_BLEND, 0.36D, 0.0D, 1.0D);
                chunkBorderFogSoftness = linggango_tweaks$parseDouble(props, KEY_CHUNK_SOFTNESS, 0.14D, 0.0D, 0.35D);
            }
        } catch (Exception ignored) {
        }

        loaded = true;
    }

    public static void save() {
        try {
            Path path = FMLPaths.CONFIGDIR.get().resolve(CONFIG_FILE);
            Properties props = new Properties();
            props.setProperty(KEY_FOG_START, String.valueOf(fogStartMultiplier));
            props.setProperty(KEY_DYNAMIC_FOG, String.valueOf(dynamicFogEnabled));
            props.setProperty(KEY_VOID_FOG, String.valueOf(voidFogEnabled));
            props.setProperty(KEY_RAIN_DENSITY, String.valueOf(rainFogDensity));
            props.setProperty(KEY_BIOME_TINT, String.valueOf(biomeTintStrength));
            props.setProperty(KEY_SKY_BLEND, String.valueOf(skyFogBlendStrength));
            props.setProperty(KEY_CHUNK_SOFTNESS, String.valueOf(chunkBorderFogSoftness));

            try (Writer writer = Files.newBufferedWriter(path)) {
                props.store(writer, "Linggango Dynamic Fog Settings");
            }
        } catch (Exception ignored) {
        }
    }

    private static class AtmosphericNoise {
        private final int[] p = new int[512];

        private AtmosphericNoise(long seed) {
            RandomSource rand = RandomSource.create(seed);
            for (int i = 0; i < 256; i++) {
                p[i] = i;
            }

            for (int i = 0; i < 256; i++) {
                int j = rand.nextInt(256);
                int tmp = p[i];
                p[i] = p[j];
                p[j] = tmp;
                p[i + 256] = p[i];
            }
        }

        private float evaluate(float x) {
            int xi = (int) Math.floor(x) & 255;
            float xf = x - (float) Math.floor(x);
            float u = xf * xf * (3.0F - 2.0F * xf);
            return (Mth.lerp(u, linggango_tweaks$grad(p[p[xi]], xf), linggango_tweaks$grad(p[p[xi + 1]], xf - 1.0F)) + 1.0F) * 0.5F;
        }

        private float linggango_tweaks$grad(int hash, float x) {
            return (hash & 1) == 0 ? x : -x;
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player != Minecraft.getInstance().player) {
            return;
        }

        if (!voidFogEnabled || !dynamicFogEnabled || event.player.hasEffect(MobEffects.NIGHT_VISION)) {
            return;
        }

        float voidStart = event.player.level().getMinBuildHeight() + 32.0F;
        float playerY = (float) event.player.getY();
        if (playerY >= voidStart || (event.player.tickCount & 1) != 0) {
            return;
        }

        float depthFactor = Mth.clamp((voidStart - playerY) / 32.0F, 0.0F, 1.0F);
        float smoothDepth = linggango_tweaks$smoothstep(depthFactor);
        int particleChecks = 2 + Mth.floor(smoothDepth * 6.0F);
        RandomSource rand = event.player.getRandom();

        for (int i = 0; i < particleChecks; ++i) {
            if (rand.nextFloat() < smoothDepth * 0.72F) {
                double px = event.player.getX() + rand.nextInt(16) - rand.nextInt(16);
                double py = event.player.getY() + rand.nextInt(14) - rand.nextInt(10);
                double pz = event.player.getZ() + rand.nextInt(16) - rand.nextInt(16);
                event.player.level().addParticle(ParticleTypes.ASH, px, py, pz, 0.0D, 0.0D, 0.0D);
            }
        }
    }

    @SubscribeEvent
    public static void onRenderFog(ViewportEvent.@NonNull RenderFog event) {
        if (!loaded) {
            load();
        }

        if (!dynamicFogEnabled || event.getCamera().getFluidInCamera() != FogType.NONE) {
            return;
        }

        if (event.getMode() != FogRenderer.FogMode.FOG_TERRAIN || event.isCanceled()) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) {
            linggango_tweaks$resetSmoothing();
            return;
        }

        var level = mc.level;

        if (event.getCamera().getEntity() != mc.player || mc.player.hasEffect(MobEffects.NIGHT_VISION) || mc.player.hasEffect(MobEffects.BLINDNESS) || mc.player.hasEffect(MobEffects.DARKNESS) || mc.player.isSpectator()) {
            return;
        }

        Vec3 cameraPos = event.getCamera().getPosition();
        float partialTick = (float) event.getPartialTick();
        linggango_tweaks$updateColorSampling(mc, cameraPos, partialTick);

        float originalEnd = event.getFarPlaneDistance();
        float clearWeatherNear = originalEnd * Mth.clamp((float) fogStartMultiplier, 0.02F, 0.60F);
        float clearWeatherFar = originalEnd * 0.92F;

        ResourceLocation centerBiomeLoc = level.getBiome(BlockPos.containing(cameraPos)).unwrapKey().map(ResourceKey::location).orElse(null);
        AtmosphereConfigManager.AtmosphereSettings centerSettings = centerBiomeLoc != null ? AtmosphereConfigManager.ATMOSPHERES.get(centerBiomeLoc.toString()) : null;

        if (centerSettings != null) {
            clearWeatherNear = centerSettings.fogStart;
            clearWeatherFar = centerSettings.fogEnd;
            originalEnd = centerSettings.fogEnd;

        }

        float edgeSoftness = (float) Mth.clamp(chunkBorderFogSoftness, 0.0D, 0.35D);
        float skyVisibility = sampledSkyVisibility;
        float valleyMist = sampledValleyMist;
        float altitudeClear = 1.0F - valleyMist;

        clearWeatherNear = Mth.lerp(altitudeClear * 0.55F + skyVisibility * 0.20F, clearWeatherNear, originalEnd * 0.24F);
        clearWeatherFar = Mth.lerp(altitudeClear * 0.70F + skyVisibility * 0.20F, clearWeatherFar, originalEnd * 0.985F);

        clearWeatherNear = Mth.lerp(valleyMist * 0.55F, clearWeatherNear, originalEnd * 0.08F);
        clearWeatherFar = Mth.lerp(valleyMist * 0.40F, clearWeatherFar, originalEnd * 0.78F);

        float atmosphereNoise = noiseGen.evaluate((level.getGameTime() * 0.0024F) + (float) ((cameraPos.x + cameraPos.z) * 0.008));
        clearWeatherNear *= Mth.lerp(atmosphereNoise, 0.97F, 1.03F);
        clearWeatherFar *= Mth.lerp(atmosphereNoise, 0.985F, 1.015F);

        float rainLevel = level.getRainLevel(partialTick);
        if (rainLevel > 0.0F) {
            float rainSmooth = linggango_tweaks$smoothstep(rainLevel);
            float thunderLevel = level.getThunderLevel(partialTick);
            float thunderSmooth = linggango_tweaks$smoothstep(thunderLevel);

            float rainNear = originalEnd * (float) Mth.clamp(rainFogDensity, 0.01D, 0.50D);
            float rainFar = originalEnd * Mth.lerp(thunderSmooth, 0.80F, 0.58F);

            rainNear = Mth.lerp(1.0F - skyVisibility * 0.45F, rainNear, originalEnd * 0.11F);
            clearWeatherNear = Mth.lerp(rainSmooth, clearWeatherNear, rainNear);
            clearWeatherFar = Mth.lerp(rainSmooth, clearWeatherFar, rainFar);
        }

        if (voidFogEnabled) {
            float voidStart = mc.level.getMinBuildHeight() + 32.0F;
            float py = (float) cameraPos.y;
            if (py < voidStart) {
                float voidDepth = linggango_tweaks$smoothstep(Mth.clamp((voidStart - py) / 32.0F, 0.0F, 1.0F));
                clearWeatherNear = Mth.lerp(voidDepth, clearWeatherNear, 0.0F);
                clearWeatherFar = Mth.lerp(voidDepth, clearWeatherFar, 18.0F);
            }
        }

        float edgeWeatherMask = 1.0F - rainLevel * 0.45F;
        float edgeBias = originalEnd * edgeSoftness * edgeWeatherMask;
        float edgeNearBias = edgeBias * 0.34F;
        float farPlaneCap = originalEnd + edgeBias;

        float targetFar = Mth.clamp(clearWeatherFar + edgeBias, originalEnd * 0.55F, farPlaneCap);
        float targetNear = Mth.clamp(clearWeatherNear + edgeNearBias, 0.0F, targetFar - 0.5F);

        boolean isEditing = mc.screen instanceof AtmosphereEditorScreen;
        float distLerp = isEditing ? 1.0F : 0.015F;

        currentNearPlane = linggango_tweaks$smoothValue(currentNearPlane, targetNear, distLerp);
        currentFarPlane = linggango_tweaks$smoothValue(currentFarPlane, targetFar, distLerp);

        event.setNearPlaneDistance(currentNearPlane);
        event.setFarPlaneDistance(currentFarPlane);
        event.setFogShape(FogShape.CYLINDER);
        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onComputeFogColor(ViewportEvent.@NonNull ComputeFogColor event) {
        if (!loaded) {
            load();
        }

        if (!dynamicFogEnabled || event.getCamera().getFluidInCamera() != FogType.NONE) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            linggango_tweaks$resetSmoothing();
            return;
        }

        var level = mc.level;

        if (mc.player != null && (mc.player.hasEffect(MobEffects.NIGHT_VISION) || mc.player.hasEffect(MobEffects.BLINDNESS) || mc.player.hasEffect(MobEffects.DARKNESS))) {
            return;
        }

        Vec3 cameraPos = event.getCamera().getPosition();
        float partialTick = (float) event.getPartialTick();
        linggango_tweaks$updateColorSampling(mc, cameraPos, partialTick);

        float r = event.getRed();
        float g = event.getGreen();
        float b = event.getBlue();

        float vanillaLuma = Math.max(0.001F, linggango_tweaks$luminance(r, g, b));
        float biomeLuma = Math.max(0.001F, linggango_tweaks$luminance(sampledBiomeR, sampledBiomeG, sampledBiomeB));
        float skyLuma = Math.max(0.001F, linggango_tweaks$luminance(sampledSkyR, sampledSkyG, sampledSkyB));

        float biomeRatio = vanillaLuma / biomeLuma;
        float skyRatio = vanillaLuma / skyLuma;

        float tBiomeR = Math.min(1.0F, sampledBiomeR * biomeRatio);
        float tBiomeG = Math.min(1.0F, sampledBiomeG * biomeRatio);
        float tBiomeB = Math.min(1.0F, sampledBiomeB * biomeRatio);

        float tSkyR = Math.min(1.0F, sampledSkyR * skyRatio);
        float tSkyG = Math.min(1.0F, sampledSkyG * skyRatio);
        float tSkyB = Math.min(1.0F, sampledSkyB * skyRatio);

        float rain = level.getRainLevel(partialTick);
        float thunder = level.getThunderLevel(partialTick);
        float timeOfDay = level.getTimeOfDay(partialTick);
        float sunCurve = Mth.cos(timeOfDay * ((float) Math.PI * 2.0F));
        float dawnDusk = linggango_tweaks$smoothstep(Mth.clamp(1.0F - Math.abs(sunCurve) * 5.0F, 0.0F, 1.0F));
        float nightFactor = linggango_tweaks$smoothstep(Mth.clamp((-sunCurve * 0.70F) + 0.12F, 0.0F, 1.0F));

        float biomeBlend = (float) biomeTintStrength;
        float skyBlend = (float) skyFogBlendStrength * sampledSkyVisibility;
        float valleyBlend = sampledValleyMist * 0.18F;

        r = Mth.lerp(Mth.clamp(biomeBlend + valleyBlend, 0.0F, 1.0F), r, tBiomeR);
        g = Mth.lerp(Mth.clamp(biomeBlend + valleyBlend, 0.0F, 1.0F), g, tBiomeG);
        b = Mth.lerp(Mth.clamp(biomeBlend + valleyBlend, 0.0F, 1.0F), b, tBiomeB);

        r = Mth.lerp(skyBlend, r, tSkyR);
        g = Mth.lerp(skyBlend, g, tSkyG);
        b = Mth.lerp(skyBlend, b, tSkyB);

        if (dawnDusk > 0.0F && rain < 0.7F) {
            float warmBlend = dawnDusk * sampledSkyVisibility * (1.0F - rain) * 0.22F;
            float warmR = Math.min(1.0F, (sampledSkyR * 1.06F + 0.05F) * skyRatio);
            float warmG = Math.min(1.0F, (sampledSkyG * 0.98F + 0.02F) * skyRatio);
            float warmB = Math.min(1.0F, (sampledSkyB * 0.88F) * skyRatio);
            r = Mth.lerp(warmBlend, r, warmR);
            g = Mth.lerp(warmBlend * 0.85F, g, warmG);
            b = Mth.lerp(warmBlend * 0.55F, b, warmB);
        }

        if (nightFactor > 0.0F) {
            float coolR = Math.min(1.0F, (sampledBiomeR * 0.45F + 0.04F) * biomeRatio);
            float coolG = Math.min(1.0F, (sampledBiomeG * 0.52F + 0.05F) * biomeRatio);
            float coolB = Math.min(1.0F, (sampledBiomeB * 0.75F + 0.10F) * biomeRatio);
            float nightBlend = 0.28F * nightFactor;
            r = Mth.lerp(nightBlend, r, coolR);
            g = Mth.lerp(nightBlend, g, coolG);
            b = Mth.lerp(nightBlend, b, coolB);
        }

        if (rain > 0.0F) {
            float rainSmooth = linggango_tweaks$smoothstep(rain);
            float thunderSmooth = linggango_tweaks$smoothstep(thunder);
            float luminance = linggango_tweaks$luminance(r, g, b);
            float rainR = Mth.lerp(0.40F, luminance * 0.78F, tSkyR * 0.60F);
            float rainG = Mth.lerp(0.42F, luminance * 0.82F, tSkyG * 0.66F);
            float rainB = Mth.lerp(0.46F, luminance * 0.90F, tSkyB * 0.78F);

            rainR = Mth.lerp(thunderSmooth, rainR, rainR * 0.72F);
            rainG = Mth.lerp(thunderSmooth, rainG, rainG * 0.74F);
            rainB = Mth.lerp(thunderSmooth, rainB, rainB * 0.84F + 0.02F);

            r = Mth.lerp(rainSmooth * 0.78F, r, rainR);
            g = Mth.lerp(rainSmooth * 0.78F, g, rainG);
            b = Mth.lerp(rainSmooth * 0.82F, b, rainB);
        }

        if (voidFogEnabled) {
            float py = (float) cameraPos.y;
            float voidStart = mc.level.getMinBuildHeight() + 32.0F;
            if (py < voidStart) {
                float voidDepth = linggango_tweaks$smoothstep(Mth.clamp((voidStart - py) / 32.0F, 0.0F, 1.0F));
                float voidR = sampledBiomeR * 0.16F + 0.015F;
                float voidG = sampledBiomeG * 0.18F + 0.018F;
                float voidB = sampledBiomeB * 0.24F + 0.026F;
                r = Mth.lerp(voidDepth, r, voidR);
                g = Mth.lerp(voidDepth, g, voidG);
                b = Mth.lerp(voidDepth, b, voidB);
            }
        }

        r = Mth.clamp(r, 0.0F, 1.0F);
        g = Mth.clamp(g, 0.0F, 1.0F);
        b = Mth.clamp(b, 0.0F, 1.0F);

        boolean isEditing = mc.screen instanceof AtmosphereEditorScreen;
        float colorLerp = isEditing ? 1.0F : 0.015F;

        currentR = linggango_tweaks$smoothValue(currentR, r, colorLerp);
        currentG = linggango_tweaks$smoothValue(currentG, g, colorLerp);
        currentB = linggango_tweaks$smoothValue(currentB, b, colorLerp);

        event.setRed(currentR);
        event.setGreen(currentG);
        event.setBlue(currentB);
    }

    private static void linggango_tweaks$updateColorSampling(@NonNull Minecraft mc, @NonNull Vec3 cameraPos, float partialTick) {
        if (mc.level == null) {
            return;
        }

        var level = mc.level;
        int sampleX = Mth.floor(cameraPos.x);
        int sampleY = Mth.floor(cameraPos.y);
        int sampleZ = Mth.floor(cameraPos.z);
        int skySampleY = sampleY + SKY_OPENNESS_SAMPLE_HEIGHT;
        long gameTime = level.getGameTime();

        int dx = sampleX - lastSampleX;
        int dy = sampleY - lastSampleY;
        int dz = sampleZ - lastSampleZ;
        boolean shouldReuse = gameTime - lastSampleTick < COLOR_SAMPLE_INTERVAL_TICKS
                && dx * dx + dy * dy + dz * dz <= COLOR_SAMPLE_REUSE_DISTANCE_SQR;
        if (shouldReuse) {
            return;
        }

        BlockPos.MutableBlockPos cursor = SAMPLE_CURSOR.get();
        float totalWeight = 0.0F;
        float biomeR = 0.0F;
        float biomeG = 0.0F;
        float biomeB = 0.0F;
        float skyOpenness = 0.0F;

        for (int i = 0; i < SAMPLE_X.length; i++) {
            int sampleOffsetX = sampleX + SAMPLE_X[i];
            int sampleOffsetZ = sampleZ + SAMPLE_Z[i];
            cursor.set(sampleOffsetX, sampleY, sampleOffsetZ);

            var biomeHolder = level.getBiome(cursor);
            int fogColor = biomeHolder.value().getFogColor();

            ResourceLocation biomeLoc = biomeHolder.unwrapKey().map(ResourceKey::location).orElse(null);
            AtmosphereConfigManager.AtmosphereSettings settings = biomeLoc != null ? AtmosphereConfigManager.ATMOSPHERES.get(biomeLoc.toString()) : null;

            if (settings != null) {
                fogColor = settings.fogHex;
            }

            float weight = SAMPLE_WEIGHT[i];
            biomeR += ((fogColor >> 16) & 255) / 255.0F * weight;
            biomeG += ((fogColor >> 8) & 255) / 255.0F * weight;
            biomeB += (fogColor & 255) / 255.0F * weight;

            cursor.set(sampleOffsetX, skySampleY, sampleOffsetZ);
            skyOpenness += (level.canSeeSky(cursor) ? 1.0F : 0.0F) * weight;
            totalWeight += weight;
        }

        if (totalWeight > 0.0F) {
            sampledBiomeR = biomeR / totalWeight;
            sampledBiomeG = biomeG / totalWeight;
            sampledBiomeB = biomeB / totalWeight;
            sampledSkyOpenness = skyOpenness / totalWeight;
        }

        Vec3 sky = level.getSkyColor(cameraPos, partialTick);
        sampledSkyR = (float) sky.x;
        sampledSkyG = (float) sky.y;
        sampledSkyB = (float) sky.z;

        float seaLevelClear = Mth.clamp(((float) cameraPos.y - level.getSeaLevel() + 12.0F) / 96.0F, 0.0F, 1.0F);
        seaLevelClear = linggango_tweaks$smoothstep(seaLevelClear);
        sampledSkyVisibility = Mth.clamp(sampledSkyOpenness * sampledSkyOpenness * (0.88F + seaLevelClear * 0.12F), 0.0F, 1.0F);
        sampledValleyMist = 1.0F - seaLevelClear;

        lastSampleTick = gameTime;
        lastSampleX = sampleX;
        lastSampleY = sampleY;
        lastSampleZ = sampleZ;
    }

    private static double linggango_tweaks$parseDouble(Properties props, String key, double defaultValue, double min, double max) {
        try {
            return Mth.clamp(Double.parseDouble(props.getProperty(key, String.valueOf(defaultValue))), min, max);
        } catch (Exception ignored) {
            return defaultValue;
        }
    }

    private static float linggango_tweaks$luminance(float r, float g, float b) {
        return r * 0.299F + g * 0.587F + b * 0.114F;
    }

    private static float linggango_tweaks$smoothstep(float value) {
        return value * value * (3.0F - 2.0F * value);
    }

    private static float linggango_tweaks$smoothValue(float current, float target, float rate) {
        if (current < 0.0F || Math.abs(current - target) < COLOR_EPSILON) {
            return target;
        }

        return Mth.lerp(rate, current, target);
    }

    private static void linggango_tweaks$resetSmoothing() {
        currentNearPlane = -1.0F;
        currentFarPlane = -1.0F;
        currentR = -1.0F;
        currentG = -1.0F;
        currentB = -1.0F;
        lastSampleTick = Long.MIN_VALUE;
    }
}