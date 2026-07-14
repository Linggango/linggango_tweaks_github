package com.misanthropy.linggango.linggango_tweaks.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


@OnlyIn(Dist.CLIENT)
public class AnimatedParryParticle extends TextureSheetParticle {
    private final SpriteSet sprites;
    private final float startSize;
    private final float peakSize;

    protected AnimatedParryParticle(@NonNull ClientLevel level, double x, double y, double z,
                                    SpriteSet spriteSet) {
        super(level, x, y, z, 0, 0, 0);
        this.sprites    = spriteSet;
        this.lifetime   = 10;
        this.startSize  = 2.4f;
        this.peakSize   = 1.4f;
        this.quadSize   = this.startSize;
        this.hasPhysics = false;
        this.setSpriteFromAge(this.sprites);
    }

    @Override
    public void tick() {
        this.quadSize = scaleForAge(this.age);
        super.tick();
        this.setSpriteFromAge(this.sprites);
    }

    private float scaleForAge(int age) {
        float p = this.lifetime <= 0 ? 1f : (float) age / (float) this.lifetime;
        return peakSize + (startSize - peakSize) * (float) Math.pow(1.0f - p, 2.0f);
    }

    @Override
    public @NonNull ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public int getLightColor(float partialTick) {
        return 15728880;
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet spriteSet;

        public Provider(SpriteSet spriteSet) { this.spriteSet = spriteSet; }

        @Nullable
        @Override
        public Particle createParticle(@NonNull SimpleParticleType type, @NonNull ClientLevel level,
                                       double x, double y, double z,
                                       double dx, double dy, double dz) {
            return new AnimatedParryParticle(level, x, y, z, this.spriteSet);
        }
    }
}