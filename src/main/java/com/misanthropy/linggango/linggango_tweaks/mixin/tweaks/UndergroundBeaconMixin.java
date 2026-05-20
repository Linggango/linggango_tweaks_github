package com.misanthropy.linggango.linggango_tweaks.mixin.tweaks;

import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(BeaconBlockEntity.class)
public class UndergroundBeaconMixin {

    @ModifyConstant(
            method = "tick",
            constant = @Constant(intValue = 15)
    )
    private static int ignoreBeaconOpacity(int originalOpacity) {
        return 100;
    }
}