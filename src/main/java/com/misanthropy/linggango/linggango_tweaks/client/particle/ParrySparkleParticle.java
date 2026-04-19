package com.misanthropy.linggango.linggango_tweaks.client.particle;

import com.misanthropy.linggango.linggango_tweaks.registry.ModParticles;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@OnlyIn(Dist.CLIENT)
public class ParrySparkleParticle extends TextureSheetParticle {

    protected ParrySparkleParticle(ClientLevel level, double x, double y, double z, double vx, double vy, double vz, SpriteSet spriteSet) {
        super(level, x, y, z, vx, vy, vz);

        this.friction = 0.98f;
        this.gravity = 0.0f;
        this.lifetime = 10 + this.random.nextInt(10);
        this.quadSize = 0.35f + this.random.nextFloat() * 0.15f;
        this.setSpriteFromAge(spriteSet);

        float b = 0.95f + this.random.nextFloat() * 0.05f;
        this.rCol = b;
        this.gCol = b;
        this.bCol = b;
        this.alpha = 1.0f;

        this.hasPhysics = false;
    }

    public static void spawnExplosion(int tier, Vec3 pos) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        RandomSource random = mc.level.random;

        int numLines = (tier == 3 ? 5 : 2) + random.nextInt(tier == 3 ? 7 : 5);
        double baseSpeed = (tier == 3 ? 0.35 : 0.25) + random.nextDouble() * 0.15;

        for (int l = 0; l < numLines; l++) {
            double angle = random.nextDouble() * Math.PI * 2.0;

            double dx = Math.cos(angle);
            double dz = Math.sin(angle);

            double upwardAngle = Math.toRadians(45 + random.nextDouble() * 15);
            double horizontalSpeed = baseSpeed * Math.cos(upwardAngle);
            double verticalSpeed = baseSpeed * Math.sin(upwardAngle);

            Vec3 velocity = new Vec3(
                    dx * horizontalSpeed,
                    verticalSpeed,
                    dz * horizontalSpeed
            );

            mc.level.addParticle(ModParticles.PARRY_SPARKLE.get(), pos.x, pos.y, pos.z, velocity.x, velocity.y, velocity.z);
        }

        mc.level.addParticle(ModParticles.PARRY_O.get(), pos.x, pos.y, pos.z, 0, 0, 0);
    }

    @Override
    public void tick() {
        super.tick();

        float progress = (float) this.age / (float) this.lifetime;
        if (progress > 0.6f) {
            this.alpha = 1.0f - ((progress - 0.6f) / 0.4f);
        }

        this.quadSize *= 0.985f;
    }

    @Override
    public @NotNull ParticleRenderType getRenderType() {
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
        public Particle createParticle(@NotNull SimpleParticleType type, @NotNull ClientLevel level, double x, double y, double z, double dx, double dy, double dz) {
            return new ParrySparkleParticle(level, x, y, z, dx, dy, dz, this.spriteSet);
        }
    }
}