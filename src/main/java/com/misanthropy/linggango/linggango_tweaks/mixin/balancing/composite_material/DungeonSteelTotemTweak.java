package com.misanthropy.linggango.linggango_tweaks.mixin.balancing.composite_material;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Random;

@Mixin(targets = "io.github.rcneg.compositematerial.common.items.magicitems.DungeonSteelTotem", remap = false)
public abstract class DungeonSteelTotemTweak {
    @Unique
    private static final Random linggango_tweaks$RANDOM = new Random();

    @Inject(method = "m_5922_", at = @At("HEAD"), cancellable = true, remap = false)
    private void linggango$addDuplicationFailure(ItemStack itemStack, Level level, LivingEntity entity, CallbackInfoReturnable<ItemStack> cir) {
        if (entity instanceof Player player && !player.getAbilities().instabuild) {
            if (linggango_tweaks$RANDOM.nextFloat() > 0.60F) {

                if (!level.isClientSide) {
                    player.displayClientMessage(Component.literal("§cThe contract failed to manifest a mirror!"), true);

                    player.hurt(level.damageSources().magic(), Float.MAX_VALUE);
                    player.getCooldowns().addCooldown(itemStack.getItem(), 100);
                    level.playSound(null, player.getX(), player.getY(), player.getZ(),
                            net.minecraft.sounds.SoundEvents.GLASS_BREAK, net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 0.5F);

                    itemStack.shrink(1);
                }
                cir.setReturnValue(itemStack);
            }
        }
    }
}