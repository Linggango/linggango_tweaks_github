package com.misanthropy.linggango.linggango_tweaks.mixin.tweaks;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

@Mixin(TitleScreen.class)
public class StartupSoundMixin {

    @Unique private static boolean linggango$hasPlayed = false;
    @Unique private static boolean linggango$waiting = false;
    @Unique private static long linggango$initTime = 0;

    @Unique private static final String[] linggango$STARTUP_SOUNDS = {
            "create:confirm",
            "create:confirm_2",
            "create:fwoomp",
            "fdbosses:attack_ding",
            "pingwheel:ping",
            "plushie_buddies:plushie_sound",
            "alexsmobs:sculk_boomer_fart",
            "relics:ricochet",
            "minecraft:block.large_amethyst_bud.break",
            "brutality:big_explosion",
            "minecraft:block.note_block.guitar",
            "minecraft:entity.experience_orb.pickup"
    };

    @Inject(method = "init", at = @At("HEAD"))
    private void linggango$startDelayTimer(CallbackInfo ci) {
        if (!linggango$hasPlayed && !linggango$waiting) {
            linggango$initTime = Util.getMillis();
            linggango$waiting = true;
        }
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void linggango$playDelayedSound(CallbackInfo ci) {
        if (linggango$waiting && Util.getMillis() - linggango$initTime >= 1000) {
            Random random = new Random();
            String soundId = linggango$STARTUP_SOUNDS[random.nextInt(linggango$STARTUP_SOUNDS.length)];
            ResourceLocation location = new ResourceLocation(soundId);
            SoundEvent sound = ForgeRegistries.SOUND_EVENTS.getValue(location);

            if (sound != null) {
                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(sound, 1.0F));
            }
            linggango$hasPlayed = true;
            linggango$waiting = false;
        }
    }
}