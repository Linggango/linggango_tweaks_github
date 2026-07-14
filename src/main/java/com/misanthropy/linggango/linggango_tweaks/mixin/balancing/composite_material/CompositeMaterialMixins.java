package com.misanthropy.linggango.linggango_tweaks.mixin.balancing.composite_material;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class CompositeMaterialMixins {
    private CompositeMaterialMixins() {}
}

@Mixin(targets = "io.github.rcneg.compositematerial.common.items.magicitems.DungeonSteelTotem", remap = false)
abstract class DungeonSteelTotemTweak {

    @Inject(method = "m_5922_", at = @At("HEAD"), cancellable = true, remap = false)
    private void linggango$addDuplicationFailure(ItemStack itemStack, Level level, LivingEntity entity, CallbackInfoReturnable<ItemStack> cir) {
        if (entity instanceof Player player && !player.getAbilities().instabuild) {
            if (level.getRandom().nextFloat() > 0.40F) {
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

@Mixin(targets = "io.github.rcneg.compositematerial.common.items.dungeontools.DungeonSwordReinforced", remap = false)
abstract class DungeonSwordMathTweak {

    @Redirect(
            method = "getAttributeModifiers",
            at = @At(value = "NEW", target = "net/minecraft/world/entity/ai/attributes/AttributeModifier"),
            remap = false
    )
    private AttributeModifier linggango$nerfDungeonDamage(UUID p_22200_, String p_22201_, double p_22202_, AttributeModifier.Operation p_22203_) {
        return new AttributeModifier(p_22200_, p_22201_, p_22202_ * 0.5D, p_22203_);
    }
}

@Mixin(Item.class)
abstract class EchoiumSwordTweak {
    @Unique
    private static final int MAX_ECHOIUM_ADDITION = 30;

    @Inject(method = "inventoryTick", at = @At("HEAD"), cancellable = true)
    private void linggango$balanceXpScaling(ItemStack itemstack, Level world, Entity entity, int slot, boolean selected, CallbackInfo ci) {
        ResourceLocation id = ForgeRegistries.ITEMS.getKey((Item) (Object) this);
        if (id != null && id.toString().equals("composite_material:echoium_sword_reinforced")) {
            if (!world.isClientSide && entity instanceof Player player) {
                long time = world.getGameTime();

                if (time % 20 == 0 || time % 100 == 0) {
                    CompoundTag tag = itemstack.getOrCreateTag();
                    int currentAddition = tag.getInt("EchoiumAddition");

                    if (currentAddition < MAX_ECHOIUM_ADDITION && player.experienceLevel > 60 && time % 20 == 0) {
                        tag.putInt("EchoiumAddition", currentAddition + 1);
                        player.giveExperienceLevels(-1);
                    }
                    else if (player.experienceLevel < 30 && currentAddition > 0 && time % 100 == 0) {
                        tag.putInt("EchoiumAddition", currentAddition - 1);
                    }

                    if (currentAddition > MAX_ECHOIUM_ADDITION) {
                        tag.putInt("EchoiumAddition", MAX_ECHOIUM_ADDITION);
                    }
                }
            }
            ci.cancel();
        }
    }

    @Inject(method = "appendHoverText", at = @At("HEAD"), cancellable = true)
    private void linggango$updateEchoiumTooltip(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn, CallbackInfo ci) {
        ResourceLocation id = ForgeRegistries.ITEMS.getKey((Item) (Object) this);
        if (id != null && id.toString().equals("composite_material:echoium_sword_reinforced")) {
            tooltip.add(Component.literal("Consumes experience levels (>60) to increase strike power.").withStyle(ChatFormatting.DARK_AQUA));

            CompoundTag tag = stack.getTag();
            int bonus = tag != null ? tag.getInt("EchoiumAddition") : 0;
            float damage = (float) bonus / 2.0F;

            tooltip.add(Component.literal("Current Bonus: +" + damage + " Damage (Cap: +15.0)").withStyle(ChatFormatting.DARK_PURPLE));

            if (bonus < MAX_ECHOIUM_ADDITION) {
                tooltip.add(Component.literal("Requires more experience to reach peak potential.").withStyle(ChatFormatting.GRAY).withStyle(ChatFormatting.ITALIC));
            }
            ci.cancel();
        }
    }
}

@Mixin(targets = "io.github.rcneg.compositematerial.common.items.PrimitiveNostrum", remap = false)
abstract class PrimitiveNostrumTweak {

    @Inject(method = "m_5922_", at = @At("HEAD"), cancellable = true, remap = false)
    private void linggango$handlePrimitiveNostrum(ItemStack pStack, Level pLevel, LivingEntity livingEntity, CallbackInfoReturnable<ItemStack> cir) {
        if (livingEntity instanceof Player player && !player.getAbilities().instabuild) {
            if (!pLevel.isClientSide) {
                for (MobEffectInstance ins : new ArrayList<>(player.getActiveEffects())) {
                    int newDuration = ins.getDuration() + 1200;
                    player.addEffect(new MobEffectInstance(ins.getEffect(), newDuration, ins.getAmplifier(), ins.isAmbient(), ins.isVisible()));
                }

                player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 400, 0));
                player.addEffect(new MobEffectInstance(MobEffects.HUNGER, 600, 1));

                player.displayClientMessage(Component.literal("§5The Nostrum burns your throat, extending your current state briefly..."), true);

                pStack.shrink(1);
                cir.setReturnValue(pStack);
            } else {
                cir.setReturnValue(pStack);
            }
        }
    }
}