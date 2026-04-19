package com.misanthropy.linggango.linggango_tweaks.mixin.l2;

import com.Polarice3.Goety.common.entities.boss.Apostle;
import com.misanthropy.linggango.linggango_tweaks.integration.l2.ApostleL2Data;
import dev.xkmc.l2hostility.content.capability.mob.MobTraitCap;
import net.minecraft.world.entity.LivingEntity;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = Apostle.class, remap = false)
public abstract class ApostleTitleMixin {

    @Unique
    private int blank_mixin_mod$lastTitleNumber = -1;

    @Inject(
            method = "allTitleApostle$titleNumber(F)I", //goety_revelation.ApostleMixin::allTitleApostle$titleNumber
            at = @At("RETURN"),
            remap = false)
    private void onTitleNumberReturn(float health, @NonNull CallbackInfoReturnable<Integer> cir) {
        int newTitle = cir.getReturnValue();
        if (newTitle == blank_mixin_mod$lastTitleNumber) return;

        blank_mixin_mod$lastTitleNumber = newTitle;

        LivingEntity self = (LivingEntity) (Object) this;
        if (self.level().isClientSide()) return;

        if (!MobTraitCap.HOLDER.isProper(self)) return;
        MobTraitCap cap = MobTraitCap.HOLDER.get(self);

        if (((MobTraitCapAccessor) cap).blank_mixin_mod$getStage() != MobTraitCap.Stage.POST_INIT) return;

        cap.setConfigCache(null);
        ApostleL2Data.SKIP_HEALTH_RESET.add(self.getUUID());
        ApostleL2Data.SKIP_TICK_HEALTH_RESET.add(self.getUUID());
        try {
            cap.reinit(self, cap.lv, false);
        } finally {
            ApostleL2Data.SKIP_HEALTH_RESET.remove(self.getUUID());
        }
    }
}
