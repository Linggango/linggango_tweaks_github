package com.misanthropy.linggango.linggango_tweaks.mixin.client;

import com.misanthropy.linggango.linggango_tweaks.client.layer.StuckArrowLayer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("unchecked")
@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity, M extends EntityModel<T>>
        extends EntityRenderer<T> implements RenderLayerParent<T, M> {
    protected LivingEntityRendererMixin(EntityRendererProvider.Context context) {
        super(context);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void linggango$addArrowLayer(EntityRendererProvider.Context context, M model, float shadow, CallbackInfo ci) {
        LivingEntityRenderer<T, M> self = (LivingEntityRenderer<T, M>) (Object) this;
        if (self instanceof PlayerRenderer) return;

        self.addLayer(new StuckArrowLayer<>(self, context));
    }
}