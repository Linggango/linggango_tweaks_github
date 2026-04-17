package com.misanthropy.linggango.linggango_tweaks.mixin;

import com.misanthropy.linggango.linggango_tweaks.fixes.ChthonianVoidShaderFix;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import net.mcreator.terramity.ChthonianVoidBlockEntityRenderer;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;

@Mixin(
        value = {ChthonianVoidBlockEntityRenderer.class},
        remap = false
)
public class MixinChthonianVoidBlockEntityRenderer {

    @Unique
    private static final ResourceLocation CHTHONIAN_TEXTURE = new ResourceLocation("terramity:textures/entities/cthonian_void.png");

    @Unique
    private static final RenderType CHTHONIAN_VOID_RENDER_TYPE = RenderType.create(
            "chthonian_void",
            DefaultVertexFormat.POSITION,
            Mode.QUADS,
            256,
            false,
            false,
            RenderType.CompositeState.builder()
                    .setShaderState(new RenderStateShard.ShaderStateShard(() -> ChthonianVoidShaderFix.chthonianVoidShader))
                    .setTextureState(RenderStateShard.MultiTextureStateShard.builder()
                            .add(CHTHONIAN_TEXTURE, false, false)
                            .add(CHTHONIAN_TEXTURE, false, false)
                            .build()
                    )
                    .createCompositeState(false)
    );

    /**
     * @author Misanthropy
     * @reason hi gamers
     */
    @Overwrite
    protected RenderType renderType() {
        return CHTHONIAN_VOID_RENDER_TYPE;
    }
}