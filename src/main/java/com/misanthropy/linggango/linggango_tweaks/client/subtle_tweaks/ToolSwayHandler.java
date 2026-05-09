package com.misanthropy.linggango.linggango_tweaks.client.subtle_tweaks;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ToolSwayHandler {

    private static float lastYaw = 0;
    private static float lastPitch = 0;

    private static float swayYaw = 0;
    private static float swayPitch = 0;

    @SubscribeEvent
    public static void onRenderTick(TickEvent.RenderTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || mc.isPaused()) return;

        float yaw = player.getViewYRot(event.renderTickTime);
        float pitch = player.getViewXRot(event.renderTickTime);

        float deltaYaw = yaw - lastYaw;
        float deltaPitch = pitch - lastPitch;

        deltaYaw = Mth.wrapDegrees(deltaYaw);
        deltaPitch = Mth.wrapDegrees(deltaPitch);

        if (Math.abs(deltaYaw) > 45 || Math.abs(deltaPitch) > 45) {
            deltaYaw = 0;
            deltaPitch = 0;
        }

        swayYaw += deltaYaw * 0.35f;
        swayPitch += deltaPitch * 0.35f;

        swayYaw = Mth.clamp(swayYaw, -16f, 16f);
        swayPitch = Mth.clamp(swayPitch, -16f, 16f);

        float decay = 1.0f - (0.18f * mc.getDeltaFrameTime());
        swayYaw *= Mth.clamp(decay, 0.45f, 0.99f);
        swayPitch *= Mth.clamp(decay, 0.45f, 0.99f);

        lastYaw = yaw;
        lastPitch = pitch;
    }

    @SubscribeEvent
    public static void onRenderHand(RenderHandEvent event) {
        PoseStack pose = event.getPoseStack();

        pose.mulPose(Axis.YP.rotationDegrees(swayYaw));
        pose.mulPose(Axis.XP.rotationDegrees(swayPitch));

        pose.translate(swayYaw * 0.0045f, swayPitch * 0.0045f, 0);
    }
}