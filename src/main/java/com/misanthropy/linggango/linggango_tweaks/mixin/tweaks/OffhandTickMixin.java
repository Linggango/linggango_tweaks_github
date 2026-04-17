package com.misanthropy.linggango.linggango_tweaks.mixin.tweaks;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class OffhandTickMixin {
    @Shadow public abstract ItemStack getOffhandItem();
    @Inject(method = "tick", at = @At("TAIL"))
    private void linggango$offHandInventoryTick(CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        ItemStack offhandStack = this.getOffhandItem();
        if (!offhandStack.isEmpty()) {
            offhandStack.inventoryTick(entity.level(), entity, 99, false);
        }
    }
}