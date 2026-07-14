package com.misanthropy.linggango.linggango_tweaks.client;

import com.misanthropy.linggango.linggango_tweaks.LinggangoTweaks;
import com.misanthropy.linggango.linggango_tweaks.client.particle.AnimatedParryParticle;
import com.misanthropy.linggango.linggango_tweaks.client.particle.ParrySparkleParticle;
import com.misanthropy.linggango.linggango_tweaks.registry.particles.ModParticles;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jspecify.annotations.NonNull;

@Mod.EventBusSubscriber(modid = LinggangoTweaks.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEvents {

    @SubscribeEvent
    public static void registerProviders(@NonNull RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ModParticles.PARRY_SPARKLE.get(), ParrySparkleParticle.Provider::new);
        event.registerSpriteSet(ModParticles.ANIMATED_PARRY.get(), AnimatedParryParticle.Provider::new);
        event.registerSpriteSet(ModParticles.PERFECT_PARRY.get(), AnimatedParryParticle.Provider::new);
    }
}