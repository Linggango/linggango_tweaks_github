package com.misanthropy.linggango.linggango_tweaks.bossbehavior;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

import java.util.*;

public abstract class BossBehavior<T extends LivingEntity> {
    public final int phaseCount;
    public static final Map<LivingEntity, Set<UUID>> MINIONS = new WeakHashMap<>();

    protected BossBehavior(int phaseCount) {
        this.phaseCount = phaseCount;
    }

    public void onPhaseTick(T boss, int phase) {}

    public void onPhaseChange(T boss, int newPhase, int oldPhase, boolean firstTime) {}

    public HurtResult onHurt(T boss, DamageSource src, float amount) {
        return HurtResult.pass();
    }

    public void onBossDied(T boss) {}

    public void onMinionAdded(T boss, Mob minion) {}

    public static class HurtResult {
        public final boolean cancel;
        public final boolean modify;
        public final float newAmount;

        private HurtResult(boolean cancel, boolean modify, float newAmount) {
            this.cancel = cancel;
            this.modify = modify;
            this.newAmount = newAmount;
        }

        public static HurtResult pass() { return new HurtResult(false, false, 0.0F); }
        public static HurtResult cancel() { return new HurtResult(true, false, 0.0F); }
        public static HurtResult modify(float amount) { return new HurtResult(false, true, amount); }
    }
}