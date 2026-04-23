package com.misanthropy.linggango.linggango_tweaks.mixin.legacy;

import com.aizistral.enigmaticlegacy.crafting.HiddenRecipe;
import com.aizistral.enigmaticlegacy.registries.EnigmaticItems;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(HiddenRecipe.class)
public class HiddenRecipeMixin {

    @Inject(
            method = "assemble(Lnet/minecraft/world/inventory/CraftingContainer;Lnet/minecraft/core/RegistryAccess;)Lnet/minecraft/world/item/ItemStack;",
            at = @At("RETURN"),
            cancellable = true,
            remap = false
    )
    private void linggango$transferPrimevalData(@NonNull CraftingContainer inv, RegistryAccess access, @NonNull CallbackInfoReturnable<ItemStack> cir) {
        ItemStack result = cir.getReturnValue();

        if (result.isEmpty()) {
            return;
        }

        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack ingredient = inv.getItem(i);

            if (ingredient.is(EnigmaticItems.ENIGMATIC_ITEM) && ingredient.hasTag()) {
                CompoundTag sourceTag = ingredient.getTag();

                if (sourceTag != null && sourceTag.contains("PrimevalDescender")) {
                    Tag data = sourceTag.get("PrimevalDescender");

                    if (data != null) {
                        result.getOrCreateTag().put("PrimevalDescender", data.copy());
                        cir.setReturnValue(result);
                        return;
                    }
                }
            }
        }
    }
}