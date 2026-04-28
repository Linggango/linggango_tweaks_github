package com.misanthropy.linggango.linggango_tweaks.mixin.tweaks;

import com.aizistral.enigmaticlegacy.handlers.SuperpositionHandler;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.function.Consumer;

@Mixin(targets = "net.llenzzz.covenant_of_the_seven.events.item.curse.FalseSalvationEvents", remap = false)
public abstract class FalseSalvationTweak {

    @Redirect(
            method = "onAttack",
            at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/LivingEntity;f_19802_:I", opcode = 181),
            remap = false
    )
    private static void linggango$chanceToResetIFrameAttack(LivingEntity instance, int value) {
        if (Math.random() < 0.6) {
            instance.invulnerableTime = 0;
            instance.hurtTime = 0;
            instance.hurtDuration = 0;
        }
    }

    @Redirect(
            method = "onAttack",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;m_41622_(ILnet/minecraft/world/entity/LivingEntity;Ljava/util/function/Consumer;)V"),
            remap = false
    )
    private static void linggango$nerfArmorShred(ItemStack stack, int amount, LivingEntity entity, Consumer<LivingEntity> onBroken) {
        stack.hurtAndBreak(Math.max(1, amount / 4), entity, onBroken);
    }

    @Redirect(
            method = "onHurt",
            at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/LivingEntity;f_19802_:I", opcode = 181),
            remap = false
    )
    private static void linggango$chanceToResetIFrameHurt(LivingEntity instance, int value) {
        if (Math.random() < 0.6) {
            instance.invulnerableTime = 0;
            instance.hurtTime = 0;
            instance.hurtDuration = 0;
        }
    }

    @Redirect(
            method = "onHurt",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;m_6469_(Lnet/minecraft/world/damagesource/DamageSource;F)Z"),
            remap = false
    )
    private static boolean linggango$balancedHPDamage(LivingEntity target, DamageSource source, float amount) {
        if (source.getEntity() instanceof Player player) {
            int curseAmount = SuperpositionHandler.getCurseAmount(player);
            float hpPercent = Math.min(0.02f, (float)(curseAmount / 4) * 0.01f);
            float newDamage = target.getMaxHealth() * hpPercent;
            return target.hurt(source, newDamage);
        }
        return target.hurt(source, amount);
    }
}