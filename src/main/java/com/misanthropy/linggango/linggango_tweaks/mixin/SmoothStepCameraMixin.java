package com.misanthropy.linggango.linggango_tweaks.mixin;

import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class SmoothStepCameraMixin {

    @Shadow protected abstract void setPosition(double x, double y, double z);
    @Shadow private net.minecraft.world.phys.Vec3 position;

    private double smoothedY = Double.NaN;

    @Inject(method = "setup", at = @At("TAIL"))
    private void applySmoothStep(BlockGetter level, Entity entity, boolean detached, boolean thirdPersonReverse, float partialTick, CallbackInfo ci) {
        if (entity == null || detached) return;

        double targetY = this.position.y;
        if (Double.isNaN(smoothedY) || Math.abs(targetY - smoothedY) > 1.0D) {
            smoothedY = targetY;
            return;
        }
        if (targetY > smoothedY && targetY - smoothedY <= 0.6D && entity.onGround()) {
            smoothedY += (targetY - smoothedY) * 0.15D;
            this.setPosition(this.position.x, smoothedY, this.position.z);
        } else {
            smoothedY = targetY;
        }
    }
}