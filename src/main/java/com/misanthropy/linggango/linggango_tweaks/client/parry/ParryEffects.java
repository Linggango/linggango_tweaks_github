package com.misanthropy.linggango.linggango_tweaks.client.parry;

import com.misanthropy.linggango.linggango_tweaks.client.KeyBindings;
import com.misanthropy.linggango.linggango_tweaks.client.particle.ParrySparkleParticle;
import com.misanthropy.linggango.linggango_tweaks.config.TweaksConfig;
import com.misanthropy.linggango.linggango_tweaks.network.parry.ParryNetwork;
import net.bettercombat.client.animation.PlayerAttackAnimatable;
import net.bettercombat.logic.AnimatedHand;
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
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;
import org.jspecify.annotations.NonNull;

@SuppressWarnings("all")
@OnlyIn(Dist.CLIENT)
public class ParryEffects {

    private static final StateManager stateManager = new StateManager();
    private static final VisualEffects visualEffects = new VisualEffects();
    private static final SoundManager soundManager = new SoundManager();
    private static final CameraEffects cameraEffects = new CameraEffects();
    private static long lastParrySoundTime = 0;
    public static void register() {
        MinecraftForge.EVENT_BUS.register(new ParryEffects());
    }

    public static @NonNull StateManager getStateManager() {
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
                animatable.playAttackAnimation(TweaksConfig.PARRY_ANIMATION_1.get(),
                        AnimatedHand.MAIN_HAND,
                        TweaksConfig.PARRY_ANIMATION_SPEED.get().floatValue(), 0.0f);
            }
        }
    }

    public static void triggerSuccessfulParry(int entityId, int tier, int comboStage) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        int activeTier = tier == 1 ? 2 : tier;

        if (mc.player != null && mc.player.getId() == entityId) {
            boolean isConsecutive = stateManager.getCurrentState() == ParryState.SUCCESS;
            stateManager.registerSuccessfulParry(comboStage);

            long now = System.currentTimeMillis();
            float volumeMultiplier = isConsecutive ? 1.6f : 1.0f;

            if (now - lastParrySoundTime > 300) {
                soundManager.playParrySuccessSound(mc, mc.player, volumeMultiplier, activeTier, comboStage);
                lastParrySoundTime = now;
            }

            if (isConsecutive) {
                Vec3 view = mc.player.getLookAngle();
                mc.player.setDeltaMovement(-view.x * 0.5, 0, -view.z * 0.5);
                mc.player.hasImpulse = true;
            }


            visualEffects.onParrySuccess(activeTier, comboStage);
            cameraEffects.onParrySuccess(activeTier, comboStage);

            Vec3 playerPos = mc.player.position().add(0, mc.player.getEyeHeight() - 0.2, 0);
            Vec3 lookVec = mc.player.getLookAngle();
            Vec3 impactPos = playerPos.add(lookVec.scale(1.8));
            ParrySparkleParticle.spawnExplosion(activeTier, impactPos, comboStage);
        } else {
            Entity entity = mc.level.getEntity(entityId);
            if (entity instanceof Player player) {
                soundManager.playParrySuccessSound(mc, player, 1.0f, activeTier, comboStage);

                Vec3 playerPos = player.position().add(0, player.getEyeHeight() - 0.2, 0);
                Vec3 lookVec = player.getLookAngle();
                Vec3 impactPos = playerPos.add(lookVec.scale(1.8));
                ParrySparkleParticle.spawnExplosion(activeTier, impactPos, comboStage);
            }
        }
    }

    private static void onParryMiss() {
        Minecraft mc = Minecraft.getInstance();
        soundManager.playParryMissSound(mc, mc.player);
        visualEffects.onParryMiss();
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.@NonNull ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getInstance();

        if (mc.player == null || mc.level == null) return;

        if (mc.isPaused()) return;

        if (KeyBindings.PARRY_KEY.consumeClick()) {
            startParryAction();
        }

        stateManager.tick();
        visualEffects.tick();
        cameraEffects.tick();

        if (stateManager.checkParryExpired()) {
            onParryMiss();
        }
    }

    @SubscribeEvent
    public void onRenderGui(RenderGuiEvent.@NonNull Post event) {
        visualEffects.renderOverlay(event.getGuiGraphics());

        if (TweaksConfig.PARRY_DEBUG_MODE.get()) {
            DebugOverlay.render(event.getGuiGraphics(), stateManager, cameraEffects);
        }
    }

    @SubscribeEvent
    public void onCameraSetup(ViewportEvent.@NonNull ComputeCameraAngles event) {
        cameraEffects.applyCameraEffects(event, (float) event.getPartialTick());
    }

    @SubscribeEvent
    public void onComputeFov(ViewportEvent.@NonNull ComputeFov event) {
        float fovBoost = Mth.lerp((float) event.getPartialTick(), cameraEffects.getPrevFovBoost(), cameraEffects.getFovBoost());
        double computedFov = event.getFOV() + fovBoost;
        event.setFOV(Mth.clamp(computedFov, 10.0, 160.0));
    }

    public enum ParryState {
        IDLE, STARTUP, ACTIVE, SUCCESS, RECOVERY, COOLDOWN
    }

    public static class StateManager {
        private @NonNull ParryState currentState = ParryState.IDLE;
        private int stateTicks = 0;
        private int cooldownTicks = 0;
        private boolean useAlternateAnim = false;

        private int currentComboStage = 0;
        private int comboTimeoutTicks = 0;

        private long lastDiffCheckTime = 0;
        private int cachedTickMod = 0;

        private int getDynamicActiveWindow() {
            return TweaksConfig.PARRY_ACTIVE_WINDOW.get();
        }

        public boolean canStartParry() {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null || !mc.player.isAlive() || mc.player.isSpectator() || mc.player.isUsingItem() || mc.screen != null) {
                return false;
            }
            return (currentState == ParryState.IDLE
                    || currentState == ParryState.SUCCESS
                    || currentState == ParryState.RECOVERY)
                    && cooldownTicks == 0;
        }

        public void startParry() {
            currentState = ParryState.STARTUP;
            stateTicks = TweaksConfig.PARRY_STARTUP_TICKS.get();

            String animToPlay = useAlternateAnim
                    ? TweaksConfig.PARRY_ANIMATION_2.get()
                    : TweaksConfig.PARRY_ANIMATION_1.get();
            useAlternateAnim = !useAlternateAnim;

            Minecraft mc = Minecraft.getInstance();
            if (mc.player instanceof PlayerAttackAnimatable animatable) {
                animatable.playAttackAnimation(animToPlay, AnimatedHand.MAIN_HAND,
                        TweaksConfig.PARRY_ANIMATION_SPEED.get().floatValue(), 0.0f);
            }
        }

        public void registerSuccessfulParry(int comboStage) {
            currentState = ParryState.SUCCESS;
            stateTicks = TweaksConfig.PARRY_RECOVERY.get();
            cooldownTicks = 0;

            this.currentComboStage = comboStage;
            this.comboTimeoutTicks = 200;
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

            currentComboStage = 0;
            comboTimeoutTicks = 0;

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
                        case STARTUP -> {
                            currentState = ParryState.ACTIVE;
                            stateTicks = getDynamicActiveWindow();
                        }
                        case SUCCESS -> {
                            currentState = ParryState.RECOVERY;
                            stateTicks = 5;
                        }
                        case RECOVERY -> currentState = ParryState.IDLE;
                    }
                }
            }
            if (cooldownTicks > 0) {
                cooldownTicks--;
                if (cooldownTicks == 0 && currentState == ParryState.COOLDOWN) {
                    currentState = ParryState.IDLE;
                }
            }
            if (comboTimeoutTicks > 0) {
                comboTimeoutTicks--;
                if (comboTimeoutTicks <= 0) {
                    currentComboStage = 0;

                    Minecraft mc = Minecraft.getInstance();

                    if (mc.getConnection() != null) {
                        ParryNetwork.CHANNEL.sendToServer(new ParryNetwork.C2SResetComboPacket());
                    }
                }
            }
        }

        public @NonNull ParryState getCurrentState() { return currentState; }
        public int getCooldownTicks() { return cooldownTicks; }
        public int getCurrentComboStage() { return currentComboStage; }

        @SuppressWarnings("unused")
        public int getStateTicks() { return stateTicks; }
        @SuppressWarnings("unused")
        public boolean isParryActive() { return currentState == ParryState.ACTIVE; }
        @SuppressWarnings("unused")
        public boolean isInStartup() { return currentState == ParryState.STARTUP; }
        @SuppressWarnings("unused")
        public float getParryWindowProgress() {
            if (currentState != ParryState.ACTIVE) return 0.0f;
            return 1.0f - ((float) stateTicks / getDynamicActiveWindow());
        }
    }

    public static class VisualEffects {
        private float flashAlpha = 0f;

        public void onParryStart() {}
        public void onParrySuccess(int tier) {
            onParrySuccess(tier, 1);
        }
        public void onParrySuccess(int tier, int comboStage) {
            this.flashAlpha = tier == 3 ? 0.35f : 0.2f;
        }
        public void onParryMiss() {}
        public void tick() {
            if (this.flashAlpha > 0) this.flashAlpha -= 0.04f;
        }
        public void renderOverlay(@NonNull GuiGraphics graphics) {
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
        private float prevFovBoost = 0.0f;

        private boolean pendingShake = false;
        private int pendingShakeTicks = 0;
        private float pendingShakeIntensity = 0.0f;
        private float pendingShakeDecay = 0.0f;
        private boolean pendingFov = false;
        private float pendingFovValue = 0.0f;

        public void onParrySuccess(int tier) {
            onParrySuccess(tier, 1);
        }

        public void onParrySuccess(int tier, int comboStage) {
            int ticks = tier == 3 ? 12 : 8;
            float intensity = tier == 3 ? 5.0f : 3.0f;
            float decay = tier == 3 ? 0.35f : 0.45f;
            float fov = tier == 3 ? -35.0f : -20.0f;

            pendingShake = true;
            pendingShakeTicks = ticks;
            pendingShakeIntensity = intensity;
            pendingShakeDecay = decay;
            pendingFov = true;
            pendingFovValue = fov;
        }

        private void applyPendingEffects() {
            if (pendingShake) {
                pendingShake = false;
                startCameraShake(pendingShakeTicks, pendingShakeIntensity, pendingShakeDecay);
            }
            if (pendingFov) {
                pendingFov = false;
                fovBoost = pendingFovValue;
                prevFovBoost = fovBoost;
            }
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
            prevFovBoost = fovBoost;
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

        public void applyCameraEffects(ViewportEvent.@NonNull ComputeCameraAngles event, float partialTick) {
            Minecraft mc = Minecraft.getInstance();
            if (!isShaking || mc.player == null || mc.screen != null || !mc.mouseHandler.isMouseGrabbed()) return;

            float shakeYaw = Mth.lerp(partialTick, currentShakeYaw, targetShakeYaw);
            float shakePitch = Mth.lerp(partialTick, currentShakePitch, targetShakePitch);

            if (!Float.isNaN(shakeYaw) && !Float.isNaN(shakePitch)
                    && Math.abs(shakeYaw) < 20.0f && Math.abs(shakePitch) < 20.0f) {
                event.setYaw(event.getYaw() + shakeYaw);
                event.setPitch(Mth.clamp(event.getPitch() + shakePitch, -89.0f, 89.0f));
            }
        }

        public float getFovBoost() { return fovBoost; }
        public float getPrevFovBoost() { return prevFovBoost; }
        public float getShakeIntensity() { return shakeIntensity; }

        @SuppressWarnings("unused")
        public boolean isShaking() { return isShaking; }
    }

    public static class SoundManager {
        private SoundEvent confirmSound;
        private SoundEvent parrySound;
        private SoundEvent perfectParrySound;

        private SoundEvent getConfirmSound() {
            if (confirmSound == null) confirmSound = getOrCreateSound("create", "confirm_2");
            return confirmSound;
        }

        private SoundEvent getParrySound() {
            if (parrySound == null) parrySound = getOrCreateSound("linggango_tweaks", "parry");
            return parrySound;
        }

        private SoundEvent getPerfectParrySound() {
            if (perfectParrySound == null) perfectParrySound = getOrCreateSound("linggango_tweaks", "perfect_parry");
            return perfectParrySound;
        }

        private SoundEvent getOrCreateSound(String namespace, String path) {
            ResourceLocation loc = ResourceLocation.fromNamespaceAndPath(namespace, path);
            SoundEvent soundEvent = ForgeRegistries.SOUND_EVENTS.getValue(loc);
            return soundEvent != null ? soundEvent : SoundEvent.createVariableRangeEvent(loc);
        }

        public void playParryStartSound(@NonNull Minecraft mc, Player player) {
            playSound(mc, player, getConfirmSound(), 1.0f, 1.0f);
        }

        public void playParrySuccessSound(@NonNull Minecraft mc, @NonNull Player player,
                                          float volumeMultiplier, int tier, int comboStage) {
            float pitchBoost = (comboStage - 1) * 0.1f;

            if (tier == 3) {
                float pitch = 0.5f + player.getRandom().nextFloat() * 0.3f + pitchBoost;
                playSound(mc, player, getPerfectParrySound(), 1.3f * volumeMultiplier, Math.min(2.0f, pitch));
            } else {
                float pitch = 0.8f + player.getRandom().nextFloat() * 0.4f + pitchBoost;
                playSound(mc, player, getParrySound(), 1.2f * volumeMultiplier, Math.min(2.0f, pitch));
            }
            playSound(mc, player, getConfirmSound(), 0.7f * volumeMultiplier, 1.8f);
        }

        public void playParryMissSound(@NonNull Minecraft mc, Player player) {
            playSound(mc, player, getConfirmSound(), 0.7f, 0.6f);
        }

        private void playSound(@NonNull Minecraft mc, Player player,
                               SoundEvent soundEvent, float volume, float pitch) {
            if (mc.level != null && player != null && soundEvent != null) {
                mc.level.playLocalSound(player.getX(), player.getY(), player.getZ(),
                        soundEvent, SoundSource.PLAYERS, volume, pitch, false);
            }
        }
    }

    public static class DebugOverlay {
        public static void render(@NonNull GuiGraphics graphics,
                                  @NonNull StateManager stateManager,
                                  @NonNull CameraEffects cameraEffects) {
            Minecraft mc = Minecraft.getInstance();
            java.util.List<String> lines = new java.util.ArrayList<>();
            lines.add("=== PARRY DEBUG ===");
            lines.add("State: " + stateManager.getCurrentState());
            lines.add("Combo Stage: " + stateManager.getCurrentComboStage());
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