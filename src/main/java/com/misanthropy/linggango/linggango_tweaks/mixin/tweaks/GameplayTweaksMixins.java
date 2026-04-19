package com.misanthropy.linggango.linggango_tweaks.mixin.tweaks;

import net.minecraft.client.gui.screens.inventory.AnvilScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.ThornsEnchantment;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Consumer;

@SuppressWarnings("unused")
public class GameplayTweaksMixins {

    @Mixin(AnvilMenu.class)
    public static class AnvilMenuMixin {
        @ModifyConstant(
                method = "createResult",
                constant = @Constant(intValue = 40),
                require = 0
        )
        private int linggango_tweaks$removeAnvilLimit(int original) {
            return Integer.MAX_VALUE;
        }
    }

    @Mixin(AnvilScreen.class)
    public static class AnvilScreenMixin {
        @ModifyConstant(
                method = "renderLabels",
                constant = @Constant(intValue = 40),
                require = 0
        )
        private int linggango_tweaks$removeAnvilLimitScreen(int original) {
            return Integer.MAX_VALUE;
        }
    }

    @Mixin(Creeper.class)
    public static class CreeperExplosionMixin {
        @ModifyArg(
                method = "explodeCreeper",
                at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;explode(Lnet/minecraft/world/entity/Entity;DDDFLnet/minecraft/world/level/Level$ExplosionInteraction;)Lnet/minecraft/world/level/Explosion;"),
                index = 5
        )
        private Level.ExplosionInteraction linggango_tweaks$modifyCreeperExplosion(Level.ExplosionInteraction originalInteraction) {
            Creeper creeper = (Creeper) (Object) this;
            boolean mobGriefing = creeper.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING);
            return mobGriefing ? Level.ExplosionInteraction.TNT : Level.ExplosionInteraction.NONE;
        }
    }

    @Mixin(SweetBerryBushBlock.class)
    public static class SweetBerryBushMixin {
        @Inject(method = "entityInside", at = @At("HEAD"), cancellable = true)
        private void linggango_tweaks$preventBushDamage(BlockState state, Level level, BlockPos pos, Entity entity, CallbackInfo ci) {
            if (entity instanceof LivingEntity living) {
                if (living.isCrouching()) {
                    ci.cancel();
                    return;
                }
                if (!living.getItemBySlot(EquipmentSlot.LEGS).isEmpty() || !living.getItemBySlot(EquipmentSlot.CHEST).isEmpty()) {
                    ci.cancel();
                }
            }
        }
    }

    @Mixin(FireBlock.class)
    public static class FireBlockMixin {
        @Inject(method = "getIgniteOdds(Lnet/minecraft/world/level/block/state/BlockState;)I", at = @At("HEAD"), cancellable = true)
        private void linggango_tweaks$cobwebIgnite(BlockState state, CallbackInfoReturnable<Integer> cir) {
            if (state.is(Blocks.COBWEB)) cir.setReturnValue(15);
        }

        @Inject(method = "getBurnOdds(Lnet/minecraft/world/level/block/state/BlockState;)I", at = @At("HEAD"), cancellable = true)
        private void linggango_tweaks$cobwebBurn(BlockState state, CallbackInfoReturnable<Integer> cir) {
            if (state.is(Blocks.COBWEB)) cir.setReturnValue(100);
        }
    }

    @Mixin(ThornsEnchantment.class)
    public static class ThornsMixin {
        @Redirect(method = "doPostHurt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;hurtAndBreak(ILnet/minecraft/world/entity/LivingEntity;Ljava/util/function/Consumer;)V"))
        private void linggango_tweaks$preventThornsDamage(ItemStack instance, int amount, LivingEntity entity, Consumer<?> onBroken) {
        }
    }

    @Mixin(LivingEntity.class)
    public static class SoulSpeedMixin {
        @Redirect(method = "tryAddSoulSpeed", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;hurtAndBreak(ILnet/minecraft/world/entity/LivingEntity;Ljava/util/function/Consumer;)V"))
        private void linggango_tweaks$preventSoulSpeedDamage(ItemStack instance, int amount, LivingEntity entity, Consumer<?> onBroken) {
        }
    }
}