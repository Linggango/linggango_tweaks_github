package com.misanthropy.linggango.linggango_tweaks.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

import java.util.List;

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

    public static final ForgeConfigSpec.BooleanValue DYNAMIC_CROSSHAIR_ENABLED;

    public static final ForgeConfigSpec.BooleanValue PROJECTILE_DEFLECT_ENABLED;
    public static final ForgeConfigSpec.DoubleValue PROJECTILE_DEFLECT_CHANCE;
    public static final ForgeConfigSpec.DoubleValue PROJECTILE_DEFLECT_SPEED;
    public static final ForgeConfigSpec.DoubleValue PARRY_HEAL_AMOUNT;

    public static final ForgeConfigSpec.BooleanValue APOSTLE_DAMAGE_CAP_FIX;
    public static final ForgeConfigSpec.BooleanValue APOSTLE_TITLE_NUMBER_LOG;

    public static final ForgeConfigSpec.DoubleValue CAMPFIRE_SMOKE_ELIMINATION;

    public static final ForgeConfigSpec.DoubleValue PERFECT_HIT_CHANCE;
    public static final ForgeConfigSpec.DoubleValue PERFECT_HIT_DAMAGE_MULT;
    public static final ForgeConfigSpec.DoubleValue PERFECT_HIT_KNOCKBACK_MULT;

    public static ForgeConfigSpec.BooleanValue BEDROCKOID_LOOK_SWAY_ENABLED;

    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> ORBITAL_PARTICLE_ITEMS;

    public static final ForgeConfigSpec.ConfigValue<String> RING_NONE_ID;
    public static final ForgeConfigSpec.ConfigValue<String> RING_CURSED_ID;
    public static final ForgeConfigSpec.ConfigValue<String> RING_VIRTUE_ID;

    public static final ForgeConfigSpec.ConfigValue<String> RING_NONE_SOUND;
    public static final ForgeConfigSpec.ConfigValue<String> RING_CURSED_SOUND;
    public static final ForgeConfigSpec.ConfigValue<String> RING_VIRTUE_SOUND;

    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> MOB_TO_RESET_HP;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> MOB_TO_DESPAWN;
    public static final ForgeConfigSpec.EnumValue<ResetMode> RESET_MODE;
    public static final ForgeConfigSpec.IntValue COMBAT_TIMEOUT_SECONDS;
    public static final ForgeConfigSpec.BooleanValue SAME_DIMENSION_ONLY;
    public static final ForgeConfigSpec.BooleanValue RESET_ON_LOGOUT;
    public static final ForgeConfigSpec.BooleanValue ENABLE_DEBUG_LOGGING;
    public static final ForgeConfigSpec.IntValue PERCENTAGE_RESTORED;

    public static final ForgeConfigSpec.BooleanValue PERFECT_HIT_FLASH_ENABLED;

    public static final ForgeConfigSpec.BooleanValue BEDROCKOID_SLOT_HIGHLIGHT_ENABLED;
    public static final ForgeConfigSpec.BooleanValue BEDROCKOID_FLOATING_ITEM_SCALE_ENABLED;
    public static final ForgeConfigSpec.BooleanValue BEDROCKOID_ITEM_BREATHING_ENABLED;
    public static final ForgeConfigSpec.BooleanValue BEDROCKOID_LOW_HEALTH_SHAKE_ENABLED;
    public static final ForgeConfigSpec.BooleanValue BEDROCKOID_SUN_GLARE_SKY_ENABLED;
    public static final ForgeConfigSpec.BooleanValue BEDROCKOID_SUN_RADIUS_SCALE_ENABLED;

    public enum ResetMode {
        ANY_PLAYER_DIES,
        ALL_PLAYERS_DIE
    }

    static {
        ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

        CAMPFIRE_SMOKE_ELIMINATION = BUILDER.comment("Percentage of campfire smoke particles to eliminate (0.01 = 1%, 1.0 = 100%) to js remove it.")
                .defineInRange("campfire_smoke_elimination", 0.65, 0.0, 1.0);

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

        BUILDER.push("perfect_hit_system");
        PERFECT_HIT_CHANCE = BUILDER.comment("Chance for a jump/falling crit to become a Perfect Hit (0.0 to 1.0)")
                .defineInRange("perfect_hit_chance", 0.05, 0.0, 1.0);
        PERFECT_HIT_DAMAGE_MULT = BUILDER.comment("Additional damage multiplier on top of the standard critical hit")
                .defineInRange("perfect_hit_damage_mult", 1.5, 1.0, 10.0);
        PERFECT_HIT_KNOCKBACK_MULT = BUILDER.comment("Knockback multiplier for the perfect hit")
                .defineInRange("perfect_hit_knockback_mult", 4.0, 1.0, 20.0);
        BUILDER.pop();

        BUILDER.push("goety");
        APOSTLE_DAMAGE_CAP_FIX = BUILDER
                .comment("Re-enforce Apostle's damage cap after all LivingDamageEvent handlers run. Prevents items/traits from dealing more damage than the configured cap.")
                .define("apostle_damage_cap_fix", true);
        APOSTLE_TITLE_NUMBER_LOG = BUILDER
                .comment("A debug statement for printing changes in Apostle title numbers.")
                .define("apostle_title_number_log", false);
        BUILDER.pop();

        BUILDER.push("orbital_particles");
        ORBITAL_PARTICLE_ITEMS = BUILDER.comment("List of item IDs that should have custom orbital particles when lying on the ground.")
                .defineListAllowEmpty(List.of("orbital_particle_items"),
                        () -> List.of(
                                "lethality:nightmare_sword",
                                "brutality:royal_guardian_sword"
                        ),
                        o -> o instanceof String
                );
        BUILDER.pop();

        BUILDER.push("ring_selection");
        RING_NONE_ID = BUILDER.comment("Item ID for the 'None' option. Usually linggango_tweaks:nothing")
                .define("ring_none_id", "linggango_tweaks:nothing");
        RING_CURSED_ID = BUILDER.comment("Item ID for the Cursed Ring")
                .define("ring_cursed_id", "enigmaticlegacy:cursed_ring");
        RING_VIRTUE_ID = BUILDER.comment("Item ID for the Virtue Ring")
                .define("ring_virtue_id", "covenant_of_the_seven:virtue_ring");

        RING_NONE_SOUND = BUILDER.comment("Sound played when selecting the None option")
                .define("ring_none_sound", "test_sound_change_this");
        RING_CURSED_SOUND = BUILDER.comment("Sound played when selecting the Cursed Ring")
                .define("ring_cursed_sound", "test_sound_change_this");
        RING_VIRTUE_SOUND = BUILDER.comment("Sound played when selecting the Virtue Ring")
                .define("ring_virtue_sound", "test_sound_change_this");
        BUILDER.pop();

        BUILDER.push("boss_hp_reset");
        MOB_TO_RESET_HP = BUILDER
                .comment("List of mob entity IDs that should have HP reset when players who damaged them die or logout (if enabled).",
                        "Format: \"minecraft:zombie\", \"minecraft:skeleton\", etc.",
                        "Default: [\"minecraft:wither\", \"minecraft:warden\", \"minecraft:elder_guardian\"]")
                .defineListAllowEmpty(List.of("mob_to_reset_hp"),
                        () -> List.of("minecraft:wither", "minecraft:warden", "minecraft:elder_guardian"),
                        obj -> obj instanceof String);

        MOB_TO_DESPAWN = BUILDER
                .comment("List of mob entity IDs that should be despawned (silently removed) when players who damaged them die or logout (if enabled).",
                        "Format: \"minecraft:zombie\", \"minecraft:skeleton\", etc.",
                        "Default: [] (empty)")
                .defineListAllowEmpty(List.of("mob_to_despawn"),
                        List::of,
                        obj -> obj instanceof String);

        RESET_MODE = BUILDER
                .comment("When should the mob reset its HP?",
                        "ANY_PLAYER_DIES - Reset HP when any player who attacked the mob dies or logs out (if enabled)",
                        "ALL_PLAYERS_DIE - Reset HP only when all players who attacked the mob die or log out (if enabled)",
                        "Default: ALL_PLAYERS_DIE")
                .defineEnum("reset_mode", ResetMode.ALL_PLAYERS_DIE);

        COMBAT_TIMEOUT_SECONDS = BUILDER
                .comment("Combat timeout in seconds. After this time with no damage, player interactions are forgotten.",
                        "-1 = No timeout (combat never expires)",
                        "> 0 = Combat expires after X seconds of no damage",
                        "Default: -1 (no timeout)")
                .defineInRange("combat_timeout_seconds", -1, -1, Integer.MAX_VALUE);

        SAME_DIMENSION_ONLY = BUILDER
                .comment("Should HP reset only trigger if player dies in the same dimension where they fought the mob?",
                        "true = Player must die in the same dimension as the combat",
                        "false = Player death in any dimension triggers reset",
                        "Default: true")
                .define("same_dimension_only", true);

        RESET_ON_LOGOUT = BUILDER
                .comment("Should boss HP reset when a player logs out?",
                        "true = Boss HP resets when player logs out (does not apply to singleplayer)",
                        "false = Boss HP only resets on player death, not logout",
                        "Default: false")
                .define("reset_on_logout", false);

        ENABLE_DEBUG_LOGGING = BUILDER
                .comment("Enable debug logging for troubleshooting and monitoring.",
                        "true = Log detailed information about capability attachments, player interactions, and resets",
                        "false = Only log HP reset events (minimal logging)",
                        "Default: false")
                .define("enable_debug_logging", false);

        PERCENTAGE_RESTORED = BUILDER
                .comment("Percentage of HP to restore when a reset is triggered.",
                        "Value should be between 0 and 100.",
                        "Default: 50")
                .defineInRange("percentage_restored", 50, 0, 100);
        BUILDER.pop();

        BUILDER.push("utility");
        BEDROCKOID_SLOT_HIGHLIGHT_ENABLED = BUILDER.comment("Enable smooth animated inventory slot highlight instead of vanilla.")
                .define("slot_highlight_enabled", true);

        BEDROCKOID_FLOATING_ITEM_SCALE_ENABLED = BUILDER.comment("Enable scaling up cursor-held floating items in the inventory.")
                .define("floating_item_scale_enabled", true);

        BEDROCKOID_ITEM_BREATHING_ENABLED = BUILDER.comment("Enable the breathing animation for empty hands and held items. (Disables sway too)")
                .define("item_breathing_enabled", true);

        BEDROCKOID_LOW_HEALTH_SHAKE_ENABLED = BUILDER.comment("Enable hand/item shaking and speed changes when player is at low health.")
                .define("low_health_shake_enabled", true);

        BEDROCKOID_SUN_GLARE_SKY_ENABLED = BUILDER.comment("Enable the atmospheric sky color tinting and dimming when looking near the sun.")
                .define("sun_glare_sky_enabled", true);

        BEDROCKOID_SUN_RADIUS_SCALE_ENABLED = BUILDER.comment("Enable the scaling up of the sun's visual size when looking directly at it.")
                .define("sun_radius_scale_enabled", true);

        PERFECT_HIT_FLASH_ENABLED = BUILDER.comment("Enable the white screen flash visual effect on a Perfect Hit.")
                .define("perfect_hit_flash_enabled", true);

        BEDROCKOID_LOOK_SWAY_ENABLED = BUILDER.comment("Enable the first-person hand sway lagging (lazy hand) effect when turning the camera.")
                .define("look_sway_enabled", true);

        DYNAMIC_CROSSHAIR_ENABLED = BUILDER.comment("Enable dynamic opacity for the crosshair depending on whether a target is focused.")
                .define("dynamic_crosshair_enabled", true);
        BUILDER.pop();


        COMMON_SPEC = BUILDER.build();
    }

    public static void register() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, COMMON_SPEC);
    }
}