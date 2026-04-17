package com.misanthropy.linggango.linggango_tweaks.mixin;

import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnchantmentHelper.class)
public class EnchantmentHelperMixin {

    @Inject(method = "getEnchantmentCost", at = @At("RETURN"), cancellable = true)
    private static void linggango_halveEnchantmentCost(RandomSource random, int enchantNum, int power, ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        int originalCost = cir.getReturnValue();
        int newCost = Math.max(1, originalCost / 2);
        cir.setReturnValue(newCost);
    }
}