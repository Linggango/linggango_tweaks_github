package com.misanthropy.linggango.linggango_tweaks.registry.particles;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES = DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, "linggango_tweaks");

    public static final RegistryObject<SimpleParticleType> PARRY_SPARKLE = PARTICLE_TYPES.register("parry_sparkle", () -> new SimpleParticleType(true));
    public static final RegistryObject<SimpleParticleType> ANIMATED_PARRY = PARTICLE_TYPES.register("animated_parry", () -> new SimpleParticleType(true));
    public static final RegistryObject<SimpleParticleType> PERFECT_PARRY = PARTICLE_TYPES.register("perfect_parry", () -> new SimpleParticleType(true));

    public static void register(IEventBus eventBus) {
        PARTICLE_TYPES.register(eventBus);
    }
}