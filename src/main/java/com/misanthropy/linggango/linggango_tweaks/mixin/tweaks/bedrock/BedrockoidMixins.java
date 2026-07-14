package com.misanthropy.linggango.linggango_tweaks.mixin.tweaks.bedrock;

import com.misanthropy.linggango.linggango_tweaks.config.TweaksConfig;
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
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
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

@OnlyIn(Dist.CLIENT)
@SuppressWarnings("unused")
public class BedrockoidMixins {

    @Mixin(AbstractContainerScreen.class)
    public static abstract class ScreenMixin extends AbstractContainerEventHandler implements Renderable, GuiEventListener {
        @Shadow @Nullable protected Slot hoveredSlot;
        @Shadow protected int leftPos;
        @Shadow protected int topPos;

        @Unique private float linggango$hlX = -1;
        @Unique private float linggango$hlY = -1;
        @Unique private float linggango$hlAlpha = 0f;

        @Inject(method = "renderSlotHighlight(Lnet/minecraft/client/gui/GuiGraphics;III)V", at = @At("HEAD"), cancellable = true)
        private static void cancelVanillaHighlight(GuiGraphics guiGraphics, int x, int y, int z, CallbackInfo ci) {
            if (TweaksConfig.BEDROCKOID_SLOT_HIGHLIGHT_ENABLED.get()) {
                ci.cancel();
            }
        }

        @Inject(method = "renderTooltip", at = @At("HEAD"))
        private void renderSmoothHighlight(GuiGraphics guiGraphics, int mouseX, int mouseY, CallbackInfo ci) {
            if (!TweaksConfig.BEDROCKOID_SLOT_HIGHLIGHT_ENABLED.get()) return;

            float delta = Mth.clamp(Minecraft.getInstance().getDeltaFrameTime() * 0.9f, 0f, 1f);

            if (this.hoveredSlot != null && this.hoveredSlot.isActive()) {
                float targetX = this.hoveredSlot.x;
                float targetY = this.hoveredSlot.y;

                if (linggango$hlAlpha < 0.05f) {
                    linggango$hlX = targetX;
                    linggango$hlY = targetY;
                }

                linggango$hlX = Mth.lerp(delta, linggango$hlX, targetX);
                linggango$hlY = Mth.lerp(delta, linggango$hlY, targetY);
                linggango$hlAlpha = Mth.lerp(delta, linggango$hlAlpha, 1.0f);
            } else {
                linggango$hlAlpha = Mth.lerp(delta, linggango$hlAlpha, 0.0f);
            }

            if (linggango$hlAlpha > 0.01f) {
                guiGraphics.pose().pushPose();
                guiGraphics.pose().translate(this.leftPos, this.topPos, 0);

                int sX = Math.round(linggango$hlX - 1);
                int sY = Math.round(linggango$hlY - 1);
                int eX = Math.round(linggango$hlX + 17);
                int eY = Math.round(linggango$hlY + 17);

                int outlineAlpha = (int) (linggango$hlAlpha * 40);
                int fillAlpha = (int) (linggango$hlAlpha * 8);
                int outlineColor = (outlineAlpha << 24) | 0xFFFFFF;
                int fillColor = (fillAlpha << 24) | 0xFFFFFF;

                guiGraphics.fill(sX, sY, eX, sY + 1, outlineColor);
                guiGraphics.fill(sX, eY - 1, eX, eY, outlineColor);
                guiGraphics.fill(sX, sY + 1, sX + 1, eY - 1, outlineColor);
                guiGraphics.fill(eX - 1, sY + 1, eX, eY - 1, outlineColor);
                guiGraphics.fill(sX + 1, sY + 1, eX - 1, eY - 1, fillColor);
                guiGraphics.pose().popPose();
            }
        }

        @Inject(method = "renderFloatingItem", at = @At("HEAD"))
        private void scaleUpItem(GuiGraphics guiGraphics, ItemStack stack, int x, int y, String text, CallbackInfo ci) {
            if (!TweaksConfig.BEDROCKOID_FLOATING_ITEM_SCALE_ENABLED.get()) return;
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(x + 8, y + 8, 0);
            guiGraphics.pose().scale(1.3f, 1.3f, 1.3f);
            guiGraphics.pose().translate(-(x + 8), -(y + 8), 0);
        }

