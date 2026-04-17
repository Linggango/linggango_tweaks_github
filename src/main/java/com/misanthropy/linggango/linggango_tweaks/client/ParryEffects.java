package com.misanthropy.linggango.linggango_tweaks.client;

import com.misanthropy.linggango.difficulty_enhancement.LinggangoEvents;
import com.misanthropy.linggango.linggango_tweaks.client.particle.ParrySparkleParticle;
import com.misanthropy.linggango.linggango_tweaks.config.TweaksConfig;
import com.misanthropy.linggango.linggango_tweaks.network.ParryNetwork;
import com.mojang.blaze3d.platform.InputConstants;
import net.bettercombat.client.animation.PlayerAttackAnimatable;
import net.bettercombat.logic.AnimatedHand;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = "linggango_tweaks", bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ParryEffects {

    public static final KeyMapping PARRY_KEY = new KeyMapping(
            "key.linggango_tweaks.parry",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_R,
            "key.categories.linggango_tweaks"
    );

    private static final StateManager stateManager = new StateManager();
    private static final VisualEffects visualEffects = new VisualEffects();
    private static final SoundManager soundManager = new SoundManager();
    private static final CameraEffects cameraEffects = new CameraEffects();

    private static long lastParrySoundTime = 0;

    public static StateManager getStateManager() {
        return stateManager;
    }

    public static void startParryAction() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;
        if (!stateManager.canStartParry()) return;

        stateManager.startParry();
        soundManager.playParryStartSound(mc, mc.player);
        visualEffects.onParryStart();

        ParryNetwork.CHANNEL.sendToServer(new ParryNetwork.C2SParryPacket());
    }

    public static void triggerParryStartForOther(int entityId) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        Entity entity = mc.level.getEntity(entityId);
        if (entity instanceof Player player && entity != mc.player) {
            soundManager.playParryStartSound(mc, player);
            if (player instanceof PlayerAttackAnimatable animatable) {
                animatable.playAttackAnimation(TweaksConfig.PARRY_ANIMATION_1.get(), AnimatedHand.MAIN_HAND, TweaksConfig.PARRY_ANIMATION_SPEED.get().floatValue(), 0.0f);
            }
        }
    }

    public static void triggerSuccessfulParry(int entityId, int tier) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        if (mc.player != null && mc.player.getId() == entityId) {
            int activeTier = tier == 1 ? 2 : tier;

            boolean isConsecutive = stateManager.getCurrentState() == ParryState.SUCCESS;
            stateManager.registerSuccessfulParry();

            long now = System.currentTimeMillis();
            float volumeMultiplier = isConsecutive ? 1.6f : 1.0f;

            if (now - lastParrySoundTime > 300) {
                soundManager.playParrySuccessSound(mc, mc.player, volumeMultiplier, activeTier);
                lastParrySoundTime = now;
            }

            if (isConsecutive) {
                Vec3 view = mc.player.getLookAngle();
                mc.player.setDeltaMovement(-view.x * 0.5, 0, -view.z * 0.5);
                mc.player.hasImpulse = true;
            }

            visualEffects.onParrySuccess(activeTier);
            cameraEffects.onParrySuccess(activeTier);

            Vec3 playerPos = mc.player.position().add(0, mc.player.getEyeHeight() - 0.2, 0);
            Vec3 lookVec = mc.player.getLookAngle();
            Vec3 impactPos = playerPos.add(lookVec.scale(1.2));

            ParrySparkleParticle.spawnExplosion(activeTier, impactPos);
        } else {
            Entity entity = mc.level.getEntity(entityId);
            if (entity instanceof Player player) {
                int activeTier = tier == 1 ? 2 : tier;
                soundManager.playParrySuccessSound(mc, player, 1.0f, activeTier);

                Vec3 playerPos = player.position().add(0, player.getEyeHeight() - 0.2, 0);
                Vec3 lookVec = player.getLookAngle();
                Vec3 impactPos = playerPos.add(lookVec.scale(1.2));

                ParrySparkleParticle.spawnExplosion(activeTier, impactPos);
            }
        }
    }

    private static void onParryMiss() {
        Minecraft mc = Minecraft.getInstance();
        soundManager.playParryMissSound(mc, mc.player);
        visualEffects.onParryMiss();
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        stateManager.tick();
        visualEffects.tick();
        cameraEffects.tick();

        if (stateManager.checkParryExpired()) {
            onParryMiss();
        }

        while (PARRY_KEY.consumeClick()) {
            startParryAction();
        }
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        visualEffects.renderOverlay(event.getGuiGraphics());

        if (TweaksConfig.PARRY_DEBUG_MODE.get()) {
            DebugOverlay.render(event.getGuiGraphics(), stateManager, cameraEffects);
        }
    }

    @SubscribeEvent
    public static void onCameraSetup(ViewportEvent.ComputeCameraAngles event) {
        cameraEffects.applyCameraEffects(event, (float) event.getPartialTick());
    }

    @SubscribeEvent
    public static void onComputeFov(ViewportEvent.ComputeFov event) {
        event.setFOV(event.getFOV() + cameraEffects.getFovBoost());
    }

    @Mod.EventBusSubscriber(modid = "linggango_tweaks", bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ModBusEvents {
        @SubscribeEvent
        public static void onKeyRegister(RegisterKeyMappingsEvent event) {
            event.register(PARRY_KEY);
        }
    }

    public enum ParryState {
        IDLE, STARTUP, ACTIVE, SUCCESS, RECOVERY, COOLDOWN
    }

    public static class StateManager {
        private ParryState currentState = ParryState.IDLE;
        private int stateTicks = 0;
        private int cooldownTicks = 0;
        private boolean useAlternateAnim = false;

        private int getDynamicActiveWindow() {
            int baseActive = TweaksConfig.PARRY_ACTIVE_WINDOW.get();
            Minecraft mc = Minecraft.getInstance();
            if (mc.level == null) return baseActive;

            int tickModifier = 0;
            LinggangoEvents.DifficultyDef diff = LinggangoEvents.getCurrentDifficulty(mc.level);
            if (diff != null) {
                switch (diff.id) {
                    case "cozy": tickModifier = 2; break;
                    case "easy": tickModifier = 1; break;
                    case "normal": tickModifier = 0; break;
                    case "veteran": tickModifier = -1; break;
                    case "extreme": tickModifier = -2; break;
                    case "torture": tickModifier = -3; break;
                    case "chaos": tickModifier = -4; break;
                }
            }
            return Math.max(3, Math.min(9, baseActive + tickModifier));
        }

        public boolean canStartParry() {
            return (currentState == ParryState.IDLE || currentState == ParryState.SUCCESS || currentState == ParryState.RECOVERY) && cooldownTicks == 0;
        }

        public void startParry() {
            currentState = ParryState.STARTUP;
            stateTicks = TweaksConfig.PARRY_STARTUP_TICKS.get();

            String animToPlay = useAlternateAnim ? TweaksConfig.PARRY_ANIMATION_2.get() : TweaksConfig.PARRY_ANIMATION_1.get();
            useAlternateAnim = !useAlternateAnim;

            Minecraft mc = Minecraft.getInstance();
            if (mc.player instanceof PlayerAttackAnimatable animatable) {
                animatable.playAttackAnimation(animToPlay, AnimatedHand.MAIN_HAND, TweaksConfig.PARRY_ANIMATION_SPEED.get().floatValue(), 0.0f);
            }
        }

        public void registerSuccessfulParry() {
            currentState = ParryState.SUCCESS;
            stateTicks = TweaksConfig.PARRY_RECOVERY.get();
            cooldownTicks = 0;
        }

        public boolean checkParryExpired() {
            if (currentState == ParryState.ACTIVE && stateTicks <= 0) {
                forceEndParry();
                return true;
            }
            return false;
        }

        public void forceEndParry() {
            currentState = ParryState.COOLDOWN;
            cooldownTicks = TweaksConfig.PARRY_COOLDOWN.get();
            stateTicks = 0;

            Minecraft mc = Minecraft.getInstance();
            if (mc.player instanceof PlayerAttackAnimatable animatable) {
                animatable.stopAttackAnimation(0.0f);
            }
        }

        public void tick() {
            if (stateTicks > 0) {
                stateTicks--;
                if (stateTicks == 0) {
                    switch (currentState) {
                        case STARTUP:
                            currentState = ParryState.ACTIVE;
                            stateTicks = getDynamicActiveWindow();
                            break;
                        case SUCCESS:
                        case RECOVERY:
                            currentState = ParryState.IDLE;
                            break;
                    }
                }
            }
            if (cooldownTicks > 0) {
                cooldownTicks--;
                if (cooldownTicks == 0 && currentState == ParryState.COOLDOWN) {
                    currentState = ParryState.IDLE;
                }
            }
        }

        public ParryState getCurrentState() { return currentState; }
        public int getStateTicks() { return stateTicks; }
        public int getCooldownTicks() { return cooldownTicks; }
        public boolean isParryActive() { return currentState == ParryState.ACTIVE; }
        public boolean isInStartup() { return currentState == ParryState.STARTUP; }
        public float getParryWindowProgress() {
            if (currentState != ParryState.ACTIVE) return 0.0f;
            return 1.0f - ((float) stateTicks / getDynamicActiveWindow());
        }
    }

    public static class VisualEffects {
        private float flashAlpha = 0f;

        public void onParryStart() {}
        public void onParrySuccess(int tier) {
            this.flashAlpha = tier == 3 ? 0.35f : 0.2f;
        }
        public void onParryMiss() {}
        public void tick() {
            if (this.flashAlpha > 0) this.flashAlpha -= 0.04f;
        }
        public void renderOverlay(GuiGraphics graphics) {
            if (this.flashAlpha <= 0) return;
            int width = Minecraft.getInstance().getWindow().getGuiScaledWidth();
            int height = Minecraft.getInstance().getWindow().getGuiScaledHeight();
            int alphaInt = (int)(this.flashAlpha * 255);
            graphics.fill(0, 0, width, height, (alphaInt << 24) | 0xFFFFFF);
        }
    }

    public static class CameraEffects {
        private final RandomSource random = RandomSource.create();
        private boolean isShaking = false;
        private int shakeTicks = 0;
        private float shakeIntensity = 0.0f;
        private float shakeDecayRate = 0.0f;
        private float currentShakeYaw = 0.0f;
        private float currentShakePitch = 0.0f;
        private float targetShakeYaw = 0.0f;
        private float targetShakePitch = 0.0f;
        private float fovBoost = 0.0f;

        public void onParrySuccess(int tier) {
            int ticks = tier == 3 ? 12 : 8;
            float intensity = tier == 3 ? 5.0f : 3.0f;
            float decay = tier == 3 ? 0.35f : 0.45f;
            startCameraShake(ticks, intensity, decay);
            fovBoost = tier == 3 ? -35.0f : -20.0f;
        }

        private void startCameraShake(int ticks, float intensity, float decayRate) {
            isShaking = true;
            shakeTicks = ticks;
            shakeIntensity = intensity;
            shakeDecayRate = decayRate;
            updateShakeTargets();
        }

        private void updateShakeTargets() {
            targetShakeYaw = (random.nextFloat() - 0.5f) * shakeIntensity * 2.2f;
            targetShakePitch = (random.nextFloat() - 0.5f) * shakeIntensity * 1.6f;
        }

        public void tick() {
            if (isShaking) {
                shakeTicks--;
                shakeIntensity = Math.max(0, shakeIntensity - shakeDecayRate);
                updateShakeTargets();
                currentShakeYaw = Mth.lerp(0.6f, currentShakeYaw, targetShakeYaw);
                currentShakePitch = Mth.lerp(0.6f, currentShakePitch, targetShakePitch);
                if (shakeTicks <= 0 && shakeIntensity <= 0.01f) {
                    isShaking = false;
                    currentShakeYaw = 0.0f;
                    currentShakePitch = 0.0f;
                }
            }
            if (fovBoost != 0.0f) {
                fovBoost = Mth.lerp(0.35f, fovBoost, 0.0f);
                if (Math.abs(fovBoost) < 0.1f) fovBoost = 0.0f;
            }
        }

        public void applyCameraEffects(ViewportEvent.ComputeCameraAngles event, float partialTick) {
            Minecraft mc = Minecraft.getInstance();
            if (isShaking && mc.screen == null && mc.mouseHandler.isMouseGrabbed()) {
                float shakeYaw = Mth.lerp(partialTick, currentShakeYaw, targetShakeYaw);
                float shakePitch = Mth.lerp(partialTick, currentShakePitch, targetShakePitch);

                if (!Float.isNaN(shakeYaw) && !Float.isNaN(shakePitch)) {
                    if (Math.abs(shakeYaw) < 20.0f && Math.abs(shakePitch) < 20.0f) {
                        event.setYaw(event.getYaw() + shakeYaw);
                        float targetPitch = event.getPitch() + shakePitch;
                        event.setPitch(Mth.clamp(targetPitch, -89.0f, 89.0f));
                    }
                }
            }
        }

        public float getFovBoost() { return fovBoost; }
        public boolean isShaking() { return isShaking; }
        public float getShakeIntensity() { return shakeIntensity; }
    }

    public static class SoundManager {
        public void playParryStartSound(Minecraft mc, Player player) {
            playSound(mc, player, "create", "confirm_2", 1.0f, 1.0f);
        }

        public void playParrySuccessSound(Minecraft mc, Player player, float volumeMultiplier, int tier) {
            if (tier == 3) {
                float pitch = 0.5f + player.getRandom().nextFloat() * 0.3f;
                playSound(mc, player, "linggango_tweaks", "perfect_parry", 1.3f * volumeMultiplier, pitch);
            } else {
                float pitch = 0.8f + player.getRandom().nextFloat() * 0.4f;
                playSound(mc, player, "linggango_tweaks", "parry", 1.2f * volumeMultiplier, pitch);
            }
            playSound(mc, player, "create", "confirm_2", 0.7f * volumeMultiplier, 1.8f);
        }

        public void playParryMissSound(Minecraft mc, Player player) {
            playSound(mc, player, "create", "confirm_2", 0.7f, 0.6f);
        }

        private void playSound(Minecraft mc, Player player, String namespace, String path, float volume, float pitch) {
            if (mc.level != null && player != null) {
                ResourceLocation soundLocation = new ResourceLocation(namespace, path);
                SoundEvent soundEvent = ForgeRegistries.SOUND_EVENTS.getValue(soundLocation);
                if (soundEvent == null) soundEvent = SoundEvent.createVariableRangeEvent(soundLocation);
                mc.level.playLocalSound(player.getX(), player.getY(), player.getZ(), soundEvent, SoundSource.PLAYERS, volume, pitch, false);
            }
        }
    }

    public static class DebugOverlay {
        public static void render(GuiGraphics graphics, StateManager stateManager, CameraEffects cameraEffects) {
            Minecraft mc = Minecraft.getInstance();
            List<String> lines = new ArrayList<>();
            lines.add("=== PARRY DEBUG ===");
            lines.add("State: " + stateManager.getCurrentState());
            lines.add("Cooldown: " + stateManager.getCooldownTicks());
            lines.add("Shake: " + String.format("%.2f", cameraEffects.getShakeIntensity()));

            int y = 10;
            for (String line : lines) {
                graphics.drawString(mc.font, line, 10, y, 0xFFFFFF, true);
                y += 10;
            }
        }
    }
}