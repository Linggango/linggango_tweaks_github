package com.misanthropy.linggango.linggango_tweaks.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.advancements.Advancement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import net.minecraftforge.registries.ForgeRegistries;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Objects;

public class BalancedLootRandomizerModifier extends LootModifier {
    public static final Codec<BalancedLootRandomizerModifier> CODEC = RecordCodecBuilder.create(inst -> codecStart(inst).apply(inst, BalancedLootRandomizerModifier::new));

    // UNIVERSAL - always appears
    public static final List<String> UNIVERSAL_COMMON_LOOT_IDS = List.of("modid:itemid");
    public static final List<String> UNIVERSAL_UNCOMMON_LOOT_IDS = List.of("modid:itemid");
    public static final List<String> UNIVERSAL_RARE_LOOT_IDS = List.of("modid:itemid");
    public static final List<String> UNIVERSAL_EPIC_LOOT_IDS = List.of("modid:itemid");
    public static final List<String> UNIVERSAL_LEGENDARY_LOOT_IDS = List.of("modid:itemid");

    // POST-IRON
    public static final List<String> POST_IRON_COMMON_LOOT_IDS = List.of("modid:itemid");
    public static final List<String> POST_IRON_UNCOMMON_LOOT_IDS = List.of("modid:itemid");
    public static final List<String> POST_IRON_RARE_LOOT_IDS = List.of("modid:itemid");
    public static final List<String> POST_IRON_EPIC_LOOT_IDS = List.of("modid:itemid");
    public static final List<String> POST_IRON_LEGENDARY_LOOT_IDS = List.of("modid:itemid");

    // POST-GOLD
    public static final List<String> POST_GOLD_COMMON_LOOT_IDS = List.of("modid:itemid");
    public static final List<String> POST_GOLD_UNCOMMON_LOOT_IDS = List.of("modid:itemid");
    public static final List<String> POST_GOLD_RARE_LOOT_IDS = List.of("modid:itemid");
    public static final List<String> POST_GOLD_EPIC_LOOT_IDS = List.of("modid:itemid");
    public static final List<String> POST_GOLD_LEGENDARY_LOOT_IDS = List.of("modid:itemid");

    // POST-EMERALD
    public static final List<String> POST_EMERALD_COMMON_LOOT_IDS = List.of("modid:itemid");
    public static final List<String> POST_EMERALD_UNCOMMON_LOOT_IDS = List.of("modid:itemid");
    public static final List<String> POST_EMERALD_RARE_LOOT_IDS = List.of("modid:itemid");
    public static final List<String> POST_EMERALD_EPIC_LOOT_IDS = List.of("modid:itemid");
    public static final List<String> POST_EMERALD_LEGENDARY_LOOT_IDS = List.of("modid:itemid");

    // POST-DIAMOND
    public static final List<String> POST_DIAMOND_COMMON_LOOT_IDS = List.of("modid:itemid");
    public static final List<String> POST_DIAMOND_UNCOMMON_LOOT_IDS = List.of("modid:itemid");
    public static final List<String> POST_DIAMOND_RARE_LOOT_IDS = List.of("modid:itemid");
    public static final List<String> POST_DIAMOND_EPIC_LOOT_IDS = List.of("modid:itemid");
    public static final List<String> POST_DIAMOND_LEGENDARY_LOOT_IDS = List.of("modid:itemid");

    // MID GAME - requires you to kill ender dragon
    public static final List<String> MID_COMMON_LOOT_IDS = List.of("modid:itemid");
    public static final List<String> MID_UNCOMMON_LOOT_IDS = List.of("modid:itemid");
    public static final List<String> MID_RARE_LOOT_IDS = List.of("modid:itemid");
    public static final List<String> MID_EPIC_LOOT_IDS = List.of("modid:itemid");
    public static final List<String> MID_LEGENDARY_LOOT_IDS = List.of("modid:itemid");

