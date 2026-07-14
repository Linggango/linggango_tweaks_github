//package com.misanthropy.linggango.linggango_tweaks.registry.features;
//
//import com.misanthropy.linggango.linggango_tweaks.LinggangoTweaks;
//import com.misanthropy.linggango.linggango_tweaks.features.CustomTreeConfig;
//import com.misanthropy.linggango.linggango_tweaks.features.CustomTreeGenerationFeature;
//import net.minecraft.world.level.levelgen.feature.Feature;
//import net.minecraftforge.eventbus.api.IEventBus;
//import net.minecraftforge.registries.DeferredRegister;
//import net.minecraftforge.registries.ForgeRegistries;
//import net.minecraftforge.registries.RegistryObject;
//
//public class ModFeatures {
//    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(ForgeRegistries.FEATURES, LinggangoTweaks.MOD_ID);
//
//    public static void register(IEventBus eventBus) {
//        FEATURES.register(eventBus);
//    }
//
//    public static final RegistryObject<CustomTreeGenerationFeature> CUSTOM_TREE = FEATURES.register("custom_tree",
//            () -> new CustomTreeGenerationFeature(CustomTreeConfig.CODEC));
//}