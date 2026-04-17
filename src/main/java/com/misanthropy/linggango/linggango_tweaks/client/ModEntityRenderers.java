package com.misanthropy.linggango.linggango_tweaks.client;

import com.misanthropy.linggango.linggango_tweaks.entity.ParrySparkEntity;
import com.misanthropy.linggango.linggango_tweaks.registry.ModEntities;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "linggango_tweaks", bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ModEntityRenderers {

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.PARRY_SPARK.get(), NoopRenderer::new);
    }
    public static class NoopRenderer extends EntityRenderer<ParrySparkEntity> {
        public NoopRenderer(EntityRendererProvider.Context context) {
            super(context);
        }

        @Override
        public ResourceLocation getTextureLocation(ParrySparkEntity entity) {
            return new ResourceLocation("minecraft", "textures/misc/white.png");
        }
    }
}