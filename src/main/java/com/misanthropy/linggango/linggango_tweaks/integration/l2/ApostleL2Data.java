package com.misanthropy.linggango.linggango_tweaks.integration.l2;

import net.minecraft.world.entity.LivingEntity;
import org.jspecify.annotations.Nullable;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ApostleL2Data {
    public static @Nullable LivingEntity CURRENT_APOSTLE = null;

    public static final Set<UUID> SKIP_HEALTH_RESET = Collections.newSetFromMap(new ConcurrentHashMap<>());
    public static final Set<UUID> SKIP_TICK_HEALTH_RESET = Collections.newSetFromMap(new ConcurrentHashMap<>());
}
