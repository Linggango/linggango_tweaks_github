package com.misanthropy.linggango.linggango_tweaks.mixin.tweaks;

import net.minecraft.world.inventory.AnvilMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Field;

@Mixin(AnvilMenu.class)
public class AnvilMenuMixin {

    @Unique
    private static Field linggango$maxCostField;

    @Unique
    private static boolean linggango$initialized = false;

    @Inject(method = "createResult", at = @At("HEAD"))
    private void removeTooExpensiveLimit(CallbackInfo ci) {
        if (!linggango$initialized) {
            try {
                linggango$maxCostField = AnvilMenu.class.getDeclaredField("maximumCost");
                linggango$maxCostField.setAccessible(true);
            } catch (Exception e) {
            }
            linggango$initialized = true;
        }

        if (linggango$maxCostField != null) {
            try {
                linggango$maxCostField.setInt(this, Integer.MAX_VALUE);
            } catch (Exception e) {
            }
        }
    }
}