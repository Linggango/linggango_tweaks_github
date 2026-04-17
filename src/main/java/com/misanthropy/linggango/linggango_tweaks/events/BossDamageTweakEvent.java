package com.misanthropy.linggango.linggango_tweaks.events;

import com.misanthropy.linggango.linggango_tweaks.config.TweaksConfig;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid = "linggango_tweaks")
public class BossDamageTweakEvent {

    @SubscribeEvent
    public static void onBossAttack(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof Player)) return;

        Entity attacker = event.getSource().getEntity();
        if (attacker == null) return;

        String entityName = ForgeRegistries.ENTITY_TYPES.getKey(attacker.getType()).toString();
        float multiplier = 1.0f;

        switch (entityName) {
            case "armageddon_mod:nyxaris_the_veil_of_oblivion":
            case "armageddon_mod:shadowed_spear":
                multiplier = TweaksConfig.NYXARIS_DMG_MULT.get().floatValue();
                break;
            case "armageddon_mod:zoranth_newborn_of_the_zenith":
                multiplier = TweaksConfig.ZORANTH_NEWBORN_DMG_MULT.get().floatValue();
                break;
            case "armageddon_mod:zoranth_the_forgotten_one":
                multiplier = TweaksConfig.ZORANTH_DMG_MULT.get().floatValue();
                break;
            case "armageddon_mod:the_discord":
            case "armageddon_mod:the_discord_illusion":
                multiplier = TweaksConfig.DISCORD_DMG_MULT.get().floatValue();
                break;
            case "armageddon_mod:arion_tyrantofthe_emerald_wrath_ravager":
                multiplier = TweaksConfig.ARION_RAVAGER_DMG_MULT.get().floatValue();
                break;
            case "armageddon_mod:arion_tyrant_of_the_emerald_wrath_soldat":
                multiplier = TweaksConfig.ARION_SOLDAT_DMG_MULT.get().floatValue();
                break;
            case "armageddon_mod:bringer_of_doom":
            case "armageddon_mod:bringer_of_doom_p_2":
                multiplier = TweaksConfig.BRINGER_OF_DOOM_DMG_MULT.get().floatValue();
                break;
            case "armageddon_mod:eldoraththe_ancient_builder":
                multiplier = TweaksConfig.ELDORATH_DMG_MULT.get().floatValue();
                break;
            case "armageddon_mod:elvenite_paladin":
                multiplier = TweaksConfig.ELVENITE_PALADIN_DMG_MULT.get().floatValue();
                break;
            case "armageddon_mod:sanghor_lord_of_blood":
            case "armageddon_mod:blood_orb":
                multiplier = TweaksConfig.SANGHOR_DMG_MULT.get().floatValue();
                break;
            case "armageddon_mod:sanghor_lord_of_blood_p_2":
            case "armageddon_mod:sanghor_lord_of_bloodp_2":
            case "armageddon_mod:bloody_slash_entity":
                multiplier = TweaksConfig.SANGHOR_P2_DMG_MULT.get().floatValue();
                break;
            case "armageddon_mod:the_chaos":
            case "armageddon_mod:chaos_meteorite_projectile":
            case "armageddon_mod:chaos_sigil_circle":
                multiplier = TweaksConfig.CHAOS_DMG_MULT.get().floatValue();
                break;
            case "armageddon_mod:the_famine":
            case "armageddon_mod:the_famine_projectile":
                multiplier = TweaksConfig.FAMINE_DMG_MULT.get().floatValue();
                break;
            case "armageddon_mod:the_gobelin_lord":
            case "armageddon_mod:little_mage_goblin":
            case "armageddon_mod:little_sword_goblin":
                multiplier = TweaksConfig.GOBELIN_LORD_DMG_MULT.get().floatValue();
                break;
            case "armageddon_mod:the_iron_colossus":
                multiplier = TweaksConfig.IRON_COLOSSUS_DMG_MULT.get().floatValue();
                break;
            case "armageddon_mod:vaedricthe_fallen_wanderer":
            case "armageddon_mod:vaedric_the_fallen_wanderer":
            case "armageddon_mod:vaedric_projectile":
            case "armageddon_mod:angry_enderman":
                multiplier = TweaksConfig.VAEDRIC_DMG_MULT.get().floatValue();
                break;
        }

        if (multiplier != 1.0f) {
            event.setAmount(event.getAmount() * multiplier);
        }
    }
}