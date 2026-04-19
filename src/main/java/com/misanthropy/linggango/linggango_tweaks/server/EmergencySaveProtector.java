package com.misanthropy.linggango.linggango_tweaks.server;

import net.minecraft.server.MinecraftServer;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod.EventBusSubscriber(modid = "linggango_tweaks")
public class EmergencySaveProtector {

    private static final Logger LOGGER = LogManager.getLogger();
    private static MinecraftServer activeServer;
    private static Thread shutdownHook;

    @SubscribeEvent
    public static void onServerStart(ServerStartedEvent event) {
        activeServer = event.getServer();

        if (shutdownHook == null) {
            shutdownHook = new Thread(() -> {
                if (activeServer != null && activeServer.isRunning()) {
                    LOGGER.info("Linggango Tweaks: Some mod is causing to shit on your save. Forcing world save...");

                    activeServer.getPlayerList().saveAll();

                    activeServer.saveAllChunks(true, true, true);
                }
            });
            Runtime.getRuntime().addShutdownHook(shutdownHook);
        }
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        if (activeServer != null) {
            LOGGER.info("Linggango Tweaks: Server seems to be stopped normally. Ensuring final save...");

            activeServer.getPlayerList().saveAll();

            activeServer.saveAllChunks(true, true, true);
            activeServer = null;
        }
    }
}