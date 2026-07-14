//package com.misanthropy.linggango.linggango_tweaks.client.background;
//
//import de.keksuccino.fancymenu.customization.variables.VariableHandler;
//import net.minecraft.client.Minecraft;
//import net.minecraft.core.BlockPos;
//import net.minecraft.core.Holder;
//import net.minecraft.core.registries.Registries;
//import net.minecraft.resources.ResourceLocation;
//import net.minecraft.world.level.biome.Biome;
//import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
//import net.minecraftforge.event.TickEvent;
//import net.minecraftforge.eventbus.api.SubscribeEvent;
//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;
//
//import java.io.File;
//
//public class BiomeBackgroundHandler {
//
//    private static final Logger LOGGER = LogManager.getLogger();
//    private static ResourceLocation lastKnownBiome = null;
//    private static final String DEFAULT_BACKGROUND = "default_background.png";
//
//    @SubscribeEvent
//    public static void onClientTick(TickEvent.ClientTickEvent event) {
//        if (event.phase == TickEvent.Phase.END) {
//            Minecraft mc = Minecraft.getInstance();
//            if (mc.player != null && mc.level != null) {
//                BlockPos pos = mc.player.blockPosition();
//                Holder<Biome> biomeHolder = mc.level.getBiome(pos);
//
//                ResourceLocation id = mc.level.registryAccess()
//                        .registryOrThrow(Registries.BIOME)
//                        .getKey(biomeHolder.value());
//
//                if (id != null) {
//                    lastKnownBiome = id;
//                }
//            }
//        }
//    }
//
//    @SubscribeEvent
//    public static void onPlayerLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
//        LOGGER.info("[Linggango Tweaks] Player logging out. Last known biome: {}", lastKnownBiome);
//        if (lastKnownBiome != null) {
//            updateFancyMenuVariable(lastKnownBiome);
//        } else {
//            LOGGER.warn("[Linggango Tweaks] No biome registered on logout. Setting default background.");
//            VariableHandler.setVariable("dynamic_biome_bg", DEFAULT_BACKGROUND);
//        }
//    }
//
//    private static void updateFancyMenuVariable(ResourceLocation biomeId) {
//        String namespace = biomeId.getNamespace();
//        String path = biomeId.getPath();
//        String baseFileName = namespace + "_background_" + path;
//
//        File gameDir = Minecraft.getInstance().gameDirectory;
//        File assetsDir = new File(gameDir, "config/fancymenu/assets");
//
//        String selectedFileName = DEFAULT_BACKGROUND;
//
//        if (assetsDir.exists() && assetsDir.isDirectory()) {
//            File backgroundFile = new File(assetsDir, baseFileName + ".png");
//            LOGGER.info("[Linggango Tweaks] Checking for biome background file at: {}", backgroundFile.getAbsolutePath());
//
//            if (backgroundFile.exists()) {
//                selectedFileName = baseFileName + ".png";
//                LOGGER.info("[Linggango Tweaks] Match found! Background set to: {}", selectedFileName);
//            } else {
//                LOGGER.warn("[Linggango Tweaks] No background image found for biome: {}. Falling back to default.", biomeId);
//            }
//        } else {
//            LOGGER.error("[Linggango Tweaks] FancyMenu assets directory not found at: {}", assetsDir.getAbsolutePath());
//        }
//
//        VariableHandler.setVariable("dynamic_biome_bg", selectedFileName);
//    }
//}