package com.misanthropy.linggango.linggango_tweaks.mixin.tweaks.client;

import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class SmoothStepCameraMixin {

    @Shadow protected abstract void setPosition(double x, double y, double z);
    @Shadow private net.minecraft.world.phys.Vec3 position;

    @Unique
    private double linggango_tweaks$smoothedY = Double.NaN;
    @Unique
    private long linggango_tweaks$lastTime = 0L;

    @Inject(
            method = "setup(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/world/entity/Entity;ZZF)V",
            at = @At("TAIL")
    )
    private void applySmoothStep(BlockGetter level, Entity entity, boolean detached, boolean thirdPersonReverse, float partialTick, CallbackInfo ci) {
        if (entity == null || detached) {
            linggango_tweaks$smoothedY = Double.NaN;
            return;
        }

        double targetY = this.position.y;
        long currentTime = System.currentTimeMillis();

        if (Double.isNaN(linggango_tweaks$smoothedY) || linggango_tweaks$lastTime == 0 || Math.abs(targetY - linggango_tweaks$smoothedY) > 1.5D) {
            linggango_tweaks$smoothedY = targetY;
            linggango_tweaks$lastTime = currentTime;
            return;
        }

        double dt = (currentTime - linggango_tweaks$lastTime) / 1000.0;
        linggango_tweaks$lastTime = currentTime;

        if (dt > 0.1) dt = 0.1;
        double diff = targetY - linggango_tweaks$smoothedY;
        boolean isGrounded = entity.onGround() || (entity.getVehicle() != null && entity.getRootVehicle().onGround());

        if (diff > 0.0D && isGrounded) {
            double smoothingFactor = 1.0 - Math.exp(-15.0 * dt);

            linggango_tweaks$smoothedY += diff * smoothingFactor;
            this.setPosition(this.position.x, linggango_tweaks$smoothedY, this.position.z);

        } else {
            linggango_tweaks$smoothedY = targetY;
        }
    }
}