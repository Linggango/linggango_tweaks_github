package com.misanthropy.linggango.linggango_tweaks.mixin.l2;

import com.misanthropy.linggango.linggango_tweaks.integration.l2.ApostleL2Data;
import dev.xkmc.l2hostility.content.config.EntityConfig;
import dev.xkmc.l2hostility.content.traits.legendary.MasterTrait;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = MasterTrait.class, remap = false)
public abstract class MasterTraitMixin {

    @Inject(method = "allow", at = @At("HEAD"))
    private void injectApostleContextLookup(LivingEntity le, int difficulty, int maxModLv, CallbackInfoReturnable<Boolean> cir) {
        ApostleL2Data.pushApostleContext(le);
    }

    @Inject(method = "allow", at = @At("RETURN"))
    private void injectApostleContextReturn(LivingEntity le, int difficulty, int maxModLv, CallbackInfoReturnable<Boolean> cir) {
        ApostleL2Data.popApostleContext();
    }

    @Inject(method = "getConfig", at = @At("HEAD"), cancellable = true)
    private static void injectApostleConfigLookup(EntityType<?> type, CallbackInfoReturnable<EntityConfig.MasterConfig> cir) {
        var config = ApostleL2Data.getApostleConfig(ApostleL2Data.currentApostle());
        if (config != null) {
            cir.setReturnValue(config.asMaster);
            cir.cancel();
        }
    }
}
