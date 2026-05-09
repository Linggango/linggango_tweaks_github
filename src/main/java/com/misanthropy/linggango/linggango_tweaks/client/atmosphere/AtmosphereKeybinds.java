package com.misanthropy.linggango.linggango_tweaks.client.atmosphere;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

public class AtmosphereKeybinds {

    public static final KeyMapping OPEN_EDITOR = new KeyMapping(
            "key.linggango_tweaks.open_atmosphere_editor",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_F8, // Defaults to F8
            "key.categories.linggango_tweaks"
    );

    @Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ModBusEvents {
        @SubscribeEvent
        public static void registerKeyBindings(RegisterKeyMappingsEvent event) {
            event.register(OPEN_EDITOR);
        }
    }

    @Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class ForgeBusEvents {
        @SubscribeEvent
        public static void onKeyInput(InputEvent.Key event) {
            if (OPEN_EDITOR.consumeClick()) {
                Minecraft mc = Minecraft.getInstance();
                if (mc.player != null && mc.level != null) {
                    Holder<Biome> biomeHolder = mc.level.getBiome(mc.player.blockPosition());
                    ResourceLocation biomeId = biomeHolder.unwrapKey()
                            .map(ResourceKey::location)
                            .orElseGet(() -> mc.level.registryAccess().registryOrThrow(Registries.BIOME).getKey(biomeHolder.get()));

                    mc.setScreen(new AtmosphereEditorScreen(biomeId));
                }
            }
        }
    }
}