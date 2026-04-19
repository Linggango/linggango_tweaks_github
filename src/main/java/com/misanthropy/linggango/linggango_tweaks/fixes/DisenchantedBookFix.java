package com.misanthropy.linggango.linggango_tweaks.fixes;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.Map;

@Mod.EventBusSubscriber(modid = "linggango_tweaks", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class DisenchantedBookFix {

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onBookUse(PlayerInteractEvent.RightClickItem event) {
        Player player = event.getEntity();
        ItemStack bookStack = event.getItemStack();
        ResourceLocation registryName = ForgeRegistries.ITEMS.getKey(bookStack.getItem());

        if (registryName != null && registryName.toString().equals("composite_material:disenchanted_book")) {
            ItemStack targetItem;

            if (event.getHand() == InteractionHand.OFF_HAND) {
                targetItem = player.getMainHandItem();
            } else {
                targetItem = player.getOffhandItem();
                if (targetItem.isEmpty() || targetItem.getItem() == bookStack.getItem()) {
                    int targetSlot = (player.getInventory().selected + 1) % 9;
                    targetItem = player.getInventory().getItem(targetSlot);
                }
            }

            Map<Enchantment, Integer> enchants = EnchantmentHelper.getEnchantments(targetItem);

            if (!targetItem.isEmpty() && !enchants.isEmpty()) {
                ItemStack enchantedBook = new ItemStack(Items.ENCHANTED_BOOK);
                for (Map.Entry<Enchantment, Integer> entry : enchants.entrySet()) {
                    EnchantedBookItem.addEnchantment(enchantedBook, new EnchantmentInstance(entry.getKey(), entry.getValue()));
                }

                EnchantmentHelper.setEnchantments(new HashMap<>(), targetItem);

                if (!player.getAbilities().instabuild) {
                    bookStack.shrink(1);
                }

                if (!player.getInventory().add(enchantedBook)) {
                    player.drop(enchantedBook, false);
                }

                event.getLevel().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ANVIL_USE, SoundSource.PLAYERS, 1.0F, 1.0F);

                event.setCanceled(true);
                event.setCancellationResult(InteractionResult.sidedSuccess(event.getLevel().isClientSide()));
            }
        }
    }
}