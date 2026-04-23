package com.misanthropy.linggango.linggango_tweaks.client;

import dev.firstdark.rpc.DiscordRpc;
import dev.firstdark.rpc.enums.ActivityType;
import dev.firstdark.rpc.enums.ErrorCode;
import dev.firstdark.rpc.handlers.RPCEventHandler;
import dev.firstdark.rpc.models.DiscordRichPresence;
import dev.firstdark.rpc.models.User;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = "linggango_tweaks", value = Dist.CLIENT)
public class LinggangoRichPresence {

    private static final String CLIENT_ID = "1413254364633628826";
    private static final DiscordRpc rpc = new DiscordRpc();
    private static final ScheduledExecutorService EXECUTOR = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "Linggango-RPC-Callback");
        t.setDaemon(true);
        return t;
    });

    private static boolean initialized = false;
    private static long startTime = 0;

    public static void init() {
        if (initialized) return;

        try {
            startTime = System.currentTimeMillis();
            rpc.setDebugMode(true);

            RPCEventHandler handler = new RPCEventHandler() {
                @Override
                public void ready(User user) {
                    System.out.println("Linggango RPC: Connected as " + user.getUsername());
                    updatePresence("Main Menu", "Idling");
                }

                @Override
                public void disconnected(ErrorCode errorCode, String message) {
                    System.out.println("Linggango RPC: Disconnected " + errorCode + " - " + message);
                }

                @Override
                public void errored(ErrorCode errorCode, String message) {
                    System.out.println("Linggango RPC: Error " + errorCode + " - " + message);
                }
            };

            rpc.init(CLIENT_ID, handler, false);
            initialized = true;

            EXECUTOR.scheduleAtFixedRate(rpc::runCallbacks, 0, 2, TimeUnit.SECONDS);
        } catch (Exception e) {
            System.err.println("Linggango RPC: Failed to initialize.");
        }
    }

    public static void updatePresence(String connectionType, String activity) {
        if (!initialized) return;

        Minecraft mc = Minecraft.getInstance();
        String mcName = mc.getUser().getName();

        DiscordRichPresence presence = DiscordRichPresence.builder()
                .details(mcName)
                .state(connectionType + " | " + activity)
                .startTimestamp(startTime)
                .largeImageKey("additionslogo")
                .activityType(ActivityType.PLAYING)
                .build();

        rpc.updatePresence(presence);
    }

    @SubscribeEvent
    public static void onScreenOpen(ScreenEvent.Opening event) {
        if (event.getScreen() instanceof TitleScreen) {
            updatePresence("Main Menu", "Idling");
        }
    }

    @SubscribeEvent
    public static void onWorldJoin(EntityJoinLevelEvent event) {
        if (!event.getLevel().isClientSide() || event.getEntity() != Minecraft.getInstance().player) return;

        Minecraft mc = Minecraft.getInstance();
        String dimPath = event.getLevel().dimension().location().getPath();
        String dimension = dimPath.substring(0, 1).toUpperCase() + dimPath.substring(1).replace("_", " ");

        String activity = "Exploring " + dimension;
        String connectionType;

        if (mc.getSingleplayerServer() != null) {
            connectionType = "Singleplayer";
        } else {
            ServerData data = mc.getCurrentServer();
            connectionType = (data != null) ? "Online: " + data.ip : "Multiplayer";
        }

        updatePresence(connectionType, activity);
    }

    public static void shutdown() {
        if (!initialized) return;
        EXECUTOR.shutdown();
        rpc.shutdown();
        initialized = false;
    }
}
