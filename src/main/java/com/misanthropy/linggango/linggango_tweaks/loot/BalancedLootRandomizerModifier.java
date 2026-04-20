package com.misanthropy.linggango.linggango_tweaks.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import org.jspecify.annotations.NonNull;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class BalancedLootRandomizerModifier extends LootModifier {
    public static final Codec<BalancedLootRandomizerModifier> CODEC = RecordCodecBuilder.create(inst -> codecStart(inst).and(
            Codec.FLOAT.fieldOf("randomization_chance").forGetter(m -> m.randomizationChance)
    ).apply(inst, BalancedLootRandomizerModifier::new));

    private final float randomizationChance;
    private static final Map<String, List<Item>> CATEGORY_POOL = new ConcurrentHashMap<>();
    private static final Set<Item> SLIGHT_REDUCTION = ConcurrentHashMap.newKeySet();
    private static final Set<Item> MID_REDUCTION = ConcurrentHashMap.newKeySet();
    private static final Set<Item> HIGH_REDUCTION = ConcurrentHashMap.newKeySet();
    private static final Map<Item, Integer> ITEM_SEEN_COUNTS = new ConcurrentHashMap<>();
    private static final Set<Item> NETHER_ONLY_ITEMS = ConcurrentHashMap.newKeySet();
    private static final Set<Item> DEEP_DARK_ONLY_ITEMS = ConcurrentHashMap.newKeySet();
    public static final Set<Item> BLACKLISTED_ITEMS = ConcurrentHashMap.newKeySet();
    private static final AtomicInteger GLOBAL_ROLL_COUNTER = new AtomicInteger(0);
    private static boolean isPoolInitialized = false;

    public BalancedLootRandomizerModifier(LootItemCondition @NonNull [] conditionsIn, float randomizationChance) {
        super(conditionsIn);
        this.randomizationChance = randomizationChance;
    }

    @Override
    protected @NonNull ObjectArrayList<ItemStack> doApply(@NonNull ObjectArrayList<ItemStack> generatedLoot, @NonNull LootContext context) {
        ResourceLocation lootTableId = context.getQueriedLootTableId();

        if (!lootTableId.getPath().contains("chests/")) {
            return generatedLoot;
        }

        initPoolIfNeeded();
        ObjectArrayList<ItemStack> newLoot = new ObjectArrayList<>();
        Set<Item> localChestItems = new HashSet<>();
        Set<EquipmentSlot> localChestEquipment = new HashSet<>();

        for (ItemStack stack : generatedLoot) {
            if (context.getRandom().nextFloat() < this.randomizationChance) {
                Item originalItem = stack.getItem();
                String categoryKey = getCategoryKey(originalItem);
                List<Item> pool = CATEGORY_POOL.getOrDefault(categoryKey, new CopyOnWriteArrayList<>());

                if (!pool.isEmpty()) {
                    Item bestItem = null;
                    int lowestScore = Integer.MAX_VALUE;

                    for (int attempt = 0; attempt < 8; attempt++) {
                        Item candidate = pool.get(context.getRandom().nextInt(pool.size()));

                        if (localChestItems.contains(candidate)) {
                            continue;
                        }

                        if (candidate instanceof ArmorItem armor) {
                            if (localChestEquipment.contains(armor.getEquipmentSlot())) continue;
                        } else if (candidate instanceof SwordItem || candidate instanceof DiggerItem || candidate instanceof TridentItem || candidate instanceof BowItem || candidate instanceof CrossbowItem) {
                            if (localChestEquipment.contains(EquipmentSlot.MAINHAND)) continue;
                        }

                        ResourceLocation id = ForgeRegistries.ITEMS.getKey(candidate);
                        if (id != null && id.getNamespace().equals("twilightforest")) {
                            ResourceLocation dimId = context.getLevel().dimension().location();
                            if (dimId.equals(Level.OVERWORLD.location()) || dimId.equals(Level.NETHER.location()) || dimId.equals(Level.END.location())) {
                                continue;
                            }
                        }

                        if (NETHER_ONLY_ITEMS.contains(candidate)) {
                            ResourceLocation dimId = context.getLevel().dimension().location();
                            if (!dimId.equals(Level.NETHER.location())) continue;
                        }

                        if (DEEP_DARK_ONLY_ITEMS.contains(candidate)) {
                            Vec3 origin = context.getParamOrNull(LootContextParams.ORIGIN);
                            if (origin != null) {
                                Holder<Biome> biome = context.getLevel().getBiome(BlockPos.containing(origin));
                                if (!biome.unwrapKey().map(key -> key.location().toString().equals("minecraft:deep_dark")).orElse(false)) {
                                    continue;
                                }
                            } else {
                                continue;
                            }
                        }

                        int score = ITEM_SEEN_COUNTS.getOrDefault(candidate, 0);
                        if (score < lowestScore) {
                            lowestScore = score;
                            bestItem = candidate;
                        }
                    }

                    if (bestItem != null) {
                        boolean pass = true;

                        if (SLIGHT_REDUCTION.contains(bestItem) && context.getRandom().nextFloat() < 0.90f) {
                            pass = false;
                        } else if (MID_REDUCTION.contains(bestItem) && context.getRandom().nextFloat() < 0.95f) {
                            pass = false;
                        } else if (HIGH_REDUCTION.contains(bestItem) && context.getRandom().nextFloat() < 0.99f) {
                            pass = false;
                        }

                        if (pass) {
                            int count = Math.min(stack.getCount(), bestItem.getMaxStackSize(bestItem.getDefaultInstance()));
                            newLoot.add(new ItemStack(bestItem, count));
                            localChestItems.add(bestItem);

                            if (bestItem instanceof ArmorItem armor) {
                                localChestEquipment.add(armor.getEquipmentSlot());
                            } else if (bestItem instanceof SwordItem || bestItem instanceof DiggerItem || bestItem instanceof TridentItem || bestItem instanceof BowItem || bestItem instanceof CrossbowItem) {
                                localChestEquipment.add(EquipmentSlot.MAINHAND);
                            }

                            ITEM_SEEN_COUNTS.put(bestItem, ITEM_SEEN_COUNTS.getOrDefault(bestItem, 0) + 10);

                            if (GLOBAL_ROLL_COUNTER.incrementAndGet() % 200 == 0) {
                                decaySeenCounts();
                            }
                        } else {
                            newLoot.add(stack);
                            localChestItems.add(stack.getItem());
                        }
                    } else {
                        newLoot.add(stack);
                        localChestItems.add(stack.getItem());
                    }
                } else {
                    newLoot.add(stack);
                    localChestItems.add(stack.getItem());
                }
            } else {
                newLoot.add(stack);
                localChestItems.add(stack.getItem());
            }
        }
        return newLoot;
    }

    private static void decaySeenCounts() {
        ITEM_SEEN_COUNTS.replaceAll((item, count) -> count / 2);
        ITEM_SEEN_COUNTS.entrySet().removeIf(entry -> entry.getValue() <= 0);
    }

    private static void initPoolIfNeeded() {
        if (!isPoolInitialized) {
            for (Item item : ForgeRegistries.ITEMS) {
                ResourceLocation id = ForgeRegistries.ITEMS.getKey(item);
                if (id == null) {
                    continue;
                }

                if (!isItemValid(item, id)) {
                    if (item != Items.AIR) {
                        BLACKLISTED_ITEMS.add(item);
                    }
                    continue;
                }

                String namespace = id.getNamespace().toLowerCase();
                String path = id.getPath().toLowerCase();

                if (namespace.equals("celestial_core") && path.equals("fire_essence")) {
                    NETHER_ONLY_ITEMS.add(item);
                } else if ((namespace.equals("celestial_core") && path.equals("warden_sclerite")) || (namespace.equals("l2complements") && path.equals("warden_bone_shard"))) {
                    DEEP_DARK_ONLY_ITEMS.add(item);
                }

                if (namespace.equals("terramity") && (path.contains("daemonium") || path.contains("dimlite") || path.contains("gaianite") || path.contains("iridium") || path.contains("profaned"))) {
                    HIGH_REDUCTION.add(item);
                } else if (namespace.equals("ars_nouveau") || namespace.equals("biomancy") || namespace.equals("sophisticatedbackpacks") || namespace.equals("sophisticatedstorage") || namespace.equals("embers")) {
                    MID_REDUCTION.add(item);
                } else if (namespace.equals("storagedrawers") || namespace.contains("mcw") || namespace.equals("mekanism") || namespace.equals("industrial_foregoing") || namespace.equals("mob_grinding_utils") || namespace.equals("modularrouters")) {
                    SLIGHT_REDUCTION.add(item);
                } else if (path.contains("seed") || path.contains("flower") || (item instanceof BlockItem blockItem && blockItem.getBlock() instanceof FlowerBlock)) {
                    SLIGHT_REDUCTION.add(item);
                } else {
                    boolean isFlowerOrSeed = item.getDefaultInstance().getTags().anyMatch(tag -> {
                        String tagName = tag.location().toString();
                        return tagName.equals("minecraft:flowers") || tagName.equals("forge:seeds");
                    });
                    if (isFlowerOrSeed) {
                        SLIGHT_REDUCTION.add(item);
                    }
                }

                String key = getCategoryKey(item);
                CATEGORY_POOL.computeIfAbsent(key, k -> new CopyOnWriteArrayList<>()).add(item);
            }
            isPoolInitialized = true;
        }
    }

    private static boolean isItemValid(Item item, @NonNull ResourceLocation id) {
        if (item == Items.AIR) return false;
        if (item instanceof SpawnEggItem || item instanceof ForgeSpawnEggItem || item instanceof MapItem) return false;

        String namespace = id.getNamespace().toLowerCase();
        String path = id.getPath().toLowerCase();
        String fullId = id.toString().toLowerCase();

        if (fullId.equals("terramity:flintlock_pistol") || fullId.equals("terramity:basic_pistol") || fullId.equals("terramity:basic_rifle") || fullId.equals("goety:empty_focus") || fullId.equals("goety:soul_light_focus") || path.contains("cognitive")) {
            return true;
        }

        if (ItemTierEvaluator.isEquipmentOP(item) || ItemTierEvaluator.hasTooManyBuffs(item)) return false;

        if (namespace.equals("l2hostility")) {
            if (!path.equals("bottle_of_curse") && !path.equals("bottle_of_sanity") && !path.equals("hostility_orb")) return false;
        }

        if (namespace.equals("l2complements")) {
            if (!path.contains("totemic") && !path.equals("soul_fire_charge") && !path.equals("strong_fire_charge") && !path.equals("black_fire_charge") && !path.equals("poseidite_nugget") && !path.equals("shulkerate_nugget") && !path.equals("warden_bone_shard")) return false;
        }

        if (namespace.equals("celestial_core")) {
            if (path.equals("guardian_ocean_ingot") || path.equals("ocean_essence") || path.equals("earth_core") || path.equals("pure_nether_star") || path.equals("death_essence") || path.equals("midnight_fragment") || path.equals("light_fragment")) return false;
        }

        if (namespace.equals("skyarena") || namespace.equals("kubejs") || namespace.equals("fdbosses") || namespace.equals("goety_revelation") || namespace.equals("mekaweapons") || namespace.equals("evolvedmekanism") || namespace.equals("armageddon_mod") || namespace.equals("enigmaticdice") || namespace.equals("ae2wtlib") || namespace.equals("megacells") || namespace.equals("animus") || namespace.equals("aeinfinitybooster") || namespace.equals("aeinsertexportcard") || namespace.equals("l2backpack") || namespace.equals("betterend") || namespace.equals("bosses_of_mass_destruction") || namespace.equals("lethality") || namespace.equals("brutality") || namespace.equals("cataclysm") || namespace.equals("celestisynth") || namespace.equals("companions") || namespace.equals("ftbquests") || namespace.equals("itemfilter") || namespace.equals("modularrouters") || namespace.equals("darkdoppelganger") || namespace.equals("easy_villagers") || namespace.equals("eeeabsmobs") || namespace.equals("elemental_synergies") || namespace.equals("adamsarsplus") || namespace.equals("orbital_railgun") || namespace.equals("covenant_of_the_seven")) {
            return false;
        }

        if (fullId.equals("species:spectralibur")) return false;

        if (path.contains("disabled") || path.contains("dummy") || path.contains("placeholder") || path.contains("api") || path.contains("creative") || path.contains("spawner") || path.contains("nuke") || path.contains("cell") || path.contains("panel") || path.contains("celestial") || path.contains("dimensional") || path.contains("bedrock") || path.contains("dragon") || path.contains("nyxium") || path.contains("exodium") || path.contains("hellspec") || path.contains("profanum") || path.contains("reverium") || path.contains("halo") || path.contains("unholy") || path.contains("bow") || path.contains("facade") || path.contains("terminal") || path.contains("bucket") || path.contains("factory") || path.contains("bin") || path.contains("meka") || path.contains("flamethrower") || path.contains("looting_charm") || path.contains("looting charm") || path.contains("mending_aura") || path.contains("mending aura") || path.contains("barrier") || path.contains("flux") || path.contains("dynamo")) {
            return false;
        }

        if (path.startsWith("item.")) return false;

        String descId = item.getDescriptionId().toLowerCase();
        if (descId.contains("api") || descId.contains("creative") || descId.contains("looting charm") || descId.contains("mending aura") || descId.contains("map")) return false;

        if (fullId.equals("enigmaticaddons:bless_ring") || fullId.equals("naturesaura:end_city_finder") || fullId.equals("goety:unholy_hat") || fullId.equals("ae2:annihilation_core") || fullId.equals("ae2:formation_core") || fullId.equals("ae2:engineering_processor") || fullId.equals("ae2:singularity") || fullId.equals("ae2:quantum_entangled_singularity") || fullId.equals("ae2:chest") || fullId.equals("ae2:interface") || fullId.equals("mekanism:osmium_compressor") || fullId.equals("mekanism:enrichment_chamber") || fullId.equals("mekanism:teleporter") || fullId.equals("mekanism:energized_smelter") || fullId.equals("mekanism:purification_chamber") || fullId.equals("ars_nouveau:apprentice_spell_book") || fullId.equals("ars_nouveau:archmage_spell_book") || fullId.equals("ars_nouveau:novice_spell_book") || fullId.equals("tomeofblood:archmage_tome_of_blood") || fullId.equals("tomeofblood:apprentice_tome_of_blood") || fullId.equals("tomeofblood:novice_tome_of_blood")) {
            return false;
        }

        if (namespace.equals("ae2")) {
            if (path.contains("4k") || path.contains("16k") || path.contains("64k") || path.contains("256k") || path.contains("card")) return false;
        }

        if (namespace.equals("mekanism")) {
            if (item instanceof DiggerItem || item instanceof SwordItem || path.contains("redstone")) return false;
        }

        if (namespace.equals("enigmaticlegacy")) {
            Rarity rarity = item.getDefaultInstance().getRarity();
            if (rarity == Rarity.RARE || rarity == Rarity.EPIC) return false;
        }

        boolean hasBannedTag = item.getDefaultInstance().getTags().anyMatch(tag -> {
            String tagName = tag.location().toString();
            return tagName.equals("forge:guns") || tagName.equals("goety:focuses") || tagName.equals("goety:wands") || tagName.equals("goety:robes") || tagName.equals("goety:crowns") || tagName.equals("ae2:covered_dense_cable") || tagName.equals("ae2:smart_dense_cable") || tagName.equals("forge:coins") || tagName.equals("forge:plates") || tagName.equals("forge:gears") || tagName.equals("thermal:machines") || (tagName.contains("curios") && namespace.equals("blood_magic"));
        });

        return !hasBannedTag;
    }

    private static @NonNull String getCategoryKey(Item item) {
        Rarity rarity = ItemTierEvaluator.evaluateRarity(item);
        String category = "MISC";

        if (item instanceof BlockItem) {
            category = "BLOCK";
        } else if (item instanceof SwordItem || item instanceof BowItem || item instanceof CrossbowItem || item instanceof TridentItem) {
            category = "WEAPON";
        } else if (item instanceof DiggerItem) {
            category = "TOOL";
        } else if (item instanceof ArmorItem) {
            category = "ARMOR";
        } else if (item.isEdible()) {
            category = "FOOD";
        } else if (item.getMaxStackSize(item.getDefaultInstance()) == 1) {
            category = "UNSTACKABLE_MISC";
        }

        return category + "_" + rarity.name();
    }

    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }

    @Mod.EventBusSubscriber(modid = "linggango_tweaks", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class ClientTooltipHandler {
        @SubscribeEvent
        public static void onTooltip(@NonNull ItemTooltipEvent event) {
            if (event.getFlags().isAdvanced()) {
                Item item = event.getItemStack().getItem();
                if (BalancedLootRandomizerModifier.BLACKLISTED_ITEMS.contains(item)) {
                    event.getToolTip().add(Component.literal("This item is blacklisted from loot tables").withStyle(ChatFormatting.GOLD));
                }
            }
        }
    }
}