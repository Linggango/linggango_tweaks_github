//package com.misanthropy.linggango.linggango_tweaks.mixin.atmosphere;
//
//import net.minecraft.client.Minecraft;
//import net.minecraft.client.multiplayer.ClientLevel;
//import net.minecraft.client.renderer.DimensionSpecialEffects;
//import org.spongepowered.asm.mixin.Mixin;
//import org.spongepowered.asm.mixin.injection.At;
//import org.spongepowered.asm.mixin.injection.Inject;
//import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
//
//@Mixin(DimensionSpecialEffects.class)
//public abstract class DimensionSpecialEffectsMixin {
//
//    @Inject(method = "getSunriseColor", at = @At("HEAD"), cancellable = true)
//    private void linggango$disableSunriseDuringWeather(float timeOfDay, float partialTick, CallbackInfoReturnable<float[]> cir) {
//        ClientLevel level = Minecraft.getInstance().level;
//        if (level != null) {
//            float rainLevel = level.getRainLevel(partialTick);
//            float thunderLevel = level.getThunderLevel(partialTick);
//            if (rainLevel > 0.0F || thunderLevel > 0.0F) {
//                cir.setReturnValue(null);
//            }
//        }
//    }
//}