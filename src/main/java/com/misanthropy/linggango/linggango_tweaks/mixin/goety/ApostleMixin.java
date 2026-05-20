package com.misanthropy.linggango.linggango_tweaks.mixin.goety;

import com.Polarice3.Goety.common.entities.boss.Apostle;
import com.Polarice3.Goety.config.AttributesConfig;
import com.misanthropy.linggango.linggango_tweaks.config.TweaksConfig;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("unused")
@Pseudo
@Mixin(value = Apostle.class, remap = false)
public abstract class ApostleMixin extends LivingEntity {
    boolean linggango_tweaks$isActuallyHurting = false;

    protected ApostleMixin(EntityType<? extends LivingEntity> p_20966_, Level p_20967_) {
        super(p_20966_, p_20967_);
    }

    @Inject(
            method = "actuallyHurt",
            at = @org.spongepowered.asm.mixin.injection.At("HEAD")
    )
    private void linggango_tweaks$onActuallyHurt(DamageSource source, float amount, CallbackInfo ci) {
        if (!source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            linggango_tweaks$isActuallyHurting = true;
        }
    }

    /**
     * @author SaloEater
     * @reason There is a chain of actuallyHurt -> LivingHurtEvent -> LivingDamageEvent -> setHealth where in these events damage value is modified by other mods, but we want to keep the cap on the final damage
     */
    @Overwrite
    public void m_21153_(float health) { //setHealth
        if (linggango_tweaks$isActuallyHurting && TweaksConfig.APOSTLE_DAMAGE_CAP_FIX.get()) {
            linggango_tweaks$isActuallyHurting = false;
            var actuallyHurtAmount = this.getHealth() - health;
            if (actuallyHurtAmount > 0) {
                health = this.getHealth() - Math.min(actuallyHurtAmount, AttributesConfig.ApostleDamageCap.get().floatValue());
            }
        }

        super.setHealth(health);
    }
}
