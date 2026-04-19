package com.misanthropy.linggango.linggango_tweaks.mixin.tweaks;

import com.misanthropy.linggango.linggango_tweaks.tweaks.RenderCullingHandler;
import me.jellysquid.mods.sodium.client.gui.SodiumGameOptionPages;
import me.jellysquid.mods.sodium.client.gui.options.OptionGroup;
import me.jellysquid.mods.sodium.client.gui.options.OptionImpact;
import me.jellysquid.mods.sodium.client.gui.options.OptionImpl;
import me.jellysquid.mods.sodium.client.gui.options.OptionPage;
import me.jellysquid.mods.sodium.client.gui.options.control.ControlValueFormatter;
import me.jellysquid.mods.sodium.client.gui.options.control.SliderControl;
import me.jellysquid.mods.sodium.client.gui.options.storage.SodiumOptionsStorage;
import me.jellysquid.mods.sodium.client.render.chunk.RenderSection;
import me.jellysquid.mods.sodium.client.render.chunk.occlusion.OcclusionCuller;
import me.jellysquid.mods.sodium.client.render.viewport.CameraTransform;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;

@SuppressWarnings("unused")
public class RenderDistanceCullingMixin {

    @Mixin(Entity.class)
    public static class EntityCullMixin {
        @Redirect(method = "shouldRender", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;shouldRenderAtSqrDistance(D)Z"))
        private boolean adaptEntityRenderDist(@NonNull Entity instance, double orgSqDist, double x, double y, double z) {
            double adjusted = RenderCullingHandler.getAdjustedDistance(instance.getX(), instance.getY(), instance.getZ(), x, y, z);
            return instance.shouldRenderAtSqrDistance(adjusted);
        }
    }

    @Mixin(value = OcclusionCuller.class, remap = false)
    public static class SodiumCullMixin {
        @Inject(method = "isWithinRenderDistance", at = @At("HEAD"), cancellable = true)
        private static void adaptSodiumRenderDist(CameraTransform camera, @NonNull RenderSection section, float maxDistance, @NonNull CallbackInfoReturnable<Boolean> cir) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null) {
                double adjusted = RenderCullingHandler.getAdjustedDistance(
                        mc.player.getX(), mc.player.getY(), mc.player.getZ(),
                        section.getOriginX(), section.getOriginY(), section.getOriginZ()
                );

                if (adjusted > RenderCullingHandler.maxSqDist) {
                    cir.setReturnValue(false);
                }
            }
        }
    }

    @Mixin(value = SodiumGameOptionPages.class, remap = false)
    public static class EmbeddiumCullingOptionsMixin {

        @Shadow @Final private static SodiumOptionsStorage sodiumOpts;

        @Inject(
                method = "performance",
                at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", remap = false, ordinal = 0, shift = At.Shift.AFTER),
                locals = LocalCapture.CAPTURE_FAILHARD
        )
        private static void addLinggangoCullingTweaks(CallbackInfoReturnable<OptionPage> cir, @NonNull List<OptionGroup> groups) {

            var horizontalOption = OptionImpl.createBuilder(Integer.TYPE, sodiumOpts)
                    .setName(Component.literal("Horizontal Culling Stretch"))
                    .setTooltip(Component.literal("Reduces chunks rendered to the sides. Higher values improve FPS."))
                    .setControl(option -> new SliderControl(option, 100, 1000, 25, ControlValueFormatter.percentage()))
                    .setBinding(
                            (opts, val) -> {
                                RenderCullingHandler.horizontalStretch = val / 100.0;
                                RenderCullingHandler.save();
                            },
                            (opts) -> (int) (RenderCullingHandler.horizontalStretch * 100)
                    )
                    .setImpact(OptionImpact.HIGH)
                    .build();

            var verticalOption = OptionImpl.createBuilder(Integer.TYPE, sodiumOpts)
                    .setName(Component.literal("Vertical Culling Stretch"))
                    .setTooltip(Component.literal("Reduces chunks rendered above and below. Higher values improve FPS."))
                    .setControl(option -> new SliderControl(option, 100, 1000, 25, ControlValueFormatter.percentage()))
                    .setBinding(
                            (opts, val) -> {
                                RenderCullingHandler.verticalStretch = val / 100.0;
                                RenderCullingHandler.save();
                            },
                            (opts) -> (int) (RenderCullingHandler.verticalStretch * 100)
                    )
                    .setImpact(OptionImpact.HIGH)
                    .build();

            groups.add(OptionGroup.createBuilder().add(horizontalOption).add(verticalOption).build());
        }
    }
}