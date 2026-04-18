package com.misanthropy.linggango.linggango_tweaks.mixin.performance;

import com.misanthropy.linggango.linggango_tweaks.config.TweaksConfig;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedDeque;

@Mixin(StructureTemplateManager.class) public abstract class StructureOptimizationAttempt {

    @Shadow(aliases = {"f_230345_", "templates", "structureRepository"})
    @Final private Map<ResourceLocation, Optional<StructureTemplate>> f_230345_;

    @Unique private final ConcurrentLinkedDeque<ResourceLocation> linggango$accessOrder = new ConcurrentLinkedDeque<>();

    @Inject(method = "get", at = @At("RETURN"))private void linggango$limitCache(ResourceLocation id, CallbackInfoReturnable<Optional<StructureTemplate>> cir) {
        if (TweaksConfig.limitStructureCache.get() && cir.getReturnValue().isPresent()) {
            linggango$accessOrder.remove(id);
            linggango$accessOrder.addLast(id);
            while (linggango$accessOrder.size() > TweaksConfig.structureCacheSize.get()) {
                ResourceLocation oldest = linggango$accessOrder.pollFirst();
                if (oldest != null && !oldest.equals(id)) {
                    f_230345_.remove(oldest);
                }
            }
        }
    }

    @Inject(method = "onResourceManagerReload", at = @At("HEAD")) private void linggango$clearCache(ResourceManager resourceManager, CallbackInfo ci) {linggango$accessOrder.clear();
    }
}