        @Inject(method = "renderFloatingItem", at = @At("RETURN"))
        private void scaleDownItem(GuiGraphics guiGraphics, ItemStack stack, int x, int y, String text, CallbackInfo ci) {
            if (!TweaksConfig.BEDROCKOID_FLOATING_ITEM_SCALE_ENABLED.get()) return;
            guiGraphics.pose().popPose();
        }
    }

    @Mixin(ItemInHandRenderer.class)
    public static class HandMixin {

        @Unique private float linggango$swayYaw = 0f;
        @Unique private float linggango$swayPitch = 0f;
        @Unique private float linggango$targetYaw = 0f;
        @Unique private float linggango$targetPitch = 0f;
        @Unique private boolean linggango$swayInitialized = false;

        @Unique
        private void linggango_tweaks$applyBreathing(PoseStack poseStack, HumanoidArm arm, boolean isHandEmpty, float partialTicks) {
            if (!TweaksConfig.BEDROCKOID_ITEM_BREATHING_ENABLED.get()) return;

            Minecraft mc = Minecraft.getInstance();
            Player player = mc.player;
            if (player == null) return;

            float speedMult = 1.0f, ampMult = 1.0f, shakeIntensity = 0.0f;

            if (!player.isCreative() && TweaksConfig.BEDROCKOID_LOW_HEALTH_SHAKE_ENABLED.get()) {
                float healthRatio = player.getHealth() / player.getMaxHealth();
                if (healthRatio < 0.2f) {
                    speedMult = 1.6f; ampMult = 1.5f; shakeIntensity = 0.015f;
                } else if (healthRatio < 0.5f) {
                    speedMult = 1.3f; ampMult = 1.2f; shakeIntensity = 0.005f;
                } else if (healthRatio < 0.8f) {
                    speedMult = 1.1f;
                }
            }

            if (isHandEmpty) {
                ampMult *= 0.5f; shakeIntensity *= 0.4f; speedMult *= 0.6f;
            } else {
                speedMult *= 1.2f;
            }

            float time = player.tickCount + partialTicks;
            float baseSpeed = 0.1f * speedMult;
            float baseAmp = 0.01f * ampMult;

            double breathY = (arm == HumanoidArm.RIGHT ? Mth.sin(time * baseSpeed) : Mth.cos(time * baseSpeed)) * baseAmp;
            double shakeX = Mth.sin(time * baseSpeed * 4.0f) * (shakeIntensity * 0.35f);
            double shakeZ = Mth.cos(time * baseSpeed * 3.5f) * (shakeIntensity * 0.6f);

            poseStack.translate(shakeX, breathY, shakeZ);
        }

        @Unique
        private void linggango_tweaks$applyLookSway(PoseStack poseStack) {
            if (!TweaksConfig.BEDROCKOID_LOOK_SWAY_ENABLED.get()) return;

            float finalYawDiff = Mth.wrapDegrees(linggango$targetYaw - linggango$swayYaw);
            float finalPitchDiff = Mth.wrapDegrees(linggango$targetPitch - linggango$swayPitch);

            float maxSway = 15.0f;
            finalYawDiff = Mth.clamp(finalYawDiff, -maxSway, maxSway);
            finalPitchDiff = Mth.clamp(finalPitchDiff, -maxSway, maxSway);

            double swayX = -finalYawDiff * 0.015f;
            double swayY = finalPitchDiff * 0.015f;

            poseStack.translate(swayX, swayY, 0.0);
        }