    // POST-ELVENITE
    public static final List<String> POST_ELVENITE_COMMON_LOOT_IDS = List.of("modid:itemid");
    public static final List<String> POST_ELVENITE_UNCOMMON_LOOT_IDS = List.of("modid:itemid");
    public static final List<String> POST_ELVENITE_RARE_LOOT_IDS = List.of("modid:itemid");
    public static final List<String> POST_ELVENITE_EPIC_LOOT_IDS = List.of("modid:itemid");
    public static final List<String> POST_ELVENITE_LEGENDARY_LOOT_IDS = List.of("modid:itemid");

    // POST-VOIDERITE
    public static final List<String> POST_VOIDERITE_COMMON_LOOT_IDS = List.of("modid:itemid");
    public static final List<String> POST_VOIDERITE_UNCOMMON_LOOT_IDS = List.of("modid:itemid");
    public static final List<String> POST_VOIDERITE_RARE_LOOT_IDS = List.of("modid:itemid");
    public static final List<String> POST_VOIDERITE_EPIC_LOOT_IDS = List.of("modid:itemid");
    public static final List<String> POST_VOIDERITE_LEGENDARY_LOOT_IDS = List.of("modid:itemid");

    // POST-DOOM BRINGER
    public static final List<String> POST_DOOM_COMMON_LOOT_IDS = List.of("modid:itemid");
    public static final List<String> POST_DOOM_UNCOMMON_LOOT_IDS = List.of("modid:itemid");
    public static final List<String> POST_DOOM_RARE_LOOT_IDS = List.of("modid:itemid");
    public static final List<String> POST_DOOM_EPIC_LOOT_IDS = List.of("modid:itemid");
    public static final List<String> POST_DOOM_LEGENDARY_LOOT_IDS = List.of("modid:itemid");

    // POST-CALAMITIES
    public static final List<String> POST_CALAMITIES_COMMON_LOOT_IDS = List.of("modid:itemid");
    public static final List<String> POST_CALAMITIES_UNCOMMON_LOOT_IDS = List.of("modid:itemid");
    public static final List<String> POST_CALAMITIES_RARE_LOOT_IDS = List.of("modid:itemid");
    public static final List<String> POST_CALAMITIES_EPIC_LOOT_IDS = List.of("modid:itemid");
    public static final List<String> POST_CALAMITIES_LEGENDARY_LOOT_IDS = List.of("modid:itemid");

    // POST-BLOOD
    public static final List<String> POST_BLOOD_COMMON_LOOT_IDS = List.of("modid:itemid");
    public static final List<String> POST_BLOOD_UNCOMMON_LOOT_IDS = List.of("modid:itemid");
    public static final List<String> POST_BLOOD_RARE_LOOT_IDS = List.of("modid:itemid");
    public static final List<String> POST_BLOOD_EPIC_LOOT_IDS = List.of("modid:itemid");
    public static final List<String> POST_BLOOD_LEGENDARY_LOOT_IDS = List.of("modid:itemid");

    // POST-SUN
    public static final List<String> POST_SUN_COMMON_LOOT_IDS = List.of("modid:itemid");
    public static final List<String> POST_SUN_UNCOMMON_LOOT_IDS = List.of("modid:itemid");
    public static final List<String> POST_SUN_RARE_LOOT_IDS = List.of("modid:itemid");
    public static final List<String> POST_SUN_EPIC_LOOT_IDS = List.of("modid:itemid");
    public static final List<String> POST_SUN_LEGENDARY_LOOT_IDS = List.of("modid:itemid");

    // POST-NYXARIS
    public static final List<String> POST_NYXARIS_COMMON_LOOT_IDS = List.of("modid:itemid");
    public static final List<String> POST_NYXARIS_UNCOMMON_LOOT_IDS = List.of("modid:itemid");
    public static final List<String> POST_NYXARIS_RARE_LOOT_IDS = List.of("modid:itemid");
    public static final List<String> POST_NYXARIS_EPIC_LOOT_IDS = List.of("modid:itemid");
    public static final List<String> POST_NYXARIS_LEGENDARY_LOOT_IDS = List.of("modid:itemid");

