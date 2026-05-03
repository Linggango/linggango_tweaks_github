package com.misanthropy.linggango.linggango_tweaks.mixin.atmosphere;

import com.misanthropy.linggango.linggango_tweaks.client.atmosphere.AtmosphereEditorScreen;
import com.misanthropy.linggango.linggango_tweaks.config.AtmosphereConfigManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientLevel.class)
public abstract class SkyColorMixin {

    @Unique
    private static Vec3 linggango$currentSky = null;

    @Inject(method = "getSkyColor", at = @At("RETURN"), cancellable = true)
    private void applyCustomSkyColor(Vec3 pos, float partialTick, CallbackInfoReturnable<Vec3> cir) {
        ClientLevel level = (ClientLevel) (Object) this;
        BlockPos blockPos = BlockPos.containing(pos);

        Holder<Biome> biomeHolder = level.getBiome(blockPos);
        ResourceLocation biomeLoc = biomeHolder.unwrapKey().map(ResourceKey::location).orElse(null);

        Vec3 targetSky = cir.getReturnValue();

        if (biomeLoc != null) {
            AtmosphereConfigManager.AtmosphereSettings data = AtmosphereConfigManager.ATMOSPHERES.get(biomeLoc.toString());

            if (data != null && data.skyHex != -1) {
                float r = ((data.skyHex >> 16) & 0xFF) / 255.0f;
                float g = ((data.skyHex >> 8) & 0xFF) / 255.0f;
                float b = (data.skyHex & 0xFF) / 255.0f;

                float timeOfDay = level.getTimeOfDay(partialTick);
                float sunBrightness = Mth.cos(timeOfDay * ((float) Math.PI * 2.0F)) * 2.0F + 0.5F;
                sunBrightness = Mth.clamp(sunBrightness, 0.0F, 1.0F);

                r *= sunBrightness;
                g *= sunBrightness;
                b *= sunBrightness;

                float rainLevel = level.getRainLevel(partialTick);
                if (rainLevel > 0.0F) {
                    float luma = r * 0.3F + g * 0.59F + b * 0.11F;
                    float rainMix = 1.0F - rainLevel * 0.75F;
                    r = r * rainMix + (luma * 0.6F) * (1.0F - rainMix);
                    g = g * rainMix + (luma * 0.6F) * (1.0F - rainMix);
                    b = b * rainMix + (luma * 0.6F) * (1.0F - rainMix);
                }

                float thunderLevel = level.getThunderLevel(partialTick);
                if (thunderLevel > 0.0F) {
                    float luma = r * 0.3F + g * 0.59F + b * 0.11F;
                    float thunderMix = 1.0F - thunderLevel * 0.75F;
                    r = r * thunderMix + (luma * 0.2F) * (1.0F - thunderMix);
                    g = g * thunderMix + (luma * 0.2F) * (1.0F - thunderMix);
                    b = b * thunderMix + (luma * 0.2F) * (1.0F - thunderMix);
                }

                targetSky = new Vec3(r, g, b);
            }
        }

        if (linggango$currentSky == null) {
            linggango$currentSky = targetSky;
        }

        boolean isEditing = Minecraft.getInstance().screen instanceof AtmosphereEditorScreen;
        float lerpFactor = isEditing ? 1.0F : 0.015F;

        double r = Mth.lerp(lerpFactor, linggango$currentSky.x, targetSky.x);
        double g = Mth.lerp(lerpFactor, linggango$currentSky.y, targetSky.y);
        double b = Mth.lerp(lerpFactor, linggango$currentSky.z, targetSky.z);

        linggango$currentSky = new Vec3(r, g, b);

        cir.setReturnValue(linggango$currentSky);
    }
}