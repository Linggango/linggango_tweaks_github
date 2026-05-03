package com.misanthropy.linggango.linggango_tweaks.registry;

import com.misanthropy.linggango.linggango_tweaks.LinggangoTweaks;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class SoundRegistry {
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, LinggangoTweaks.MOD_ID);

    public static final RegistryObject<SoundEvent> PERFECT_HIT = SOUNDS.register("perfect_hit",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(LinggangoTweaks.MOD_ID, "perfect_hit")));

    public static void register(IEventBus eventBus) {
        SOUNDS.register(eventBus);
    }
}