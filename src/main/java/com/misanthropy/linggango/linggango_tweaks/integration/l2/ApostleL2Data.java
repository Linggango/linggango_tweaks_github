//package com.misanthropy.linggango.linggango_tweaks.integration.l2;
//
//import com.Polarice3.Goety.common.entities.boss.Apostle;
//import com.misanthropy.linggango.linggango_tweaks.LinggangoTweaks;
//import com.misanthropy.linggango.linggango_tweaks.config.TweaksConfig;
//import dev.xkmc.l2hostility.content.capability.mob.MobTraitCap;
//import dev.xkmc.l2hostility.content.config.EntityConfig;
//import dev.xkmc.l2hostility.init.L2Hostility;
//import net.minecraft.network.chat.Component;
//import net.minecraft.server.level.ServerPlayer;
//import net.minecraft.world.entity.LivingEntity;
//import org.jspecify.annotations.Nullable;
//import z1gned.goetyrevelation.util.ApollyonAbilityHelper;
//
//import java.util.ArrayDeque;
//import java.util.Collections;
//import java.util.Deque;
//import java.util.Set;
//import java.util.UUID;
//import java.util.concurrent.ConcurrentHashMap;
//
//public class ApostleL2Data {
//    private static final Deque<LivingEntity> APOSTLE_CONTEXT = new ArrayDeque<>();
//
//    public static void pushApostleContext(LivingEntity entity) {
//        APOSTLE_CONTEXT.push(entity);
//    }
//
//    public static void popApostleContext() {
//        if (!APOSTLE_CONTEXT.isEmpty()) APOSTLE_CONTEXT.pop();
//    }
//
//    public static @Nullable LivingEntity currentApostle() {
//        return APOSTLE_CONTEXT.isEmpty() ? null : APOSTLE_CONTEXT.peek();
//    }
//
//    public static final Set<UUID> SKIP_HEALTH_RESET = Collections.newSetFromMap(new ConcurrentHashMap<>());
//    public static final Set<UUID> SKIP_TICK_HEALTH_RESET = Collections.newSetFromMap(new ConcurrentHashMap<>());
//    public static final Set<UUID> TICKING_MOBS = Collections.newSetFromMap(new ConcurrentHashMap<>());
//    public static final Set<UUID> PENDING_REINIT = Collections.newSetFromMap(new ConcurrentHashMap<>());
//
//    public static void scheduleReinit(LivingEntity entity, MobTraitCap cap) {
//        if (TICKING_MOBS.contains(entity.getUUID())) {
//            notifyPlayers(entity, "Currently ticking, deferring reinit");
//            PENDING_REINIT.add(entity.getUUID());
//            return;
//        }
//        notifyPlayers(entity, "Scheduling immediate reinit");
//        doReinit(entity, cap);
//    }
//
//    public static void doReinit(LivingEntity entity, MobTraitCap cap) {
//        notifyPlayers(entity, "Performing reinit");
//        cap.setConfigCache(null);
//        SKIP_HEALTH_RESET.add(entity.getUUID());
//        SKIP_TICK_HEALTH_RESET.add(entity.getUUID());
//        try {
//            cap.reinit(entity, cap.lv, false);
//        } finally {
//            SKIP_HEALTH_RESET.remove(entity.getUUID());
//        }
//    }
//
//    public static EntityConfig.Config getApostleConfig(LivingEntity le) {
//        if (!(le instanceof Apostle)) return null;
//
//        var data = new ApostleEntityContext(le);
//        return L2Hostility.ENTITY.getMerged().get(
//                le.getType(),
//                LinggangoTweaks.APOSTLE_CONTEXT,
//                ApostleEntityContext.class,
//                data);
//    }
//
//    public static void notifyPlayers(LivingEntity entity, String message) {
//        if (!TweaksConfig.APOSTLE_TITLE_NUMBER_LOG.get()) {
//            return;
//        }
//
//        var entities = entity.level().getEntitiesOfClass(ServerPlayer.class, entity.getBoundingBox().inflate(100), s -> true);
//        for (var p : entities) {
//            var apostle = (Apostle) entity;
//            p.sendSystemMessage(prepareMessage(apostle, message));
//        }
//        System.out.println(prepareMessage((Apostle) entity, "Notified about " + message).getString());
//    }
//
//    public static Component prepareMessage(Apostle apostle, String message) {
//        return Component.literal(message + ": Apostle " + apostle.getUUID() + ": title " + getApostleTitleNumber(apostle) + ", secondPhase " + apostle.isSecondPhase());
//    }
//
//    public static int getApostleTitleNumber(LivingEntity le) {
//        if (!(le instanceof Apostle)) return -1;
//        var apostle = (Apostle) le;
//        return ((ApollyonAbilityHelper) apostle).allTitleApostle$getTitleNumber();
//    }
//}