        @Inject(method = "renderHandsWithItems", at = @At("HEAD"))
        private void updateLookSwayValues(float partialTicks, PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, LocalPlayer player, int combinedLight, CallbackInfo ci) {
            if (!TweaksConfig.BEDROCKOID_LOOK_SWAY_ENABLED.get()) return;

            linggango$targetYaw = player.getViewYRot(partialTicks);
            linggango$targetPitch = player.getViewXRot(partialTicks);

            if (!linggango$swayInitialized) {
                linggango$swayYaw = linggango$targetYaw;
                linggango$swayPitch = linggango$targetPitch;
                linggango$swayInitialized = true;
            }

            float lerpSpeed = 0.15f;
            float yawDiff = Mth.wrapDegrees(linggango$targetYaw - linggango$swayYaw);
            float pitchDiff = Mth.wrapDegrees(linggango$targetPitch - linggango$swayPitch);

            linggango$swayYaw += yawDiff * lerpSpeed;
            linggango$swayPitch += pitchDiff * lerpSpeed;
        }

        @Inject(method = "applyItemArmTransform", at = @At("TAIL"))
        private void addItemBreathing(PoseStack poseStack, HumanoidArm arm, float equipProgress, CallbackInfo ci) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null) {
                ItemStack heldItem = arm == HumanoidArm.RIGHT ? mc.player.getMainHandItem() : mc.player.getOffhandItem();

                if (TweaksConfig.BEDROCKOID_ITEM_BREATHING_ENABLED.get()) {
                    linggango_tweaks$applyBreathing(poseStack, arm, heldItem.isEmpty(), mc.getDeltaFrameTime());
                }

                if (TweaksConfig.BEDROCKOID_LOOK_SWAY_ENABLED.get()) {
                    linggango_tweaks$applyLookSway(poseStack);
                }
            }
        }

        @Inject(method = "renderPlayerArm", at = @At("HEAD"))
        private void addEmptyHandBreathing(PoseStack poseStack, MultiBufferSource buffer, int combinedLight, float equipProgress, float swingProgress, HumanoidArm arm, CallbackInfo ci) {
            Minecraft mc = Minecraft.getInstance();

            if (TweaksConfig.BEDROCKOID_ITEM_BREATHING_ENABLED.get()) {
                linggango_tweaks$applyBreathing(poseStack, arm, true, mc.getDeltaFrameTime());
            }

            if (TweaksConfig.BEDROCKOID_LOOK_SWAY_ENABLED.get()) {
                linggango_tweaks$applyLookSway(poseStack);
            }
        }
    }

    @Mixin(ClientLevel.class)
    public static class ClientLevelMixin {
        @Inject(method = "getSkyColor", at = @At("RETURN"), cancellable = true)
        private void addSunGlareSky(Vec3 cameraPos, float partialTick, CallbackInfoReturnable<Vec3> cir) {
            if (!TweaksConfig.BEDROCKOID_SUN_GLARE_SKY_ENABLED.get()) return;

            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null || mc.level == null) return;

            float angle = mc.level.getTimeOfDay(partialTick) * Mth.TWO_PI;
            Vec3 sunDir = new Vec3(-Mth.sin(angle), Mth.cos(angle), 0.0).normalize();
            Vector3f jomlLook = mc.gameRenderer.getMainCamera().getLookVector();

            double dot = new Vec3(jomlLook.x(), jomlLook.y(), jomlLook.z()).dot(sunDir);
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
            if (!TweaksConfig.BEDROCKOID_SUN_RADIUS_SCALE_ENABLED.get()) return originalSunRadius;

            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null || mc.level == null) return originalSunRadius;

            float partialTick = mc.getDeltaFrameTime();
            float angle = mc.level.getTimeOfDay(partialTick) * Mth.TWO_PI;
            Vec3 sunDir = new Vec3(-Mth.sin(angle), Mth.cos(angle), 0.0).normalize();
            Vector3f jomlLook = mc.gameRenderer.getMainCamera().getLookVector();

            double dot = new Vec3(jomlLook.x(), jomlLook.y(), jomlLook.z()).dot(sunDir);
            float rainFactor = mc.level.getRainLevel(partialTick);

            if (dot > 0.8 && rainFactor < 1.0f) {
                float intensity = (float) ((dot - 0.8) / 0.2) * (1.0f - rainFactor);
                return Mth.lerp(intensity, originalSunRadius, originalSunRadius * 1.3f);
            }
            return originalSunRadius;
        }
    }
}