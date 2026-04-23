package com.misanthropy.linggango.linggango_tweaks.mixin.l2;

import com.Polarice3.Goety.common.entities.boss.Apostle;
import com.misanthropy.linggango.linggango_tweaks.LinggangoTweaks;
import com.misanthropy.linggango.linggango_tweaks.integration.l2.ApostleEntityContext;
import com.misanthropy.linggango.linggango_tweaks.integration.l2.ApostleL2Data;
import dev.xkmc.l2hostility.content.capability.mob.MobTraitCap;
import dev.xkmc.l2hostility.content.config.EntityConfig;
import dev.xkmc.l2hostility.init.L2Hostility;
import net.minecraft.world.entity.LivingEntity;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = MobTraitCap.class, remap = false)
public abstract class MobTraitCapMixin {

    @Inject(method = "getConfigCache", at = @At("HEAD"), cancellable = true)
    private void injectApostleContextLookup(LivingEntity le,
                                            @NonNull CallbackInfoReturnable<EntityConfig.Config> cir) {
        if (!(le instanceof Apostle)) return;

        var data = new ApostleEntityContext(le);
        EntityConfig.Config config = L2Hostility.ENTITY.getMerged().get(
                le.getType(),
                LinggangoTweaks.APOSTLE_CONTEXT,
                ApostleEntityContext.class,
                data);

        if (config != null) {
            ((MobTraitCap) (Object) this).setConfigCache(config);
            cir.setReturnValue(config);
            cir.cancel();
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
