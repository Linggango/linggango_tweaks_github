package com.misanthropy.linggango.linggango_tweaks.skills.passive;

import com.misanthropy.linggango.linggango_tweaks.LinggangoTweaks;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jspecify.annotations.NonNull;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = LinggangoTweaks.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class VanguardSwordBlockingClient {

    @SubscribeEvent
    public static void onRenderHand(@NonNull RenderHandEvent event) {
        Minecraft mc = Minecraft.getInstance();
        AbstractClientPlayer player = mc.player;

        if (player != null && VanguardSwordBlocking.isVanguardBlocking(player) && event.getHand() == player.getUsedItemHand()) {
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