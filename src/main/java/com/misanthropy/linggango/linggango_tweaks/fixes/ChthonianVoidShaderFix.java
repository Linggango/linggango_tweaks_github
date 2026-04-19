package com.misanthropy.linggango.linggango_tweaks.fixes;

import com.misanthropy.linggango.linggango_tweaks.LinggangoTweaks;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import org.jspecify.annotations.NonNull;

import java.io.IOException;

@EventBusSubscriber(
        modid = LinggangoTweaks.MOD_ID,
        bus = EventBusSubscriber.Bus.MOD,
        value = {Dist.CLIENT}
)
public class ChthonianVoidShaderFix {
    public static ShaderInstance chthonianVoidShader;

    @SubscribeEvent
    public static void onRegisterShaders(@NonNull RegisterShadersEvent event) throws IOException {
        event.registerShader(
                new ShaderInstance(
                        event.getResourceProvider(),
                        new ResourceLocation("terramity:rendertype_chthonian_void"),
                        DefaultVertexFormat.POSITION
                ),
                (shader) -> chthonianVoidShader = shader
        );
    }
}