package com.misanthropy.linggango.linggango_tweaks.mixin.loot;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.mattias.capybara.core.loot.AddItemModifier", remap = false)
public class CapybaraLootNerfMixin {

    @Inject(method = "doApply", at = @At("HEAD"), cancellable = true)
    private void linggango$nerfCapybaraLoot(ObjectArrayList<ItemStack> generatedLoot, LootContext context, CallbackInfoReturnable<ObjectArrayList<ItemStack>> cir) {
        if (context.getRandom().nextFloat() > 0.05f) {
            cir.setReturnValue(generatedLoot);
        }
    }
}