package com.misanthropy.linggango.linggango_tweaks.features;

import com.misanthropy.linggango.linggango_tweaks.LinggangoTweaks;
import com.misanthropy.linggango.linggango_tweaks.network.handler.NetworkHandler;
import com.misanthropy.linggango.linggango_tweaks.network.macabre.PitTeleportPacket;
import com.mojang.logging.LogUtils;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.AdvancementEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@SuppressWarnings("unused")
public class NBTVaultForMacabre implements INBTSerializable<CompoundTag> {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final ResourceKey<Level> PIT_DIMENSION = ResourceKey.create(Registries.DIMENSION, new ResourceLocation("macabre", "the_pit"));

    private static final Set<ResourceLocation> IGNORED_ITEMS = Set.of(
            new ResourceLocation("enigmaticlegacy", "cursed_ring"),
            new ResourceLocation("covenant_of_the_seven", "virtue_ring"),
            new ResourceLocation("enigmaticaddons", "bless_ring"),
            new ResourceLocation("enigmaticlegacy", "desolation_ring"),
            new ResourceLocation("covenant_of_the_seven", "exaltation_ring")
    );

    private static final boolean CURIOS_LOADED = ModList.get().isLoaded("curios");
    private static Method GET_CURIOS_INVENTORY;
    private static Method GET_EQUIPPED_CURIOS;

    static {
        if (CURIOS_LOADED) {
            try {
                Class<?> CURIOS_API_CLASS = Class.forName("top.theillusivec4.curios.api.CuriosApi");
                GET_CURIOS_INVENTORY = CURIOS_API_CLASS.getMethod("getCuriosInventory", net.minecraft.world.entity.LivingEntity.class);
                Class<?> handlerClass = Class.forName("top.theillusivec4.curios.api.type.capability.ICuriosItemHandler");
                GET_EQUIPPED_CURIOS = handlerClass.getMethod("getEquippedCurios");
            } catch (Exception e) {
                LOGGER.error("Failed to initialize Curios API reflection caches", e);
            }
        }
    }

    @Override
    public CompoundTag serializeNBT() {
        return null;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
    }

    public interface IPitVault {
        CompoundTag getSnapshot();
        void setSnapshot(CompoundTag snapshot);
        boolean hasSnapshot();
    }

    public static class PitVault implements IPitVault {
        private CompoundTag snapshot = new CompoundTag();
        @Override public CompoundTag getSnapshot() { return snapshot; }
        @Override public void setSnapshot(CompoundTag snapshot) { this.snapshot = snapshot; }
        @Override public boolean hasSnapshot() { return snapshot != null && !snapshot.isEmpty(); }
    }

    public static class PitVaultProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {
        public static final Capability<IPitVault> PIT_VAULT = CapabilityManager.get(new CapabilityToken<>() {});
        private IPitVault vault = null;
        private final LazyOptional<IPitVault> optional = LazyOptional.of(this::createVault);

        private IPitVault createVault() {
            if (this.vault == null) this.vault = new PitVault();
            return this.vault;
        }

        @Override
        public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
            return cap == PIT_VAULT ? optional.cast() : LazyOptional.empty();
        }

        @Override
        public CompoundTag serializeNBT() {
            CompoundTag nbt = new CompoundTag();
            nbt.put("PlayerSnapshot", createVault().getSnapshot());
            return nbt;
        }

