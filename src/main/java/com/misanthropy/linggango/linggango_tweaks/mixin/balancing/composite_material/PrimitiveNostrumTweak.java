package com.misanthropy.linggango.linggango_tweaks.mixin.balancing.composite_material;

import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;

@Mixin(targets = "io.github.rcneg.compositematerial.common.items.PrimitiveNostrum", remap = false)
public abstract class PrimitiveNostrumTweak {

    @Inject(method = "m_5922_", at = @At("HEAD"), cancellable = true, remap = false)
    private void linggango$handlePrimitiveNostrum(ItemStack itemstack, Level level, LivingEntity entity, CallbackInfoReturnable<ItemStack> cir) {
        if (entity instanceof Player player && !player.getAbilities().instabuild) {
            if (!level.isClientSide) {
                for (MobEffectInstance ins : new ArrayList<>(player.getActiveEffects())) {
                    int newDuration = ins.getDuration() + 1200;
                    player.addEffect(new MobEffectInstance(ins.getEffect(), newDuration, ins.getAmplifier(), ins.isAmbient(), ins.isVisible()));
                }

                player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 400, 0));
                player.addEffect(new MobEffectInstance(MobEffects.HUNGER, 600, 1));

                player.displayClientMessage(Component.literal("§5The Nostrum burns your throat, extending your current state briefly..."), true);

                itemstack.shrink(1);
                cir.setReturnValue(itemstack);
            } else {
                cir.setReturnValue(itemstack);
            }
        }
    }
}