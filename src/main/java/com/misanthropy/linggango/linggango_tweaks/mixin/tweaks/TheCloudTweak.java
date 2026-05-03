package com.misanthropy.linggango.linggango_tweaks.mixin.tweaks;

import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import java.util.function.Consumer;

@Mixin(targets = "net.goo.brutality.item.weapon.generic.TheCloudItem", remap = false)
public abstract class TheCloudTweak {

    @Redirect(
            method = "m_7203_",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;m_21219_()Z"),
            remap = false
    )
    private boolean linggango$stopEffectClearing(Player instance) {
        return false;
    }

    @Redirect(
            method = "m_7203_",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/ListTag;forEach(Ljava/util/function/Consumer;)V"),
            remap = false
    )
    private void linggango$stopEffectRestoration(ListTag instance, Consumer<?> action) {
    }

    @Redirect(
            method = "m_7203_",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/food/FoodData;m_38717_(F)V"),
            remap = false
    )
    private void linggango$stopSaturationRestore(FoodData instance, float saturation) {
    }

    @Redirect(
            method = "m_7203_",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;m_20301_(I)V"),
            remap = false
    )
    private void linggango$stopOxygenRestore(Player instance, int oxygen) {
    }
}