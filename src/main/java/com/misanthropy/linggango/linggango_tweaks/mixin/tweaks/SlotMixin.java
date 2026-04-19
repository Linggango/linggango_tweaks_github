package com.misanthropy.linggango.linggango_tweaks.mixin.tweaks;

import com.misanthropy.linggango.linggango_tweaks.fixes.ItemSplitFix;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Slot.class)
public abstract class SlotMixin {
    @Shadow @Final public Container container;
    @Shadow @Final private int slot;
    @Inject(method = "getItem", at = @At("HEAD"), cancellable = true)
    private void getItemInject(@NonNull CallbackInfoReturnable<ItemStack> cir) {
        if (this.container != null) {
            ItemStack stack = this.container.getItem(this.slot);
            if (!stack.isEmpty()) {
                CompoundTag tag = stack.getTag();
                if (tag != null && tag.isEmpty()) {
                    stack.setTag(null);
                    cir.setReturnValue(stack);
                }
            }
        }
    }

    @Inject(method = "set", at = @At("HEAD"))
    private void setInject(ItemStack stack, CallbackInfo ci) {
        ItemSplitFix.fixBug(stack);
    }

    @Inject(method = "onQuickCraft*", at = @At("HEAD"))
    private void onQuickCraftInject(ItemStack stack1, ItemStack stack2, CallbackInfo ci) {
        ItemSplitFix.fixBug(stack1);
        ItemSplitFix.fixBug(stack2);
    }

    @Inject(method = "onTake", at = @At("HEAD"))
    private void onTakeInject(Player player, ItemStack stack, CallbackInfo ci) {
        ItemSplitFix.fixBug(stack);
    }

    @Inject(method = "mayPlace", at = @At("HEAD"))
    private void mayPlaceInject(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        ItemSplitFix.fixBug(stack);
    }

    @Inject(method = "setByPlayer", at = @At("HEAD"))
    private void setByPlayerInject(ItemStack stack, CallbackInfo ci) {
        ItemSplitFix.fixBug(stack);
    }
}