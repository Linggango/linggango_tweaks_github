package com.misanthropy.linggango.linggango_tweaks.mixin.l2;

import com.Polarice3.Goety.common.entities.boss.Apostle;
import com.misanthropy.linggango.linggango_tweaks.LinggangoTweaks;
import com.misanthropy.linggango.linggango_tweaks.integration.l2.ApostleEntityContext;
import com.misanthropy.linggango.linggango_tweaks.integration.l2.ApostleL2Data;
import dev.xkmc.l2hostility.content.config.EntityConfig;
import dev.xkmc.l2hostility.content.traits.legendary.MasterTrait;
import dev.xkmc.l2hostility.init.L2Hostility;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = MasterTrait.class, remap = false)
public abstract class MasterTraitMixin {

    @Inject(method = "allow", at = @At("HEAD"))
    private void injectApostleContextLookup(LivingEntity le, int difficulty, int maxModLv, CallbackInfoReturnable<Boolean> cir) {
        ApostleL2Data.CURRENT_APOSTLE = le;
    }

    @Inject(method = "allow", at = @At("RETURN"))
    private void injectApostleContextReturn(LivingEntity le, int difficulty, int maxModLv, CallbackInfoReturnable<Boolean> cir) {
            ApostleL2Data.CURRENT_APOSTLE = null;
    }

    @Inject(method = "getConfig", at = @At("HEAD"), cancellable = true)
    private static void injectApostleConfigLookup(@NonNull EntityType<?> type, @NonNull CallbackInfoReturnable<EntityConfig.MasterConfig> cir) {
        if (!(ApostleL2Data.CURRENT_APOSTLE instanceof Apostle)) {
            return;
        }

        var data = new ApostleEntityContext(ApostleL2Data.CURRENT_APOSTLE);
        EntityConfig.Config config = L2Hostility.ENTITY.getMerged().get(
                type,
                LinggangoTweaks.APOSTLE_CONTEXT,
                ApostleEntityContext.class,
                data);

        if (config != null) {
            cir.setReturnValue(config.asMaster);
            cir.cancel();
        }
    }
}
