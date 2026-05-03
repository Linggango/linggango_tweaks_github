package com.misanthropy.linggango.linggango_tweaks.config;

import com.misanthropy.linggango.linggango_tweaks.LinggangoTweaks;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import org.jspecify.annotations.NonNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mod.EventBusSubscriber(modid = LinggangoTweaks.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class TweaksConfig {
    public static final ForgeConfigSpec COMMON_SPEC;
    public static final ForgeConfigSpec.DoubleValue NYXARIS_DMG_MULT;
    public static final ForgeConfigSpec.DoubleValue ZORANTH_NEWBORN_DMG_MULT;
    public static final ForgeConfigSpec.DoubleValue ZORANTH_DMG_MULT;
    public static final ForgeConfigSpec.DoubleValue DISCORD_DMG_MULT;
    public static final ForgeConfigSpec.DoubleValue ARION_RAVAGER_DMG_MULT;
    public static final ForgeConfigSpec.DoubleValue ARION_SOLDAT_DMG_MULT;
    public static final ForgeConfigSpec.DoubleValue BRINGER_OF_DOOM_DMG_MULT;
    public static final ForgeConfigSpec.DoubleValue ELDORATH_DMG_MULT;
    public static final ForgeConfigSpec.DoubleValue ELVENITE_PALADIN_DMG_MULT;
    public static final ForgeConfigSpec.DoubleValue SANGHOR_DMG_MULT;
    public static final ForgeConfigSpec.DoubleValue SANGHOR_P2_DMG_MULT;
    public static final ForgeConfigSpec.DoubleValue CHAOS_DMG_MULT;
    public static final ForgeConfigSpec.DoubleValue FAMINE_DMG_MULT;
    public static final ForgeConfigSpec.DoubleValue GOBELIN_LORD_DMG_MULT;
    public static final ForgeConfigSpec.DoubleValue IRON_COLOSSUS_DMG_MULT;
    public static final ForgeConfigSpec.DoubleValue VAEDRIC_DMG_MULT;

    public static final ForgeConfigSpec.DoubleValue GLOBAL_SPREAD_FACTOR;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> CUSTOM_SPREADS;
    public static final ForgeConfigSpec.BooleanValue ID_BASED_SALT;
    public static ForgeConfigSpec.BooleanValue limitStructureCache;
    public static ForgeConfigSpec.IntValue structureCacheSize;

    public static final ForgeConfigSpec.IntValue PARRY_STARTUP_TICKS;
    public static final ForgeConfigSpec.IntValue PARRY_ACTIVE_WINDOW;
    public static final ForgeConfigSpec.IntValue PARRY_PERFECT_TICKS;
    public static final ForgeConfigSpec.IntValue PARRY_SUCCESS_TICKS;
    public static final ForgeConfigSpec.IntValue PARRY_COOLDOWN;
    public static final ForgeConfigSpec.IntValue PARRY_RECOVERY;
    public static final ForgeConfigSpec.BooleanValue PARRY_DEBUG_MODE;
    public static final ForgeConfigSpec.ConfigValue<String> PARRY_ANIMATION_1;
    public static final ForgeConfigSpec.ConfigValue<String> PARRY_ANIMATION_2;
    public static final ForgeConfigSpec.DoubleValue PARRY_ANIMATION_SPEED;

    public static final ForgeConfigSpec.BooleanValue PROJECTILE_DEFLECT_ENABLED;
    public static final ForgeConfigSpec.DoubleValue PROJECTILE_DEFLECT_CHANCE;
    public static final ForgeConfigSpec.DoubleValue PROJECTILE_DEFLECT_SPEED;
    public static final ForgeConfigSpec.DoubleValue PARRY_HEAL_AMOUNT;

    public static final ForgeConfigSpec.BooleanValue ENABLE_DYNAMIC_BALANCING;
    public static final ForgeConfigSpec.DoubleValue GLOBAL_MOB_MULTIPLIER;
    public static final ForgeConfigSpec.DoubleValue TERRA_ENTITY_OVERWORLD_MULTIPLIER;
    public static final ForgeConfigSpec.IntValue TERRA_ENTITY_OVERWORLD_WEIGHT;
    public static final ForgeConfigSpec.DoubleValue TARGET_MODDED_RATIO;
    public static final ForgeConfigSpec.DoubleValue BIOME_NATIVE_MULTIPLIER;
    public static final ForgeConfigSpec.DoubleValue BIOME_FOREIGN_MULTIPLIER;
    public static final ForgeConfigSpec.IntValue HARD_CAP_PER_TYPE;
    public static final ForgeConfigSpec.IntValue MAX_SAME_TYPE_PER_CHUNK;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> BOSS_MOB_BLACKLIST;

    public static final ForgeConfigSpec.BooleanValue ENABLE_BEDROCK_CULLING;
    public static final ForgeConfigSpec.BooleanValue CULL_TOP_BEDROCK;
    public static final ForgeConfigSpec.BooleanValue CULL_BOTTOM_BEDROCK;

    public static final ForgeConfigSpec.DoubleValue PERFECT_HIT_CHANCE;
    public static final ForgeConfigSpec.DoubleValue PERFECT_HIT_DAMAGE_MULT;
    public static final ForgeConfigSpec.DoubleValue PERFECT_HIT_KNOCKBACK_MULT;

    private static final Map<String, Double> parsedCustomSpreads = new HashMap<>();

    static {
        ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

        BUILDER.push("armageddon_bosses");
        NYXARIS_DMG_MULT = BUILDER.defineInRange("nyxaris_multiplier", 1.0, 0.0, 10.0);
        ZORANTH_NEWBORN_DMG_MULT = BUILDER.defineInRange("zoranth_newborn_multiplier", 1.0, 0.0, 10.0);
        ZORANTH_DMG_MULT = BUILDER.defineInRange("zoranth_multiplier", 1.0, 0.0, 10.0);
        DISCORD_DMG_MULT = BUILDER.defineInRange("discord_multiplier", 1.0, 0.0, 10.0);
        ARION_RAVAGER_DMG_MULT = BUILDER.defineInRange("arion_ravager_multiplier", 1.0, 0.0, 10.0);
        ARION_SOLDAT_DMG_MULT = BUILDER.defineInRange("arion_soldat_multiplier", 1.0, 0.0, 10.0);
        BRINGER_OF_DOOM_DMG_MULT = BUILDER.defineInRange("bringer_of_doom_multiplier", 1.0, 0.0, 10.0);
        ELDORATH_DMG_MULT = BUILDER.defineInRange("eldorath_multiplier", 1.0, 0.0, 10.0);
        ELVENITE_PALADIN_DMG_MULT = BUILDER.defineInRange("elvenite_paladin_multiplier", 1.0, 0.0, 10.0);
        SANGHOR_DMG_MULT = BUILDER.defineInRange("sanghor_multiplier", 1.0, 0.0, 10.0);
        SANGHOR_P2_DMG_MULT = BUILDER.defineInRange("sanghor_p2_multiplier", 1.0, 0.0, 10.0);
        CHAOS_DMG_MULT = BUILDER.defineInRange("chaos_multiplier", 1.0, 0.0, 10.0);
        FAMINE_DMG_MULT = BUILDER.defineInRange("famine_multiplier", 1.0, 0.0, 10.0);
        GOBELIN_LORD_DMG_MULT = BUILDER.defineInRange("gobelin_lord_multiplier", 1.0, 0.0, 10.0);
        IRON_COLOSSUS_DMG_MULT = BUILDER.defineInRange("iron_colossus_multiplier", 1.0, 0.0, 10.0);
        VAEDRIC_DMG_MULT = BUILDER.defineInRange("vaedric_multiplier", 1.0, 0.0, 10.0);
        BUILDER.pop();

        BUILDER.push("structure_spawn_rates");
        GLOBAL_SPREAD_FACTOR = BUILDER.defineInRange("globalSpreadFactor", 1.0, 0.0, 1000.0);
        CUSTOM_SPREADS = BUILDER.defineListAllowEmpty(List.of("customSpreads"), List::of, o -> o instanceof String && ((String) o).contains("|"));
        ID_BASED_SALT = BUILDER.define("idBasedSalt", true);
        BUILDER.pop();

        limitStructureCache = BUILDER.define("limitStructureCache", true);
        structureCacheSize = BUILDER.defineInRange("structureCacheSize", 128, 16, 1024);

        BUILDER.push("parry_system");
        PARRY_STARTUP_TICKS = BUILDER.defineInRange("parry_startup_ticks", 1, 1, 100);
        PARRY_ACTIVE_WINDOW = BUILDER.defineInRange("parry_active_window", 15, 1, 100);
        PARRY_PERFECT_TICKS = BUILDER.defineInRange("parry_perfect_ticks", 1, 1, 100);
        PARRY_SUCCESS_TICKS = BUILDER.defineInRange("parry_success_ticks", 2, 1, 100);
        PARRY_COOLDOWN = BUILDER.defineInRange("parry_cooldown", 200, 1, 1000);
        PARRY_RECOVERY = BUILDER.defineInRange("parry_recovery", 5, 1, 100);
        PARRY_DEBUG_MODE = BUILDER.define("debug_mode", false);
        PARRY_ANIMATION_1 = BUILDER.define("parry_animation_1", "malfu_combat_animation:two_handed_downup_rl");
        PARRY_ANIMATION_2 = BUILDER.define("parry_animation_2", "malfu_combat_animation:two_handed_heavy_lr");
        PARRY_ANIMATION_SPEED = BUILDER.defineInRange("animation_speed", 15.0, 0.1, 100.0);
        PROJECTILE_DEFLECT_ENABLED = BUILDER.define("projectile_deflect_enabled", true);
        PROJECTILE_DEFLECT_CHANCE = BUILDER.defineInRange("projectile_deflect_chance", 1.0, 0.0, 1.0);
        PROJECTILE_DEFLECT_SPEED = BUILDER.defineInRange("projectile_deflect_speed", 2.5, 0.1, 10.0);
        PARRY_HEAL_AMOUNT = BUILDER.defineInRange("parry_heal_amount", 4.0, 0.0, 100.0);
        BUILDER.pop();

        BUILDER.push("mob_spawning");
        ENABLE_DYNAMIC_BALANCING = BUILDER.define("enable_dynamic_balancing", true);
        GLOBAL_MOB_MULTIPLIER = BUILDER.defineInRange("global_mob_multiplier", 1.0, 0.0, 100.0);
        TERRA_ENTITY_OVERWORLD_MULTIPLIER = BUILDER.defineInRange("terra_entity_overworld_multiplier", 1.5, 0.0, 100.0);
        TERRA_ENTITY_OVERWORLD_WEIGHT = BUILDER.defineInRange("terra_entity_overworld_weight", 105, 1, 1000);
        TARGET_MODDED_RATIO = BUILDER.defineInRange("target_modded_ratio", 0.40, 0.0, 1.0);
        BIOME_NATIVE_MULTIPLIER = BUILDER.defineInRange("biome_native_multiplier", 1.5, 0.0, 10.0);
        BIOME_FOREIGN_MULTIPLIER = BUILDER.defineInRange("biome_foreign_multiplier", 0.25, 0.0, 10.0);
        HARD_CAP_PER_TYPE = BUILDER.defineInRange("hard_cap_per_type", 8, 1, 100);
        MAX_SAME_TYPE_PER_CHUNK = BUILDER.defineInRange("max_same_type_per_chunk", 4, 1, 100);
        BOSS_MOB_BLACKLIST = BUILDER.defineListAllowEmpty(List.of("boss_mob_blacklist"),
                () -> List.of(
                        "minecraft:wither",
                        "minecraft:ender_dragon",
                        "minecraft:warden",
                        "cataclysm:ender_golem",
                        "cataclysm:ignis",
                        "cataclysm:netherite_monstrosity",
                        "cataclysm:the_harbinger",
                        "cataclysm:the_leviathan"
                ),
                o -> o instanceof String && ((String) o).contains(":")
        );
        BUILDER.pop();

        BUILDER.push("bedrock_culling");
        ENABLE_BEDROCK_CULLING = BUILDER.define("enable_bedrock_culling", true);
        CULL_TOP_BEDROCK = BUILDER.define("cull_top_bedrock", true);
        CULL_BOTTOM_BEDROCK = BUILDER.define("cull_bottom_bedrock", true);
        BUILDER.pop();

        BUILDER.push("perfect_hit_system");
        PERFECT_HIT_CHANCE = BUILDER.comment("Chance for a jump/falling crit to become a Perfect Hit (0.0 to 1.0)")
                .defineInRange("perfect_hit_chance", 0.05, 0.0, 1.0);
        PERFECT_HIT_DAMAGE_MULT = BUILDER.comment("Additional damage multiplier on top of the standard critical hit")
                .defineInRange("perfect_hit_damage_mult", 1.5, 1.0, 10.0);
        PERFECT_HIT_KNOCKBACK_MULT = BUILDER.comment("Knockback multiplier for the perfect hit")
                .defineInRange("perfect_hit_knockback_mult", 4.0, 1.0, 20.0);
        BUILDER.pop();

        COMMON_SPEC = BUILDER.build();
    }

    public static void register() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, COMMON_SPEC);
    }

    @SubscribeEvent
    public static void onLoad(final @NonNull ModConfigEvent event) {
        if (event.getConfig().getSpec() == COMMON_SPEC) {
            parsedCustomSpreads.clear();
            for (String entry : CUSTOM_SPREADS.get()) {
                String[] parts = entry.split("\\|");
                if (parts.length == 2) {
                    try {
                        parsedCustomSpreads.put(parts[0].trim(), Double.parseDouble(parts[1].trim()));
                    } catch (NumberFormatException ignored) {}
                }
            }
        }
    }

    public static double getFactor(String structureSetId) {
        return parsedCustomSpreads.getOrDefault(structureSetId, GLOBAL_SPREAD_FACTOR.get());
    }
}