        @Override
        public void deserializeNBT(CompoundTag nbt) {
            createVault().setSnapshot(nbt.getCompound("PlayerSnapshot"));
        }
    }

    @Mod.EventBusSubscriber(modid = LinggangoTweaks.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ModBusEvents {
        @SubscribeEvent
        public static void registerCaps(RegisterCapabilitiesEvent event) {
            event.register(IPitVault.class);
        }
    }

    @Mod.EventBusSubscriber(modid = LinggangoTweaks.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class ForgeBusEvents {
        @SubscribeEvent
        public static void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
            if (event.getObject() instanceof Player) {
                event.addCapability(new ResourceLocation(LinggangoTweaks.MOD_ID, "pit_vault"), new PitVaultProvider());
            }
        }

        @SubscribeEvent
        public static void onPlayerClone(PlayerEvent.Clone event) {
            event.getOriginal().getCapability(PitVaultProvider.PIT_VAULT).ifPresent(oldVault ->
                    event.getEntity().getCapability(PitVaultProvider.PIT_VAULT).ifPresent(newVault ->
                            newVault.setSnapshot(oldVault.getSnapshot())
                    )
            );
        }

        @SubscribeEvent
        public static void onAdvancementEarned(AdvancementEvent.AdvancementEarnEvent event) {
            if (event.getEntity() instanceof ServerPlayer player) {
                ResourceLocation advId = event.getAdvancement().getId();
                if (advId.getNamespace().equals("macabre") && advId.getPath().equals("ach_1")) {
                    player.getCapability(PitVaultProvider.PIT_VAULT).ifPresent(vault -> {
                        if (!vault.hasSnapshot()) snapshotAndStrip(player, vault);
                    });
                } else if (advId.getNamespace().equals("macabre") && advId.getPath().equals("ach_5")) {
                    checkAndRestore(player);
                }
            }
        }

        @SubscribeEvent
        public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
            ItemStack stack = event.getItemStack();
            ResourceLocation itemKey = net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(stack.getItem());

            if (itemKey != null && itemKey.getNamespace().equals("macabre") && itemKey.getPath().equals("crystalized_blood")) {
                event.setCanceled(true);
                event.setCancellationResult(net.minecraft.world.InteractionResult.SUCCESS);
                return;
            }

            if (stack.hasTag()) {
                CompoundTag tag = stack.getTag();
                if (tag != null && tag.contains("LinggangoPitLoot")) {
                    event.setCanceled(true);

                    if (!event.getLevel().isClientSide && event.getEntity() instanceof ServerPlayer player) {
                        ListTag loot = tag.getList("LinggangoPitLoot", 10);
                        boolean spawnedAny = false;

                        for (int i = 0; i < loot.size(); i++) {
                            ItemStack extracted = ItemStack.of(loot.getCompound(i));
                            if (!extracted.isEmpty()) {
                                player.getInventory().add(extracted);
                                if (!extracted.isEmpty()) {
                                    ItemEntity drop = player.drop(extracted, false);
                                    if (drop != null) drop.setGlowingTag(true);
                                }
                                spawnedAny = true;
                            }
                        }

                        stack.shrink(1);
                        if (spawnedAny) {
                            player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§aMacabre loot unpacked!"));
                            player.level().playSound(null, player.blockPosition(), net.minecraft.sounds.SoundEvents.PLAYER_LEVELUP, net.minecraft.sounds.SoundSource.PLAYERS, 0.5f, 1.0f);
                        }
                    }
                }
            }
        }
    }

    @Mod.EventBusSubscriber(modid = LinggangoTweaks.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
    public static class ClientForgeEvents {
        @SubscribeEvent(receiveCanceled = true)
        public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
            ItemStack stack = event.getItemStack();
            ResourceLocation itemKey = net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(stack.getItem());

            if (itemKey != null && itemKey.getNamespace().equals("macabre") && itemKey.getPath().equals("crystalized_blood")) {
                if (event.getLevel().isClientSide) {
                    net.minecraft.client.Minecraft.getInstance().setScreen(
                            new com.misanthropy.linggango.linggango_tweaks.client.gui.macabre.MacabreConfirmationScreen(
                                    () -> NetworkHandler.CHANNEL.sendToServer(new PitTeleportPacket())
                            )
                    );
                }
            }
        }
    }

    private static boolean isIgnoredItem(ItemStack stack) {
        if (stack.isEmpty()) return false;
        ResourceLocation key = net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(stack.getItem());
        return key != null && IGNORED_ITEMS.contains(key);
    }

    private static void removeIgnoredFromList(CompoundTag tag, String listKey) {
        if (!tag.contains(listKey)) return;
        ListTag list = tag.getList(listKey, 10);
        ListTag newList = new ListTag();
        for (int i = 0; i < list.size(); i++) {
            CompoundTag itemTag = list.getCompound(i);
            ItemStack stack = ItemStack.of(itemTag);
            if (!isIgnoredItem(stack)) {
                newList.add(itemTag);
            }
        }
        tag.put(listKey, newList);
    }

    private static void snapshotAndStrip(ServerPlayer player, IPitVault vault) {
        LOGGER.info("Initiating strip for {}", player.getName().getString());
        try {
            CompoundTag fullNbt = new CompoundTag();
            player.saveWithoutId(fullNbt);

            CompoundTag snapshot = fullNbt.copy();
            String[] toRemove = {"Pos", "Motion", "Rotation", "Dimension", "SpawnX", "SpawnY", "SpawnZ", "SpawnDimension", "SpawnForced", "FallDistance"};
            for (String key : toRemove) snapshot.remove(key);

            if (player.getRespawnPosition() != null) {
                snapshot.putInt("LinggangoSpawnX", player.getRespawnPosition().getX());
                snapshot.putInt("LinggangoSpawnY", player.getRespawnPosition().getY());
                snapshot.putInt("LinggangoSpawnZ", player.getRespawnPosition().getZ());
                snapshot.putString("LinggangoSpawnDim", player.getRespawnDimension().location().toString());
                snapshot.putFloat("LinggangoSpawnAngle", player.getRespawnAngle());
                snapshot.putBoolean("LinggangoSpawnForced", player.isRespawnForced());
            } else {
                snapshot.putBoolean("LinggangoNoSpawn", true);
            }

            removeIgnoredFromList(snapshot, "Inventory");
            removeIgnoredFromList(snapshot, "EnderItems");

            backupAndClearCurios(player, snapshot);
            vault.setSnapshot(snapshot);

            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                if (!isIgnoredItem(player.getInventory().getItem(i))) {
                    player.getInventory().setItem(i, ItemStack.EMPTY);
                }
            }
            for (int i = 0; i < player.getEnderChestInventory().getContainerSize(); i++) {
                if (!isIgnoredItem(player.getEnderChestInventory().getItem(i))) {
                    player.getEnderChestInventory().setItem(i, ItemStack.EMPTY);
                }
            }

            player.experienceLevel = 0;
            player.experienceProgress = 0.0F;
            player.totalExperience = 0;
            player.setScore(0);
            player.removeAllEffects();
            player.setHealth(player.getMaxHealth());
            player.getFoodData().setFoodLevel(20);
            player.getAttributes().load(new ListTag());

            player.setRespawnPosition(player.level().dimension(), player.blockPosition(), player.getYRot(), true, false);

            player.containerMenu.broadcastChanges();
            player.inventoryMenu.broadcastChanges();
            LOGGER.info("Player stripped successfully.");
        } catch (Exception e) {
            LOGGER.error("CRITICAL ERROR: Failed to snapshot and strip player.", e);
        }
    }

    private static void checkAndRestore(ServerPlayer player) {
        player.getCapability(PitVaultProvider.PIT_VAULT).ifPresent(vault -> {
            if (vault.hasSnapshot()) {
                LOGGER.info("Restoring vault snapshot for {}", player.getName().getString());
                try {
                    ItemStack lootPaper = packagePitLoot(player);

                    List<ItemStack> keptItems = new ArrayList<>();
                    for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                        ItemStack stack = player.getInventory().getItem(i);
                        if (isIgnoredItem(stack)) keptItems.add(stack.copy());
                    }
                    for (int i = 0; i < player.getEnderChestInventory().getContainerSize(); i++) {
                        ItemStack stack = player.getEnderChestInventory().getItem(i);
                        if (isIgnoredItem(stack)) keptItems.add(stack.copy());
                    }

                    CompoundTag snapshot = vault.getSnapshot();
                    player.getInventory().load(snapshot.getList("Inventory", 10));
                    player.getEnderChestInventory().fromTag(snapshot.getList("EnderItems", 10));

                    for (ItemStack kept : keptItems) {
                        if (!player.getInventory().add(kept)) {
                            ItemEntity drop = player.drop(kept, false);
                            if (drop != null) drop.setGlowingTag(true);
                        }
                    }

                    if (snapshot.contains("Attributes")) {
                        player.getAttributes().load(snapshot.getList("Attributes", 10));
                    }

                    player.experienceLevel = snapshot.getInt("XpLevel");
                    player.experienceProgress = snapshot.getFloat("XpP");
                    player.totalExperience = snapshot.getInt("XpTotal");
                    player.setScore(snapshot.getInt("Score"));

                    if (snapshot.contains("Health")) player.setHealth(snapshot.getFloat("Health"));
                    if (snapshot.contains("foodLevel")) player.getFoodData().readAdditionalSaveData(snapshot);

                    if (snapshot.getBoolean("LinggangoNoSpawn")) {
                        player.setRespawnPosition(Level.OVERWORLD, null, 0.0F, false, false);
                    } else if (snapshot.contains("LinggangoSpawnX")) {
                        int sx = snapshot.getInt("LinggangoSpawnX");
                        int sy = snapshot.getInt("LinggangoSpawnY");
                        int sz = snapshot.getInt("LinggangoSpawnZ");
                        ResourceKey<Level> dim = ResourceKey.create(Registries.DIMENSION, new ResourceLocation(snapshot.getString("LinggangoSpawnDim")));
                        float angle = snapshot.getFloat("LinggangoSpawnAngle");
                        boolean forced = snapshot.getBoolean("LinggangoSpawnForced");
                        player.setRespawnPosition(dim, new net.minecraft.core.BlockPos(sx, sy, sz), angle, forced, false);
                    }

                    restoreCurios(player, snapshot);

                    vault.setSnapshot(new CompoundTag());
                    LOGGER.info("Snapshot restored and cleared.");

                    if (!lootPaper.isEmpty()) {
                        player.getInventory().add(lootPaper);
                        if (!lootPaper.isEmpty()) {
                            ItemEntity drop = player.drop(lootPaper, false);
                            if (drop != null) drop.setGlowingTag(true);
                        }
                    }

                    player.containerMenu.broadcastChanges();
                    player.inventoryMenu.broadcastChanges();
                } catch (Exception e) {
                    LOGGER.error("CRITICAL ERROR: Failed to restore player data.", e);
                }
            }
        });
    }

    private static ItemStack packagePitLoot(ServerPlayer player) {
        ListTag pitLootList = new ListTag();

        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty() && !isIgnoredItem(stack)) pitLootList.add(stack.save(new CompoundTag()));
        }

        for (int i = 0; i < player.getEnderChestInventory().getContainerSize(); i++) {
            ItemStack stack = player.getEnderChestInventory().getItem(i);
            if (!stack.isEmpty() && !isIgnoredItem(stack)) pitLootList.add(stack.save(new CompoundTag()));
        }

        extractCurrentCuriosToLoot(player, pitLootList);

        if (pitLootList.isEmpty()) return ItemStack.EMPTY;

        ItemStack paper = new ItemStack(Items.PAPER);
        CompoundTag tag = paper.getOrCreateTag();
        tag.put("Enchantments", new ListTag());

        CompoundTag display = new CompoundTag();
        display.putString("Name", "{\"text\":\"Macabre Loot Cache\",\"color\":\"dark_purple\",\"italic\":false}");
        ListTag lore = new ListTag();
        lore.add(StringTag.valueOf("{\"text\":\"Right-click to unpack your hard-earned loot from the Pit.\",\"color\":\"gray\",\"italic\":false}"));
        display.put("Lore", lore);

        tag.put("display", display);
        tag.put("LinggangoPitLoot", pitLootList);
        return paper;
    }

    private static void extractCurrentCuriosToLoot(ServerPlayer player, ListTag pitLootList) {
        if (!CURIOS_LOADED) return;
        try {
            LazyOptional<?> optional = (LazyOptional<?>) GET_CURIOS_INVENTORY.invoke(null, player);

            optional.ifPresent(handler -> {
                try {
                    net.minecraftforge.items.IItemHandlerModifiable inv = (net.minecraftforge.items.IItemHandlerModifiable) GET_EQUIPPED_CURIOS.invoke(handler);

                    for (int i = 0; i < inv.getSlots(); i++) {
                        ItemStack stack = inv.getStackInSlot(i);
                        if (!stack.isEmpty()) {
                            if (isIgnoredItem(stack)) continue;
                            pitLootList.add(stack.save(new CompoundTag()));
                            inv.setStackInSlot(i, ItemStack.EMPTY);
                        }
                    }
                } catch (Exception e) {
                    LOGGER.error("Failed to extract Curios for Loot Cache.", e);
                }
            });
        } catch (Exception e) {
            LOGGER.error("Curios API not found.", e);
        }
    }

    private static void backupAndClearCurios(ServerPlayer player, CompoundTag snapshot) {
        if (!CURIOS_LOADED) return;
        try {
            LazyOptional<?> optional = (LazyOptional<?>) GET_CURIOS_INVENTORY.invoke(null, player);

            optional.ifPresent(handler -> {
                try {
                    net.minecraftforge.items.IItemHandlerModifiable inv = (net.minecraftforge.items.IItemHandlerModifiable) GET_EQUIPPED_CURIOS.invoke(handler);

                    ListTag curiosBackup = new ListTag();
                    for (int i = 0; i < inv.getSlots(); i++) {
                        ItemStack stack = inv.getStackInSlot(i);
                        if (!stack.isEmpty()) {
                            if (isIgnoredItem(stack)) continue;
                            CompoundTag itemTag = new CompoundTag();
                            itemTag.putInt("CurioSlot", i);
                            stack.save(itemTag);
                            curiosBackup.add(itemTag);
                        }
                        inv.setStackInSlot(i, ItemStack.EMPTY);
                    }
                    snapshot.put("LinggangoCuriosBackup", curiosBackup);
                } catch (Exception e) {
                    LOGGER.error("Failed to backup and clear Curios.", e);
                }
            });
        } catch (Exception e) {
            LOGGER.error("Curios API not found.", e);
        }
    }

    private static void restoreCurios(ServerPlayer player, CompoundTag snapshot) {
        if (!CURIOS_LOADED) return;
        if (!snapshot.contains("LinggangoCuriosBackup")) {
            return;
        }

        ListTag curiosBackup = snapshot.getList("LinggangoCuriosBackup", 10);
        try {
            LazyOptional<?> optional = (LazyOptional<?>) GET_CURIOS_INVENTORY.invoke(null, player);

            optional.ifPresent(handler -> {
                try {
                    net.minecraftforge.items.IItemHandlerModifiable inv = (net.minecraftforge.items.IItemHandlerModifiable) GET_EQUIPPED_CURIOS.invoke(handler);

                    for (int i = 0; i < curiosBackup.size(); i++) {
                        CompoundTag itemTag = curiosBackup.getCompound(i);
                        int slot = itemTag.getInt("CurioSlot");
                        ItemStack stack = ItemStack.of(itemTag);
                        inv.setStackInSlot(slot, stack);
                    }
                } catch (Exception e) {
                    LOGGER.error("Failed to inject Curios items.", e);
                }
            });
        } catch (Exception e) {
            LOGGER.error("Curios API error during restore.", e);
        }
    }
}