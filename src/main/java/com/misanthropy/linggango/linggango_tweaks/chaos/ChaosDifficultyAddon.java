package com.misanthropy.linggango.linggango_tweaks.chaos;

import com.misanthropy.linggango.difficulty_enhancement.LinggangoEvents;
import com.misanthropy.linggango.linggango_tweaks.LinggangoTweaks;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Mod.EventBusSubscriber(modid = LinggangoTweaks.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ChaosDifficultyAddon {

    private static final String NYXARIS_ID = "armageddon_mod:nyxaris_the_veil_of_oblivion";
    private static @Nullable List<EntityType<?>> cachedChaosPool = null;

    public static void registerChaos() {
        if (ModList.get().isLoaded("difficulty_enhancement")) {
            List<String> traits = new ArrayList<>(Arrays.asList(
                    "- Removed Dual-Ring Limits",
                    "- Randomized Mob Spawns"
            ));

            LinggangoEvents.register(new LinggangoEvents.DifficultyDef.Builder("chaos")
                    .name("Chaos")
                    .description("The hardest!")
                    .recommendation("Don't play this at all!")
                    .uiTheme(4)
                    .hpMultiplier(6.0)
                    .dmgMultiplier(10.0)
                    .fallDmgMultiplier(4.0f)
                    .enableBleeding(true)
                    .enableCrippling(true)
                    .enableVengefulAI(true)
                    .enableRegen(false)
                    .lifestealAmount(2.0f)
                    .exhaustionAmount(1.0f)
                    .bgColorHex("#110000")
                    .accentColorHex("#FF0000")
                    .hoverSound("embers:item.resonating_bell.ring")
                    .selectSound("goety:resonance_crystal_on")
                    .confirmSound("morerelics:judgement_bell")
                    .traits(traits)
                    .commands(new ArrayList<>())
                    .build());
        }
    }

    public static boolean isChaos(@NonNull Level level) {
        return LinggangoEvents.getCurrentDifficulty(level).id.equals("chaos");
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.@NonNull ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        var server = net.minecraftforge.server.ServerLifecycleHooks.getCurrentServer();
        if (server == null || server.getTickCount() % 40 != 0) return;

        for (ServerLevel world : server.getAllLevels()) {
            if (isChaos(world)) {
                triggerChaosSpawning(world);
            }
        }
    }

    private static void triggerChaosSpawning(@NonNull ServerLevel level) {
        List<ServerPlayer> players = level.players();
        if (players.isEmpty()) return;

        if (cachedChaosPool == null) {
            cachedChaosPool = ForgeRegistries.ENTITY_TYPES.getValues().stream()
                    .filter(type -> {
                        ResourceLocation id = ForgeRegistries.ENTITY_TYPES.getKey(type);
                        if (id == null) return false;

                        String p = id.getPath();
                        boolean isMob = Mob.class.isAssignableFrom(type.getBaseClass());
                        boolean isTechnical = p.contains("projectile") || p.contains("technical") ||
                                p.contains("effect") || p.contains("marker") ||
                                p.contains("breath") || p.contains("spell") ||
                                p.contains("missile") || p.contains("orb");

                        return isMob && !isTechnical;
                    }).collect(Collectors.toList());
        }

        if (cachedChaosPool.isEmpty()) return;

        for (int i = 0; i < 3; i++) {
            ServerPlayer player = players.get(ThreadLocalRandom.current().nextInt(players.size()));
            EntityType<?> randomType = cachedChaosPool.get(ThreadLocalRandom.current().nextInt(cachedChaosPool.size()));

            int rx = (ThreadLocalRandom.current().nextInt(48) - 24);
            int rz = (ThreadLocalRandom.current().nextInt(48) - 24);
            BlockPos spawnPos = player.blockPosition().offset(rx, ThreadLocalRandom.current().nextInt(10) - 5, rz);

            try {
                Entity spawned = randomType.spawn(level, (CompoundTag) null, null, spawnPos, MobSpawnType.EVENT, true, false);
                if (spawned instanceof Mob mob) {
                    mob.setPersistenceRequired();
                    if (mob instanceof PathfinderMob pathfinder) pathfinder.setTarget(player);
                }
            } catch (Exception ignored) {}
        }
    }

    @SubscribeEvent
    public static void onCheckSpawn(MobSpawnEvent.@NonNull SpawnPlacementCheck event) {
        if (event.getLevel() instanceof ServerLevel sl) {
            if (isChaos(sl)) {
                event.setResult(Event.Result.ALLOW);
            }
        }
    }

    @SubscribeEvent
    public static void onBossDeath(@NonNull LivingDeathEvent event) {
        LivingEntity victim = event.getEntity();
        if (victim.level().isClientSide()) return;
        ResourceLocation id = ForgeRegistries.ENTITY_TYPES.getKey(victim.getType());
        if (id != null && id.toString().equals(NYXARIS_ID)) {
            if (victim.level() instanceof ServerLevel serverLevel) {
                LinggangoEvents.unlockChaos(serverLevel);
                serverLevel.players().forEach(player -> player.sendSystemMessage(Component.literal("Nyxaris has fallen... Chaos Difficulty has been unlocked!").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD, ChatFormatting.OBFUSCATED)));
            }
        }
    }
}