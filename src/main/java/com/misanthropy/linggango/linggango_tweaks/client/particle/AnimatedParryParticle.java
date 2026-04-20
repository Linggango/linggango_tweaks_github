package com.misanthropy.linggango.linggango_tweaks.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;


@OnlyIn(Dist.CLIENT)
public class AnimatedParryParticle extends TextureSheetParticle {
    private final SpriteSet sprites;

    protected AnimatedParryParticle(@NonNull ClientLevel level, double x, double y, double z, SpriteSet spriteSet) {
        super(level, x, y, z, 0, 0, 0);
        this.sprites = spriteSet;
        this.lifetime = 10;
        this.quadSize = 1.5f;
        this.hasPhysics = false;
        this.setSpriteFromAge(this.sprites);
    }

    @Override
    public void tick() {
        super.tick();
        this.setSpriteFromAge(this.sprites);
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

        public Provider(SpriteSet spriteSet) {
            this.spriteSet = spriteSet;
        }

        @Nullable
        @Override
        public Particle createParticle(@NonNull SimpleParticleType type, @NonNull ClientLevel level, double x, double y, double z, double dx, double dy, double dz) {
            return new AnimatedParryParticle(level, x, y, z, this.spriteSet);
        }
    }
}