    private static final ObjectArrayList<Item> UNIVERSAL_COMMON_LOOT = new ObjectArrayList<>();
    private static final ObjectArrayList<Item> UNIVERSAL_UNCOMMON_LOOT = new ObjectArrayList<>();
    private static final ObjectArrayList<Item> UNIVERSAL_RARE_LOOT = new ObjectArrayList<>();
    private static final ObjectArrayList<Item> UNIVERSAL_EPIC_LOOT = new ObjectArrayList<>();
    private static final ObjectArrayList<Item> UNIVERSAL_LEGENDARY_LOOT = new ObjectArrayList<>();

    private static class Stage {
        ResourceLocation advId;
        List<String> cIds, uIds, rIds, eIds, lIds;
        ObjectArrayList<Item> common = new ObjectArrayList<>();
        ObjectArrayList<Item> uncommon = new ObjectArrayList<>();
        ObjectArrayList<Item> rare = new ObjectArrayList<>();
        ObjectArrayList<Item> epic = new ObjectArrayList<>();
        ObjectArrayList<Item> legendary = new ObjectArrayList<>();

        Stage(String advId, List<String> c, List<String> u, List<String> r, List<String> e, List<String> l) {
            this.advId = advId == null ? null : new ResourceLocation(advId);
            this.cIds = c; this.uIds = u; this.rIds = r; this.eIds = e; this.lIds = l;
        }

        void load() {
            loadList(cIds, common);
            loadList(uIds, uncommon);
            loadList(rIds, rare);
            loadList(eIds, epic);
            loadList(lIds, legendary);
        }
    }

    private static Stage[] STAGES;
    private static boolean poolsLoaded = false;

    public BalancedLootRandomizerModifier(LootItemCondition @NonNull [] conditionsIn) {
        super(conditionsIn);
    }

