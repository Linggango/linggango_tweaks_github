package com.misanthropy.linggango.linggango_tweaks.tweaks;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber(modid = "linggango_tweaks", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PrimitiveTotemEvents {
    private static final String PROTECTION_COOLDOWN = "ProtectionCooldown";

    @SubscribeEvent
    public static void onTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (stack.isEmpty()) return;

        String className = stack.getItem().getClass().getSimpleName();

        if (className.equals("PrimitiveTotem")) {
            List<Component> tooltip = event.getToolTip();
            tooltip.add(Component.literal("Hold in hand with infinite effects to seal them.").withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.literal("50% success rate to store up to 3 essences.").withStyle(ChatFormatting.DARK_RED));
            tooltip.add(Component.literal("Caps fatal damage (>40% HP) once every 60 seconds.").withStyle(ChatFormatting.GOLD));

            CompoundTag tag = stack.getTag();
            if (tag != null && tag.contains("PrimitiveAddition")) {
                ListTag list = tag.getList("PrimitiveAddition", 8);
                tooltip.add(Component.literal("Stored Essences: " + list.size() + "/3").withStyle(ChatFormatting.DARK_AQUA));
            }
        } else if (className.equals("PrimitiveNostrum")) {
            List<Component> tooltip = event.getToolTip();
            tooltip.add(Component.literal("A volatile mixture that extends all active effects.").withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.literal("Adds 60 seconds to all current buffs.").withStyle(ChatFormatting.LIGHT_PURPLE));
            tooltip.add(Component.literal("High toxicity: Causes Nausea and Hunger on use.").withStyle(ChatFormatting.RED));
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onDamageProtection(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.level().isClientSide()) return;

        ItemStack main = player.getMainHandItem();
        ItemStack off = player.getOffhandItem();
        ItemStack totem = null;

        if (main.getItem().getClass().getSimpleName().equals("PrimitiveTotem")) totem = main;
        else if (off.getItem().getClass().getSimpleName().equals("PrimitiveTotem")) totem = off;

        if (totem != null) {
            long currentTime = player.level().getGameTime();
            CompoundTag tag = totem.getOrCreateTag();

            boolean onCooldown = tag.contains(PROTECTION_COOLDOWN) && currentTime < tag.getLong(PROTECTION_COOLDOWN) + 1200;

            if (!onCooldown && event.getAmount() >= player.getMaxHealth() * 0.4F) {
                tag.putLong(PROTECTION_COOLDOWN, currentTime);
            }
        }
    }
}