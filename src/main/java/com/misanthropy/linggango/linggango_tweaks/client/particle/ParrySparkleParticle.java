package com.misanthropy.linggango.linggango_tweaks.client.particle;

import com.misanthropy.linggango.linggango_tweaks.registry.particles.ModParticles;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


@OnlyIn(Dist.CLIENT)
public class ParrySparkleParticle extends TextureSheetParticle {

    protected ParrySparkleParticle(@NonNull ClientLevel level, double x, double y, double z,
                                   double vx, double vy, double vz, @NonNull SpriteSet spriteSet) {
        super(level, x, y, z, vx, vy, vz);

        this.friction   = 0.98f;
        this.gravity    = 0.0f;
        this.lifetime   = 10 + this.random.nextInt(10);
        this.quadSize   = 0.35f + this.random.nextFloat() * 0.15f;
        this.pickSprite(spriteSet);

        float b = 0.95f + this.random.nextFloat() * 0.05f;
        this.rCol  = b;
        this.gCol  = b;
        this.bCol  = b;
        this.alpha = 1.0f;

        this.hasPhysics = false;
    }

    public static void spawnExplosion(int tier, @NonNull Vec3 pos, int comboStage) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        RandomSource random = mc.level.random;
        int stage = Math.max(1, comboStage);
        float comboMultiplier = 1.0f + ((stage - 1) / 7.0f);

        int baseLines = (tier == 3 ? 6 : 2) + random.nextInt(tier == 3 ? 7 : 5);
        int numLines  = (int)(baseLines * comboMultiplier);
        double baseSpeed = ((tier == 3 ? 0.38 : 0.25) + random.nextDouble() * 0.15) * comboMultiplier;

        for (int l = 0; l < numLines; l++) {
            double angle          = random.nextDouble() * Math.PI * 2.0;
            double dx             = Math.cos(angle);
            double dz             = Math.sin(angle);
            double upwardAngle    = Math.toRadians(45 + random.nextDouble() * 15);
            double horizontalSpeed = baseSpeed * Math.cos(upwardAngle);
            double verticalSpeed   = baseSpeed * Math.sin(upwardAngle);

            mc.level.addParticle(ModParticles.PARRY_SPARKLE.get(),
                    pos.x, pos.y, pos.z,
                    dx * horizontalSpeed, verticalSpeed, dz * horizontalSpeed);
        }

        int ringParticles = (tier == 3 ? 18 : 8) + Math.min(stage - 1, 7) * 2;
        double ringSpeed  = (tier == 3 ? 0.55 : 0.32) * comboMultiplier;
        for (int i = 0; i < ringParticles; i++) {
            double angle = ((double) i / ringParticles) * Math.PI * 2.0;
            double dx = Math.cos(angle);
            double dz = Math.sin(angle);
            mc.level.addParticle(ModParticles.PARRY_SPARKLE.get(),
                    pos.x, pos.y, pos.z,
                    dx * ringSpeed, 0.02, dz * ringSpeed);
        }

        if (tier == 3) {
            for (int i = 0; i < 6; i++) {
                mc.level.addParticle(ParticleTypes.FLASH,
                        pos.x + random.nextGaussian() * 0.1,
                        pos.y + random.nextGaussian() * 0.1,
                        pos.z + random.nextGaussian() * 0.1, 0, 0, 0);
            }
            for (int i = 0; i < 10; i++) {
                mc.level.addParticle(ParticleTypes.END_ROD,
                        pos.x + random.nextGaussian() * 0.2,
                        pos.y + random.nextGaussian() * 0.2,
                        pos.z + random.nextGaussian() * 0.2,
                        random.nextGaussian() * 0.05,
                        random.nextDouble() * 0.08,
                        random.nextGaussian() * 0.05);
            }
        }

        if (comboStage >= 4 && comboStage <= 5) {
            for (int i = 0; i < 10; i++) {
                mc.level.addParticle(ParticleTypes.WITCH,
                        pos.x + random.nextGaussian() * 0.2,
                        pos.y + random.nextGaussian() * 0.2,
                        pos.z + random.nextGaussian() * 0.2, 0, 0, 0);
            }
            for (int i = 0; i < 5; i++) {
                mc.level.addParticle(ParticleTypes.ENCHANTED_HIT,
                        pos.x + random.nextGaussian() * 0.3,
                        pos.y + random.nextGaussian() * 0.3,
                        pos.z + random.nextGaussian() * 0.3, 0, 0, 0);
            }
        } else if (comboStage >= 6) {
            for (int i = 0; i < 15; i++) {
                mc.level.addParticle(ParticleTypes.REVERSE_PORTAL,
                        pos.x + random.nextGaussian() * 0.25,
                        pos.y + random.nextGaussian() * 0.25,
                        pos.z + random.nextGaussian() * 0.25, 0, 0, 0);
            }
            for (int i = 0; i < 8; i++) {
                mc.level.addParticle(ParticleTypes.DRAGON_BREATH,
                        pos.x + random.nextGaussian() * 0.15,
                        pos.y + random.nextGaussian() * 0.15,
                        pos.z + random.nextGaussian() * 0.15, 0, 0, 0);
            }
        }

        SimpleParticleType mainParticle = tier == 3
                ? ModParticles.PERFECT_PARRY.get()
                : ModParticles.ANIMATED_PARRY.get();
        mc.level.addParticle(mainParticle, pos.x, pos.y, pos.z, 0, 0, 0);
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
            return new ParrySparkleParticle(level, x, y, z, dx, dy, dz, this.spriteSet);
        }
    }
}