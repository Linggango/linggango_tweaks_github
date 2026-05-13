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

    public static final RegistryObject<SoundEvent> PERFECT_HIT = registerSound("perfect_hit");
    public static final RegistryObject<SoundEvent> HORROR_AMBIENCE = registerSound("horror_ambience");
    public static final RegistryObject<SoundEvent> DIMENSION_WIND = registerSound("dimension_wind");
    public static final RegistryObject<SoundEvent> HORROR_AMBIENCE_LOOP = registerSound("horror_ambience_loop");
    public static final RegistryObject<SoundEvent> ASCENSION_AMBIENCE = registerSound("ascension_ambience");
    public static final RegistryObject<SoundEvent> ASCENSION_ENTRANCE = registerSound("ascension_entrance");
    public static final RegistryObject<SoundEvent> ASCENSION_AFTERMATH = registerSound("ascension_aftermath");
    public static final RegistryObject<SoundEvent> WATER_STEP = registerSound("water_step");

    private static RegistryObject<SoundEvent> registerSound(String name) {
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(LinggangoTweaks.MOD_ID, name)));
    }

    public static void register(IEventBus eventBus) {
        SOUNDS.register(eventBus);
    }
}