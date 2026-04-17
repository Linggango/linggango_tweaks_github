package com.misanthropy.linggango.linggango_tweaks.mixin.tweaks;

import com.misanthropy.linggango.linggango_tweaks.fixes.ItemSplitFix;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    @Inject(method = "split", at = @At("HEAD"))
    private void splitInject(CallbackInfoReturnable<ItemStack> cir) {
        ItemSplitFix.fixBug((ItemStack) (Object) this);
    }
    @Inject(method = "copy", at = @At("HEAD"))
    private void copyInject(CallbackInfoReturnable<ItemStack> cir) {
        ItemSplitFix.fixBug((ItemStack) (Object) this);
    }
    @Inject(method = "isSameItemSameTags", at = @At("HEAD"))
    private static void isSameItemSameTagsInject(ItemStack stack1, ItemStack stack2, CallbackInfoReturnable<Boolean> cir) {
        ItemSplitFix.fixBug(stack1);
        ItemSplitFix.fixBug(stack2);
    }
    @Inject(method = "isSameItem", at = @At("HEAD"))
    private static void isSameItemInject(ItemStack stack1, ItemStack stack2, CallbackInfoReturnable<Boolean> cir) {
        ItemSplitFix.fixBug(stack1);
        ItemSplitFix.fixBug(stack2);
    }
    @Inject(method = "getCount", at = @At("HEAD"))
    private void getCountInject(CallbackInfoReturnable<Integer> cir) {
        ItemSplitFix.fixBug((ItemStack) (Object) this);
    }
    @Inject(method = "setCount", at = @At("HEAD"))
    private void setCountInject(int count, CallbackInfo ci) {
        ItemSplitFix.fixBug((ItemStack) (Object) this);
    }
}