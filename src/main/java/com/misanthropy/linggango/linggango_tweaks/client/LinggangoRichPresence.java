package com.misanthropy.linggango.linggango_tweaks.client;

import com.jagrosh.discordipc.IPCClient;
import com.jagrosh.discordipc.IPCListener;
import com.jagrosh.discordipc.entities.RichPresence;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.json.JSONObject;

import java.time.OffsetDateTime;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = "linggango_tweaks", value = Dist.CLIENT)
public class LinggangoRichPresence {

    private static final long CLIENT_ID = 1413254364633628826L;
    private static boolean initialized = false;
    private static boolean libraryPresent = false;
    private static OffsetDateTime startTime = null;

    public static void init() {
        if (initialized) return;

        try {
            startTime = OffsetDateTime.now();
            DiscordInternal.start();
            libraryPresent = true;
            initialized = true;
        } catch (Throwable t) {
            System.err.println("Linggango RPC Error: Missing libraries or linkage error.");
            libraryPresent = false;
            initialized = true;
        }
    }

    public static void updatePresence(String connectionType, String activity) {
        if (!initialized || !libraryPresent) return;

        try {
            Minecraft mc = Minecraft.getInstance();
            String mcName = mc.getUser().getName();
            DiscordInternal.update(activity, mcName, connectionType, startTime);
        } catch (Throwable t) {
            System.err.println("Linggango RPC: Failed to update presence.");
        }
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
        if (!initialized || !libraryPresent) return;
        DiscordInternal.stop();
        initialized = false;
    }

    private static class DiscordInternal {
        private static IPCClient client;
        private static boolean connected = false;

        private static void start() {
            client = new IPCClient(CLIENT_ID);
            client.setListener(new IPCListener() {
                @Override
                public void onReady(IPCClient c) {
                    connected = true;
                    System.out.println("Linggango RPC: Connected to Discord");
                    updatePresence("Main Menu", "Idling");
                }

                @Override
                public void onClose(IPCClient c, JSONObject json) {
                    connected = false;
                    System.out.println("Linggango RPC: Disconnected from Discord");
                }
            });
            try {
                client.connect();
            } catch (Exception e) {
                System.err.println("Linggango RPC: Connection attempt failed.");
            }
        }

        private static void update(String activity, String mcName, String connectionType, OffsetDateTime start) {
            if (client == null || !connected) return;

            RichPresence presence = new RichPresence.Builder()
                    .setDetails(mcName)
                    .setState(connectionType + " | " + activity)
                    .setStartTimestamp(start)
                    .build();

            client.sendRichPresence(presence);
        }

        private static void stop() {
            if (client != null) {
                try {
                    client.close();
                } catch (Exception ignored) {}
                connected = false;
            }
        }
    }
}