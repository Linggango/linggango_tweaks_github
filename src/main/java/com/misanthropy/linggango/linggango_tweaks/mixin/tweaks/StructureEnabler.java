package com.misanthropy.linggango.linggango_tweaks.mixin.tweaks;

import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;
import java.util.function.Predicate;

@Mixin(Structure.class)
public class StructureEnabler {

    @Inject(method = "generate", at = @At("HEAD"), cancellable = true)
    private void linggango$protectDreamwoods(
            RegistryAccess registryAccess,
            ChunkGenerator chunkGenerator,
            BiomeSource biomeSource,
            RandomState randomState,
            StructureTemplateManager templateManager,
            long seed,
            ChunkPos chunkPos,
            int references,
            LevelHeightAccessor heightAccessor,
            Predicate<Holder<Biome>> validBiome,
            CallbackInfoReturnable<StructureStart> cir
    ) {
        Holder<Biome> biome = biomeSource.getNoiseBiome(
                chunkPos.getBlockX(8) >> 2,
                chunkGenerator.getSeaLevel() >> 2,
                chunkPos.getBlockZ(8) >> 2,
                randomState.sampler()
        );

        Optional<String> biomeNamespace = biome.unwrapKey().map(key -> key.location().getNamespace());
        if (biomeNamespace.isPresent() && biomeNamespace.get().equals("dreamwoods")) {
            ResourceLocation structureId = registryAccess.registryOrThrow(Registries.STRUCTURE).getKey((Structure) (Object) this);
            if (structureId != null && !structureId.getNamespace().equals("dreamwoods")) {
                cir.setReturnValue(StructureStart.INVALID_START);
            }
        }
    }
}