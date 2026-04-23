package com.misanthropy.linggango.linggango_tweaks.tweaks; // Source: https://github.com/juancarloscp52/BedrockIfy

import com.misanthropy.linggango.linggango_tweaks.LinggangoTweaks;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.CandleCakeBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = LinggangoTweaks.MOD_ID)
public class BedrockFeatures {

    @SubscribeEvent
    public static void onBlockInteract(PlayerInteractEvent.RightClickBlock event) {
        ItemStack stack = event.getItemStack();
        if (stack.isEmpty() || EnchantmentHelper.getItemEnchantmentLevel(Enchantments.FIRE_ASPECT, stack) == 0) return;

        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        BlockState state = level.getBlockState(pos);
        Player player = event.getEntity();

        if (CampfireBlock.canLight(state) || CandleBlock.canLight(state) || CandleCakeBlock.canLight(state)) {
            level.playSound(player, pos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1.0F, level.getRandom().nextFloat() * 0.4F + 0.8F);
            level.setBlock(pos, state.setValue(BlockStateProperties.LIT, Boolean.TRUE), 11);
            level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);

            if (player != null) {
                stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(event.getHand()));
            }

            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.sidedSuccess(level.isClientSide()));
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Player player = event.player;

        if (player.isCrouching()) {
            ItemStack main = player.getMainHandItem();
            ItemStack off = player.getOffhandItem();

            if (main.getItem() instanceof ShieldItem && !player.isUsingItem()) {
                player.startUsingItem(InteractionHand.MAIN_HAND);
            } else if (off.getItem() instanceof ShieldItem && !player.isUsingItem()) {
                player.startUsingItem(InteractionHand.OFF_HAND);
            }
        } else if (player.isUsingItem() && player.getUseItem().getItem() instanceof ShieldItem) {
            player.stopUsingItem();
        }
    }
}