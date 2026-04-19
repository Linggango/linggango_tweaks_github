package com.misanthropy.linggango.linggango_tweaks.integration;

import com.misanthropy.linggango.difficulty_enhancement.LinggangoEvents;
import com.misanthropy.linggango.linggango_tweaks.LinggangoTweaks;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = LinggangoTweaks.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class FTBECooldownManager {

    private static final Map<UUID, Map<String, Long>> PLAYER_COOLDOWNS = new HashMap<>();

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onCommand(CommandEvent event) {

        if (!ModList.get().isLoaded("ftbessentials")) return;

        if (!(event.getParseResults().getContext().getSource().getEntity() instanceof ServerPlayer player)) {
            return;
        }

        String commandName;
        try {
            commandName = event.getParseResults().getContext().getNodes().get(0).getNode().getName().toLowerCase();
        } catch (Exception e) {

            commandName = event.getParseResults().getReader().getString().split(" ")[0].replace("/", "").toLowerCase();
        }

        String group = getCommandGroup(commandName);
        if (group != null) {
            LinggangoEvents.DifficultyDef diff = LinggangoEvents.getCurrentDifficulty(player.serverLevel());
            if (diff == null) return;

            if (isDisabled(diff.id, group)) {
                player.displayClientMessage(
                        Component.literal("Difficulty Restriction: ")
                                .withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD)
                                .append(Component.literal("The /" + commandName + " command is DISABLED on " + diff.name + " difficulty!")
                                        .withStyle(ChatFormatting.RED)),
                        false
                );
                event.setCanceled(true);
                return;
            }

            int requiredCooldownSeconds = getCooldownSeconds(diff.id, group);

            if (requiredCooldownSeconds > 0) {
                long currentTime = System.currentTimeMillis();
                long lastUse = getLastUse(player.getUUID(), group);

                long timePassed = currentTime - lastUse;
                long requiredCooldownMs = requiredCooldownSeconds * 1000L;

                if (timePassed < requiredCooldownMs) {
                    long remainingSeconds = (requiredCooldownMs - timePassed) / 1000L;
                    player.displayClientMessage(
                            Component.literal("Difficulty Restriction: ")
                                    .withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD)
                                    .append(Component.literal("You must wait " + remainingSeconds + "s before using /" + commandName + " again.")
                                            .withStyle(ChatFormatting.RED)),
                            false
                    );
                    event.setCanceled(true);
                } else {

                    setLastUse(player.getUUID(), group, currentTime);
                }
            }
        }
    }

    private static String getCommandGroup(String cmd) {
        return switch (cmd) {
            case "home", "sethome" -> "home";
            case "warp", "setwarp" -> "warp";
            case "tpa", "tpahere", "tpaccept" -> "tpa";
            case "rtp" -> "rtp";
            case "back" -> "back";
            case "enderchest" -> "enderchest";
            default -> null;
        };
    }

    private static boolean isDisabled(String diff, String group) {

        if (diff.equals("chaos")) return true;

        return switch (group) {

            case "enderchest" -> !diff.equals("cozy");

            case "home", "warp" -> diff.equals("extreme") || diff.equals("torture");

            case "back" -> diff.equals("torture");

            case "tpa", "rtp" -> false;
            default -> false;
        };
    }

    private static int getCooldownSeconds(String diff, String group) {
        return switch (diff) {
            case "cozy" -> group.equals("rtp") ? 60 : 10;
            case "normal" -> group.equals("rtp") ? 180 : 30;
            case "extreme" -> switch (group) {
                case "rtp" -> 300;

                case "back" -> 120;

                case "tpa" -> 60;

                default -> 0;
            };
            case "torture" -> switch (group) {
                case "rtp" -> 300;
                case "tpa" -> 120;
                default -> 0;
            };
            default -> 0;
        };
    }

    private static long getLastUse(UUID playerId, String command) {
        return PLAYER_COOLDOWNS.computeIfAbsent(playerId, k -> new HashMap<>()).getOrDefault(command, 0L);
    }

    private static void setLastUse(UUID playerId, String command, long time) {
        PLAYER_COOLDOWNS.computeIfAbsent(playerId, k -> new HashMap<>()).put(command, time);
    }
}