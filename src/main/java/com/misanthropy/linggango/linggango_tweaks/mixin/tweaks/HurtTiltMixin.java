package com.misanthropy.linggango.linggango_tweaks.mixin.tweaks;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Field;

@Mixin(LivingEntity.class)
public abstract class HurtTiltMixin {
    @Unique private static Field linggango$hurtDirField;
    @Unique private static boolean linggango$initialized = false;
    @Inject(method = "handleEntityEvent", at = @At("HEAD"))
    private void adjustHurtTilt(byte id, CallbackInfo ci) {
        if (id == 2) {
            LivingEntity entity = (LivingEntity) (Object) this;

            if (entity.level().isClientSide() && entity.equals(Minecraft.getInstance().player)) {
                float yaw = entity.getYRot();
                if (!linggango$initialized) {
                    try {
                        linggango$hurtDirField = LivingEntity.class.getField("hurtDir");
                    } catch (NoSuchFieldException e) {
                        try {
                            linggango$hurtDirField = LivingEntity.class.getField("f_20892_");
                        } catch (NoSuchFieldException ex) {
                        }
                    }
                    linggango$initialized = true;
                }
                if (linggango$hurtDirField != null) {
                    try {
                        float currentDir = linggango$hurtDirField.getFloat(entity);
                        linggango$hurtDirField.setFloat(entity, currentDir - yaw);
                    } catch (Exception e) {
                    }
                }
            }
        }
    }
}