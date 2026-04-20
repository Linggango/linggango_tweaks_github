package com.misanthropy.linggango.linggango_tweaks.fixes;

import com.misanthropy.linggango.linggango_tweaks.LinggangoTweaks;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jspecify.annotations.NonNull;


@Mod.EventBusSubscriber(modid = LinggangoTweaks.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class FakePlayerKillFix {

    @SubscribeEvent
    public static void onFakePlayerAttack(@NonNull AttackEntityEvent event) {
        if (event.getEntity() instanceof FakePlayer fakePlayer) {

            if (event.getTarget() instanceof LivingEntity target) {
                String mobName = BuiltInRegistries.ENTITY_TYPE.getKey(target.getType()).toString();
                if (mobName.equals("species:quake")) {
                    DamageSource source = target.damageSources().playerAttack(fakePlayer);
                    target.setHealth(0.0F);
                    target.die(source);
                    event.setCanceled(true);
                }
            }
        }
    }
}