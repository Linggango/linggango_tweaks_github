package com.misanthropy.linggango.linggango_tweaks.bossbehavior;

import com.github.L_Ender.cataclysm.entity.InternalAnimationMonster.IABossMonsters.Maledictus.Maledictus_Entity;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraftforge.registries.ForgeRegistries;

public class MaledictusBehavior extends BossBehavior<Maledictus_Entity> {
    public MaledictusBehavior() {
        super(2);
    }

    @Override
    public void onPhaseTick(Maledictus_Entity boss, int phase) {

        if (boss.tickCount % 400 == 0 && boss.getTarget() != null) {
            AbstractSpell blackout = SpellRegistry.getSpell(new ResourceLocation("traveloptics:blackout"));
            if (blackout != null && !blackout.getSpellId().equals("irons_spellbooks:none")) {
                blackout.onCast(boss.level(), 2, boss, CastSource.MOB, null);
            }
        }
    }

    @Override
    public void onPhaseChange(Maledictus_Entity boss, int newPhase, int oldPhase, boolean firstTime) {
        if (firstTime && newPhase == 2) {
            MobEffect rage = ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation("traveloptics:phantom_rage"));
            if (rage != null) boss.addEffect(new MobEffectInstance(rage, Integer.MAX_VALUE, 0));

            boss.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, Integer.MAX_VALUE, 0));
        }
    }
}