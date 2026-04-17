package com.misanthropy.linggango.linggango_tweaks.mixin.performance;



import com.mojang.serialization.Lifecycle;

import net.minecraft.client.Minecraft;

import net.minecraft.client.gui.screens.Screen;

import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;

import net.minecraft.client.gui.screens.worldselection.WorldOpenFlows;

import org.spongepowered.asm.mixin.Mixin;

import org.spongepowered.asm.mixin.Shadow;

import org.spongepowered.asm.mixin.injection.At;

import org.spongepowered.asm.mixin.injection.Inject;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;



@Mixin(WorldOpenFlows.class)

public abstract class WorldOpenFlowsBypassMixin {



    @Shadow(aliases = "m_233145_")

    protected abstract void doLoadLevel(Screen screen, String levelName, boolean safeMode, boolean checkExperimental);



    @Inject(

            method = "confirmWorldCreation",

            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;setScreen(Lnet/minecraft/client/gui/screens/Screen;)V", ordinal = 0),

            cancellable = true

    )

    private static void linggango$bypassCreationWarning(Minecraft client, CreateWorldScreen parent, Lifecycle lifecycle, Runnable loader, boolean bypassWarnings, CallbackInfo ci) {

        loader.run();

        ci.cancel();

    }



    @Inject(method = "loadLevel", at = @At("HEAD"), cancellable = true)

    private void linggango$bypassLoadWarning(Screen screen, String levelName, CallbackInfo ci) {

        this.doLoadLevel(screen, levelName, false, false);

        ci.cancel();

    }

}