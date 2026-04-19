package com.misanthropy.linggango.linggango_tweaks.mixin.celestisynth;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.thecelestialworkshop.celestisynth.common.entity.base.CSEffectEntity;
import org.thecelestialworkshop.celestisynth.common.entity.helper.CSVisualType;
import org.thecelestialworkshop.celestisynth.common.registry.CSVisualTypes;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

@Pseudo
@Mixin(CSEffectEntity.class)
public abstract class TurretFlashFix {

    @Unique
    private static Method linggango$getViewVectorMethod;
    @Unique
    private static Method linggango$setYRotMethod;
    @Unique
    private static Method linggango$setXRotMethod;
    @Unique
    private static Field linggango$yRotOField;
    @Unique
    private static Field linggango$xRotOField;
    @Inject(method = "getEffectInstance", at = @At("RETURN"), remap = false)
    private static void linggango$fixTurretMuzzleRotation(LivingEntity owner, @Nullable Entity toFollow, CSVisualType visual, double offsetX, double offsetY, double offsetZ, @NonNull CallbackInfoReturnable<?> cir) {
        Object effectObj = cir.getReturnValue();
        if (!(effectObj instanceof Entity effect) || toFollow == null) return;
        if (toFollow.getClass().getSimpleName().equals("RainfallTurret")) {
            if (visual == CSVisualTypes.RAINFALL_SHOOT.get()) {
                Vec3 look = toFollow.getLookAngle();
                double horizontalMag = Math.sqrt(look.x * look.x + look.z * look.z);
                double pitchRadians = Math.atan2(look.y, horizontalMag);
                double minScale = 2.0;
                double maxScale = 3.5;
                double forwardDistance = minScale + (maxScale - minScale) * Math.abs(Math.sin(pitchRadians));
                double maxLift = -1.0;
                double minLift = 0.0;
                double liftY = maxLift - (minLift - maxLift) * Math.abs(Math.sin(pitchRadians));
                Vec3 finalOffset = look.scale(forwardDistance).add(0, liftY, 0);
                effect.setPos(toFollow.getX() + finalOffset.x, toFollow.getY() + finalOffset.y, toFollow.getZ() + finalOffset.z);
                Vec3 shotVec = linggango$getTurretViewVector(toFollow);
                if (shotVec != null) {
                    float yaw = (float) Math.toDegrees(Math.atan2(shotVec.z, shotVec.x)) - 90.0F;
                    float pitch = (float) (-Math.toDegrees(Math.atan2(shotVec.y, Math.sqrt(shotVec.x * shotVec.x + shotVec.z * shotVec.z))));
                    linggango$setEntityRotation(effect, yaw - 17.0F, pitch);
                }
            }
        }
    }

    @Unique
    private static void linggango$setEntityRotation(Entity entity, float yaw, float pitch) {
        try {
            if (linggango$setYRotMethod == null) {
                try {
                    linggango$setYRotMethod = Entity.class.getDeclaredMethod("setYRot", float.class);
                    linggango$setXRotMethod = Entity.class.getDeclaredMethod("setXRot", float.class);
                    linggango$yRotOField = Entity.class.getDeclaredField("yRotO");
                    linggango$xRotOField = Entity.class.getDeclaredField("xRotO");
                } catch (NoSuchMethodException | NoSuchFieldException e) {
                    String mPrefix = "m_";
                    String fPrefix = "f_";

                    linggango$setYRotMethod = Entity.class.getDeclaredMethod(mPrefix + "146922_", float.class);
                    linggango$setXRotMethod = Entity.class.getDeclaredMethod(mPrefix + "146926_", float.class);
                    linggango$yRotOField = Entity.class.getDeclaredField(fPrefix + "19859_");
                    linggango$xRotOField = Entity.class.getDeclaredField(fPrefix + "19860_");
                }
                linggango$setYRotMethod.setAccessible(true);
                linggango$setXRotMethod.setAccessible(true);
                linggango$yRotOField.setAccessible(true);
                linggango$xRotOField.setAccessible(true);
            }

            linggango$setYRotMethod.invoke(entity, yaw);
            linggango$yRotOField.setFloat(entity, yaw);
            linggango$setXRotMethod.invoke(entity, pitch);
            linggango$xRotOField.setFloat(entity, pitch);
        } catch (Exception ignored) {}
    }

    @Unique
    private static Vec3 linggango$getTurretViewVector(@NonNull Entity turret) {
        try {
            if (linggango$getViewVectorMethod == null) {
                try {
                    linggango$getViewVectorMethod = turret.getClass().getMethod("getViewVector", float.class);
                } catch (NoSuchMethodException e) {
                    linggango$getViewVectorMethod = turret.getClass().getMethod("m_" + "20252_", float.class);
                }
            }
            return (Vec3) linggango$getViewVectorMethod.invoke(turret, 1.0F);
        } catch (Exception e) {
            return turret.getLookAngle();
        }
    }
}