package com.misanthropy.linggango.linggango_tweaks.ring_selection;

import com.misanthropy.linggango.linggango_tweaks.LinggangoTweaks;
import com.misanthropy.linggango.linggango_tweaks.StatsGUI;
import com.misanthropy.linggango.linggango_tweaks.client.ClientModEvents;
import com.misanthropy.linggango.linggango_tweaks.client.KeyBindings;
import com.misanthropy.linggango.linggango_tweaks.client.discord.LinggangoRichPresence;
import com.misanthropy.linggango.linggango_tweaks.client.gui.macabre.MacabreConfirmationScreen;
import com.misanthropy.linggango.linggango_tweaks.client.parry.ParryEffects;
import com.misanthropy.linggango.linggango_tweaks.client.parry.ParryOverlay;
import com.misanthropy.linggango.linggango_tweaks.client.particle.ParrySparkleParticle;
import com.misanthropy.linggango.linggango_tweaks.client.screen.ModernCreditsScreen;
import com.misanthropy.linggango.linggango_tweaks.tweaks.jei.JeiSortStuff;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@SuppressWarnings("unused")
@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = LinggangoTweaks.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientAccess {

    public static void openScreen() {
        Minecraft.getInstance().setScreen(new RingSelectionScreen());
    }

    public static void openStatsScreen() {
        Minecraft.getInstance().setScreen(new StatsGUI());
    }

    public static void openModernCreditsScreen() {
        Minecraft.getInstance().setScreen(new ModernCreditsScreen());
    }

    public static void openMacabreConfirmationScreen(Runnable onConfirm) {
        Minecraft.getInstance().setScreen(new MacabreConfirmationScreen(onConfirm));
    }

    public static void spawnParryExplosion(int tier, Vec3 pos, int comboStage) {
        ParrySparkleParticle.spawnExplosion(tier, pos, comboStage);
    }

    public static void triggerParryEffects(int entityId, int tier, int comboStage) {
        ParryEffects.triggerSuccessfulParry(entityId, tier, comboStage);
    }

    public static void triggerParryStartForOther(int entityId) {
        ParryEffects.triggerParryStartForOther(entityId);
    }

    public static void initClient(IEventBus modEventBus) {
        LinggangoRichPresence.init();
        JeiSortStuff.patchJeiSortOrder();
        ParryEffects.register();
        ParryOverlay.register(modEventBus);
        modEventBus.register(ClientModEvents.class);
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null) {
                while (KeyBindings.STATS_KEY.consumeClick()) {
                    if (mc.screen == null) {
                        openStatsScreen();
                    } else if (mc.screen instanceof StatsGUI statsGui) {
                        statsGui.initiateClose();
                    }
                }
            }
        }
    }

    public static void handleCelestialStarboardClient(Player player, ItemStack stack) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != player || mc.screen != null || stack.isEmpty() || player.isPassenger()) return;

        if (mc.options.keyJump.isDown() && mc.options.keyShift.isDown()) {
            if (!player.getCooldowns().isOnCooldown(stack.getItem())) {
                Vec3 lookVec = player.getLookAngle();
                double len = Math.sqrt(lookVec.x * lookVec.x + lookVec.z * lookVec.z);

                if (len > 0) {
                    double dashX = (lookVec.x / len) * 0.6D;
                    double dashZ = (lookVec.z / len) * 0.6D;
                    player.setDeltaMovement(new Vec3(dashX, 0.0D, dashZ));
                }

                player.getCooldowns().addCooldown(stack.getItem(), 100);
            }
        }

        if (mc.options.keyJump.isDown() && !player.onGround()) {
            Vec3 currentMotion = player.getDeltaMovement();
            if (currentMotion.y < 0.3D) {
                player.setDeltaMovement(new Vec3(currentMotion.x, 0.03D, currentMotion.z));
            }
        }
    }
}