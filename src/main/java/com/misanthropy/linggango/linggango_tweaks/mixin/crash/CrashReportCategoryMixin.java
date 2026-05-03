package com.misanthropy.linggango.linggango_tweaks.mixin.crash;

import com.misanthropy.linggango.linggango_tweaks.crash.TraceUtils;
import net.minecraft.CrashReportCategory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = CrashReportCategory.class)
public class CrashReportCategoryMixin {
    @Shadow
    private StackTraceElement[] stackTrace;

    @Inject(method = "getDetails", at = @At("TAIL"))
    private void mixintrace_addTrace(StringBuilder crashReportBuilder, CallbackInfo ci) {
        TraceUtils.printTrace(stackTrace, crashReportBuilder);
    }
}