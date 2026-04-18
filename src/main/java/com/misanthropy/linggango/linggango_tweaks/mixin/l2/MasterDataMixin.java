package com.misanthropy.linggango.linggango_tweaks.mixin.l2;

import com.misanthropy.linggango.linggango_tweaks.integration.l2.ApostleL2Data;
import dev.xkmc.l2hostility.content.capability.mob.MasterData;
import dev.xkmc.l2hostility.content.capability.mob.MobTraitCap;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = MasterData.class, remap = false)
public abstract class MasterDataMixin {
    @Inject(method = "tick", at = @At("HEAD"))
    private void injectTickHead(MobTraitCap cap, Mob mob, CallbackInfoReturnable<Boolean> cir) {
        ApostleL2Data.CURRENT_APOSTLE = mob;
    }

    @Inject(method = "tick", at = @At("RETURN"))
    private void injectTickReturn(MobTraitCap cap, Mob mob, CallbackInfoReturnable<Boolean> cir) {
        ApostleL2Data.CURRENT_APOSTLE = null;
    }
}
