package com.misanthropy.linggango.linggango_tweaks.tweaks;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber(modid = "linggango_tweaks", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class EtheriteSwordTweak {

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onEntityDeathChargeCap(LivingDeathEvent event) {
        if (event.getSource().getEntity() instanceof Player player) {
            ItemStack mainHand = player.getMainHandItem();

            if (mainHand.getItem().getClass().getSimpleName().equals("EtheriteSwordReinforced")) {
                CompoundTag tag = mainHand.getTag();
                if (tag != null && tag.contains("EtheriteAddition")) {
                    int currentCharge = tag.getInt("EtheriteAddition");

                    if (currentCharge > 50) {
                        tag.putInt("EtheriteAddition", 50);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (stack.isEmpty()) return;

        if (stack.getItem().getClass().getSimpleName().equals("EtheriteSwordReinforced")) {
            List<Component> tooltip = event.getToolTip();
            if (!tooltip.isEmpty()) {
                Component name = tooltip.get(0);
                tooltip.clear();
                tooltip.add(name);
            }

            tooltip.add(Component.literal("Seals the life force of the fallen to empower the next strike.").withStyle(ChatFormatting.DARK_PURPLE));

            CompoundTag tag = stack.getTag();
            int currentCharge = (tag != null && tag.contains("EtheriteAddition")) ? tag.getInt("EtheriteAddition") : 0;

            if (currentCharge > 0) {
                tooltip.add(Component.literal(String.format("Stored Damage: +%d.0 Damage", currentCharge)).withStyle(ChatFormatting.GOLD));
                tooltip.add(Component.literal("The next strike will consume this as damage.").withStyle(ChatFormatting.GRAY).withStyle(ChatFormatting.ITALIC));
            } else {
                tooltip.add(Component.literal("Sword is currently empty.").withStyle(ChatFormatting.GRAY));
            }

            tooltip.add(Component.literal("Maximum Damage: +50.0 Damage").withStyle(ChatFormatting.DARK_RED));
        }
    }
}