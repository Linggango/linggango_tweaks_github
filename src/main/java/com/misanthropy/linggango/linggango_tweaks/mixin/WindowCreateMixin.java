package com.misanthropy.linggango.linggango_tweaks.mixin;

import com.misanthropy.linggango.linggango_tweaks.tweaks.DarkWindowBar;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import net.minecraft.client.main.GameConfig;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class WindowCreateMixin {
    @Shadow @Final private Window window;

    @Inject(at = @At("TAIL"), method = "<init>")
    public void linggango$createWindow(GameConfig gameConfig, CallbackInfo ci) {
        DarkWindowBar.setDarkWindowBar(this.window);
    }
}