package com.misanthropy.linggango.linggango_tweaks.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import net.minecraftforge.registries.ForgeRegistries;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomEntityDropsModifier extends LootModifier {
    public static final Codec<CustomEntityDropsModifier> CODEC = RecordCodecBuilder.create(inst -> codecStart(inst).apply(inst, CustomEntityDropsModifier::new));

    // SWITCHED LOOTJS TO JAVA
    private static final List<ConfigDrop> DROP_RULES = List.of(
            new ConfigDrop("minecraft:ender_dragon", "minecraft:dragon_egg", 1.0),
            new ConfigDrop("minecraft:enderman", "endrem:corrupted_eye", 0.01),
            new ConfigDrop("minecraft:enderman", "endrem:black_eye", 0.01),
            new ConfigDrop("minecraft:enderman", "endrem:lost_eye", 0.01),
            new ConfigDrop("minecraft:enderman", "endrem:cold_eye", 0.01),
            new ConfigDrop("minecraft:enderman", "endrem:cold_eye", 0.01),
            new ConfigDrop("minecraft:enderman", "endrem:old_eye", 0.01),
            new ConfigDrop("minecraft:enderman", "endrem:rogue_eye", 0.01),
            new ConfigDrop("minecraft:enderman", "endrem:magical_eye", 0.01),
            new ConfigDrop("armageddon_mod:the_iron_colossus", "meetyourfight:fossil_bait", 0.15),
            new ConfigDrop("armageddon_mod:the_iron_colossus", "mowzies_cataclysm:wrought_eye", 0.15),
            new ConfigDrop("armageddon_mod:the_gobelin_lord", "artifacts:golden_hook", 0.05),
            new ConfigDrop("armageddon_mod:the_gobelin_lord", "mynethersdelight:golden_egg", 0.15),
            new ConfigDrop("armageddon_mod:arion_tyrant_of_the_emerald_wrath_soldat", "artifacts:lucky_scarf", 0.10),
            new ConfigDrop("armageddon_mod:arion_tyrant_of_the_emerald_wrath_soldat", "undergarden:catalyst", 0.10),
            new ConfigDrop("terramity:gatmancer", "terramity:requiem", 0.005),
            new ConfigDrop("terramity:conjurling", "terramity:ruby", 0.01),
            new ConfigDrop("cataclysm:maledictus", "brutality:frostmourne", 0.5),
            new ConfigDrop("block_factorys_bosses:underworld_knight", "goety_revelation:quietus_star", 1.0),
            new ConfigDrop("block_factorys_bosses:underworld_knight", "kubejs:fartinium_ingot", 1.0)
    );

    private record ConfigDrop(String entityId, String itemId, double chance) {}
    private record ResolvedDrop(Item item, double chance) {}
    private static final Map<String, List<ResolvedDrop>> ENTITY_DROPS_MAP = new HashMap<>();
    private static boolean isInitialized = false;

    public CustomEntityDropsModifier(LootItemCondition @NonNull [] conditionsIn) {
        super(conditionsIn);
    }

    private static void initMapIfNeeded() {
        if (isInitialized) return;

        for (ConfigDrop rule : DROP_RULES) {
            Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(rule.itemId()));
            if (item != null && item != Items.AIR) {
                ENTITY_DROPS_MAP.computeIfAbsent(rule.entityId(), k -> new ArrayList<>())
                        .add(new ResolvedDrop(item, rule.chance()));
            }
        }
        isInitialized = true;
    }

    @Override
    protected @NonNull ObjectArrayList<ItemStack> doApply(@NonNull ObjectArrayList<ItemStack> generatedLoot, @NonNull LootContext context) {
        if (!context.hasParam(LootContextParams.THIS_ENTITY)) return generatedLoot;

        Entity entity = context.getParamOrNull(LootContextParams.THIS_ENTITY);
        if (entity == null) return generatedLoot;

        ResourceLocation entityId = ForgeRegistries.ENTITY_TYPES.getKey(entity.getType());
        if (entityId == null) return generatedLoot;

        initMapIfNeeded();
        List<ResolvedDrop> possibleDrops = ENTITY_DROPS_MAP.get(entityId.toString());

        if (possibleDrops != null) {
            for (ResolvedDrop drop : possibleDrops) {
                if (drop.chance() >= 1.0 || context.getRandom().nextDouble() < drop.chance()) {
                    generatedLoot.add(new ItemStack(drop.item(), 1));
                }
            }
        }

        return generatedLoot;
    }

    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}