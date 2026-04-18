package com.misanthropy.linggango.linggango_tweaks;

import com.misanthropy.linggango.linggango_tweaks.chaos.ChaosDifficultyAddon;
import com.misanthropy.linggango.linggango_tweaks.config.TweaksConfig;
import com.misanthropy.linggango.linggango_tweaks.config.SpawnerClientConfig;
import com.misanthropy.linggango.linggango_tweaks.enchant.LinggangoEnchantments;
import com.misanthropy.linggango.linggango_tweaks.fixes.LanguageRelatedCrashFixes;
import com.misanthropy.linggango.linggango_tweaks.network.ParryNetwork;
import com.misanthropy.linggango.linggango_tweaks.qol.LogSpamFilter;
import com.misanthropy.linggango.linggango_tweaks.registry.LinggangoAttributes;
import com.misanthropy.linggango.linggango_tweaks.registry.ModEntities;
import com.misanthropy.linggango.linggango_tweaks.registry.ModParticles;
import com.misanthropy.linggango.linggango_tweaks.skills.TweaksSkillNetwork;
import com.misanthropy.linggango.linggango_tweaks.structures.CleanWaterProcessor;
import com.misanthropy.linggango.linggango_tweaks.structures.GriddyStructureSystem;
import com.misanthropy.linggango.linggango_tweaks.tweaks.JeiSortStuff;
import com.misanthropy.linggango.linggango_tweaks.tweaks.LeashAllat;
import com.misanthropy.linggango.linggango_tweaks.tweaks.SpawnChanges;
import com.mojang.serialization.Codec;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacementType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod(LinggangoTweaks.MOD_ID)
public class LinggangoTweaks {
    public static final String MOD_ID = "linggango_tweaks";
    public static final DeferredRegister<Codec<? extends BiomeModifier>> BIOME_MODIFIERS = DeferredRegister.create(ForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS, MOD_ID);
    public static final RegistryObject<Codec<SpawnChanges>> SPAWN_CHANGES_CODEC = BIOME_MODIFIERS.register("spawn_changes", () -> Codec.unit(SpawnChanges.INSTANCE));
    public static final DeferredRegister<StructureProcessorType<?>> PROCESSORS = DeferredRegister.create(Registries.STRUCTURE_PROCESSOR, MOD_ID);
    public static final DeferredRegister<StructurePlacementType<?>> PLACEMENTS = DeferredRegister.create(Registries.STRUCTURE_PLACEMENT, MOD_ID);
    public static final RegistryObject<StructureProcessorType<CleanWaterProcessor>> CLEAN_WATER_PROCESSOR = PROCESSORS.register("clean_water", () -> () -> CleanWaterProcessor.CODEC);
    public static final RegistryObject<StructurePlacementType<GriddyStructureSystem>> EXACT_GRID_PLACEMENT = PLACEMENTS.register("exact_grid", () -> () -> GriddyStructureSystem.CODEC);
    public static final DeferredRegister<Codec<? extends IGlobalLootModifier>> LOOT_MODIFIERS = DeferredRegister.create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, "linggango_tweaks");
    public static final ResourceLocation APOSTLE_CONTEXT = new ResourceLocation(MOD_ID, "apostle_context");

    public LinggangoTweaks() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        BIOME_MODIFIERS.register(modEventBus);
        LinggangoEnchantments.ENCHANTMENTS.register(modEventBus);
        LOOT_MODIFIERS.register(FMLJavaModLoadingContext.get().getModEventBus());
        PROCESSORS.register(modEventBus);
        PLACEMENTS.register(modEventBus);
        TweaksConfig.register();
        ModParticles.register(FMLJavaModLoadingContext.get().getModEventBus());
        LanguageRelatedCrashFixes.fixLocale();
        JeiSortStuff.patchJeiSortOrder();
        SpawnChanges.init();
        LinggangoAttributes.ATTRIBUTES.register(modEventBus);
        LogSpamFilter.register();
        ModEntities.ENTITIES.register(modEventBus);
        TweaksSkillNetwork.register();
        ChaosDifficultyAddon.registerChaos();
        MinecraftForge.EVENT_BUS.register(this);
        ParryNetwork.register();
        MinecraftForge.EVENT_BUS.register(LeashAllat.class);
        MinecraftForge.EVENT_BUS.register(com.misanthropy.linggango.linggango_tweaks.bossbehavior.BossEventHandler.class);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, SpawnerClientConfig.SPEC, "linggango-client.toml");
    }
}