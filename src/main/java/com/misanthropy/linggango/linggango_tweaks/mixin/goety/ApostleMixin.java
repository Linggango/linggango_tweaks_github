package com.misanthropy.linggango.linggango_tweaks.mixin.goety;

import com.Polarice3.Goety.common.entities.boss.Apostle;
import com.Polarice3.Goety.config.AttributesConfig;
import com.misanthropy.linggango.linggango_tweaks.config.TweaksConfig;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class ApostleMixin {

    @Unique
    private boolean linggango_tweaks$isActuallyHurting = false;

    @Inject(
            method = "actuallyHurt",
            at = @At("HEAD")
    )
    private void linggango_tweaks$onActuallyHurt(DamageSource source, float amount, CallbackInfo ci) {
        if ((Object) this instanceof Apostle) {
            if (!source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
                linggango_tweaks$isActuallyHurting = true;
            }
        }
    }

    @ModifyVariable(
            method = "setHealth",
            at = @At("HEAD"),
            argsOnly = true
    )
    private float linggango_tweaks$modifyHealth(float health) {
        if ((Object) this instanceof Apostle apostle) {
            if (linggango_tweaks$isActuallyHurting && TweaksConfig.APOSTLE_DAMAGE_CAP_FIX.get()) {
                linggango_tweaks$isActuallyHurting = false;
                float actuallyHurtAmount = apostle.getHealth() - health;
                if (actuallyHurtAmount > 0) {
                    float cap = AttributesConfig.ApostleDamageCap.get().floatValue();
                    return apostle.getHealth() - Math.min(actuallyHurtAmount, cap);
                }
            }
        }
        return health;
    }
}