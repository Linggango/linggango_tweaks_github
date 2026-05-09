package com.misanthropy.linggango.linggango_tweaks.mixin.tweaks;

import com.yungnickyoung.minecraft.ribbits.entity.RibbitEntity;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = RibbitEntity.class, remap = false)
public abstract class RibbitHomeFixMixin {

    @Shadow
    private BlockPos homePosition;

    @Inject(method = "getHomePosition", at = @At("HEAD"), cancellable = true)
    private void linggango$preventNullHomeCrash(CallbackInfoReturnable<BlockPos> cir) {
        if (this.homePosition == null) {
            RibbitEntity ribbit = (RibbitEntity) (Object) this;
            cir.setReturnValue(ribbit.blockPosition());
        }
    }
}