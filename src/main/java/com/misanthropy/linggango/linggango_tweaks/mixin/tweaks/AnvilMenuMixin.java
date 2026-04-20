package com.misanthropy.linggango.linggango_tweaks.mixin.tweaks;

import net.minecraft.world.inventory.AnvilMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(AnvilMenu.class)
public class AnvilMenuMixin {

    @ModifyConstant(
            method = "createResult",
            constant = @Constant(intValue = 40),
            require = 0
    )
    private int linggango$removeAnvilLimit(int constant) {
        return Integer.MAX_VALUE;
    }
}