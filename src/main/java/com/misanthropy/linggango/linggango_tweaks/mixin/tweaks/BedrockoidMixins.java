package com.misanthropy.linggango.linggango_tweaks.mixin.tweaks;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.AbstractContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@SuppressWarnings("unused")
public class BedrockoidMixins {

    @Mixin(AbstractContainerScreen.class)
    public static abstract class ScreenMixin extends AbstractContainerEventHandler implements Renderable, GuiEventListener {
        @Shadow @Nullable protected Slot hoveredSlot;

        @Inject(method = "renderSlotHighlight(Lnet/minecraft/client/gui/GuiGraphics;III)V", at = @At("HEAD"), cancellable = true)
        private static void cancelVanillaHighlight(GuiGraphics guiGraphics, int x, int y, int z, CallbackInfo ci) {
            ci.cancel();
        }

        @Inject(method = "renderSlot", at = @At("TAIL"))
        private void drawCustomHighlight(GuiGraphics guiGraphics, Slot slot, CallbackInfo ci) {
            if (slot == this.hoveredSlot && slot.isActive()) {
                int startX = slot.x - 1;
                int startY = slot.y - 1;
                int endX = slot.x + 17;
                int endY = slot.y + 17;

                int outlineColor = 0x80FFFFFF;
                int fillColor = 0x1AFFFFFF;

                guiGraphics.fill(startX, startY, endX, startY + 1, outlineColor);
                guiGraphics.fill(startX, endY - 1, endX, endY, outlineColor);
                guiGraphics.fill(startX, startY, startX + 1, endY, outlineColor);
                guiGraphics.fill(endX - 1, startY, endX, endY, outlineColor);
                guiGraphics.fill(startX + 1, startY + 1, endX - 1, endY - 1, fillColor);
            }
        }

        @Inject(method = "renderFloatingItem", at = @At("HEAD"))
        private void scaleUpItem(GuiGraphics guiGraphics, ItemStack stack, int x, int y, String text, CallbackInfo ci) {
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(x + 8, y + 8, 0);
            guiGraphics.pose().scale(1.3f, 1.3f, 1.3f);
            guiGraphics.pose().translate(-(x + 8), -(y + 8), 0);
        }

        @Inject(method = "renderFloatingItem", at = @At("RETURN"))
        private void scaleDownItem(GuiGraphics guiGraphics, ItemStack stack, int x, int y, String text, CallbackInfo ci) {
            guiGraphics.pose().popPose();
        }
    }

    @Mixin(ItemInHandRenderer.class)
    public static class HandMixin {

        @Unique
        private void linggango_tweaks$applyBreathing(PoseStack poseStack, HumanoidArm arm, boolean isHandEmpty, float partialTicks) {
            Minecraft mc = Minecraft.getInstance();
            Player player = mc.player;
            if (player == null) return;

            float healthRatio = player.getHealth() / player.getMaxHealth();

            float speedMult;
            float ampMult;
            float shakeIntensity;

            if (healthRatio >= 0.8f) {
                speedMult = 1.0f;
                ampMult = 1.0f;
                shakeIntensity = 0.0f;
            } else if (healthRatio >= 0.5f) {
                speedMult = 1.1f;
                ampMult = 1.0f;
                shakeIntensity = 0.0f;
            } else if (healthRatio >= 0.2f) {
                speedMult = 1.3f;
                ampMult = 1.2f;
                shakeIntensity = 0.005f;
            } else {
                speedMult = 1.6f;
                ampMult = 1.5f;
                shakeIntensity = 0.015f;
            }

            if (isHandEmpty) {
                ampMult *= 0.5f;
                shakeIntensity *= 0.4f;
                speedMult *= 0.6f;
            } else {
                speedMult *= 1.2f;
            }

            float time = player.tickCount + partialTicks;
            int direction = arm == HumanoidArm.RIGHT ? 1 : -1;
            float baseSpeed = 0.1f * speedMult;
            float baseAmp = 0.01f * ampMult;

            double breathY = (direction == 1 ? Mth.sin(time * baseSpeed) : Mth.cos(time * baseSpeed)) * baseAmp;
            double shakeX = Mth.sin(time * baseSpeed * 4.0f) * (shakeIntensity * 0.35f);
            double shakeZ = Mth.cos(time * baseSpeed * 3.5f) * (shakeIntensity * 0.6f);

            poseStack.translate(shakeX, breathY, shakeZ);
        }

