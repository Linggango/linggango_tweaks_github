package com.misanthropy.linggango.linggango_tweaks.mixin.embeddium;

import com.llamalad7.mixinextras.sugar.Local;
import com.misanthropy.linggango.linggango_tweaks.config.SpawnerClientConfig;
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
public class EmbeddiumOptionsMixin {

    @Shadow @Final private static SodiumOptionsStorage sodiumOpts;

    @Inject(
            method = "performance",
            at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", remap = false, ordinal = 0, shift = At.Shift.AFTER)
    )
    private static void addLinggangoSpawnerTweaks(CallbackInfoReturnable<OptionPage> cir, @Local(name = "groups") @NonNull List<OptionGroup> groups) {

        var particleOption = OptionImpl.createBuilder(Integer.TYPE, sodiumOpts)
                .setName(Component.literal("Spawner Particles (%)"))
                .setTooltip(Component.literal("Reduces spawner particles. Lower values improve FPS."))
                .setControl(option -> new SliderControl(option, 0, 100, 5, ControlValueFormatter.number()))
                .setBinding(
                        (opts, val) -> SpawnerClientConfig.PARTICLE_CHANCE.set(val),
                        (opts) -> SpawnerClientConfig.PARTICLE_CHANCE.get()
                )
                .setImpact(OptionImpact.VARIES)
                .build();

        var entityOption = OptionImpl.createBuilder(Boolean.TYPE, sodiumOpts)
                .setName(Component.literal("Hide Spawner Entity"))
                .setTooltip(Component.literal("Hides the spinning mob inside spawners unless you hold Shift."))
                .setControl(TickBoxControl::new)
                .setBinding(
                        (opts, val) -> SpawnerClientConfig.REQUIRE_SNEAK_FOR_ENTITY.set(val),
                        (opts) -> SpawnerClientConfig.REQUIRE_SNEAK_FOR_ENTITY.get()
                )
                .setImpact(OptionImpact.MEDIUM)
                .build();

        groups.add(OptionGroup.createBuilder().add(particleOption).add(entityOption).build());
    }
}