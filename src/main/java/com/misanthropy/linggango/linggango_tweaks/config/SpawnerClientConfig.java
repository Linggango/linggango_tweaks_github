package com.misanthropy.linggango.linggango_tweaks.config;
import net.minecraftforge.common.ForgeConfigSpec;

public class SpawnerClientConfig {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec.IntValue PARTICLE_CHANCE;
    public static final ForgeConfigSpec.BooleanValue REQUIRE_SNEAK_FOR_ENTITY;

    static {
        BUILDER.push("SpawnerOptimizations");
        PARTICLE_CHANCE = BUILDER
                .comment("Percentage chance for a spawner particle to render. Lower = better FPS. (0 to 100)")
                .defineInRange("particleRenderChance", 50, 0, 100);

        REQUIRE_SNEAK_FOR_ENTITY = BUILDER
                .comment("If true, the spinning mob inside spawners will ONLY render when you hold Sneak (Shift). Massive FPS boost!")
                .define("requireSneakToRender", true);
        BUILDER.pop();
    }

    public static final ForgeConfigSpec SPEC = BUILDER.build();
}