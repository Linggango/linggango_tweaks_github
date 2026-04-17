package com.misanthropy.linggango.linggango_tweaks.bossbehavior;

import com.github.L_Ender.cataclysm.entity.AnimationMonster.BossMonsters.Ignis_Entity;
import com.github.L_Ender.cataclysm.entity.InternalAnimationMonster.IABossMonsters.Maledictus.Maledictus_Entity;
import com.github.L_Ender.cataclysm.entity.InternalAnimationMonster.IABossMonsters.Scylla.Scylla_Entity;
import net.miauczel.legendary_monsters.entity.AnimatedMonster.IAnimatedBoss.TheObliterator.TheObliteratorEntity;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "linggango_tweaks", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class BossEventHandler {

    private static final IgnisBehavior IGNIS = new IgnisBehavior();
    private static final MaledictusBehavior MALEDICTUS = new MaledictusBehavior();
    private static final ScyllaBehavior SCYLLA = new ScyllaBehavior();
    private static final ObliteratorBehavior OBLITERATOR = new ObliteratorBehavior();

    @SubscribeEvent
    @SuppressWarnings("resource")
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        if (event.getEntity().level().isClientSide()) return;

        if (event.getEntity() instanceof Ignis_Entity ignis) IGNIS.onPhaseTick(ignis, ignis.getBossPhase());
        else if (event.getEntity() instanceof Maledictus_Entity maledictus) MALEDICTUS.onPhaseTick(maledictus, 0);

        else if (event.getEntity() instanceof Scylla_Entity scylla) SCYLLA.onPhaseTick(scylla, scylla.isPhase());
        else if (event.getEntity() instanceof TheObliteratorEntity obliterator) OBLITERATOR.onPhaseTick(obliterator, obliterator.getPhase());
    }

    @SubscribeEvent
    @SuppressWarnings("resource")
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.getEntity().level().isClientSide()) return;

        if (event.getEntity() instanceof TheObliteratorEntity obliterator) {
            BossBehavior.HurtResult result = OBLITERATOR.onHurt(obliterator, event.getSource(), event.getAmount());
            if (result.cancel) event.setCanceled(true);
        }
    }

    @SubscribeEvent
    @SuppressWarnings("resource")
    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.getEntity().level().isClientSide()) return;

        if (event.getEntity() instanceof TheObliteratorEntity obliterator) {
            OBLITERATOR.onBossDied(obliterator);
        }
    }
}