package com.misanthropy.linggango.linggango_tweaks.bossbehavior;

import net.miauczel.legendary_monsters.entity.ModEntities;
import net.miauczel.legendary_monsters.entity.AnimatedMonster.IAnimatedBoss.TheObliterator.TheObliteratorEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import java.util.UUID;

public class ObliteratorBehavior extends BossBehavior<TheObliteratorEntity> {
    public ObliteratorBehavior() {
        super(1);
    }

    @Override
    public void onPhaseTick(TheObliteratorEntity obliterator, int phase) {
        if (isClone(obliterator)) {
            obliterator.setBossBarVisible(false);
            if (obliterator.tickCount >= 100) {
                LivingEntity boss = getMasterBoss(obliterator);
                if (boss == null || !boss.isAlive() || boss.isRemoved()) {
                    obliterator.discard();
                }
            }
        } else if (obliterator.tickCount % 20 == 0) {
            CompoundTag data = obliterator.getPersistentData();
            int p = obliterator.getPhase();
            if ((p == 2 && !data.getBoolean("Linggango_P2Flag") && obliterator.getTarget() != null) ||
                    (p == 3 && !data.getBoolean("Linggango_P3Flag") && obliterator.getTarget() != null)) {
                this.spawnClone(obliterator);
                data.putBoolean(p == 2 ? "Linggango_P2Flag" : "Linggango_P3Flag", true);
            }
        }
    }

    @Override
    public HurtResult onHurt(TheObliteratorEntity boss, DamageSource src, float amount) {
        return isClone(boss) ? HurtResult.cancel() : HurtResult.pass();
    }

    @Override
    public void onBossDied(TheObliteratorEntity boss) {
        if (boss.level() instanceof ServerLevel level) {
            UUID bossId = boss.getUUID();
            level.getEntitiesOfClass(TheObliteratorEntity.class, boss.getBoundingBox().inflate(256.0D),
                    e -> isClone(e) && bossId.equals(e.getPersistentData().getUUID("Linggango_MasterUUID"))
            ).forEach(TheObliteratorEntity::discard);
        }
    }

    private void spawnClone(TheObliteratorEntity boss) {
        if (!(boss.level() instanceof ServerLevel level)) return;

        TheObliteratorEntity clone = ModEntities.THE_OBLITERATOR.get().create(level);
        if (clone != null) {
            BlockPos spawnPos = boss.blockPosition().offset(2, 0, 2);

            clone.moveTo(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5, level.random.nextFloat() * 360F, 0);

            clone.getPersistentData().putBoolean("Linggango_IsClone", true);
            clone.getPersistentData().putUUID("Linggango_MasterUUID", boss.getUUID());

            clone.setHealth(clone.getMaxHealth());
            clone.setCustomName(Component.literal("Annihilation Clone").withStyle(ChatFormatting.RED));

            if (boss.getTarget() != null) clone.setTarget(boss.getTarget());
            level.addFreshEntity(clone);
            clone.setPhase(2);
        }
    }

    private boolean isClone(TheObliteratorEntity entity) {
        return entity.getPersistentData().getBoolean("Linggango_IsClone");
    }

    private LivingEntity getMasterBoss(TheObliteratorEntity clone) {
        if (!(clone.level() instanceof ServerLevel level)) return null;
        UUID masterId = clone.getPersistentData().getUUID("Linggango_MasterUUID");
        return (LivingEntity) level.getEntity(masterId);
    }
}