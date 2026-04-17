package com.misanthropy.linggango.linggango_tweaks.mixin;

import net.minecraft.client.Options;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

@Mixin(Options.class)
public class OptionsMixin {

    @Shadow @Final private File optionsFile;

    @Inject(method = "load", at = @At("HEAD"))
    private void onLoad(CallbackInfo ci) {
        if (this.optionsFile == null) {
            return;
        }

        File backupFile = new File(this.optionsFile.getParentFile(), "options_backup.txt");

        if (!this.optionsFile.exists() || this.optionsFile.length() < 10) {
            if (backupFile.exists() && backupFile.length() > 10) {
                try {
                    Files.copy(backupFile.toPath(), this.optionsFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            if (!backupFile.exists()) {
                try {
                    Files.copy(this.optionsFile.toPath(), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Inject(method = "save", at = @At("RETURN"))
    private void onSave(CallbackInfo ci) {
        if (this.optionsFile == null || !this.optionsFile.exists()) {
            return;
        }

        File backupFile = new File(this.optionsFile.getParentFile(), "options_backup.txt");

        try {
            Files.copy(this.optionsFile.toPath(), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}