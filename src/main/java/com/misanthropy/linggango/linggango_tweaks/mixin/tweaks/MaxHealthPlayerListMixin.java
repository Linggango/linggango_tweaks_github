package com.misanthropy.linggango.linggango_tweaks.mixin.tweaks;

import java.util.Optional;

import com.misanthropy.linggango.linggango_tweaks.util.HealthFix;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(PlayerList.class)
public class MaxHealthPlayerListMixin {

    @Inject(
            method = "respawn",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;setHealth(F)V"),
            locals = LocalCapture.CAPTURE_FAILSOFT
    )
    private void linggango$onPlayerRespawn(ServerPlayer oldPlayer, boolean fromEnd, CallbackInfoReturnable<ServerPlayer> cbi, BlockPos respawnPos, float respawnAngle, boolean wasForced, ServerLevel oldDimension, Optional<Vec3> calculatedPos, ServerLevel overworld, ServerPlayer newPlayer) {
        if (newPlayer instanceof HealthFix fixable) {
            fixable.linggango$setRestorePoint(oldPlayer.getMaxHealth());
        }
    }
}