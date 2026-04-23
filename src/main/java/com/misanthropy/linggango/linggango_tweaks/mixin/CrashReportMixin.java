package com.misanthropy.linggango.linggango_tweaks.mixin;

import com.misanthropy.linggango.linggango_tweaks.TraceUtils;
import net.minecraft.CrashReport;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CrashReport.class)
public class CrashReportMixin {
    @Shadow
    private StackTraceElement[] uncategorizedStackTrace;

    @Inject(method = "getDetails(Ljava/lang/StringBuilder;)V", at = @At(value = "FIELD", target = "Lnet/minecraft/CrashReport;details:Ljava/util/List;", opcode = Opcodes.GETFIELD))
    private void mixintrace_addTrace(StringBuilder crashReportBuilder, CallbackInfo ci) {
        int trailingNewlineCount = 0;
        // Remove trailing \n
        if (crashReportBuilder.charAt(crashReportBuilder.length() - 1) == '\n') {
            crashReportBuilder.deleteCharAt(crashReportBuilder.length() - 1);
            trailingNewlineCount++;
        }
        if (crashReportBuilder.charAt(crashReportBuilder.length() - 1) == '\n') {
            crashReportBuilder.deleteCharAt(crashReportBuilder.length() - 1);
            trailingNewlineCount++;
        }
        TraceUtils.printTrace(uncategorizedStackTrace, crashReportBuilder);
        crashReportBuilder.append("\n".repeat(trailingNewlineCount));
    }
}