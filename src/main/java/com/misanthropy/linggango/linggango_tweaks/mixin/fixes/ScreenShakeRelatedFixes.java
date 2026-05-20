package com.misanthropy.linggango.linggango_tweaks.mixin.fixes;

import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("unused")
public class ScreenShakeRelatedFixes {

    public static boolean isTravelOpticsCaller() {
        return StackWalker.getInstance().walk(stream ->
                stream.anyMatch(frame -> frame.getClassName().startsWith("com.gametechbc.traveloptics."))
        );
    }

    @Pseudo
    @Mixin(targets = {"com.github.L_Ender.cataclysm.entity.effect.ScreenShake_Entity"}, remap = false)
    public static class CataclysmMixin {
        @Inject(method = {"ScreenShake"}, at = {@At("HEAD")}, cancellable = true, remap = false)
        private static void disableTravelOpticsCataclysmScreenShake(Level world, Vec3 position, float radius, float magnitude, int duration, int fadeDuration, CallbackInfo ci) {
            if (ScreenShakeRelatedFixes.isTravelOpticsCaller()) ci.cancel();
        }
    }

    @Pseudo
    @Mixin(targets = {"io.redspace.ironsspellbooks.api.util.CameraShakeManager"}, remap = false)
    public static class IronsSpellbooksMixin {
        @Inject(method = {"addCameraShake(Lio/redspace/ironsspellbooks/api/util/CameraShakeData;)V"}, at = {@At("HEAD")}, cancellable = true, remap = false)
        private static void disableTravelOpticsIronsCameraShake(@Coerce Object data, CallbackInfo ci) {
            if (ScreenShakeRelatedFixes.isTravelOpticsCaller()) ci.cancel();
        }
    }

    @Pseudo
    @Mixin(targets = {"com.gametechbc.traveloptics.events.ForgeClientEvents"}, remap = false)
    public static class TravelOpticsMixin {
        @Inject(method = {"onCameraSetup"}, at = {@At("HEAD")}, cancellable = true, remap = false)
        private static void disableTravelOpticsNativeCameraShake(ViewportEvent.ComputeCameraAngles event, CallbackInfo ci) {
            ci.cancel();
        }
    }

    @Mixin(targets = {"com.gametechbc.traveloptics.events.ForgeClientOverlayEvent"}, remap = false)
    public static class TravelOpticsOverlayPostMixin {
        @Inject(method = {"onRenderOverlay"}, at = {@At("HEAD")}, cancellable = true, remap = false)
        private static void limitTravelOpticsOverlayPostRendering(RenderGuiOverlayEvent.Post event, CallbackInfo ci) {
            if (event.getOverlay() != VanillaGuiOverlay.CROSSHAIR.type()) ci.cancel();
        }
    }
}