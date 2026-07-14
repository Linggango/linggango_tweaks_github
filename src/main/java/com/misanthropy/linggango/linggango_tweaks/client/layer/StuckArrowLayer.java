package com.misanthropy.linggango.linggango_tweaks.client.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Arrow;
import org.jspecify.annotations.NonNull;

public class StuckArrowLayer<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {
    private final EntityRenderDispatcher dispatcher;
    private Arrow dummyArrow;

    public StuckArrowLayer(RenderLayerParent<T, M> renderer, EntityRendererProvider.Context context) {
        super(renderer);
        this.dispatcher = context.getEntityRenderDispatcher();
    }

    @Override
    public void render(@NonNull PoseStack poseStack, @NonNull MultiBufferSource buffer, int packedLight, @NonNull T entity, float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        int arrowCount = entity.getArrowCount();
        if (arrowCount <= 0) {
            return;
        }

        if (this.dummyArrow == null || this.dummyArrow.level() != entity.level()) {
            this.dummyArrow = new Arrow(entity.level(), entity.getX(), entity.getY(), entity.getZ());
        }

        RandomSource random = RandomSource.create(entity.getId());
        float bbWidth = entity.getBbWidth();
        float bbHeight = entity.getBbHeight();

        for (int i = 0; i < arrowCount; i++) {
            poseStack.pushPose();

            float rx = (random.nextFloat() * 2.0F - 1.0F) * bbWidth * 0.5F;
            float ry = random.nextFloat() * bbHeight;
            float rz = (random.nextFloat() * 2.0F - 1.0F) * bbWidth * 0.5F;

            float normalize = Mth.sqrt(rx * rx + rz * rz);
            if (normalize > 0) {
                rx = (rx / normalize) * (bbWidth * 0.5F);
                rz = (rz / normalize) * (bbWidth * 0.5F);
            }

            poseStack.translate(rx, ry, rz);

            float dirX = -rx;
            float dirY = (bbHeight / 2.0F) - ry;
            float dirZ = -rz;
            float xzDist = Mth.sqrt(dirX * dirX + dirZ * dirZ);

            this.dummyArrow.setYRot((float)(Math.atan2(dirX, dirZ) * (180F / Math.PI)));
            this.dummyArrow.setXRot((float)(Math.atan2(dirY, xzDist) * (180F / Math.PI)));
            this.dummyArrow.yRotO = this.dummyArrow.getYRot();
            this.dummyArrow.xRotO = this.dummyArrow.getXRot();

            this.dispatcher.render(this.dummyArrow, 0.0D, 0.0D, 0.0D, 0.0F, partialTick, poseStack, buffer, packedLight);

            poseStack.popPose();
        }
    }
}