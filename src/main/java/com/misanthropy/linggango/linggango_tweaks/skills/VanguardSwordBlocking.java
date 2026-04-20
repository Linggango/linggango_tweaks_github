package com.misanthropy.linggango.linggango_tweaks.skills; // Mozilla Public License

import com.misanthropy.linggango.linggango_tweaks.LinggangoTweaks;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderHandEvent;
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
                Vec3 attackVector = damagePos.subtract(player.position()).normalize();

                if (attackVector.dot(viewVector) > -0.5) {

                    event.setAmount(event.getAmount() * 0.5F);

                    SkillManager.playCustomSound(player, "alexscaves:cinder_block_step", 1.0F, 0.8F + player.level().random.nextFloat() * 0.4F);

                    player.getUseItem().hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(player.getUsedItemHand()));
                }
            }
        }
    }

    @Mod.EventBusSubscriber(modid = LinggangoTweaks.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class ClientVisuals {
        @SubscribeEvent
        public static void onRenderHand(@NonNull RenderHandEvent event) {
            Minecraft mc = Minecraft.getInstance();
            AbstractClientPlayer player = mc.player;

            if (player != null && isVanguardBlocking(player) && event.getHand() == player.getUsedItemHand()) {
                event.setCanceled(true);

                PoseStack poseStack = event.getPoseStack();
                poseStack.pushPose();

                boolean isMainHand = event.getHand() == InteractionHand.MAIN_HAND;
                HumanoidArm arm = isMainHand ? player.getMainArm() : player.getMainArm().getOpposite();
                int dir = arm == HumanoidArm.RIGHT ? 1 : -1;

                float equipProgress = event.getEquipProgress();
                poseStack.translate(dir * 0.56F, -0.52F + equipProgress * -0.6F, -0.72F);

                poseStack.translate(dir * -0.14142136F, 0.08F, 0.14142136F);
                poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(-102.25F));
                poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(dir * 13.365F));
                poseStack.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(dir * 78.05F));

                mc.getItemRenderer().renderStatic(player, event.getItemStack(),
                        arm == HumanoidArm.RIGHT ? ItemDisplayContext.FIRST_PERSON_RIGHT_HAND : ItemDisplayContext.FIRST_PERSON_LEFT_HAND,
                        arm == HumanoidArm.LEFT, poseStack, event.getMultiBufferSource(), player.level(), event.getPackedLight(), OverlayTexture.NO_OVERLAY, 0);

                poseStack.popPose();
            }
        }
    }
}