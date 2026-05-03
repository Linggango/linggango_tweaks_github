package com.misanthropy.linggango.linggango_tweaks.mixin.balancing.composite_material;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(targets = "io.github.rcneg.compositematerial.common.items.echoiumtools.EchoiumSwordReinforced", remap = false)
public abstract class EchoiumSwordTweak {
    @Unique
    private static final int MAX_ECHOIUM_ADDITION = 30;

    @Inject(method = "m_6883_", at = @At("HEAD"), cancellable = true)
    private void linggango$balanceXpScaling(ItemStack itemstack, Level world, Entity entity, int slot, boolean selected, CallbackInfo ci) {
        if (!world.isClientSide && entity instanceof Player player) {
            CompoundTag tag = itemstack.getOrCreateTag();
            int currentAddition = tag.getInt("EchoiumAddition");

            if (currentAddition < MAX_ECHOIUM_ADDITION && player.experienceLevel > 60) {
                if (world.getGameTime() % 20 == 0) {
                    tag.putInt("EchoiumAddition", currentAddition + 1);
                    player.giveExperienceLevels(-1);
                }
            }
            else if (player.experienceLevel < 30 && currentAddition > 0) {
                if (world.getGameTime() % 100 == 0) {
                    tag.putInt("EchoiumAddition", currentAddition - 1);
                }
            }

            if (currentAddition > MAX_ECHOIUM_ADDITION) {
                tag.putInt("EchoiumAddition", MAX_ECHOIUM_ADDITION);
            }
        }
        ci.cancel();
    }

    @Inject(method = "m_7373_", at = @At("HEAD"), cancellable = true)
    private void linggango$updateEchoiumTooltip(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn, CallbackInfo ci) {
        tooltip.add(Component.literal("Consumes experience levels (>60) to increase strike power.").withStyle(ChatFormatting.DARK_AQUA));

        CompoundTag tag = stack.getTag();
        int bonus = tag != null ? tag.getInt("EchoiumAddition") : 0;
        float damage = (float)bonus / 2.0F;

        tooltip.add(Component.literal(String.format("Current Bonus: +%.1f Damage (Cap: +15.0)", damage)).withStyle(ChatFormatting.DARK_PURPLE));

        if (bonus < MAX_ECHOIUM_ADDITION) {
            tooltip.add(Component.literal("Requires more experience to reach peak potential.").withStyle(ChatFormatting.GRAY).withStyle(ChatFormatting.ITALIC));
        }
        ci.cancel();
    }
}