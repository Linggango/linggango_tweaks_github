package com.misanthropy.linggango.linggango_tweaks.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings("unused")
public class BalancedLootRandomizerModifier extends LootModifier {
    public static final Codec<BalancedLootRandomizerModifier> CODEC = RecordCodecBuilder.create(inst -> codecStart(inst).and(
            Codec.FLOAT.fieldOf("randomization_chance").forGetter(m -> m.randomizationChance)
    ).apply(inst, BalancedLootRandomizerModifier::new));

    private final float randomizationChance;
    private static final Map<String, List<Item>> CATEGORY_POOL = new ConcurrentHashMap<>();
    private static final Set<Item> MODERATE_REDUCTION = ConcurrentHashMap.newKeySet();
    private static final Set<Item> EXTREME_REDUCTION = ConcurrentHashMap.newKeySet();
    private static final Map<Item, Integer> ITEM_SEEN_COUNTS = new ConcurrentHashMap<>();
    private static final AtomicInteger GLOBAL_ROLL_COUNTER = new AtomicInteger(0);
    private static boolean isPoolInitialized = false;

    public BalancedLootRandomizerModifier(LootItemCondition[] conditionsIn, float randomizationChance) {
        super(conditionsIn);
        this.randomizationChance = randomizationChance;
    }

    @Override
    protected @NotNull ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        ResourceLocation lootTableId = context.getQueriedLootTableId();

        if (!lootTableId.getPath().contains("chests/")) {
            return generatedLoot;
        }

        initPoolIfNeeded();
        ObjectArrayList<ItemStack> newLoot = new ObjectArrayList<>();
        Set<Item> localChestItems = new HashSet<>();

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

                        int score = ITEM_SEEN_COUNTS.getOrDefault(candidate, 0);
                        if (score < lowestScore) {
                            lowestScore = score;
                            bestItem = candidate;
                        }
                    }

                    if (bestItem != null) {
                        boolean pass = true;

                        if (MODERATE_REDUCTION.contains(bestItem) && context.getRandom().nextFloat() < 0.90f) {
                            pass = false;
                        } else if (EXTREME_REDUCTION.contains(bestItem) && context.getRandom().nextFloat() < 0.99f) {
                            pass = false;
                        }

                        if (pass) {
                            int count = Math.min(stack.getCount(), bestItem.getMaxStackSize(bestItem.getDefaultInstance()));
                            newLoot.add(new ItemStack(bestItem, count));
                            localChestItems.add(bestItem);

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
                if (item == Items.AIR) continue;

                if (item instanceof SpawnEggItem || item instanceof ForgeSpawnEggItem) {
                    continue;
                }

                if (ItemTierEvaluator.isEquipmentOP(item) || ItemTierEvaluator.hasTooManyBuffs(item)) {
                    continue;
                }

                ResourceLocation id = ForgeRegistries.ITEMS.getKey(item);
                if (id != null) {
                    String namespace = id.getNamespace().toLowerCase();
                    String path = id.getPath().toLowerCase();
                    String descId = item.getDescriptionId().toLowerCase();

                    if (namespace.equals("adamsarsplus") || namespace.equals("lethality") || namespace.equals("cataclysm")) {
                        continue;
                    }

                    if (path.contains("disabled") || path.contains("dummy") || path.contains("placeholder") || path.contains("api") || path.contains("creative") || path.contains("spawner") || path.contains("nuke") || descId.contains("api") || descId.contains("creative")) {
                        continue;
                    }

                    if (namespace.equals("covenant_of_the_seven")) {
                        EXTREME_REDUCTION.add(item);
                    } else if (namespace.equals("terramity") && (path.contains("daemonium") || path.contains("dimlite") || path.contains("gaianite") || path.contains("iridium") || path.contains("profaned"))) {
                        EXTREME_REDUCTION.add(item);
                    } else if (namespace.equals("storagedrawers") || namespace.contains("mcw") || namespace.equals("mekanism") || namespace.equals("industrial_foregoing") || namespace.equals("mob_grinding_utils") || namespace.equals("modularrouters") || namespace.equals("sophisticatedbackpacks")) {
                        MODERATE_REDUCTION.add(item);
                    } else if (path.contains("seed") || path.contains("flower") || (item instanceof BlockItem && ((BlockItem)item).getBlock() instanceof FlowerBlock)) {
                        MODERATE_REDUCTION.add(item);
                    }
                }

                String key = getCategoryKey(item);
                CATEGORY_POOL.computeIfAbsent(key, k -> new CopyOnWriteArrayList<>()).add(item);
            }
            isPoolInitialized = true;
        }
    }

    private static String getCategoryKey(Item item) {
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
}