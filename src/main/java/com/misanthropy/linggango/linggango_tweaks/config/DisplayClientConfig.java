package com.misanthropy.linggango.linggango_tweaks.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class DisplayClientConfig {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.BooleanValue AUTO_FPS_SYNC;
//    public static final ForgeConfigSpec.BooleanValue ACCEPTED_OCULUS_WARNING;

    static {
        BUILDER.push("display_tweaks");
        AUTO_FPS_SYNC = BUILDER.comment("Automatically locks max framerate to your monitor's refresh rate and enables VSync.")
                .define("autoFpsSync", false);

// for the oculus installation shi.. sorry for putting random things in classes. This is a terrible habit.
//        ACCEPTED_OCULUS_WARNING = BUILDER.comment("Did the user see")
//                .define("acceptedOculusWarning", false);
//        BUILDER.pop();

        SPEC = BUILDER.build();
    }
}