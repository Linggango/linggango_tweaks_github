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
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Predicate;

@Mixin(Structure.class)
public class StructureDisableMixin {

    @Inject(method = "generate", at = @At("HEAD"), cancellable = true)
    private void linggango$disableSpecificStructures(
            @NonNull RegistryAccess registryAccess,
            ChunkGenerator chunkGenerator,
            BiomeSource biomeSource,
            RandomState randomState,
            StructureTemplateManager templateManager,
            long seed,
            ChunkPos chunkPos,
            int references,
            LevelHeightAccessor heightAccessor,
            Predicate<Holder<Biome>> validBiome,
            @NonNull CallbackInfoReturnable<StructureStart> cir
    ) {
        ResourceLocation id = registryAccess.registryOrThrow(Registries.STRUCTURE).getKey((Structure) (Object) this);
        if (id != null) {
            String path = id.toString();
            if (path.equals("create_structures_arise:darkcastle") ||
                    path.equals("create_structures_arise:obsidiantemple") ||
                    path.equals("structures:castle_war") ||
                    path.equals("terramity:suspicious_shrine")) {
                cir.setReturnValue(StructureStart.INVALID_START);
            }
        }
    }
}