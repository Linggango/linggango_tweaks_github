package com.misanthropy.linggango.linggango_tweaks.config;

import com.misanthropy.linggango.linggango_tweaks.LinggangoTweaks;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;

import java.util.List;

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
    public static final ForgeConfigSpec.DoubleValue TERRA_ENTITY_SLIME_MULTIPLIER;
    public static final ForgeConfigSpec.DoubleValue ALEXSMOBS_RHINO_MULTIPLIER;
    public static final ForgeConfigSpec.DoubleValue MONSTEREXPANSION_MULTIPLIER;
    public static final ForgeConfigSpec.DoubleValue SAINTSDRAGONS_MULTIPLIER;
    public static final ForgeConfigSpec.DoubleValue GOETYHOSTILITY_MULTIPLIER;
    public static final ForgeConfigSpec.DoubleValue KNIGHTQUEST_MULTIPLIER;
    public static final ForgeConfigSpec.BooleanValue KNIGHTQUEST_IGNORE_BIOMES;
    public static final ForgeConfigSpec.IntValue TERRA_ENTITY_OVERWORLD_WEIGHT;
    public static final ForgeConfigSpec.DoubleValue TARGET_MODDED_RATIO;
    public static final ForgeConfigSpec.DoubleValue BIOME_NATIVE_MULTIPLIER;
    public static final ForgeConfigSpec.DoubleValue BIOME_FOREIGN_MULTIPLIER;
    public static final ForgeConfigSpec.IntValue HARD_CAP_PER_TYPE;
    public static final ForgeConfigSpec.IntValue MAX_SAME_TYPE_PER_CHUNK;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> BOSS_MOB_BLACKLIST;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> AUTO_EXCLUDED_MOB_SEGMENTS;

    public static final ForgeConfigSpec.DoubleValue PERFECT_HIT_CHANCE;
    public static final ForgeConfigSpec.DoubleValue PERFECT_HIT_DAMAGE_MULT;
    public static final ForgeConfigSpec.DoubleValue PERFECT_HIT_KNOCKBACK_MULT;


    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> ORBITAL_PARTICLE_ITEMS;

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
        TERRA_ENTITY_OVERWORLD_MULTIPLIER = BUILDER.comment("General multiplier for all terra_entity mobs in overworld (0.33 = 3x less)")
                .defineInRange("terra_entity_overworld_multiplier", 0.33, 0.0, 100.0);
        TERRA_ENTITY_SLIME_MULTIPLIER = BUILDER.comment("Additional multiplier for terra_entity slimes (0.05 = 20x less on top of general)")
                .defineInRange("terra_entity_slime_multiplier", 0.05, 0.0, 100.0);
        ALEXSMOBS_RHINO_MULTIPLIER = BUILDER.comment("Multiplier for alexsmobs rhinoceros (0.2 = 5x less)")
                .defineInRange("alexsmobs_rhino_multiplier", 0.2, 0.0, 100.0);
        MONSTEREXPANSION_MULTIPLIER = BUILDER.comment("Multiplier for all monsterexpansion mobs")
                .defineInRange("monsterexpansion_multiplier", 0.5, 0.0, 100.0);
        SAINTSDRAGONS_MULTIPLIER = BUILDER.comment("Multiplier for all saintsdragons mobs")
                .defineInRange("saintsdragons_multiplier", 0.5, 0.0, 100.0);
        GOETYHOSTILITY_MULTIPLIER = BUILDER.comment("Multiplier for all goetyhostility mobs")
                .defineInRange("goetyhostility_multiplier", 1.2, 0.0, 100.0);
        KNIGHTQUEST_MULTIPLIER = BUILDER.comment("Multiplier for all knightquest mobs")
                .defineInRange("knightquest_multiplier", 3.0, 0.0, 100.0);
        KNIGHTQUEST_IGNORE_BIOMES = BUILDER.comment("Should knightquest mobs ignore biome restrictions and spawn anywhere?")
                .define("knightquest_ignore_biomes", true);
        TERRA_ENTITY_OVERWORLD_WEIGHT = BUILDER.defineInRange("terra_entity_overworld_weight", 105, 1, 1000);
        TARGET_MODDED_RATIO = BUILDER.defineInRange("target_modded_ratio", 0.40, 0.0, 1.0);
        BIOME_NATIVE_MULTIPLIER = BUILDER.comment("Weight bonus for mobs in their native biome")
                .defineInRange("biome_native_multiplier", 1.15, 0.0, 10.0);
        BIOME_FOREIGN_MULTIPLIER = BUILDER.comment("Weight penalty for mobs outside their native biome (0.85 = mild penalty, allows variety)")
                .defineInRange("biome_foreign_multiplier", 0.85, 0.0, 10.0);
        HARD_CAP_PER_TYPE = BUILDER.defineInRange("hard_cap_per_type", 8, 1, 100);
        MAX_SAME_TYPE_PER_CHUNK = BUILDER.defineInRange("max_same_type_per_chunk", 4, 1, 100);
        BOSS_MOB_BLACKLIST = BUILDER.comment("Manual blacklist for specific boss IDs (auto-detection catches most)")
                .defineListAllowEmpty(List.of("boss_mob_blacklist"),
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
        AUTO_EXCLUDED_MOB_SEGMENTS = BUILDER.comment("If a mob's registry path contains any of these underscore-separated words, it is excluded from natural spawn pools automatically")
                .defineListAllowEmpty(List.of("auto_excluded_mob_segments"),
                        () -> List.of(
                                "boss", "miniboss", "bomb", "explosive", "projectile",
                                "missile", "bullet", "dummy", "test", "companion",
                                "npc", "merchant", "trader", "minion", "summon",
                                "totem", "trap", "turret", "vehicle", "seat",
                                "sentry", "decoy", "clone", "part", "segment"
                        ),
                        o -> o instanceof String
                );
        BUILDER.pop();

        BUILDER.push("perfect_hit_system");
        PERFECT_HIT_CHANCE = BUILDER.comment("Chance for a jump/falling crit to become a Perfect Hit (0.0 to 1.0)")
                .defineInRange("perfect_hit_chance", 0.05, 0.0, 1.0);
        PERFECT_HIT_DAMAGE_MULT = BUILDER.comment("Additional damage multiplier on top of the standard critical hit")
                .defineInRange("perfect_hit_damage_mult", 1.5, 1.0, 10.0);
        PERFECT_HIT_KNOCKBACK_MULT = BUILDER.comment("Knockback multiplier for the perfect hit")
                .defineInRange("perfect_hit_knockback_mult", 4.0, 1.0, 20.0);
        BUILDER.pop();

        BUILDER.push("visuals");
        ORBITAL_PARTICLE_ITEMS = BUILDER.comment("Items that gain complex orbital particles when dropped as permanent items")
                .defineListAllowEmpty(List.of("orbital_particle_items"),
                        () -> List.of(
                                "lethality:nightmare_sword",
                                "brutality:royal_guardian_sword",
                                "composite_material:etherite_sword_reinforced",
                                "enigmaticlegacy:the_judgement"
                        ),
                        o -> o instanceof String && ((String) o).contains(":")
                );
        BUILDER.pop();

        COMMON_SPEC = BUILDER.build();
    }

    public static void register() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, COMMON_SPEC);
    }
}