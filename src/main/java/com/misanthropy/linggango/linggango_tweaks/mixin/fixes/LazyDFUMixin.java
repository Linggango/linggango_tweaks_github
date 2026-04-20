package com.misanthropy.linggango.linggango_tweaks.mixin.fixes;

import net.minecraft.SharedConstants;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(SharedConstants.class)
public class LazyDFUMixin {
    /**
     * @author Misanthropy
     * @reason test
     */
    @Overwrite
    public static void enableDataFixerOptimizations() {
    }
}