package com.misanthropy.linggango.linggango_tweaks.mixin.performance;

import com.misanthropy.linggango.linggango_tweaks.config.SpawnerClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.blockentity.SpawnerRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(SpawnerRenderer.class)
public class SpawnerRenderMixin {

    @Redirect(
            method = "render(Lnet/minecraft/world/level/block/entity/SpawnerBlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/BaseSpawner;getOrCreateDisplayEntity(Lnet/minecraft/world/level/Level;Lnet/minecraft/util/RandomSource;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/entity/Entity;")
    )
    private Entity hideSpinningEntity(BaseSpawner spawner, Level level, RandomSource random, BlockPos pos) {
        if (SpawnerClientConfig.REQUIRE_SNEAK_FOR_ENTITY.get()) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null || !mc.player.isShiftKeyDown()) {
                return null;
            }
        }
        return spawner.getOrCreateDisplayEntity(level, random, pos);
    }
}