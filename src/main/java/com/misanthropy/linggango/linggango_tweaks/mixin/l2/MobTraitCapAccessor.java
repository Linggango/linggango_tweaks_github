package com.misanthropy.linggango.linggango_tweaks.mixin.l2;

import dev.xkmc.l2hostility.content.capability.mob.MobTraitCap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = MobTraitCap.class, remap = false)
public interface MobTraitCapAccessor {
    @Accessor("stage")
    MobTraitCap.Stage blank_mixin_mod$getStage();
}
