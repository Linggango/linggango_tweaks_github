package com.misanthropy.linggango.linggango_tweaks.bossbehavior;

import com.github.L_Ender.cataclysm.entity.AnimationMonster.BossMonsters.Ignis_Entity;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraftforge.registries.ForgeRegistries;

public class IgnisBehavior extends BossBehavior<Ignis_Entity> {
    public IgnisBehavior() {
        super(2);
    }

    @Override
    public void onPhaseTick(Ignis_Entity boss, int phase) {
        CompoundTag data = boss.getPersistentData();
        int p = boss.getBossPhase();

        if (p == 1 && !data.getBoolean("Linggango_P1Flag") && boss.getTarget() != null) {
            this.castMeteors(boss);
            data.putBoolean("Linggango_P1Flag", true);
        }

        if (p == 2 && !data.getBoolean("Linggango_P2Flag") && boss.getTarget() != null) {
            data.putBoolean("Linggango_P2Flag", true);

            data.putInt("Linggango_P2Delay", 60);
        }

        if (data.getBoolean("Linggango_P2Flag") && data.getInt("Linggango_P2Delay") > 0) {
            int delay = data.getInt("Linggango_P2Delay") - 1;
            data.putInt("Linggango_P2Delay", delay);

            if (delay <= 0) {
                data.putInt("Linggango_P2Timer", 200);

                data.putInt("Linggango_RH_Count", 5);

            }
        }

        int p2Timer = data.getInt("Linggango_P2Timer");
        if (p2Timer > 0) {

            if (p2Timer % 4 == 0) {
                AbstractSpell heatSurge = SpellRegistry.getSpell(new ResourceLocation("irons_spellbooks:heat_surge"));
                if (heatSurge != null && !heatSurge.getSpellId().equals("irons_spellbooks:none")) {
                    boss.setYRot(boss.level().random.nextFloat() * 360f);
                    boss.setXRot((boss.level().random.nextFloat() * 90f) - 45f);
                    heatSurge.onCast(boss.level(), 10, boss, CastSource.MOB, null);
                }
            }

            int rhCount = data.getInt("Linggango_RH_Count");
            if (p2Timer % 40 == 0 && rhCount > 0) {
                AbstractSpell raiseHell = SpellRegistry.getSpell(new ResourceLocation("irons_spellbooks:raise_hell"));
                if (raiseHell != null && !raiseHell.getSpellId().equals("irons_spellbooks:none")) {
                    raiseHell.onCast(boss.level(), 10, boss, CastSource.MOB, null);
                }
                data.putInt("Linggango_RH_Count", rhCount - 1);
            }

            data.putInt("Linggango_P2Timer", p2Timer - 1);
        }
    }

    private void castMeteors(Ignis_Entity boss) {
        AbstractSpell meteorStorm = SpellRegistry.getSpell(new ResourceLocation("traveloptics:meteor_storm"));
        if (meteorStorm != null && !meteorStorm.getSpellId().equals("irons_spellbooks:none")) {
            meteorStorm.onCast(boss.level(), 10, boss, CastSource.MOB, null);
        }

        MobEffect meteorEffect = ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation("traveloptics:meteor_storm"));
        if (meteorEffect != null) {
            boss.addEffect(new MobEffectInstance(meteorEffect, 400, 150));
        }
        boss.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 400, 1));
    }
}