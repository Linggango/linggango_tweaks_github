package com.misanthropy.linggango.linggango_tweaks.tweaks;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

@Mod.EventBusSubscriber(modid = "linggango_tweaks", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class DungeonSteelTotemEvents {

    @SubscribeEvent
    public static void onTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (stack.isEmpty()) return;

        if (stack.getItem().getClass().getSimpleName().equals("DungeonSteelTotem")) {
            List<Component> tooltip = event.getToolTip();

            if (!tooltip.isEmpty()) {
                Component name = tooltip.get(0);
                tooltip.clear();
                tooltip.add(name);
            }

            tooltip.add(Component.literal("Hold in main-hand with a damageable item in off-hand.").withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.literal("60% chance to duplicate the item at the cost of your life.").withStyle(ChatFormatting.DARK_RED));
            tooltip.add(Component.literal("Left-click a vanilla entity (<20% HP) to seal it into an egg.").withStyle(ChatFormatting.DARK_AQUA));
        }
    }

    @SubscribeEvent
    public static void onEntityAttack(AttackEntityEvent event) {
        Player player = event.getEntity();
        ItemStack mainHand = player.getMainHandItem();

        if (mainHand.getItem().getClass().getSimpleName().equals("DungeonSteelTotem")) {
            if (event.getTarget() instanceof LivingEntity target) {
                ResourceLocation id = ForgeRegistries.ENTITY_TYPES.getKey(target.getType());

                if (id != null && (!id.getNamespace().equals("minecraft") || !target.canChangeDimensions())) {
                    if (player.level().isClientSide) {
                        player.displayClientMessage(Component.literal("§cThis entity's essence is too unstable to mirror! Modded entities are not supported."), true);
                    }
                    event.setCanceled(true);
                }
            }
        }
    }
}