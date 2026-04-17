package com.misanthropy.linggango.linggango_tweaks.registry; // Might as well delete this overtime

import com.misanthropy.linggango.linggango_tweaks.LinggangoTweaks;
import com.misanthropy.linggango.linggango_tweaks.entity.ParrySparkEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, LinggangoTweaks.MOD_ID);

    public static final RegistryObject<EntityType<ParrySparkEntity>> PARRY_SPARK = ENTITIES.register("parry_spark",
            () -> EntityType.Builder.<ParrySparkEntity>of(ParrySparkEntity::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F)
                    .clientTrackingRange(64)
                    .updateInterval(1)
                    .fireImmune()
                    .noSave()
                    .build("parry_spark")
    );
}