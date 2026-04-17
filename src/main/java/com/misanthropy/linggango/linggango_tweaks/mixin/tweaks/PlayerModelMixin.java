package com.misanthropy.linggango.linggango_tweaks.mixin.tweaks; // Mozilla Public License

import com.misanthropy.linggango.linggango_tweaks.skills.VanguardSwordBlocking;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerModel.class)
public class PlayerModelMixin<T extends LivingEntity> {

    @Inject(method = "setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V", at = @At("TAIL"))
    public void setupAnimBlocking(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci) {
        if (entity instanceof Player player && VanguardSwordBlocking.isVanguardBlocking(player)) {
            PlayerModel<?> model = (PlayerModel<?>) (Object) this;

            boolean isRightHand = player.getMainArm() == HumanoidArm.RIGHT;
            boolean isMainHandUsed = player.getUsedItemHand() == InteractionHand.MAIN_HAND;

            ModelPart arm = (isRightHand == isMainHandUsed) ? model.rightArm : model.leftArm;

            arm.xRot = arm.xRot - ((float)Math.PI / 5F);
            arm.yRot = (isRightHand == isMainHandUsed) ? -((float)Math.PI / 6F) : ((float)Math.PI / 6F);
        }
    }
}