package com.misanthropy.linggango.linggango_tweaks.bossbehavior;

import com.github.L_Ender.cataclysm.entity.InternalAnimationMonster.IABossMonsters.Scylla.Scylla_Entity;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraftforge.registries.ForgeRegistries;

public class ScyllaBehavior extends BossBehavior<Scylla_Entity> {
    public ScyllaBehavior() {
        super(2);
    }

    @Override
    public void onPhaseTick(Scylla_Entity boss, int phase) {
        CompoundTag data = boss.getPersistentData();
        int p = boss.isPhase();

        if (p == 1 && !data.getBoolean("Linggango_P1Flag") && boss.getTarget() != null) {
            this.heal(boss);
            data.putBoolean("Linggango_P1Flag", true);
        }

        if (p == 2 && !data.getBoolean("Linggango_P2Flag") && boss.getTarget() != null) {
            this.heal(boss);
            this.castThunderstorm(boss);
            boss.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 200, 1));
            data.putBoolean("Linggango_P2Flag", true);

            data.putInt("Linggango_P2Delay", 60);
        }

        if (data.getBoolean("Linggango_P2Flag") && data.getInt("Linggango_P2Delay") > 0) {
            int delay = data.getInt("Linggango_P2Delay") - 1;
            data.putInt("Linggango_P2Delay", delay);

            if (delay <= 0) {
                data.putInt("Linggango_NimbusCount", 3);
                data.putInt("Linggango_NimbusTimer", 0);

            }
        }

        int nimbusCount = data.getInt("Linggango_NimbusCount");
        if (nimbusCount > 0) {
            int nimbusTimer = data.getInt("Linggango_NimbusTimer");

            if (nimbusTimer <= 0) {
                this.castNimbusArray(boss);
                data.putInt("Linggango_NimbusCount", nimbusCount - 1);
                data.putInt("Linggango_NimbusTimer", 30);

            } else {
                data.putInt("Linggango_NimbusTimer", nimbusTimer - 1);
            }
        }
    }

    private void heal(Scylla_Entity boss) {
        boss.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 9));
    }

    private void castThunderstorm(Scylla_Entity boss) {
        AbstractSpell storm = SpellRegistry.getSpell(new ResourceLocation("irons_spellbooks:thunderstorm"));
        if (storm != null && !storm.getSpellId().equals("irons_spellbooks:none")) {
            storm.onCast(boss.level(), 10, boss, CastSource.MOB, null);
        }

        MobEffect stormEffect = ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation("irons_spellbooks:thunderstorm"));
        if (stormEffect != null) {
            boss.addEffect(new MobEffectInstance(stormEffect, Integer.MAX_VALUE, 92, false, true, true));
        }
    }

    private void castNimbusArray(Scylla_Entity boss) {
        AbstractSpell nimbus = SpellRegistry.getSpell(new ResourceLocation("legendary_spellbooks:triple_nimbus_array"));
        if (nimbus != null && !nimbus.getSpellId().equals("irons_spellbooks:none")) {

            float origY = boss.getYRot();
            float origX = boss.getXRot();
            float origYHead = boss.yHeadRot;

            boss.setXRot(0);

            for (int i = 0; i < 8; i++) {
                float angle = i * 45f;
                boss.setYRot(angle);
                boss.yHeadRot = angle;
                nimbus.onCast(boss.level(), 10, boss, CastSource.MOB, null);
            }

            boss.setYRot(origY);
            boss.setXRot(origX);
            boss.yHeadRot = origYHead;
        }
    }
}