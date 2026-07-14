package com.misanthropy.linggango.linggango_tweaks.features;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;

@Mod.EventBusSubscriber(modid = "linggango_tweaks", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ItemTransformationForMacabre {

    private static final ResourceLocation ANCHOR_ITEM = new ResourceLocation("naturesaura", "ancient_stick");
    private static final ResourceLocation ALLOY_MODIFIER = new ResourceLocation("terramity", "hellspec_alloy");
    private static final ResourceLocation RESULT_ITEM = new ResourceLocation("macabre", "sacrificial_dirk");

    private static final SoundEvent CUSTOM_SUCCESS = SoundEvent.createVariableRangeEvent(new ResourceLocation("linggango_tweaks", "macabre_succeed"));
    private static final SoundEvent CUSTOM_FAILURE = SoundEvent.createVariableRangeEvent(new ResourceLocation("linggango_tweaks", "macabre_failure"));

    private static final Map<ResourceLocation, Integer> RECIPE = new HashMap<>();
    static {
        RECIPE.put(new ResourceLocation("terramity", "reverium"), 2);
        RECIPE.put(new ResourceLocation("armageddon_mod", "bloody_ingot"), 2);
        RECIPE.put(new ResourceLocation("born_in_chaos_v1", "bloody_gadfly_eye"), 1);
    }

    private static final List<RitualInstance> ACTIVE_RITUALS = new ArrayList<>();

    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.level.isClientSide()) {
            return;
        }
        ServerLevel level = (ServerLevel) event.level;

        Iterator<RitualInstance> iterator = ACTIVE_RITUALS.iterator();
        while (iterator.hasNext()) {
            RitualInstance ritual = iterator.next();
            if (ritual.level != level) continue;
            if (ritual.tick()) iterator.remove();
        }

        if (level.getGameTime() % 20 == 0) {
            scanForRituals(level);
        }
    }

    @SubscribeEvent
    public static void onLevelUnload(LevelEvent.Unload event) {
        if (!event.getLevel().isClientSide() && event.getLevel() instanceof ServerLevel level) {
            ACTIVE_RITUALS.removeIf(ritual -> ritual.level == level);
        }
    }

    private static void scanForRituals(ServerLevel level) {
        Set<ItemEntity> scanned = new HashSet<>();
        for (ServerPlayer player : level.players()) {
            AABB playerBounds = player.getBoundingBox().inflate(16.0D);
            List<ItemEntity> nearbyItems = level.getEntitiesOfClass(ItemEntity.class, playerBounds);

            for (ItemEntity anchor : nearbyItems) {
                if (!anchor.isAlive() || scanned.contains(anchor)) {
                    continue;
                }
                CompoundTag anchorTag = anchor.getItem().getTag();
                if (anchorTag != null && anchorTag.contains("MacabreRitualID")) {
                    continue;
                }

                if (Objects.equals(ForgeRegistries.ITEMS.getKey(anchor.getItem().getItem()), ANCHOR_ITEM)) {
                    scanned.add(anchor);
                    boolean isResting = anchor.onGround() || anchor.getDeltaMovement().lengthSqr() < 0.001;
                    if (!isResting) continue;

                    AABB ritualBounds = anchor.getBoundingBox().inflate(2.0D);
                    List<ItemEntity> ritualItems = level.getEntitiesOfClass(ItemEntity.class, ritualBounds);

                    Map<ResourceLocation, Integer> counts = new HashMap<>();
                    int availableAlloys = 0;

                    for (ItemEntity e : ritualItems) {
                        CompoundTag eTag = e.getItem().getTag();
                        if (eTag != null && eTag.contains("MacabreRitualID")) {
                            continue;
                        }

                        ResourceLocation id = ForgeRegistries.ITEMS.getKey(e.getItem().getItem());
                        int count = e.getItem().getCount();

                        if (ALLOY_MODIFIER.equals(id)) {
                            availableAlloys += count;
                        } else if (RECIPE.containsKey(id)) {
                            counts.put(id, counts.getOrDefault(id, 0) + count);
                        }
                    }

                    boolean match = true;
                    for (Map.Entry<ResourceLocation, Integer> req : RECIPE.entrySet()) {
                        if (counts.getOrDefault(req.getKey(), 0) < req.getValue()) {
                            match = false;
                            break;
                        }
                    }

                    if (match) {
                        if (level.dimension() != Level.OVERWORLD) {
                            punishWrongDimension(level, anchor.position());
                            return;
                        }
                        startRitual(level, ritualItems, anchor, availableAlloys);
                        return;
                    }
                }
            }
        }
    }

    private static void punishWrongDimension(ServerLevel level, Vec3 pos) {
        spawnLightning(level, pos, false);
        sendShakingActionBar(level, pos, "ALL OFFERINGS SHALL BE MADE IN OVERWORLD");
    }

    private static void startRitual(ServerLevel level, List<ItemEntity> nearbyItems, ItemEntity anchor, int availableAlloys) {
        UUID ritualId = UUID.randomUUID();
        List<ItemEntity> consumedEntities = new ArrayList<>();
        Map<ResourceLocation, Integer> remainingReqs = new HashMap<>(RECIPE);

        int alloysToConsume = Math.min(20, availableAlloys);
        int consumedAlloys = 0;

        consumedEntities.add(splitAndLockItem(level, anchor, 1, ritualId));

        for (ItemEntity entity : nearbyItems) {
            if (entity == anchor) continue;
            CompoundTag entityTag = entity.getItem().getTag();
            if (entityTag != null && entityTag.contains("MacabreRitualID")) continue;

            ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(entity.getItem().getItem());

            if (ALLOY_MODIFIER.equals(itemId) && alloysToConsume > 0) {
                int toConsume = Math.min(alloysToConsume, entity.getItem().getCount());
                consumedEntities.add(splitAndLockItem(level, entity, toConsume, ritualId));
                alloysToConsume -= toConsume;
                consumedAlloys += toConsume;
            } else if (remainingReqs.containsKey(itemId) && remainingReqs.get(itemId) > 0) {
                int needed = remainingReqs.get(itemId);
                int available = entity.getItem().getCount();
                int toConsume = Math.min(needed, available);

                consumedEntities.add(splitAndLockItem(level, entity, toConsume, ritualId));
                remainingReqs.put(itemId, needed - toConsume);
            }
        }

        ACTIVE_RITUALS.add(new RitualInstance(level, consumedEntities, anchor.position(), consumedAlloys));
    }

    private static ItemEntity splitAndLockItem(ServerLevel level, ItemEntity original, int amount, UUID ritualId) {
        if (original.getItem().getCount() <= amount) {
            original.getItem().getOrCreateTag().putUUID("MacabreRitualID", ritualId);
            original.setNoGravity(true);
            return original;
        } else {
            ItemStack splitStack = original.getItem().split(amount);
            ItemEntity newEntity = new ItemEntity(level, original.getX(), original.getY(), original.getZ(), splitStack);
            newEntity.getItem().getOrCreateTag().putUUID("MacabreRitualID", ritualId);
            newEntity.setNoGravity(true);
            newEntity.setDeltaMovement(original.getDeltaMovement());
            level.addFreshEntity(newEntity);
            return newEntity;
        }
    }

    private static void spawnLightning(ServerLevel level, Vec3 pos, boolean visualOnly) {
        LightningBolt lightning = EntityType.LIGHTNING_BOLT.create(level);
        if (lightning != null) {
            lightning.moveTo(pos);
            lightning.setVisualOnly(visualOnly);
            level.addFreshEntity(lightning);
        }
    }

    private static void sendShakingActionBar(ServerLevel level, Vec3 pos, String text) {
        MutableComponent message = Component.literal(text).withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD);

        for (ServerPlayer player : level.players()) {
            if (player.position().distanceTo(pos) < 30.0D) {
                player.displayClientMessage(message, true);
            }
        }
    }

    private static void sendChatDots(ServerLevel level, Vec3 pos, String dots) {
        MutableComponent message = Component.literal(dots).withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC);
        for (ServerPlayer player : level.players()) {
            if (player.position().distanceTo(pos) < 20.0D) {
                player.displayClientMessage(message, false);
            }
        }
    }

    private static class RitualInstance {
        final ServerLevel level;
        final List<ItemEntity> involvedItems;
        final Vec3 centerPos;
        final int successChance;

        int age = 0;
        boolean isDestroying = false;
        boolean isWon = false;
        int failureTicks = 0;

        RitualInstance(ServerLevel level, List<ItemEntity> items, Vec3 centerPos, int alloys) {
            this.level = level;
            this.involvedItems = items;
            this.centerPos = centerPos;
            this.successChance = Math.min(90, 5 + (int)(4.25 * alloys));
        }

        boolean tick() {
            if (isDestroying) {
                if (failureTicks % 4 == 0) {
                    spawnLightning(level, centerPos, false);
                }

                if (failureTicks == 0) {
                    level.sendParticles(ParticleTypes.LARGE_SMOKE, centerPos.x, centerPos.y + 0.5, centerPos.z, 80, 1.0, 1.0, 1.0, 0.05);
                    level.sendParticles(ParticleTypes.SQUID_INK, centerPos.x, centerPos.y + 0.5, centerPos.z, 50, 0.5, 0.5, 0.5, 0.05);
                }

                failureTicks++;

                if (failureTicks >= 20) {
                    for (ItemEntity item : involvedItems) item.discard();
                    return true;
                }
                return false;
            }

            age++;

            for (ItemEntity item : involvedItems) {
                if (!item.isAlive()) {
                    abortRitual();
                    return true;
                }
                item.setPickUpDelay(32767);
            }

            if (age < 100) {
                for (ItemEntity item : involvedItems) {
                    item.setDeltaMovement(0, 0, 0);
                    if (age % 5 == 0) {
                        level.sendParticles(ParticleTypes.SQUID_INK, item.getX(), item.getY() + 0.2, item.getZ(), 2, 0.1, 0.1, 0.1, 0.01);
                    }
                }
                if (age == 10) sendChatDots(level, centerPos, ".");
                return false;
            }

            if (age == 100) {
                for (ItemEntity item : involvedItems) {
                    item.setDeltaMovement(0, 0.05, 0);
                }
                level.playSound(null, BlockPos.containing(centerPos), SoundEvents.AMBIENT_CAVE.value(), SoundSource.MASTER, 3.0F, 0.5F);
            }

            if (age > 120 && age < 480) {
                for (ItemEntity item : involvedItems) {
                    double yBob = Math.sin((age - 120) * 0.1) * 0.02;
                    item.setDeltaMovement(0, yBob, 0);
                }
            }

            if (age >= 150 && age < 480) {
                double speedMultiplier = (age - 150) * 0.05;
                if (age > 400) speedMultiplier *= 3.0;
                double angle = (age * (5 + speedMultiplier)) % 360;
                double rad = Math.toRadians(angle);
                double px = centerPos.x + Math.cos(rad) * 1.5;
                double pz = centerPos.z + Math.sin(rad) * 1.5;

                level.sendParticles(ParticleTypes.SQUID_INK, px, centerPos.y + 0.5, pz, 1, 0, 0, 0, 0);
                level.sendParticles(ParticleTypes.PORTAL, px, centerPos.y + 0.5, pz, 2, 0, 0, 0, -0.5);
            }

            if (age >= 100 && age < 400) {
                int interval = 40;
                if (age >= 350) interval = 5;
                else if (age >= 300) interval = 10;
                else if (age >= 200) interval = 20;

                if (age % interval == 0) {
                    float pitch = 0.5f + ((age - 100) / 300.0f) * 1.5f;
                    level.playSound(null, BlockPos.containing(centerPos), SoundEvents.WARDEN_HEARTBEAT, SoundSource.MASTER, 3.0F, pitch);
                }
            }

            if (age == 180) sendChatDots(level, centerPos, "..");
            if (age == 280) sendChatDots(level, centerPos, "...");
            if (age == 360) sendChatDots(level, centerPos, "....");

            if (age == 400) {
                int roll = level.random.nextInt(100);
                isWon = (roll < successChance);

                if (isWon) {
                    level.playSound(null, BlockPos.containing(centerPos), CUSTOM_SUCCESS, SoundSource.MASTER, 3.0F, 1.0F);
                } else {
                    level.playSound(null, BlockPos.containing(centerPos), CUSTOM_FAILURE, SoundSource.MASTER, 3.0F, 1.0F);
                }
            }

            if (age >= 480) {
                if (isWon) {
                    spawnLightning(level, centerPos, true);

                    level.sendParticles(ParticleTypes.SQUID_INK, centerPos.x, centerPos.y + 1.0, centerPos.z, 150, 1.0, 1.0, 1.0, 0.1);
                    level.sendParticles(ParticleTypes.PORTAL, centerPos.x, centerPos.y + 1.0, centerPos.z, 100, 1.0, 1.0, 1.0, 0.5);
                    level.sendParticles(ParticleTypes.LARGE_SMOKE, centerPos.x, centerPos.y + 1.0, centerPos.z, 50, 1.0, 1.0, 1.0, 0.1);

                    sendShakingActionBar(level, centerPos, "YOUR OFFERING IS ACCEPTED");
                    level.playSound(null, BlockPos.containing(centerPos), SoundEvents.WITHER_SPAWN, SoundSource.MASTER, 1.5F, 0.5F);

                    for (ItemEntity item : involvedItems) item.discard();

                    Item resultItem = ForgeRegistries.ITEMS.getValue(RESULT_ITEM);
                    if (resultItem != null) {
                        ItemEntity resultEntity = new ItemEntity(level, centerPos.x, centerPos.y + 1.5, centerPos.z, new ItemStack(resultItem));
                        resultEntity.setDeltaMovement(0, 0.3, 0);
                        level.addFreshEntity(resultEntity);
                    }
                    return true;
                } else {
                    isDestroying = true;
                    sendShakingActionBar(level, centerPos, "YOUR OFFERING IS PATHETIC");
                    return false;
                }
            }

            return false;
        }

        private void abortRitual() {
            for (ItemEntity item : involvedItems) {
                if (item.isAlive()) {
                    item.setNoGravity(false);
                    CompoundTag tag = item.getItem().getTag();
                    if (tag != null) {
                        tag.remove("MacabreRitualID");
                        if (tag.isEmpty()) {
                            item.getItem().setTag(null);
                        }
                    }
                }
            }
        }
    }
}