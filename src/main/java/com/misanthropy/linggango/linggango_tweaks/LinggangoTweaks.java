package com.misanthropy.linggango.linggango_tweaks;

import com.misanthropy.linggango.linggango_tweaks.chaos.ChaosDifficultyAddon;
import com.misanthropy.linggango.linggango_tweaks.config.TweaksConfig;
import com.misanthropy.linggango.linggango_tweaks.config.SpawnerClientConfig;
import com.misanthropy.linggango.linggango_tweaks.enchant.LinggangoEnchantments;
import com.misanthropy.linggango.linggango_tweaks.fixes.LanguageRelatedCrashFixes;
import com.misanthropy.linggango.linggango_tweaks.loot.BalancedLootRandomizerModifier;
import com.misanthropy.linggango.linggango_tweaks.network.ParryNetwork;
import com.misanthropy.linggango.linggango_tweaks.qol.LogSpamFilter;
import com.misanthropy.linggango.linggango_tweaks.registry.LinggangoAttributes;
import com.misanthropy.linggango.linggango_tweaks.registry.ModParticles;
import com.misanthropy.linggango.linggango_tweaks.skills.TweaksSkillNetwork;
import com.misanthropy.linggango.linggango_tweaks.structures.CleanWaterProcessor;
import com.misanthropy.linggango.linggango_tweaks.structures.GriddyStructureSystem;
import com.misanthropy.linggango.linggango_tweaks.tweaks.JeiSortStuff;
import com.misanthropy.linggango.linggango_tweaks.tweaks.LeashAllat;
import com.misanthropy.linggango.linggango_tweaks.tweaks.SpawnChanges;
import com.mojang.serialization.Codec;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacementType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@SuppressWarnings("unused")
@Mod(LinggangoTweaks.MOD_ID)
public class LinggangoTweaks {
    public static final String MOD_ID = "linggango_tweaks";
    public static final DeferredRegister<Codec<? extends BiomeModifier>> BIOME_MODIFIERS = DeferredRegister.create(ForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS, MOD_ID);
    public static final RegistryObject<Codec<SpawnChanges>> SPAWN_CHANGES_CODEC = BIOME_MODIFIERS.register("spawn_changes", () -> Codec.unit(SpawnChanges.INSTANCE));
    public static final DeferredRegister<StructureProcessorType<?>> PROCESSORS = DeferredRegister.create(Registries.STRUCTURE_PROCESSOR, MOD_ID);
    public static final RegistryObject<StructureProcessorType<CleanWaterProcessor>> CLEAN_WATER_PROCESSOR = PROCESSORS.register("clean_water", () -> () -> CleanWaterProcessor.CODEC);
    public static final DeferredRegister<StructurePlacementType<?>> PLACEMENTS = DeferredRegister.create(Registries.STRUCTURE_PLACEMENT, MOD_ID);
    public static final RegistryObject<StructurePlacementType<GriddyStructureSystem>> EXACT_GRID_PLACEMENT = PLACEMENTS.register("exact_grid", () -> () -> GriddyStructureSystem.CODEC);
    public static final DeferredRegister<Codec<? extends IGlobalLootModifier>> LOOT_MODIFIERS = DeferredRegister.create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, MOD_ID);
    public static final RegistryObject<Codec<BalancedLootRandomizerModifier>> BALANCED_RANDOMIZER = LOOT_MODIFIERS.register("balanced_loot_randomizer", () -> BalancedLootRandomizerModifier.CODEC);
    public static final ResourceLocation APOSTLE_CONTEXT = new ResourceLocation(MOD_ID, "apostle_context");

    public LinggangoTweaks() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        BIOME_MODIFIERS.register(modEventBus);
        LinggangoEnchantments.ENCHANTMENTS.register(modEventBus);
        LOOT_MODIFIERS.register(modEventBus);
        PROCESSORS.register(modEventBus);
        PLACEMENTS.register(modEventBus);
        LinggangoAttributes.ATTRIBUTES.register(modEventBus);
        ModParticles.register(modEventBus);
        TweaksConfig.register();
        LanguageRelatedCrashFixes.fixLocale();
        JeiSortStuff.patchJeiSortOrder();
        SpawnChanges.init();
        LogSpamFilter.register();
        TweaksSkillNetwork.register();
        ChaosDifficultyAddon.registerChaos();
        ParryNetwork.register();
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(LeashAllat.class);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, SpawnerClientConfig.SPEC, "linggango-client.toml");
    }
}