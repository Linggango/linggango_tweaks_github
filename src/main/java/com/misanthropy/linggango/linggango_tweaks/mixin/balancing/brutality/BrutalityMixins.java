package com.misanthropy.linggango.linggango_tweaks.mixin.balancing.brutality;

import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;
import top.theillusivec4.curios.api.SlotContext;

import java.util.function.Consumer;

public final class BrutalityMixins {
    private BrutalityMixins() {}
}

@Mixin(targets = "net.goo.brutality.item.weapon.sword.BladeOfTheRuinedKingSword", remap = false)
abstract class BORKTweak {

    @ModifyConstant(
            method = "m_7579_",
            constant = @Constant(floatValue = 0.08F),
            remap = false
    )
    private float linggango$reduceHPDamage(float original) {
        return 0.01F;
    }
}

@Mixin(targets = "net.goo.brutality.item.curios.charm.CelestialStarboard", remap = false)
abstract class CelestialStarboardTweak {

    @Shadow private boolean wasOnGround;

    /**
     * @author Misanthropy
     * @reason Fixing a crash in brutality (the 50001st crash)
     */
    @Overwrite
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        LivingEntity entity = slotContext.entity();
        if (!(entity instanceof Player player)) return;

        Level level = player.level();

        if (level instanceof ServerLevel) {
            boolean currentlyOnGround = player.onGround();
            if (!currentlyOnGround && this.wasOnGround) {
                this.wasOnGround = false;
            } else if (currentlyOnGround && !this.wasOnGround) {
                this.wasOnGround = true;
            }
            return;
        }

        if (level.isClientSide()) {
            try {
                Class.forName("com.misanthropy.linggango.linggango_tweaks.ring_selection.ClientAccess")
                        .getMethod("handleCelestialStarboardClient", Player.class, ItemStack.class)
                        .invoke(null, player, stack);
            } catch (Exception ignored) {
            }
        }
    }
}

@Mixin(targets = "net.goo.brutality.item.weapon.generic.TheCloudItem", remap = false)
abstract class TheCloudTweak {

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