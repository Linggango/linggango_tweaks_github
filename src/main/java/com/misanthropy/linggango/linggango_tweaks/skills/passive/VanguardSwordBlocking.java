package com.misanthropy.linggango.linggango_tweaks.skills.passive;

import com.misanthropy.linggango.linggango_tweaks.LinggangoTweaks;
import com.misanthropy.linggango.linggango_tweaks.skills.manager.SkillManager;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jspecify.annotations.NonNull;

@Mod.EventBusSubscriber(modid = LinggangoTweaks.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class VanguardSwordBlocking {

    public static boolean isVanguardBlocking(@NonNull Player player) {
        return player.isUsingItem() && player.getUseItem().getItem() instanceof SwordItem
                && ("warrior".equals(SkillManager.getPlayerClass(player)) || "warrior_".equals(SkillManager.getPlayerClass(player)));
    }

    @SubscribeEvent
    public static void onHurt(@NonNull LivingHurtEvent event) {
        if (event.getEntity() instanceof Player player && isVanguardBlocking(player)) {
            DamageSource source = event.getSource();
            Vec3 damagePos = source.getSourcePosition();

            if (damagePos != null && !source.is(net.minecraft.tags.DamageTypeTags.BYPASSES_ARMOR)) {
                Vec3 viewVector = player.getViewVector(1.0F);
                Vec3 attackVector = new Vec3(damagePos.x - player.getX(), damagePos.y - player.getY(), damagePos.z - player.getZ()).normalize();

                if (attackVector.dot(viewVector) > -0.5) {
                    event.setAmount(event.getAmount() * 0.5F);
                    SkillManager.playCustomSound(player, "alexscaves:cinder_block_step", 1.0F, 0.8F + player.level().random.nextFloat() * 0.4F);
                    player.getUseItem().hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(player.getUsedItemHand()));
                }
            }
        }
    }
}