        @Inject(method = "applyItemArmTransform", at = @At("TAIL"))
        private void addItemBreathing(PoseStack poseStack, HumanoidArm arm, float equipProgress, CallbackInfo ci) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null) return;

            ItemStack heldItem = arm == HumanoidArm.RIGHT ? mc.player.getMainHandItem() : mc.player.getOffhandItem();
            linggango_tweaks$applyBreathing(poseStack, arm, heldItem.isEmpty(), mc.getDeltaFrameTime());
        }

        @Inject(method = "renderPlayerArm", at = @At("HEAD"))
        private void addEmptyHandBreathing(PoseStack poseStack, MultiBufferSource buffer, int combinedLight, float equipProgress, float swingProgress, HumanoidArm arm, CallbackInfo ci) {
            Minecraft mc = Minecraft.getInstance();
            linggango_tweaks$applyBreathing(poseStack, arm, true, mc.getDeltaFrameTime());
        }
    }

    @Mixin(ClientLevel.class)
    public static class ClientLevelMixin {
        @Inject(method = "getSkyColor", at = @At("RETURN"), cancellable = true)
        private void addSunGlareSky(Vec3 cameraPos, float partialTick, CallbackInfoReturnable<Vec3> cir) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null || mc.level == null) return;

            float timeOfDay = mc.level.getTimeOfDay(partialTick);
            float angle = timeOfDay * Mth.TWO_PI;
            Vec3 sunDir = new Vec3(-Mth.sin(angle), Mth.cos(angle), 0.0).normalize();
            Vector3f jomlLook = mc.gameRenderer.getMainCamera().getLookVector();
            Vec3 lookDir = new Vec3(jomlLook.x(), jomlLook.y(), jomlLook.z());

            double dot = lookDir.dot(sunDir);
            float rainFactor = mc.level.getRainLevel(partialTick);

            if (dot > 0.8 && rainFactor < 1.0f) {
                float intensity = (float) ((dot - 0.8) / 0.2) * (1.0f - rainFactor);
                float multiplier = 1.0f - (intensity * 0.3f);
                Vec3 color = cir.getReturnValue();
                cir.setReturnValue(color.multiply(multiplier, multiplier, multiplier + (1f - multiplier) * 0.45f));
            }
        }
    }

    @Mixin(LevelRenderer.class)
    public static class LevelRendererMixin {
        @ModifyConstant(method = "renderSky", constant = @Constant(floatValue = 30.0f, ordinal = 0))
        private float modifySunRadius(float originalSunRadius) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null || mc.level == null) return originalSunRadius;

            float partialTick = mc.getDeltaFrameTime();
            float timeOfDay = mc.level.getTimeOfDay(partialTick);
            float angle = timeOfDay * Mth.TWO_PI;
            Vec3 sunDir = new Vec3(-Mth.sin(angle), Mth.cos(angle), 0.0).normalize();
            Vector3f jomlLook = mc.gameRenderer.getMainCamera().getLookVector();
            Vec3 lookDir = new Vec3(jomlLook.x(), jomlLook.y(), jomlLook.z());

            double dot = lookDir.dot(sunDir);
            float rainFactor = mc.level.getRainLevel(partialTick);

            if (dot > 0.8 && rainFactor < 1.0f) {
                float intensity = (float) ((dot - 0.8) / 0.2) * (1.0f - rainFactor);
                return Mth.lerp(intensity, originalSunRadius, originalSunRadius * 1.3f);
            }
            return originalSunRadius;
        }
    }
}