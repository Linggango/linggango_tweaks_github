package com.misanthropy.linggango.linggango_tweaks.client.gui.options;

import com.misanthropy.linggango.linggango_tweaks.config.DisplayClientConfig;
import com.misanthropy.linggango.linggango_tweaks.config.SpawnerClientConfig;
import com.misanthropy.linggango.linggango_tweaks.tweaks.minecraft.RenderCullingHandler;
import me.jellysquid.mods.sodium.client.gui.options.storage.OptionStorage;

public class OptionsStorageForEmbeddium implements OptionStorage<Void> {

    public static final OptionsStorageForEmbeddium INSTANCE = new OptionsStorageForEmbeddium();

    @Override
    public Void getData() {
        return null;
    }

    @Override
    public void save() {
        DisplayClientConfig.SPEC.save();

        SpawnerClientConfig.SPEC.save();

        RenderCullingHandler.save();
    }
}