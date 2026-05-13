package com.misanthropy.linggango.linggango_tweaks.registry;

import com.misanthropy.linggango.linggango_tweaks.LinggangoTweaks;
import com.misanthropy.linggango.linggango_tweaks.features.CustomTreeConfiguration;
import com.misanthropy.linggango.linggango_tweaks.features.CustomTreeFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModFeatures {
    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(ForgeRegistries.FEATURES, LinggangoTweaks.MOD_ID);

    public static void register(IEventBus eventBus) {
        FEATURES.register(eventBus);
    }

    public static final RegistryObject<CustomTreeFeature> CUSTOM_TREE = FEATURES.register("custom_tree",
            () -> new CustomTreeFeature(CustomTreeConfiguration.CODEC));
}