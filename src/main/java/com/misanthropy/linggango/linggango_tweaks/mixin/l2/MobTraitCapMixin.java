package com.misanthropy.linggango.linggango_tweaks.mixin.l2;

import com.misanthropy.linggango.linggango_tweaks.integration.l2.ApostleL2Data;
import dev.xkmc.l2hostility.content.capability.mob.MobTraitCap;
import dev.xkmc.l2hostility.content.config.EntityConfig;
import net.minecraft.world.entity.LivingEntity;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = MobTraitCap.class, remap = false)
public abstract class MobTraitCapMixin {

    @Inject(method = "getConfigCache", at = @At("HEAD"), cancellable = true)
    private void injectApostleContextLookup(LivingEntity le,
            @NonNull CallbackInfoReturnable<EntityConfig.Config> cir) {
        var config = ApostleL2Data.getApostleConfig(le);
        if (config != null) {
            ((MobTraitCap) (Object) this).setConfigCache(config);
            cir.setReturnValue(config);
            cir.cancel();
        }
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTickStart(LivingEntity entity, CallbackInfo ci) {
        ApostleL2Data.TICKING_MOBS.add(entity.getUUID());
    }

    @Inject(method = "tick", at = @At("RETURN"))
    private void onTickEnd(LivingEntity entity, CallbackInfo ci) {
        ApostleL2Data.TICKING_MOBS.remove(entity.getUUID());
        if (ApostleL2Data.PENDING_REINIT.remove(entity.getUUID())) {
            MobTraitCap cap = (MobTraitCap) (Object) this;
            if (((MobTraitCapAccessor) cap).blank_mixin_mod$getStage() != MobTraitCap.Stage.POST_INIT) return;
            ApostleL2Data.doReinit(entity, cap);
        }
    }

    @Redirect(
            method = "tick",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;setHealth(F)V"),
            remap = true)
    private void skipTickHealthReset(@NonNull LivingEntity mob, float p_21154_) {
        if (!ApostleL2Data.SKIP_TICK_HEALTH_RESET.remove(mob.getUUID())) {
            mob.setHealth(p_21154_);
        }
    }
}
