package com.misanthropy.linggango.linggango_tweaks.chaos;

import com.misanthropy.linggango.linggango_tweaks.LinggangoTweaks;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jspecify.annotations.NonNull;


@Mod.EventBusSubscriber(modid = LinggangoTweaks.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CommandStuff {

    @SubscribeEvent
    public static void onRegisterCommands(@NonNull RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("linggango_tweaks")
                        .requires(source -> source.hasPermission(1))
                        .then(Commands.literal("reset_progression")
                                .executes(context -> {
                                    if (context.getSource().getEntity() instanceof ServerPlayer player) {
                                        boolean deleted = GlobalProgressionHandler.resetProgression();
                                        if (deleted) {
                                            player.sendSystemMessage(Component.literal("Global progression has been reset. Chaos difficulty is locked once again..")
                                                    .withStyle(ChatFormatting.YELLOW));
                                        } else {
                                            player.sendSystemMessage(Component.literal("No progression file found to reset. Make sure to unlock chaos difficulty first!")
                                                    .withStyle(ChatFormatting.RED));
                                        }
                                    }
                                    return 1;
                                })
                        )
        );
    }
}