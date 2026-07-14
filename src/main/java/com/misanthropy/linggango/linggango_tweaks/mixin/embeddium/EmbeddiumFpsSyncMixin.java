package com.misanthropy.linggango.linggango_tweaks.mixin.embeddium;

import com.llamalad7.mixinextras.sugar.Local;
import com.misanthropy.linggango.linggango_tweaks.client.gui.options.OptionsStorageForEmbeddium;
import com.misanthropy.linggango.linggango_tweaks.config.DisplayClientConfig;
import me.jellysquid.mods.sodium.client.gui.SodiumGameOptionPages;
import me.jellysquid.mods.sodium.client.gui.options.OptionGroup;
import me.jellysquid.mods.sodium.client.gui.options.OptionImpact;
import me.jellysquid.mods.sodium.client.gui.options.OptionImpl;
import me.jellysquid.mods.sodium.client.gui.options.OptionPage;
import me.jellysquid.mods.sodium.client.gui.options.control.TickBoxControl;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(value = SodiumGameOptionPages.class, remap = false)
public class EmbeddiumFpsSyncMixin {

    @Inject(
            method = "general",
            at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", remap = false, ordinal = 0, shift = At.Shift.AFTER)
    )
    private static void addLinggangoAutoFps(CallbackInfoReturnable<OptionPage> cir, @Local(name = "groups") @NonNull List<OptionGroup> groups) {

        var autoFpsOption = OptionImpl.createBuilder(Boolean.TYPE, OptionsStorageForEmbeddium.INSTANCE)
                .setName(Component.literal("Auto-Sync FPS to Monitor"))
                .setTooltip(Component.literal("Automatically locks max framerate to your refresh rate. Possibly improving input latency and frame stability."))
                .setControl(TickBoxControl::new)
                .setBinding(
                        (opts, val) -> {
                            DisplayClientConfig.AUTO_FPS_SYNC.set(val);

                            if (val) {
                                int refreshRate = Minecraft.getInstance().getWindow().getRefreshRate();
                                if (refreshRate < 30) {
                                    refreshRate = 60;
                                }
                                Minecraft.getInstance().options.framerateLimit().set(refreshRate);
                                Minecraft.getInstance().options.enableVsync().set(true);
                                Minecraft.getInstance().options.save();
                            } else {
                                Minecraft.getInstance().options.enableVsync().set(false);
                                Minecraft.getInstance().options.save();
                            }
                        },
                        (opts) -> DisplayClientConfig.AUTO_FPS_SYNC.get()
                )
                .setImpact(OptionImpact.LOW)
                .build();
        groups.add(1, OptionGroup.createBuilder().add(autoFpsOption).build());
    }
}