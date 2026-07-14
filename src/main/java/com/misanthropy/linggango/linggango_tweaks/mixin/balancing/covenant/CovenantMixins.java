package com.misanthropy.linggango.linggango_tweaks.mixin.balancing.covenant;

import com.aizistral.enigmaticlegacy.handlers.SuperpositionHandler;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

public final class CovenantMixins {
    private CovenantMixins() {}
}

@Mixin(targets = "net.llenzzz.covenant_of_the_seven.events.item.curse.InversionEvents", remap = false)
abstract class InversionTweak {

    @Redirect(
            method = "onHurt",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;m_5634_(F)V"),
            remap = false
    )
    private static void linggango$nerfInversionHealing(Player instance, float amount) {
        instance.heal(amount * 0.10F);
    }

    @Inject(
            method = "onEffectReceived",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private static void linggango$flipChance(MobEffectEvent.Applicable event, CallbackInfo ci) {

        if (event.getEntity().getRandom().nextFloat() < 0.5F) {
            ci.cancel();
        }
    }

    @ModifyConstant(
            method = "onEffectReceived",
            constant = @Constant(intValue = 255),
            remap = false
    )
    private static int linggango$limitStacking(int original) {
        return 3;
    }
}

@Mixin(targets = "net.llenzzz.covenant_of_the_seven.events.item.curse.FalseSalvationEvents", remap = false)
abstract class FalseSalvationTweak {

    @Redirect(
            method = "onAttack",
            at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/LivingEntity;f_19802_:I", opcode = 181),
            remap = false
    )
    private static void linggango$chanceToResetIFrameAttack(LivingEntity instance, int value) {

        if (instance.getRandom().nextFloat() < 0.6F) {
            instance.invulnerableTime = 0;
            instance.hurtTime = 0;
            instance.hurtDuration = 0;
        } else {
            instance.invulnerableTime = value;
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

        if (instance.getRandom().nextFloat() < 0.6F) {
            instance.invulnerableTime = 0;
            instance.hurtTime = 0;
            instance.hurtDuration = 0;
        } else {
            instance.invulnerableTime = value;
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
            float hpPercent = Math.min(0.02F, curseAmount * 0.0025F);
            float newDamage = target.getMaxHealth() * hpPercent;

            return target.hurt(source, newDamage);
        }
        return target.hurt(source, amount);
    }
}

@Mixin(targets = "com.aizistral.enigmaticlegacy.handlers.EnigmaticEventHandler", remap = false)
abstract class CursedScrollTweak {

    @ModifyVariable(
            method = "onEntityHurt",
            at = @At(value = "STORE"),
            ordinal = 0,
            remap = false
    )
    private float linggango$capCursedScrollDamage(float damageBoost, LivingHurtEvent event) {
        float originalDamage = event.getAmount();
        float maxBonus = originalDamage * 0.15F;

        return Math.min(damageBoost, maxBonus);
    }

    @ModifyVariable(
            method = "miningStuff",
            at = @At(value = "STORE"),
            remap = false,
            name = "miningBoost")
    private float linggango$capCursedScrollMining(float miningBoost) {
        float additiveBoost = miningBoost - 1.0F;
        return 1.0F + Math.min(additiveBoost, 0.20F);
    }

    @Redirect(
            method = "onLivingHeal",
            at = @At(value = "INVOKE", target = "Lnet/minecraftforge/event/entity/living/LivingHealEvent;setAmount(F)V"),
            remap = false
    )
    private void linggango$capCursedScrollRegen(LivingHealEvent event, float amount) {
        float originalHeal = event.getAmount();
        float proposedBonus = amount - originalHeal;
        float maxBonus = originalHeal * 0.06F;

        event.setAmount(originalHeal + Math.min(proposedBonus, maxBonus));
    }
}

@Mixin(targets = "net.llenzzz.covenant_of_the_seven.events.item.virtue.ConsecrationEvents", remap = false)
abstract class ConsecrationTweak {

    @Redirect(
            method = "onHurt",
            at = @At(value = "INVOKE", target = "Lnet/minecraftforge/event/entity/living/LivingHurtEvent;setAmount(F)V", ordinal = 0),
            remap = false
    )
    private static void linggango$nerfConsecrationImmunity(LivingHurtEvent event, float amount) {
        event.setAmount(event.getAmount() * 0.50F);
    }

    @ModifyConstant(
            method = "onHurt",
            constant = @Constant(floatValue = 4.0F),
            remap = false
    )
    private static float linggango$reduceConsecrationPenalty(float original) {
        return 3.0F;
    }
}