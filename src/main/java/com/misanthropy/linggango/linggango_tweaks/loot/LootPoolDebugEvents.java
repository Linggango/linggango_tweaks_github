package com.misanthropy.linggango.linggango_tweaks.loot;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = "linggango_tweaks", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class LootPoolDebugEvents {

    private static boolean isDebugMode = false;
    private static ItemStack lastHoveredItem = ItemStack.EMPTY;
    private static long lastHoverTime = 0;

    @SubscribeEvent
    public static void onClientCommandRegister(RegisterClientCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("lootpooldebug")
                .then(Commands.literal("on").executes(context -> {
                    isDebugMode = true;
                    if (Minecraft.getInstance().player != null) {
                        Minecraft.getInstance().player.displayClientMessage(
                                Component.literal("Loot pool is on").withStyle(ChatFormatting.YELLOW), false
                        );
                    }
                    return 1;
                }))
                .then(Commands.literal("off").executes(context -> {
                    isDebugMode = false;
                    if (Minecraft.getInstance().player != null) {
                        Minecraft.getInstance().player.displayClientMessage(
                                Component.literal("Loot pool off").withStyle(ChatFormatting.YELLOW), false
                        );
                    }
                    return 1;
                }))
        );
    }

    @SubscribeEvent
    public static void onKeyPress(ScreenEvent.KeyPressed.Pre event) {
        Minecraft mc = Minecraft.getInstance();

        if (mc.player != null && mc.player.isCreative() && mc.player.hasPermissions(2)) {
            if (Screen.hasControlDown() && event.getKeyCode() == GLFW.GLFW_KEY_C) {
                if (!lastHoveredItem.isEmpty() && (System.currentTimeMillis() - lastHoverTime < 250)) {
                    ResourceLocation id = ForgeRegistries.ITEMS.getKey(lastHoveredItem.getItem());

                    if (id != null) {
                        String copyString = id.toString();

                        mc.keyboardHandler.setClipboard(copyString);
                        mc.player.displayClientMessage(
                                Component.literal("Copied: " + copyString).withStyle(ChatFormatting.GREEN), true
                        );

                        event.setCanceled(true);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onTooltip(ItemTooltipEvent event) {
        lastHoveredItem = event.getItemStack();
        lastHoverTime = System.currentTimeMillis();

        if (!isDebugMode) return;

        ResourceLocation id = ForgeRegistries.ITEMS.getKey(event.getItemStack().getItem());
        if (id == null) return;

        String idStr = id.toString();

        if (BalancedLootRandomizerModifier.isLegendary(idStr)) {
            event.getToolTip().add(Component.literal("LEGENDARY (1%)").withStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xFFA500))));
        } else if (BalancedLootRandomizerModifier.isEpic(idStr)) {
            event.getToolTip().add(Component.literal("EPIC (3%)").withStyle(ChatFormatting.LIGHT_PURPLE));
        } else if (BalancedLootRandomizerModifier.isRare(idStr)) {
            event.getToolTip().add(Component.literal("RARE (15%)").withStyle(ChatFormatting.BLUE));
        } else if (BalancedLootRandomizerModifier.isUncommon(idStr)) {
            event.getToolTip().add(Component.literal("UNCOMMON (30%)").withStyle(ChatFormatting.GREEN));
        } else if (BalancedLootRandomizerModifier.isCommon(idStr)) {
            event.getToolTip().add(Component.literal("COMMON (60%)").withStyle(ChatFormatting.WHITE));
        } else {
            event.getToolTip().add(Component.literal("not in whitelist").withStyle(ChatFormatting.RED));
        }
    }
}