package com.misanthropy.linggango.linggango_tweaks.tweaks;

import com.misanthropy.linggango.linggango_tweaks.LinggangoTweaks;
import com.misanthropy.linggango.linggango_tweaks.config.TweaksConfig;
import com.mojang.serialization.Codec;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.common.world.ModifiableBiomeInfo;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import org.jspecify.annotations.NonNull;

import java.util.*;

@Mod.EventBusSubscriber(modid = LinggangoTweaks.MOD_ID)
public class SpawnChanges implements BiomeModifier {
    public static final SpawnChanges INSTANCE = new SpawnChanges();

    public static final Set<String> TWEAKED_ENTITIES = new HashSet<>();
    private static final Set<ModifiableBiomeInfo.BiomeInfo.Builder> PROCESSED_BUILDERS = Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));

    private static final List<String> ENTITY_IDS = List.of("alexsmobs:skunk",
            "alexsmobs:anteater", "alexsmobs:centipede_head", "alexsmobs:anaconda", "alexsmobs:terrapin", "alexsmobs:soul_vulture",
            "alexsmobs:rain_frog", "alexsmobs:rhinoceros", "alexsmobs:sugar_glider",
            "alexsmobs:platypus", "alexsmobs:skreecher", "alexsmobs:underminer", "alexsmobs:banana_slug",
            "alexsmobs:caiman", "alexsmobs:straddler", "alexsmobs:emu", "alexsmobs:grizzly_bear",
            "alexsmobs:roadrunner", "alexsmobs:gazelle", "alexsmobs:kangaroo",
            "alexsmobs:elephant", "alexsmobs:komodo_dragon", "alexsmobs:moose",
            "alexsmobs:gorilla", "alexsmobs:tiger", "alexsmobs:bald_eagle", "alexsmobs:mountain_goat", "alexsmobs:snow_leopard",
            "cataclysm:endermaptera", "cataclysm:amethyst_crab",
            "minecraft:allay", "minecraft:sheep", "minecraft:cow", "minecraft:pig",
            "minecraft:ocelot", "minecraft:cave_spider", "minecraft:enderman", "minecraft:rabbit", "minecraft:fox",
            "minecraft:chicken", "minecraft:bat", "minecraft:parrot", "minecraft:wolf",
            "capybara:capybara", "companions:living_candle", "companions:golden_allay", "companions:wild_antlion",
            "ribbits:ribbit", "species:leaf_hanger", "species:trooper", "species:goober", "species:mammutilation",
            "species:cliff_hanger", "mowziesmobs:lantern", "mowziesmobs:naga", "goety:black_wolf",
            "opposing_force:fire_slime", "opposing_force:frowzy", "opposing_force:guzzler", "opposing_force:trembler",
            "opposing_force:umber_spider", "opposing_force:volt", "friendsandfoes:moobloom", "friendsandfoes:glare",
            "friendsandfoes:copper_golem", "quark:stoneling", "quark:rascal"
    );

    private static final Set<String> DISABLED_ENTITIES = Set.of(
            "terra_entity:anger_bones", "terra_entity:anger_goblin", "terra_entity:angler",
            "terra_entity:arms_dealer", "terra_entity:base_bones", "terra_entity:zoologist",
            "terra_entity:yellow_slime", "terra_entity:wizard", "terra_entity:undead_viking",
            "terra_entity:big_anger_bones", "terra_entity:big_bones", "terra_entity:big_helmet_anger_bones",
            "terra_entity:big_muscle_anger_bones", "terra_entity:black_slime", "terra_entity:red_slime",
            "terra_entity:purple_slime", "terra_entity:honey_slime", "terra_entity:dungeon_slime",
            "terra_entity:desert_slime", "terra_entity:green_dumpling_slime", "terra_entity:swamp_slime",
            "terra_entity:jungle_slime", "terra_entity:golden_slime", "terra_entity:demon_eye",
            "terra_entity:ice_slime", "terra_entity:lava_slime", "terra_entity:crimslime",
            "terra_entity:corrupt_slime", "terra_entity:tropic_slime", "terra_entity:evil_slime",
            "terra_entity:hat_spore_zombie", "terra_entity:decayeder", "terra_entity:harpy",
            "terra_entity:drippler", "terra_entity:wandering_eye_fish", "terra_entity:ghost",
            "terra_entity:face_monster", "terra_entity:snow_flinx", "terra_entity:piranha",
            "terra_entity:shark", "terra_entity:arapaima", "terra_entity:goblin_archer",
            "terra_entity:goblin_peon", "terra_entity:goblin_warrior", "terra_entity:goblin_thief",
            "terra_entity:goblin_scout", "terra_entity:pixie", "terra_entity:possess_armor",
            "terra_entity:wraith", "terra_entity:guide", "terra_entity:demolitionist",
            "terra_entity:goblin_tinkerer", "terra_entity:nurse", "terra_entity:merchant",
            "terra_entity:painter", "terra_entity:dryad", "terra_entity:dye_trader",
            "terra_entity:old_man", "terra_entity:mechanic", "terra_entity:traveling_merchant",
            "terra_entity:witch_doctor", "terra_entity:party_girl", "terra_entity:clothier",
            "terra_entity:truffle", "terra_entity:duck", "terra_entity:fairy",
            "terra_entity:fealing", "terra_entity:grasshopper", "terra_entity:green_slime",
            "terra_entity:blue_slime"
    );

    public static void init() {
        if (!TWEAKED_ENTITIES.isEmpty()) return;

        TWEAKED_ENTITIES.addAll(ENTITY_IDS);

        for (EntityType<?> type : ForgeRegistries.ENTITY_TYPES.getValues()) {
            ResourceLocation id = ForgeRegistries.ENTITY_TYPES.getKey(type);
            if (id != null && id.getNamespace().equals("terra_entity") && !DISABLED_ENTITIES.contains(id.toString())) {
                TWEAKED_ENTITIES.add(id.toString());
            }
        }
    }

    @SubscribeEvent
    public static void onMobFallDamage(@NonNull LivingHurtEvent event) {
        if (event.getSource().is(DamageTypes.FALL) && event.getEntity().tickCount < 200) {
            ResourceLocation id = ForgeRegistries.ENTITY_TYPES.getKey(event.getEntity().getType());
            if (id != null && TWEAKED_ENTITIES.contains(id.toString())) {
                event.setCanceled(true);
            }
        }
    }

    @Override
    public void modify(@NonNull Holder<Biome> biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.@NonNull Builder builder) {
        if (phase != Phase.MODIFY) return;
        if (!PROCESSED_BUILDERS.add(builder)) return;

        init();

        for (MobCategory category : MobCategory.values()) {
            List<MobSpawnSettings.SpawnerData> spawners = builder.getMobSpawnSettings().getSpawner(category);
            if (spawners.isEmpty()) continue;

            List<MobSpawnSettings.SpawnerData> newSpawns = new ArrayList<>();
            double globalMultiplier = TweaksConfig.GLOBAL_MOB_MULTIPLIER.get();

            for (MobSpawnSettings.SpawnerData data : spawners) {
                ResourceLocation id = ForgeRegistries.ENTITY_TYPES.getKey(data.type);
                if (id == null || DISABLED_ENTITIES.contains(id.toString())) continue;

                String namespace = id.getNamespace();
                String fullId = id.toString();
                int targetWeight = data.getWeight().asInt();

                if (fullId.equals("monsterexpansion:rhyza")) {
                    targetWeight = Math.max(1, targetWeight / 5);
                } else if (fullId.equals("alexsmobs:flying_fish")) {
                    targetWeight = Math.max(1, targetWeight / 6);
                } else if (namespace.equals("terra_entity")) {
                    if (biome.is(BiomeTags.IS_OVERWORLD)) {
                        targetWeight = Math.max(1, (int)(targetWeight * TweaksConfig.TERRA_ENTITY_OVERWORLD_MULTIPLIER.get()));
                    } else {
                        targetWeight = Math.max(1, targetWeight / 2);
                    }
                } else if (namespace.equals("born_in_chaos_v1")) {
                    targetWeight = Math.max(1, targetWeight / 4);
                } else if (namespace.equals("alexsmobs") && (category.name().contains("WATER"))) {
                    targetWeight = Math.max(1, targetWeight / 6);
                }

                targetWeight = Math.max(1, (int)(targetWeight * globalMultiplier));
                newSpawns.add(new MobSpawnSettings.SpawnerData(data.type, targetWeight, data.minCount, data.maxCount));
            }

            spawners.clear();
            spawners.addAll(newSpawns);
        }

        if (biome.is(BiomeTags.IS_OVERWORLD)) {
            for (String typeId : TWEAKED_ENTITIES) {
                EntityType<?> type = ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation(typeId));
                if (type == null) continue;

                MobCategory category = type.getCategory();

                boolean alreadyExists = builder.getMobSpawnSettings().getSpawner(category).stream()
                        .anyMatch(s -> {
                            ResourceLocation sId = ForgeRegistries.ENTITY_TYPES.getKey(s.type);
                            return sId != null && sId.toString().equals(typeId);
                        });

                if (!alreadyExists) {
                    int weight = (category == MobCategory.CREATURE) ? 10 : (category == MobCategory.MONSTER ? 12 : 6);
                    int minCount = 1;
                    int maxCount = (category == MobCategory.MONSTER) ? 4 : 3;

                    ResourceLocation id = ForgeRegistries.ENTITY_TYPES.getKey(type);
                    if (id != null && id.getNamespace().equals("cataclysm")) {
                        weight = 2;
                        maxCount = 1;
                    } else if (id != null && id.getNamespace().equals("terra_entity")) {
                        weight = TweaksConfig.TERRA_ENTITY_OVERWORLD_WEIGHT.get();
                        maxCount = 3;
                    }

                    weight = Math.max(1, (int)(weight * TweaksConfig.GLOBAL_MOB_MULTIPLIER.get()));
                    builder.getMobSpawnSettings().addSpawn(category, new MobSpawnSettings.SpawnerData(type, weight, minCount, maxCount));
                }
            }
        }
    }

    @Override
    public @NonNull Codec<? extends BiomeModifier> codec() {
        return LinggangoTweaks.SPAWN_CHANGES_CODEC.get();
    }
}