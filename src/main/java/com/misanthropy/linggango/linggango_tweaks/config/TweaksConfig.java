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

    public static final ForgeConfigSpec.DoubleValue GLOBAL_MOB_MULTIPLIER;
    public static final ForgeConfigSpec.DoubleValue TERRA_ENTITY_OVERWORLD_MULTIPLIER;
    public static final ForgeConfigSpec.IntValue TERRA_ENTITY_OVERWORLD_WEIGHT;

    public static final ForgeConfigSpec.ConfigValue<String> BIOME_FALLBACK_NAMESPACE;
    public static final ForgeConfigSpec.DoubleValue GLOBAL_BIOME_SIZE_MULTIPLIER;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> BIOME_REDUCTIONS;

    private static final Map<String, Double> parsedCustomSpreads = new HashMap<>();
    public static final Map<String, Float> parsedBiomeReductions = new HashMap<>();

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
        GLOBAL_MOB_MULTIPLIER = BUILDER.defineInRange("global_mob_multiplier", 1.0, 0.0, 100.0);
        TERRA_ENTITY_OVERWORLD_MULTIPLIER = BUILDER.defineInRange("terra_entity_overworld_multiplier", 1.5, 0.0, 100.0);
        TERRA_ENTITY_OVERWORLD_WEIGHT = BUILDER.defineInRange("terra_entity_overworld_weight", 105, 1, 1000);
        BUILDER.pop();

        BUILDER.push("biome_tweaks");
        BIOME_FALLBACK_NAMESPACE = BUILDER.define("biome_fallback_namespace", "dreamwoods:");
        GLOBAL_BIOME_SIZE_MULTIPLIER = BUILDER.defineInRange("global_biome_size_multiplier", 1.5, 0.1, 100.0);
        BIOME_REDUCTIONS = BUILDER.defineListAllowEmpty(List.of("biome_reductions"),
                () -> List.of(
                        "dreamwoods:grassy_shore|1.0",
                        "dreamwoods:prairie|1.0",
                        "dreamwoods:lush_grassland|0.85",
                        "dreamwoods:alpine_fields|0.85",
                        "dreamwoods:lush_meadow|0.95"
                ),
                o -> o instanceof String && ((String) o).contains("|")
        );
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

            parsedBiomeReductions.clear();
            for (String entry : BIOME_REDUCTIONS.get()) {
                String[] parts = entry.split("\\|");
                if (parts.length == 2) {
                    try {
                        parsedBiomeReductions.put(parts[0].trim(), Float.parseFloat(parts[1].trim()));
                    } catch (NumberFormatException ignored) {}
                }
            }
        }
    }

    public static double getFactor(String structureSetId) {
        return parsedCustomSpreads.getOrDefault(structureSetId, GLOBAL_SPREAD_FACTOR.get());
    }
}