    private static void loadList(List<String> ids, ObjectArrayList<Item> pool) {
        for (String id : ids) {
            Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(id));
            if (item != null) pool.add(item);
        }
    }

    private void loadPools() {
        if (poolsLoaded) return;

        loadList(UNIVERSAL_COMMON_LOOT_IDS, UNIVERSAL_COMMON_LOOT);
        loadList(UNIVERSAL_UNCOMMON_LOOT_IDS, UNIVERSAL_UNCOMMON_LOOT);
        loadList(UNIVERSAL_RARE_LOOT_IDS, UNIVERSAL_RARE_LOOT);
        loadList(UNIVERSAL_EPIC_LOOT_IDS, UNIVERSAL_EPIC_LOOT);
        loadList(UNIVERSAL_LEGENDARY_LOOT_IDS, UNIVERSAL_LEGENDARY_LOOT);

        STAGES = new Stage[] {
                new Stage(null, POST_IRON_COMMON_LOOT_IDS, POST_IRON_UNCOMMON_LOOT_IDS, POST_IRON_RARE_LOOT_IDS, POST_IRON_EPIC_LOOT_IDS, POST_IRON_LEGENDARY_LOOT_IDS),
                new Stage("armageddon_mod:the_gold_keeper", POST_GOLD_COMMON_LOOT_IDS, POST_GOLD_UNCOMMON_LOOT_IDS, POST_GOLD_RARE_LOOT_IDS, POST_GOLD_EPIC_LOOT_IDS, POST_GOLD_LEGENDARY_LOOT_IDS),
                new Stage("armageddon_mod:the_emerald_keeper", POST_EMERALD_COMMON_LOOT_IDS, POST_EMERALD_UNCOMMON_LOOT_IDS, POST_EMERALD_RARE_LOOT_IDS, POST_EMERALD_EPIC_LOOT_IDS, POST_EMERALD_LEGENDARY_LOOT_IDS),
                new Stage("armageddon_mod:the_diamond_keeper", POST_DIAMOND_COMMON_LOOT_IDS, POST_DIAMOND_UNCOMMON_LOOT_IDS, POST_DIAMOND_RARE_LOOT_IDS, POST_DIAMOND_EPIC_LOOT_IDS, POST_DIAMOND_LEGENDARY_LOOT_IDS),
                new Stage("minecraft:end/kill_dragon", MID_COMMON_LOOT_IDS, MID_UNCOMMON_LOOT_IDS, MID_RARE_LOOT_IDS, MID_EPIC_LOOT_IDS, MID_LEGENDARY_LOOT_IDS),
                new Stage("armageddon_mod:the_elvenite_keeper", POST_ELVENITE_COMMON_LOOT_IDS, POST_ELVENITE_UNCOMMON_LOOT_IDS, POST_ELVENITE_RARE_LOOT_IDS, POST_ELVENITE_EPIC_LOOT_IDS, POST_ELVENITE_LEGENDARY_LOOT_IDS),
                new Stage("armageddon_mod:the_voiderite_keeper", POST_VOIDERITE_COMMON_LOOT_IDS, POST_VOIDERITE_UNCOMMON_LOOT_IDS, POST_VOIDERITE_RARE_LOOT_IDS, POST_VOIDERITE_EPIC_LOOT_IDS, POST_VOIDERITE_LEGENDARY_LOOT_IDS),
                new Stage("armageddon_mod:bringer_od_doom_advancement", POST_DOOM_COMMON_LOOT_IDS, POST_DOOM_UNCOMMON_LOOT_IDS, POST_DOOM_RARE_LOOT_IDS, POST_DOOM_EPIC_LOOT_IDS, POST_DOOM_LEGENDARY_LOOT_IDS),
                new Stage("armageddon_mod:the_calamities_death", POST_CALAMITIES_COMMON_LOOT_IDS, POST_CALAMITIES_UNCOMMON_LOOT_IDS, POST_CALAMITIES_RARE_LOOT_IDS, POST_CALAMITIES_EPIC_LOOT_IDS, POST_CALAMITIES_LEGENDARY_LOOT_IDS),
                new Stage("armageddon_mod:the_blood_keeper", POST_BLOOD_COMMON_LOOT_IDS, POST_BLOOD_UNCOMMON_LOOT_IDS, POST_BLOOD_RARE_LOOT_IDS, POST_BLOOD_EPIC_LOOT_IDS, POST_BLOOD_LEGENDARY_LOOT_IDS),
                new Stage("armageddon_mod:the_sun_keeper", POST_SUN_COMMON_LOOT_IDS, POST_SUN_UNCOMMON_LOOT_IDS, POST_SUN_RARE_LOOT_IDS, POST_SUN_EPIC_LOOT_IDS, POST_SUN_LEGENDARY_LOOT_IDS),
                new Stage("armageddon_mod:the_shadow_keeper", POST_NYXARIS_COMMON_LOOT_IDS, POST_NYXARIS_UNCOMMON_LOOT_IDS, POST_NYXARIS_RARE_LOOT_IDS, POST_NYXARIS_EPIC_LOOT_IDS, POST_NYXARIS_LEGENDARY_LOOT_IDS)
        };

        for (Stage stage : STAGES) {
            stage.load();
        }

        poolsLoaded = true;
    }

    @Override
    protected @NonNull ObjectArrayList<ItemStack> doApply(@NonNull ObjectArrayList<ItemStack> generatedLoot, @NonNull LootContext context) {
        ResourceLocation lootTableId = context.getQueriedLootTableId();

        if (!lootTableId.getPath().contains("chests/")) {
            return generatedLoot;
        }

        loadPools();

        int highestStage = 0;

        if (context.hasParam(LootContextParams.THIS_ENTITY)) {
            Entity entity = context.getParamOrNull(LootContextParams.THIS_ENTITY);

            if (entity instanceof ServerPlayer player) {
                // Loop backwards to find the highest unlocked stage
                for (int i = STAGES.length - 1; i >= 1; i--) {
                    Advancement adv = Objects.requireNonNull(player.getServer()).getAdvancements().getAdvancement(STAGES[i].advId);
                    if (adv != null && player.getAdvancements().getOrStartProgress(adv).isDone()) {
                        highestStage = i;
                        break;
                    }
                }
            }
        }

        boolean[] tierSpawned = new boolean[2];
        for (int i = highestStage; i >= 0; i--) {
            float divisor = (i == highestStage) ? 1.0f : (highestStage - i + 2.0f);
            float chanceMultiplier = 1.0f / divisor;

            rollForStage(generatedLoot, context, STAGES[i].common, STAGES[i].uncommon, STAGES[i].rare, STAGES[i].epic, STAGES[i].legendary, chanceMultiplier, tierSpawned);
        }

        float universalChance = 0.30f;
        if (tierSpawned[1]) {
            universalChance = 0.10f;
        } else if (tierSpawned[0]) {
            universalChance = 0.20f;
        }

        rollForStage(generatedLoot, context, UNIVERSAL_COMMON_LOOT, UNIVERSAL_UNCOMMON_LOOT, UNIVERSAL_RARE_LOOT, UNIVERSAL_EPIC_LOOT, UNIVERSAL_LEGENDARY_LOOT, universalChance, tierSpawned);

        return generatedLoot;
    }

    private void rollForStage(ObjectArrayList<ItemStack> generatedLoot, LootContext context,
                              ObjectArrayList<Item> commonPool, ObjectArrayList<Item> uncommonPool, ObjectArrayList<Item> rarePool, ObjectArrayList<Item> epicPool, ObjectArrayList<Item> legendaryPool,
                              float chanceMultiplier, boolean[] tierSpawned) {

        RandomSource random = context.getRandom();
        float reductionFactor = 1.0f;
        float highTierRoll = random.nextFloat();

        float legBound = 0.01f * chanceMultiplier;
        float epicBound = 0.04f * chanceMultiplier;
        float rareBound = 0.19f * chanceMultiplier;

        if (highTierRoll < legBound && !legendaryPool.isEmpty()) {
            Item item = legendaryPool.get(random.nextInt(legendaryPool.size()));
            generatedLoot.add(new ItemStack(item, 1));
            reductionFactor = 5.0f;
            tierSpawned[1] = true;
        }
        else if (highTierRoll < epicBound && !epicPool.isEmpty()) {
            Item item = epicPool.get(random.nextInt(epicPool.size()));
            generatedLoot.add(new ItemStack(item, 1));
            reductionFactor = 2.0f;
        }
        else if (highTierRoll < rareBound && !rarePool.isEmpty()) {
            Item item = rarePool.get(random.nextInt(rarePool.size()));
            generatedLoot.add(new ItemStack(item, 1));
            reductionFactor = 2.0f;
            tierSpawned[0] = true;
        }

        boolean commonSpawned = false;
        float commonChance = (0.60f * chanceMultiplier) / reductionFactor;

        if (random.nextFloat() < commonChance && !commonPool.isEmpty()) {
            Item item = commonPool.get(random.nextInt(commonPool.size()));
            generatedLoot.add(new ItemStack(item, 1));
            commonSpawned = true;
        }

        float uncommonBaseChance = commonSpawned ? 0.20f : 0.30f;
        float uncommonChance = (uncommonBaseChance * chanceMultiplier) / reductionFactor;

        if (random.nextFloat() < uncommonChance && !uncommonPool.isEmpty()) {
            Item item = uncommonPool.get(random.nextInt(uncommonPool.size()));
            generatedLoot.add(new ItemStack(item, 1));
        }
    }

    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }

    public static boolean isLegendary(String id) {
        return UNIVERSAL_LEGENDARY_LOOT_IDS.contains(id) || POST_IRON_LEGENDARY_LOOT_IDS.contains(id) || POST_GOLD_LEGENDARY_LOOT_IDS.contains(id) || POST_EMERALD_LEGENDARY_LOOT_IDS.contains(id) || POST_DIAMOND_LEGENDARY_LOOT_IDS.contains(id) || MID_LEGENDARY_LOOT_IDS.contains(id) || POST_ELVENITE_LEGENDARY_LOOT_IDS.contains(id) || POST_VOIDERITE_LEGENDARY_LOOT_IDS.contains(id) || POST_DOOM_LEGENDARY_LOOT_IDS.contains(id) || POST_CALAMITIES_LEGENDARY_LOOT_IDS.contains(id) || POST_BLOOD_LEGENDARY_LOOT_IDS.contains(id) || POST_SUN_LEGENDARY_LOOT_IDS.contains(id) || POST_NYXARIS_LEGENDARY_LOOT_IDS.contains(id);
    }
    public static boolean isEpic(String id) {
        return UNIVERSAL_EPIC_LOOT_IDS.contains(id) || POST_IRON_EPIC_LOOT_IDS.contains(id) || POST_GOLD_EPIC_LOOT_IDS.contains(id) || POST_EMERALD_EPIC_LOOT_IDS.contains(id) || POST_DIAMOND_EPIC_LOOT_IDS.contains(id) || MID_EPIC_LOOT_IDS.contains(id) || POST_ELVENITE_EPIC_LOOT_IDS.contains(id) || POST_VOIDERITE_EPIC_LOOT_IDS.contains(id) || POST_DOOM_EPIC_LOOT_IDS.contains(id) || POST_CALAMITIES_EPIC_LOOT_IDS.contains(id) || POST_BLOOD_EPIC_LOOT_IDS.contains(id) || POST_SUN_EPIC_LOOT_IDS.contains(id) || POST_NYXARIS_EPIC_LOOT_IDS.contains(id);
    }
    public static boolean isRare(String id) {
        return UNIVERSAL_RARE_LOOT_IDS.contains(id) || POST_IRON_RARE_LOOT_IDS.contains(id) || POST_GOLD_RARE_LOOT_IDS.contains(id) || POST_EMERALD_RARE_LOOT_IDS.contains(id) || POST_DIAMOND_RARE_LOOT_IDS.contains(id) || MID_RARE_LOOT_IDS.contains(id) || POST_ELVENITE_RARE_LOOT_IDS.contains(id) || POST_VOIDERITE_RARE_LOOT_IDS.contains(id) || POST_DOOM_RARE_LOOT_IDS.contains(id) || POST_CALAMITIES_RARE_LOOT_IDS.contains(id) || POST_BLOOD_RARE_LOOT_IDS.contains(id) || POST_SUN_RARE_LOOT_IDS.contains(id) || POST_NYXARIS_RARE_LOOT_IDS.contains(id);
    }
    public static boolean isUncommon(String id) {
        return UNIVERSAL_UNCOMMON_LOOT_IDS.contains(id) || POST_IRON_UNCOMMON_LOOT_IDS.contains(id) || POST_GOLD_UNCOMMON_LOOT_IDS.contains(id) || POST_EMERALD_UNCOMMON_LOOT_IDS.contains(id) || POST_DIAMOND_UNCOMMON_LOOT_IDS.contains(id) || MID_UNCOMMON_LOOT_IDS.contains(id) || POST_ELVENITE_UNCOMMON_LOOT_IDS.contains(id) || POST_VOIDERITE_UNCOMMON_LOOT_IDS.contains(id) || POST_DOOM_UNCOMMON_LOOT_IDS.contains(id) || POST_CALAMITIES_UNCOMMON_LOOT_IDS.contains(id) || POST_BLOOD_UNCOMMON_LOOT_IDS.contains(id) || POST_SUN_UNCOMMON_LOOT_IDS.contains(id) || POST_NYXARIS_UNCOMMON_LOOT_IDS.contains(id);
    }
    public static boolean isCommon(String id) {
        return UNIVERSAL_COMMON_LOOT_IDS.contains(id) || POST_IRON_COMMON_LOOT_IDS.contains(id) || POST_GOLD_COMMON_LOOT_IDS.contains(id) || POST_EMERALD_COMMON_LOOT_IDS.contains(id) || POST_DIAMOND_COMMON_LOOT_IDS.contains(id) || MID_COMMON_LOOT_IDS.contains(id) || POST_ELVENITE_COMMON_LOOT_IDS.contains(id) || POST_VOIDERITE_COMMON_LOOT_IDS.contains(id) || POST_DOOM_COMMON_LOOT_IDS.contains(id) || POST_CALAMITIES_COMMON_LOOT_IDS.contains(id) || POST_BLOOD_COMMON_LOOT_IDS.contains(id) || POST_SUN_COMMON_LOOT_IDS.contains(id) || POST_NYXARIS_COMMON_LOOT_IDS.contains(id);
    }
}