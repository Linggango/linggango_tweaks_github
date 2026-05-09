package com.misanthropy.linggango.linggango_tweaks.server;

import net.minecraft.server.MinecraftServer;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.NonNull;

import java.util.concurrent.atomic.AtomicReference;

@Mod.EventBusSubscriber(modid = "linggango_tweaks")
public class EmergencySaveProtector {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final AtomicReference<MinecraftServer> SERVER_REF = new AtomicReference<>(null);
    private static boolean done = false;

    @SubscribeEvent
    public static void onServerStart(@NonNull ServerStartedEvent event) {
        SERVER_REF.set(event.getServer());

        if (!done) {
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                MinecraftServer s = SERVER_REF.get();

                if (s != null && s.isRunning()) {
                    LOGGER.info("Linggango Tweaks: Some mod is causing to shit on your save. Forcing world save...");
                    try {
                        s.getPlayerList().saveAll();
                        s.saveAllChunks(false, true, false);
                    } catch (Exception e) {
                        LOGGER.error("Save failed: {}", e.getMessage());
                    }
                }
            }, "save-hook-thing"));
            done = true;
        }
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        SERVER_REF.set(null);
    }
}