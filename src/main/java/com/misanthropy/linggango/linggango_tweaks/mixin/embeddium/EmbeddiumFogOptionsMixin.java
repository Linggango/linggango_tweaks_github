package com.misanthropy.linggango.linggango_tweaks.mixin.embeddium;

import com.llamalad7.mixinextras.sugar.Local;
import com.misanthropy.linggango.linggango_tweaks.tweaks.DynamicFogHandler;
import me.jellysquid.mods.sodium.client.gui.SodiumGameOptionPages;
import me.jellysquid.mods.sodium.client.gui.options.OptionGroup;
import me.jellysquid.mods.sodium.client.gui.options.OptionImpact;
import me.jellysquid.mods.sodium.client.gui.options.OptionImpl;
import me.jellysquid.mods.sodium.client.gui.options.OptionPage;
import me.jellysquid.mods.sodium.client.gui.options.control.ControlValueFormatter;
import me.jellysquid.mods.sodium.client.gui.options.control.SliderControl;
import me.jellysquid.mods.sodium.client.gui.options.control.TickBoxControl;
import me.jellysquid.mods.sodium.client.gui.options.storage.SodiumOptionsStorage;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(value = SodiumGameOptionPages.class, remap = false)
public class EmbeddiumFogOptionsMixin {

    @Shadow @Final private static SodiumOptionsStorage sodiumOpts;

    @Inject(
            method = "quality",
            at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", remap = false, ordinal = 0, shift = At.Shift.AFTER)
    )
    private static void addLinggangoFogTweaks(CallbackInfoReturnable<OptionPage> cir, @Local(name = "groups") @NonNull List<OptionGroup> groups) {

        var enableFogOption = OptionImpl.createBuilder(Boolean.TYPE, sodiumOpts)
                .setName(Component.literal("Dynamic Atmospheric Fog"))
                .setTooltip(Component.literal("Enables the custom dynamic fog system that scales with render distance."))
                .setControl(TickBoxControl::new)
                .setBinding(
                        (opts, val) -> {
                            DynamicFogHandler.dynamicFogEnabled = val;
                            DynamicFogHandler.save();
                        },
                        (opts) -> DynamicFogHandler.dynamicFogEnabled
                )
                .setImpact(OptionImpact.LOW)
                .build();

        var fogStartOption = OptionImpl.createBuilder(Integer.TYPE, sodiumOpts)
                .setName(Component.literal("Fog Start Distance (%)"))
                .setTooltip(Component.literal("How far away the fog begins. Lower = more fog, Higher = clearer view."))
                .setControl(option -> new SliderControl(option, 0, 100, 1, ControlValueFormatter.percentage()))
                .setBinding(
                        (opts, val) -> {
                            DynamicFogHandler.fogStartMultiplier = val / 100.0;
                            DynamicFogHandler.save();
                        },
                        (opts) -> (int) (DynamicFogHandler.fogStartMultiplier * 100)
                )
                .setImpact(OptionImpact.LOW)
                .build();

        var enableVoidFogOption = OptionImpl.createBuilder(Boolean.TYPE, sodiumOpts)
                .setName(Component.literal("Atmospheric Void Fog"))
                .setTooltip(Component.literal("Darkens deep caves dynamically based on biome colors."))
                .setControl(TickBoxControl::new)
                .setBinding(
                        (opts, val) -> {
                            DynamicFogHandler.voidFogEnabled = val;
                            DynamicFogHandler.save();
                        },
                        (opts) -> DynamicFogHandler.voidFogEnabled
                )
                .setImpact(OptionImpact.LOW)
                .build();

        var rainFogDensityOption = OptionImpl.createBuilder(Integer.TYPE, sodiumOpts)
                .setName(Component.literal("Rain Mist Density (%)"))
                .setTooltip(Component.literal("How thick the fog gets during rainstorms. Lower = heavier mist."))
                .setControl(option -> new SliderControl(option, 0, 50, 1, ControlValueFormatter.percentage()))
                .setBinding(
                        (opts, val) -> {
                            DynamicFogHandler.rainFogDensity = val / 100.0;
                            DynamicFogHandler.save();
                        },
                        (opts) -> (int) (DynamicFogHandler.rainFogDensity * 100)
                )
                .setImpact(OptionImpact.LOW)
                .build();

        groups.add(OptionGroup.createBuilder()
                .add(enableFogOption)
                .add(fogStartOption)
                .add(enableVoidFogOption)
                .add(rainFogDensityOption)
                .build());
    }
}