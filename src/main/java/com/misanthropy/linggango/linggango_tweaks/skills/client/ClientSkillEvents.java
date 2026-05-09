package com.misanthropy.linggango.linggango_tweaks.skills.client;

import com.misanthropy.linggango.linggango_tweaks.LinggangoTweaks;
import com.misanthropy.linggango.linggango_tweaks.skills.TweaksSkillNetwork;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jspecify.annotations.NonNull;
import org.lwjgl.glfw.GLFW;

@SuppressWarnings("all")
@Mod.EventBusSubscriber(modid = LinggangoTweaks.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ClientSkillEvents {

    public static final KeyMapping SKILL_KEY = new KeyMapping(
            "key.linggango.skill",
            GLFW.GLFW_KEY_G,
            "category.linggango"
    );

    public static int combatTimer = 0;
    public static String currentClassId = "";
    public static int cdRemaining = 0;
    public static int maxCd = 0;
    public static boolean isSkillActive = false;

    public static void syncSkillData(String classId, int cd, int max, boolean active) {
        currentClassId = classId;
        cdRemaining = cd;
        maxCd = max;
        isSkillActive = active;
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.@NonNull ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) return;

        if (cdRemaining > 0 && --cdRemaining == 0) {
            combatTimer = 200;
        }

        if (combatTimer > 0) {
            combatTimer--;
        }

        while (SKILL_KEY.consumeClick()) {
            TweaksSkillNetwork.INSTANCE.sendToServer(new TweaksSkillNetwork.SkillUseC2SPacket());
            combatTimer = 200;

            if ("miner".equals(currentClassId)) {
                player.getPersistentData().putBoolean("lt_crawling",
                        !player.getPersistentData().getBoolean("lt_crawling"));
            }
        }
    }

    @SubscribeEvent
    public static void onAttack(@NonNull AttackEntityEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && event.getEntity() == mc.player) {
            combatTimer = 200;
        }
    }

    @SubscribeEvent
    public static void onHurt(@NonNull LivingDamageEvent event) {
        if (!event.getEntity().level().isClientSide()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && event.getEntity() == mc.player) {
            combatTimer = 200;
        }
    }
}