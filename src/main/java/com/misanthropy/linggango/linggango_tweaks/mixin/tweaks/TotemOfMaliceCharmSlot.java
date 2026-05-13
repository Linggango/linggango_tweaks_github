package com.misanthropy.linggango.linggango_tweaks.mixin.tweaks; // Credits: https://www.curseforge.com/minecraft/mc-mods/celestweaks

import com.google.gson.JsonElement;
import com.misanthropy.linggango.linggango_tweaks.LinggangoTweaks;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.theillusivec4.curios.common.data.CuriosSlotManager;
import top.theillusivec4.curios.mixin.CuriosImplMixinHooks;

import java.util.Map;

@Mixin(CuriosSlotManager.class)
public class TotemOfMaliceCharmSlot {

    @Inject(
            method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V",
            at = @At("TAIL"),
            remap = false
    )
    private void linggango$injectTotem(Map<ResourceLocation, JsonElement> pObject, ResourceManager pResourceManager, ProfilerFiller pProfiler, CallbackInfo ci) {
        Item totem = ForgeRegistries.ITEMS.getValue(new ResourceLocation("enigmaticaddons:totem_of_malice"));

        if (totem != null) {
            CuriosSlotManager.SERVER.getSlot("charm").ifPresent(slot -> {
                ResourceLocation identifier = new ResourceLocation(LinggangoTweaks.MOD_ID, "totem_of_malice_slot");
                slot.getValidators().add(identifier);
                CuriosImplMixinHooks.registerCurioPredicate(identifier, slotResult ->
                        slotResult.stack().getItem() == totem
                );
            });
        }
    }
}