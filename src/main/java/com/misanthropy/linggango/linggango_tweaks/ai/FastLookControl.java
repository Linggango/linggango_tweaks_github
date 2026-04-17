package com.misanthropy.linggango.linggango_tweaks.ai;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.control.LookControl;
import java.util.Optional;

public class FastLookControl extends LookControl {

    public FastLookControl(Mob mob) {
        super(mob);
    }

    @Override
    @SuppressWarnings("NullableProblems")
    protected Optional<Float> getXRotD() {
        double d0 = this.wantedX - this.mob.getX();
        double d1 = this.wantedY - this.mob.getEyeY();
        double d2 = this.wantedZ - this.mob.getZ();
        double d3 = Math.sqrt(d0 * d0 + d2 * d2);
        return Optional.of((float) (-(FastTrig.atan2(d1, d3) * (double) (180F / (float) Math.PI))));
    }

    @Override
    @SuppressWarnings("NullableProblems")
    protected Optional<Float> getYRotD() {
        double d0 = this.wantedX - this.mob.getX();
        double d1 = this.wantedZ - this.mob.getZ();
        return Optional.of((float) (FastTrig.atan2(d1, d0) * (double) (180F / (float) Math.PI)) - 90.0F);
    }
}