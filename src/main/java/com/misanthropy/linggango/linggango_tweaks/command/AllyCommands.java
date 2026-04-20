package com.misanthropy.linggango.linggango_tweaks.command;

import com.misanthropy.linggango.linggango_tweaks.ally.AllyManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jspecify.annotations.NonNull;

import java.util.UUID;

@Mod.EventBusSubscriber
public class AllyCommands {

    @SubscribeEvent
    public static void onRegisterCommands(@NonNull RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> d = event.getDispatcher();

        LiteralArgumentBuilder<CommandSourceStack> allyNode = Commands.literal("ally")
                .then(Commands.literal("invite")
                        .then(Commands.argument("target", EntityArgument.player())
                                .executes(c -> invitePlayer(c.getSource(), EntityArgument.getPlayer(c, "target")))))
                .then(Commands.literal("accept")
                        .executes(c -> acceptInvite(c.getSource())))
                .then(Commands.literal("disband")
                        .executes(c -> disbandAlly(c.getSource())))
                .then(Commands.literal("rules")
                        .then(Commands.literal("friendlyfire")
                                .then(Commands.argument("value", BoolArgumentType.bool())
                                        .executes(c -> setFriendlyFire(c.getSource(), BoolArgumentType.getBool(c, "value"))))))
                .then(Commands.literal("request")
                        .then(Commands.literal("item")
                                .then(Commands.argument("target", EntityArgument.player())
                                        .executes(c -> requestItem(c.getSource(), EntityArgument.getPlayer(c, "target")))))
                        .then(Commands.literal("xp")
                                .then(Commands.argument("target", EntityArgument.player())
                                        .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                                .executes(c -> requestXp(c.getSource(), EntityArgument.getPlayer(c, "target"), IntegerArgumentType.getInteger(c, "amount"))))))
                        .then(Commands.literal("accept")
                                .executes(c -> acceptRequest(c.getSource()))));

