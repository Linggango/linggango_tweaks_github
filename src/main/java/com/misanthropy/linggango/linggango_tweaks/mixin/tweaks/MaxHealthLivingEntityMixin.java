package com.misanthropy.linggango.linggango_tweaks.mixin.tweaks;

import com.misanthropy.linggango.linggango_tweaks.util.HealthFix;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = LivingEntity.class, priority = 9001)
public abstract class MaxHealthLivingEntityMixin implements HealthFix {

    @Unique
    private Float linggango$actualHealth = null;

    @Shadow public abstract float getMaxHealth();
    @Shadow public abstract float getHealth();
    @Shadow public abstract void setHealth(float health);

    @Inject(method = "readAdditionalSaveData", at = @At("HEAD"))
    private void linggango$readAdditionalSaveData(CompoundTag tag, CallbackInfo callback) {
        if (tag.contains("Health", 99)) {
            float savedHealth = tag.getFloat("Health");
            if (savedHealth > this.getMaxHealth() && savedHealth > 0.0F) {
                this.linggango$actualHealth = savedHealth;
            }
        }
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void linggango$tick(CallbackInfo callback) {
        if (this.linggango$actualHealth != null) {
            if (this.linggango$actualHealth > 0.0F && this.linggango$actualHealth > this.getHealth()) {
                this.setHealth(this.linggango$actualHealth);
            }
            this.linggango$actualHealth = null;
        }
    }

    @Override
    public void linggango$setRestorePoint(float restorePoint) {
        this.linggango$actualHealth = restorePoint;
    }
}