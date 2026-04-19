package com.misanthropy.linggango.linggango_tweaks.registry;

import com.misanthropy.linggango.linggango_tweaks.client.particle.AnimatedParryParticle;
import com.misanthropy.linggango.linggango_tweaks.client.particle.ParrySparkleParticle;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.jspecify.annotations.NonNull;

@Mod.EventBusSubscriber(modid = "linggango_tweaks", bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES = DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, "linggango_tweaks");

    public static final RegistryObject<SimpleParticleType> PARRY_O = PARTICLE_TYPES.register("parry_o_vfx", () -> new SimpleParticleType(true));
    public static final RegistryObject<SimpleParticleType> PARRY_SPARKLE = PARTICLE_TYPES.register("parry_sparkle", () -> new SimpleParticleType(true));
    public static final RegistryObject<SimpleParticleType> ANIMATED_PARRY = PARTICLE_TYPES.register("animated_parry", () -> new SimpleParticleType(true));
    public static final RegistryObject<SimpleParticleType> PERFECT_PARRY = PARTICLE_TYPES.register("perfect_parry", () -> new SimpleParticleType(true));

    public static void register(IEventBus eventBus) {
        PARTICLE_TYPES.register(eventBus);
    }

    @SubscribeEvent
    public static void registerProviders(@NonNull RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ModParticles.PARRY_SPARKLE.get(), ParrySparkleParticle.Provider::new);
        event.registerSpriteSet(ModParticles.ANIMATED_PARRY.get(), AnimatedParryParticle.Provider::new);
        event.registerSpriteSet(ModParticles.PERFECT_PARRY.get(), AnimatedParryParticle.Provider::new);
    }
}