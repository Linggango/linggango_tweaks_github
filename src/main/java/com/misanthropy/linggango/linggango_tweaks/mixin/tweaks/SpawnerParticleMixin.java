package com.misanthropy.linggango.linggango_tweaks.mixin.tweaks;

import com.misanthropy.linggango.linggango_tweaks.config.SpawnerClientConfig;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.concurrent.ThreadLocalRandom;

@Mixin(BaseSpawner.class)
public class SpawnerParticleMixin {

    @Redirect(method = "clientTick",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)V"))
    private void optimizeSpawnerParticles(Level level, ParticleOptions particle, double x, double y, double z, double vx, double vy, double vz) {
        int renderChance = SpawnerClientConfig.PARTICLE_CHANCE.get();
        if (renderChance >= 100 || (renderChance > 0 && ThreadLocalRandom.current().nextInt(100) < renderChance)) {
            level.addParticle(particle, x, y, z, vx, vy, vz);
        }
    }
}