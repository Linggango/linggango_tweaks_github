package com.misanthropy.linggango.linggango_tweaks.tweaks;

import com.misanthropy.linggango.linggango_tweaks.client.atmosphere.AtmosphereEditorScreen;
import com.misanthropy.linggango.linggango_tweaks.config.AtmosphereConfigManager;
import com.mojang.blaze3d.shaders.FogShape;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
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
<<<<<<< HEAD
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

=======
>>>>>>> parent of 29d0554 (update)
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
<<<<<<< HEAD
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
=======
>>>>>>> parent of 29d0554 (update)

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

<<<<<<< HEAD
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
=======
        if (currentNearPlane < 0f) {
            currentNearPlane = targetNear;
            currentFarPlane = targetFar;
        } else {
            currentNearPlane = Mth.lerp(0.03f, currentNearPlane, targetNear);
            currentFarPlane = Mth.lerp(0.03f, currentFarPlane, targetFar);
        }
>>>>>>> parent of 29d0554 (update)

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

<<<<<<< HEAD
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
=======
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
>>>>>>> parent of 29d0554 (update)

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

<<<<<<< HEAD
        r = Mth.clamp(r, 0.0F, 1.0F);
        g = Mth.clamp(g, 0.0F, 1.0F);
        b = Mth.clamp(b, 0.0F, 1.0F);

        boolean isEditing = mc.screen instanceof AtmosphereEditorScreen;
        float colorLerp = isEditing ? 1.0F : 0.015F;

        currentR = linggango_tweaks$smoothValue(currentR, r, colorLerp);
        currentG = linggango_tweaks$smoothValue(currentG, g, colorLerp);
        currentB = linggango_tweaks$smoothValue(currentB, b, colorLerp);
=======
        if (currentR < 0f) {
            currentR = r;
            currentG = g;
            currentB = b;
        } else {
            currentR = Mth.lerp(0.04f, currentR, r);
            currentG = Mth.lerp(0.04f, currentG, g);
            currentB = Mth.lerp(0.04f, currentB, b);
        }
>>>>>>> parent of 29d0554 (update)

        event.setRed(currentR);
        event.setGreen(currentG);
        event.setBlue(currentB);
    }
<<<<<<< HEAD

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
=======
>>>>>>> parent of 29d0554 (update)
}