        d.register(allyNode);
    }

    private static int invitePlayer(@NonNull CommandSourceStack source, @NonNull ServerPlayer target) {
        try {
            ServerPlayer sender = source.getPlayerOrException();
            if (sender.getUUID().equals(target.getUUID())) {
                sender.sendSystemMessage(Component.literal("You cannot invite yourself."));
                return 0;
            }

            AllyManager.Alliance targetAlly = AllyManager.ALLIANCES.get(target.getUUID());
            if (targetAlly != null) {
                sender.sendSystemMessage(Component.literal("That player is already in an alliance."));
                return 0;
            }

            AllyManager.PENDING_INVITES.put(target.getUUID(), sender.getUUID());
            sender.sendSystemMessage(Component.literal("Invite sent to " + target.getName().getString()));

            MutableComponent acceptButton = Component.literal(" [ACCEPT]")
                    .withStyle(style -> style.withColor(ChatFormatting.GREEN).withBold(true)
                            .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/ally accept")));

            target.sendSystemMessage(Component.literal(sender.getName().getString() + " invited you to an alliance.")
                    .append(acceptButton));

        } catch (Exception ignored) {}
        return 1;
    }

    private static int acceptInvite(@NonNull CommandSourceStack source) {
        try {
            ServerPlayer target = source.getPlayerOrException();
            UUID targetId = target.getUUID();

            if (!AllyManager.PENDING_INVITES.containsKey(targetId)) {
                target.sendSystemMessage(Component.literal("You have no pending invites."));
                return 0;
            }

            UUID senderId = AllyManager.PENDING_INVITES.get(targetId);
            ServerPlayer sender = source.getServer().getPlayerList().getPlayer(senderId);

            if (sender == null) {
                target.sendSystemMessage(Component.literal("The inviter is no longer online."));
                AllyManager.PENDING_INVITES.remove(targetId);
                return 0;
            }

            AllyManager.Alliance senderAlliance = AllyManager.ALLIANCES.get(senderId);
            if (senderAlliance == null) {
                senderAlliance = new AllyManager.Alliance();
                senderAlliance.addMember(senderId);
                AllyManager.ALLIANCES.put(senderId, senderAlliance);
            }

            senderAlliance.addMember(targetId);
            AllyManager.ALLIANCES.put(targetId, senderAlliance);
            AllyManager.PENDING_INVITES.remove(targetId);

            target.sendSystemMessage(Component.literal("You joined the alliance."));
            sender.sendSystemMessage(Component.literal(target.getName().getString() + " joined your alliance."));

        } catch (Exception ignored) {}
        return 1;
    }

    private static int disbandAlly(@NonNull CommandSourceStack source) {
        try {
            ServerPlayer player = source.getPlayerOrException();
            UUID playerId = player.getUUID();
            AllyManager.Alliance alliance = AllyManager.ALLIANCES.get(playerId);

            if (alliance == null) {
                player.sendSystemMessage(Component.literal("You are not in an alliance."));
                return 0;
            }

            for (UUID memberId : alliance.getMembers()) {
                AllyManager.ALLIANCES.remove(memberId);
                ServerPlayer member = source.getServer().getPlayerList().getPlayer(memberId);
                if (member != null) {
                    member.sendSystemMessage(Component.literal("The alliance has been disbanded by " + player.getName().getString() + "."));
                }
            }
        } catch (Exception ignored) {}
        return 1;
    }

    private static int setFriendlyFire(@NonNull CommandSourceStack source, boolean value) {
        try {
            ServerPlayer player = source.getPlayerOrException();
            AllyManager.Alliance alliance = AllyManager.ALLIANCES.get(player.getUUID());

            if (alliance == null) {
                player.sendSystemMessage(Component.literal("You are not in an alliance."));
                return 0;
            }

            alliance.setFriendlyFire(value);
            for (UUID memberId : alliance.getMembers()) {
                ServerPlayer member = source.getServer().getPlayerList().getPlayer(memberId);
                if (member != null) {
                    member.sendSystemMessage(Component.literal("Alliance friendly fire is now set to: " + value));
                }
            }
        } catch (Exception ignored) {}
        return 1;
    }

    private static int requestItem(@NonNull CommandSourceStack source, @NonNull ServerPlayer target) {
        try {
            ServerPlayer requester = source.getPlayerOrException();
            if (requester.getUUID().equals(target.getUUID())) return 0;

            AllyManager.Alliance reqAlly = AllyManager.ALLIANCES.get(requester.getUUID());
            AllyManager.Alliance tarAlly = AllyManager.ALLIANCES.get(target.getUUID());

            if (reqAlly == null || reqAlly != tarAlly) {
                requester.sendSystemMessage(Component.literal("You can only request items from your allies."));
                return 0;
            }

            AllyManager.PENDING_REQUESTS.put(target.getUUID(), new AllyManager.RequestData(requester.getUUID(), "item", 0));
            requester.sendSystemMessage(Component.literal("Item request sent to " + target.getName().getString()));

            MutableComponent acceptBtn = Component.literal(" [ACCEPT]")
                    .withStyle(style -> style.withColor(ChatFormatting.GOLD).withBold(true)
                            .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/ally request accept")));

            target.sendSystemMessage(Component.literal(requester.getName().getString() + " requested the item in your hand.")
                    .append(acceptBtn));

        } catch (Exception ignored) {}
        return 1;
    }

    private static int requestXp(@NonNull CommandSourceStack source, @NonNull ServerPlayer target, int amount) {
        try {
            ServerPlayer requester = source.getPlayerOrException();
            if (requester.getUUID().equals(target.getUUID())) return 0;

            AllyManager.Alliance reqAlly = AllyManager.ALLIANCES.get(requester.getUUID());
            AllyManager.Alliance tarAlly = AllyManager.ALLIANCES.get(target.getUUID());

            if (reqAlly == null || reqAlly != tarAlly) {
                requester.sendSystemMessage(Component.literal("You can only request XP from your allies."));
                return 0;
            }

            AllyManager.PENDING_REQUESTS.put(target.getUUID(), new AllyManager.RequestData(requester.getUUID(), "xp", amount));
            requester.sendSystemMessage(Component.literal("XP request sent to " + target.getName().getString()));

            MutableComponent acceptBtn = Component.literal(" [ACCEPT]")
                    .withStyle(style -> style.withColor(ChatFormatting.GOLD).withBold(true)
                            .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/ally request accept")));

            target.sendSystemMessage(Component.literal(requester.getName().getString() + " requested " + amount + " XP levels.")
                    .append(acceptBtn));

        } catch (Exception ignored) {}
        return 1;
    }

    private static int acceptRequest(@NonNull CommandSourceStack source) {
        try {
            ServerPlayer target = source.getPlayerOrException();
            UUID targetId = target.getUUID();

            if (!AllyManager.PENDING_REQUESTS.containsKey(targetId)) {
                target.sendSystemMessage(Component.literal("You have no pending requests."));
                return 0;
            }

            AllyManager.RequestData req = AllyManager.PENDING_REQUESTS.get(targetId);
            ServerPlayer requester = source.getServer().getPlayerList().getPlayer(req.requester());

            if (requester == null) {
                target.sendSystemMessage(Component.literal("The requester is no longer online."));
                AllyManager.PENDING_REQUESTS.remove(targetId);
                return 0;
            }

            if (req.type().equals("item")) {
                ItemStack handItem = target.getMainHandItem();
                if (handItem.isEmpty()) {
                    target.sendSystemMessage(Component.literal("You don't have the item. Make sure you are holding it in your main-hand."));
                    return 0;
                }

                if (!requester.getInventory().add(handItem.copy())) {
                    target.sendSystemMessage(Component.literal("Their inventory is full!"));
                    requester.sendSystemMessage(Component.literal("Your inventory is full!"));
                    return 0;
                }

                handItem.setCount(0);
                target.getInventory().setChanged();

                target.sendSystemMessage(Component.literal("Item given to " + requester.getName().getString()));
                requester.sendSystemMessage(Component.literal(target.getName().getString() + " gave you an item."));
            }

            if (req.type().equals("xp")) {
                if (target.experienceLevel < req.amount()) {
                    target.sendSystemMessage(Component.literal("You don't have enough XP levels."));
                    return 0;
                }

                target.giveExperienceLevels(-req.amount());
                requester.giveExperienceLevels(req.amount());

                target.sendSystemMessage(Component.literal("Gave " + req.amount() + " XP levels to " + requester.getName().getString()));
                requester.sendSystemMessage(Component.literal(target.getName().getString() + " gave you " + req.amount() + " XP levels."));
            }

            AllyManager.PENDING_REQUESTS.remove(targetId);

        } catch (Exception ignored) {}
        return 1;
    }
}