package com.misanthropy.linggango.linggango_tweaks.events;

import com.misanthropy.linggango.linggango_tweaks.LinggangoTweaks;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = LinggangoTweaks.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class playerLoginEventHandler {
    private static final Component EMPTY_LINE = Component.literal("");
    private static final Component WELCOME_MESSAGE = Component.literal(" >> Welcome to ").withStyle(ChatFormatting.GRAY)
            .append(Component.literal("Linggango").withStyle(Style.EMPTY.withColor(ChatFormatting.GOLD).withBold(true)))
            .append(Component.literal("!").withStyle(ChatFormatting.GRAY));

    private static final Component THANKS_MESSAGE = Component.literal(" Thanks for playing!").withStyle(ChatFormatting.GRAY, ChatFormatting.BOLD);

    private static final Component SUPPORT_MESSAGE = Component.literal("Consider supporting me by:").withStyle(ChatFormatting.GRAY);

    private static final Component PATREON_BUTTON = Component.literal("[ Patreon ]")
            .withStyle(Style.EMPTY
                    .withColor(ChatFormatting.AQUA)
                    .withBold(true)
                    .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://www.patreon.com/MisanthropyDEV"))
                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Click to see my Patreon!").withStyle(ChatFormatting.AQUA))));

    private static final Component DISCORD_BUTTON = Component.literal("[ Discord ]")
            .withStyle(Style.EMPTY
                    .withColor(ChatFormatting.BLUE)
                    .withBold(true)
                    .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://discord.gg/linggango"))
                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Click to join the community!").withStyle(ChatFormatting.BLUE))));

    private static final Component LINKS_LINE = Component.literal("   ")
            .append(PATREON_BUTTON)
            .append(Component.literal("   "))
            .append(DISCORD_BUTTON);

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        if (!player.level().isClientSide()) {
            player.sendSystemMessage(EMPTY_LINE);
            player.sendSystemMessage(WELCOME_MESSAGE);
            player.sendSystemMessage(EMPTY_LINE);
            player.sendSystemMessage(THANKS_MESSAGE);
            player.sendSystemMessage(EMPTY_LINE);
            player.sendSystemMessage(SUPPORT_MESSAGE);
            player.sendSystemMessage(LINKS_LINE);
            player.sendSystemMessage(EMPTY_LINE);
        }
    }
}