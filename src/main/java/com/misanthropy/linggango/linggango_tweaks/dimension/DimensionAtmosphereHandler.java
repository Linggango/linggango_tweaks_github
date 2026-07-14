//package com.misanthropy.linggango.linggango_tweaks.dimension;
//
//import com.misanthropy.linggango.linggango_tweaks.registry.dimension.ApollyonDimension;
//import com.misanthropy.linggango.linggango_tweaks.registry.sounds.SoundRegistry;
//import net.minecraft.client.Minecraft;
//import net.minecraft.client.renderer.DimensionSpecialEffects;
//import net.minecraft.client.resources.sounds.SimpleSoundInstance;
//import net.minecraft.client.resources.sounds.SoundInstance;
//import net.minecraft.nbt.CompoundTag;
//import net.minecraft.resources.ResourceLocation;
//import net.minecraft.server.level.ServerPlayer;
//import net.minecraft.sounds.SoundSource;
//import net.minecraft.util.RandomSource;
//import net.minecraft.world.entity.player.Player;
//import net.minecraft.world.phys.Vec3;
//import net.minecraftforge.api.distmarker.Dist;
//import net.minecraftforge.api.distmarker.OnlyIn;
//import net.minecraftforge.client.event.RegisterDimensionSpecialEffectsEvent;
//import net.minecraftforge.event.TickEvent;
//import net.minecraftforge.eventbus.api.SubscribeEvent;
//import net.minecraftforge.fml.common.Mod;
//import org.jetbrains.annotations.NotNull;
//
//import java.util.Random;
//
//@SuppressWarnings("unused")
//public class DimensionAtmosphereHandler {
//    private static final Random RANDOM = new Random();
//    private static final String AMBIENT_TIMER_KEY = "ApollyonAmbientTimer";
//    private static final String WIND_TIMER_KEY = "ApollyonWindTimer";
//    private static final String LAST_DIM_KEY = "LastDimension";
//
//    @OnlyIn(Dist.CLIENT)
//    private static boolean isHorrorLoopPlaying = false;
//    @OnlyIn(Dist.CLIENT)
//    private static boolean isAscensionSequencePlaying = false;
//    @OnlyIn(Dist.CLIENT)
//    private static long ascensionEntryStartTime = -1;
//
//    @Mod.EventBusSubscriber(modid = "linggango_tweaks", bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
//    public static class ClientModBusEvents {
//        @SubscribeEvent
//        public static void onRegisterDimensionEffects(RegisterDimensionSpecialEffectsEvent event) {
//            event.register(new ResourceLocation("linggango_tweaks", "revelation_type"),
//                    new DimensionSpecialEffects(Float.NaN, false, DimensionSpecialEffects.SkyType.NORMAL, false, false) {
//                        @Override @NotNull public Vec3 getBrightnessDependentFogColor(@NotNull Vec3 fogColor, float brightness) { return fogColor; }
//                        @Override public boolean isFoggyAt(int x, int y) { return false; }
//                    });
//
//            event.register(new ResourceLocation("linggango_tweaks", "ascension_type"),
//                    new DimensionSpecialEffects(Float.NaN, false, DimensionSpecialEffects.SkyType.NORMAL, false, false) {
//                        @Override @NotNull public Vec3 getBrightnessDependentFogColor(@NotNull Vec3 fogColor, float brightness) {
//                            return new Vec3(0.95, 0.92, 0.85);
//                        }
//                        @Override public boolean isFoggyAt(int x, int y) { return false; }
//                    });
//        }
//    }
//
//    @Mod.EventBusSubscriber(modid = "linggango_tweaks", bus = Mod.EventBusSubscriber.Bus.FORGE)
//    public static class ForgeBusEvents {
//        @SubscribeEvent
//        public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
//            if (event.phase != TickEvent.Phase.END) return;
//
//            Player player = event.player;
//            boolean isRevelation = player.level().dimension().equals(ApollyonDimension.APOLLYON_LEVEL);
//            boolean isAscension = player.level().dimension().equals(ApollyonDimension.ASCENSION_LEVEL);
//            boolean isMacabre = player.level().dimension().location().toString().equals("macabre:the_pit");
//
//            if (isRevelation) {
//                if (!player.level().isClientSide) {
//                    handleRevelationServerAtmosphere((ServerPlayer) player);
//                } else {
//                    handleRevelationClientAtmosphere(player);
//                }
//            } else if (isAscension) {
//                if (!player.level().isClientSide) {
//                    handleAscensionServerAtmosphere((ServerPlayer) player);
//                } else {
//                    handleAscensionClientAtmosphere(player);
//                }
//            } else if (isMacabre) {
//                if (!player.level().isClientSide) {
//                    handleMacabreServerAtmosphere((ServerPlayer) player);
//                } else {
//                    handleMacabreClientAtmosphere(player);
//                }
//            } else if (player.level().isClientSide) {
//                String lastDim = player.getPersistentData().getString(LAST_DIM_KEY);
//                if ("macabre:the_pit".equals(lastDim)) {
//                    Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forAmbientAddition(SoundRegistry.MACABRE_DIMENSION_LEAVE.get()));
//                }
//                resetClientStates();
//            }
//
//            player.getPersistentData().putString(LAST_DIM_KEY, player.level().dimension().location().toString());
//        }
//    }
//
//    private static void handleMacabreServerAtmosphere(ServerPlayer player) {
//        CompoundTag data = player.getPersistentData();
//        String lastDim = data.getString(LAST_DIM_KEY);
//
//        if (!"macabre:the_pit".equals(lastDim)) {
//            player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundRegistry.MACABRE_DIMENSION_ENTER.get(), SoundSource.AMBIENT, 1.0f, 1.0f);
//        }
//
//        int ambientTimer = data.contains("MacabreAmbientTimer") ? data.getInt("MacabreAmbientTimer") : 600;
//        if (--ambientTimer <= 0) {
//            if (RANDOM.nextFloat() < 0.3f) {
//                if (RANDOM.nextBoolean()) {
//                    player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundRegistry.MACABRE_AMBIENCE_1.get(), SoundSource.AMBIENT, 1.0f, 1.0f);
//                } else {
//                    player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundRegistry.MACABRE_AMBIENCE_2.get(), SoundSource.AMBIENT, 1.0f, 1.0f);
//                }
//            }
//            ambientTimer = 600 + RANDOM.nextInt(400);
//        }
//        data.putInt("MacabreAmbientTimer", ambientTimer);
//    }
//
//    @OnlyIn(Dist.CLIENT)
//    private static void handleMacabreClientAtmosphere(Player player) {
//        Minecraft mc = Minecraft.getInstance();
//        mc.getMusicManager().stopPlaying();
//    }
//
//    private static void handleRevelationServerAtmosphere(ServerPlayer player) {
//        CompoundTag data = player.getPersistentData();
//        int ambientTimer = data.contains(AMBIENT_TIMER_KEY) ? data.getInt(AMBIENT_TIMER_KEY) : 400;
//        int windTimer = data.contains(WIND_TIMER_KEY) ? data.getInt(WIND_TIMER_KEY) : 600;
//
//        if (--ambientTimer <= 0) {
//            player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundRegistry.HORROR_AMBIENCE.get(), SoundSource.AMBIENT, 1.0f, 1.0f);
//            ambientTimer = 400 + RANDOM.nextInt(400);
//        }
//
//        if (--windTimer <= 0) {
//            if (RANDOM.nextBoolean()) {
//                player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundRegistry.DIMENSION_WIND.get(), SoundSource.AMBIENT, 0.7f, 1.0f);
//            }
//            windTimer = 600 + RANDOM.nextInt(400);
//        }
//
//        data.putInt(AMBIENT_TIMER_KEY, ambientTimer);
//        data.putInt(WIND_TIMER_KEY, windTimer);
//    }
//
//    private static void handleAscensionServerAtmosphere(ServerPlayer player) {
//        CompoundTag data = player.getPersistentData();
//        int ambientTimer = data.contains("AscensionAmbientTimer") ? data.getInt("AscensionAmbientTimer") : 500;
//        int waterStepTimer = data.contains("WaterStepTimer") ? data.getInt("WaterStepTimer") : 1200;
//
//        if (--ambientTimer <= 0) {
//            player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundRegistry.ASCENSION_AMBIENCE.get(), SoundSource.AMBIENT, 0.8f, 1.0f);
//            ambientTimer = 600 + RANDOM.nextInt(600);
//        }
//
//        if (--waterStepTimer <= 0) {
//            if (RANDOM.nextFloat() < 0.01f) {
//                player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundRegistry.WATER_STEP.get(), SoundSource.AMBIENT, 0.5f, 0.8f + RANDOM.nextFloat() * 0.4f);
//            }
//            waterStepTimer = 1200;
//        }
//
//        data.putInt("AscensionAmbientTimer", ambientTimer);
//        data.putInt("WaterStepTimer", waterStepTimer);
//    }
//
//    @OnlyIn(Dist.CLIENT)
//    private static void handleRevelationClientAtmosphere(Player player) {
//        Minecraft mc = Minecraft.getInstance();
//        mc.getMusicManager().stopPlaying();
//
//        if (!isHorrorLoopPlaying) {
//            mc.getSoundManager().play(new SimpleSoundInstance(SoundRegistry.HORROR_AMBIENCE_LOOP.get().getLocation(), SoundSource.AMBIENT, 1.0f, 1.0f, RandomSource.create(), true, 0, SoundInstance.Attenuation.NONE, 0.0D, 0.0D, 0.0D, true));
//            isHorrorLoopPlaying = true;
//            isAscensionSequencePlaying = false;
//        }
//    }
//
//    @OnlyIn(Dist.CLIENT)
//    private static void handleAscensionClientAtmosphere(Player player) {
//        Minecraft mc = Minecraft.getInstance();
//        mc.getMusicManager().stopPlaying();
//
//        if (isHorrorLoopPlaying) {
//            isHorrorLoopPlaying = false;
//            mc.getSoundManager().stop(SoundRegistry.HORROR_AMBIENCE_LOOP.get().getLocation(), SoundSource.AMBIENT);
//        }
//
//        if (!isAscensionSequencePlaying) {
//            mc.getSoundManager().play(SimpleSoundInstance.forAmbientAddition(SoundRegistry.ASCENSION_ENTRANCE.get()));
//            ascensionEntryStartTime = System.currentTimeMillis();
//            isAscensionSequencePlaying = true;
//        } else if (ascensionEntryStartTime != -1) {
//            long elapsed = System.currentTimeMillis() - ascensionEntryStartTime;
//            if (elapsed > 5000) {
//                mc.getSoundManager().play(new SimpleSoundInstance(SoundRegistry.ASCENSION_AFTERMATH.get().getLocation(), SoundSource.AMBIENT, 1.0f, 1.0f, RandomSource.create(), true, 0, SoundInstance.Attenuation.NONE, 0.0D, 0.0D, 0.0D, true));
//                ascensionEntryStartTime = -1;
//            }
//        }
//    }
//
//    @OnlyIn(Dist.CLIENT)
//    private static void resetClientStates() {
//        isHorrorLoopPlaying = false;
//        isAscensionSequencePlaying = false;
//        ascensionEntryStartTime = -1;
//    }
//}