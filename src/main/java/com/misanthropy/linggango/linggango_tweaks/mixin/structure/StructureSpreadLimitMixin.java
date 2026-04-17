package com.misanthropy.linggango.linggango_tweaks.mixin.structure;

import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(RandomSpreadStructurePlacement.class)
public class StructureSpreadLimitMixin {
    @ModifyConstant(
            method = {"lambda$static$0", "m_204995_"},
            constant = @Constant(intValue = 4096),
            require = 0
    )
    private static int linggango$pushSpreadLimit(int originalLimit) {
        return Integer.MAX_VALUE;
    }
}