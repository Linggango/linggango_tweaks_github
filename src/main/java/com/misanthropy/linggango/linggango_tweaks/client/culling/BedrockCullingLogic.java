package com.misanthropy.linggango.linggango_tweaks.client.culling;

import com.misanthropy.linggango.linggango_tweaks.config.TweaksConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.Direction;

public class BedrockCullingLogic {

    public static boolean shouldCull(Direction facing, int height) {
        if (!TweaksConfig.ENABLE_BEDROCK_CULLING.get()) return false;

        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return false;

        if (TweaksConfig.CULL_BOTTOM_BEDROCK.get() && facing == Direction.DOWN && height == level.getMinBuildHeight()) {
            return true;
        }

        return TweaksConfig.CULL_TOP_BEDROCK.get() && facing == Direction.UP && height == level.getMaxBuildHeight() - 1 && level.dimensionType().hasCeiling();
    }
}