package com.misanthropy.linggango.linggango_tweaks.events;

import com.misanthropy.linggango.linggango_tweaks.config.TweaksConfig;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Objects;

@Mod.EventBusSubscriber(modid = "linggango_tweaks")
public class BossDamageTweakEvent {

    @SubscribeEvent
    public static void onBossAttack(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof Player)) return;

        Entity attacker = event.getSource().getEntity();
        if (attacker == null) return;

        String entityName = Objects.requireNonNull(ForgeRegistries.ENTITY_TYPES.getKey(attacker.getType())).toString();
        float multiplier = switch (entityName) {
            case "armageddon_mod:nyxaris_the_veil_of_oblivion", "armageddon_mod:shadowed_spear" ->
                    TweaksConfig.NYXARIS_DMG_MULT.get().floatValue();
            case "armageddon_mod:zoranth_newborn_of_the_zenith" ->
                    TweaksConfig.ZORANTH_NEWBORN_DMG_MULT.get().floatValue();
            case "armageddon_mod:zoranth_the_forgotten_one" -> TweaksConfig.ZORANTH_DMG_MULT.get().floatValue();
            case "armageddon_mod:the_discord", "armageddon_mod:the_discord_illusion" ->
                    TweaksConfig.DISCORD_DMG_MULT.get().floatValue();
            case "armageddon_mod:arion_tyrantofthe_emerald_wrath_ravager" ->
                    TweaksConfig.ARION_RAVAGER_DMG_MULT.get().floatValue();
            case "armageddon_mod:arion_tyrant_of_the_emerald_wrath_soldat" ->
                    TweaksConfig.ARION_SOLDAT_DMG_MULT.get().floatValue();
            case "armageddon_mod:bringer_of_doom", "armageddon_mod:bringer_of_doom_p_2" ->
                    TweaksConfig.BRINGER_OF_DOOM_DMG_MULT.get().floatValue();
            case "armageddon_mod:eldoraththe_ancient_builder" -> TweaksConfig.ELDORATH_DMG_MULT.get().floatValue();
            case "armageddon_mod:elvenite_paladin" -> TweaksConfig.ELVENITE_PALADIN_DMG_MULT.get().floatValue();
            case "armageddon_mod:sanghor_lord_of_blood", "armageddon_mod:blood_orb" ->
                    TweaksConfig.SANGHOR_DMG_MULT.get().floatValue();
            case "armageddon_mod:sanghor_lord_of_blood_p_2", "armageddon_mod:sanghor_lord_of_bloodp_2",
                 "armageddon_mod:bloody_slash_entity" -> TweaksConfig.SANGHOR_P2_DMG_MULT.get().floatValue();
            case "armageddon_mod:the_chaos", "armageddon_mod:chaos_meteorite_projectile",
                 "armageddon_mod:chaos_sigil_circle" -> TweaksConfig.CHAOS_DMG_MULT.get().floatValue();
            case "armageddon_mod:the_famine", "armageddon_mod:the_famine_projectile" ->
                    TweaksConfig.FAMINE_DMG_MULT.get().floatValue();
            case "armageddon_mod:the_gobelin_lord", "armageddon_mod:little_mage_goblin",
                 "armageddon_mod:little_sword_goblin" -> TweaksConfig.GOBELIN_LORD_DMG_MULT.get().floatValue();
            case "armageddon_mod:the_iron_colossus" -> TweaksConfig.IRON_COLOSSUS_DMG_MULT.get().floatValue();
            case "armageddon_mod:vaedricthe_fallen_wanderer", "armageddon_mod:vaedric_the_fallen_wanderer",
                 "armageddon_mod:vaedric_projectile", "armageddon_mod:angry_enderman" ->
                    TweaksConfig.VAEDRIC_DMG_MULT.get().floatValue();
            default -> 1.0f;
        };

        if (multiplier != 1.0f) {
            event.setAmount(event.getAmount() * multiplier);
        }
    }
}