package com.misanthropy.linggango.linggango_tweaks.tweaks.corpse;

import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.OutgoingChatMessage;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;

public class YourCorpseStillExists {

    private static final boolean IS_CORPSE_LOADED = ModList.get().isLoaded("corpse");

    @SubscribeEvent
    public static void handleVoidDeath(LivingDeathEvent event) {
        if (!IS_CORPSE_LOADED) {
            return;
        }

        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        if (!event.getSource().is(DamageTypes.FELL_OUT_OF_WORLD)) {
            return;
        }

        String notification = "Void doesn't delete your stuff. Your corpse can still be recovered at Y=0.";
        var sourceStack = player.createCommandSourceStack();
        var chatMessage = PlayerChatMessage.unsigned(player.getUUID(), notification);
        var voidSender = ChatType.bind(
                ChatType.MSG_COMMAND_INCOMING,
                sourceStack.registryAccess(),
                Component.literal("The Void")
        );

        sourceStack.sendChatMessage(
                new OutgoingChatMessage.Player(chatMessage),
                false,
                voidSender
        );
    }
}