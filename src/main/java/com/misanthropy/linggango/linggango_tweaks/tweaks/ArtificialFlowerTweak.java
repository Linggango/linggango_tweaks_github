package com.misanthropy.linggango.linggango_tweaks.tweaks;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Mod.EventBusSubscriber(modid = "linggango_tweaks", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ArtificialFlowerTweak {
    private static final Random RANDOM = new Random();
    private static List<MobEffect> VANILLA_BENEFICIAL;
    private static List<MobEffect> VANILLA_HARMFUL;

    @SubscribeEvent
    public static void onEffectApply(MobEffectEvent.Applicable event) {
        if (!(event.getEntity() instanceof Player player)) return;

        MobEffectInstance instance = event.getEffectInstance();
        MobEffect effect = instance.getEffect();
        ResourceLocation effectKey = ForgeRegistries.MOB_EFFECTS.getKey(effect);

        if (effectKey == null || effectKey.getNamespace().equals("minecraft")) return;

        boolean hasFlower = player.getInventory().items.stream()
                .anyMatch(stack -> stack.getItem().getClass().getSimpleName().equals("ArtificialFlower"));

        if (hasFlower) {

            event.setResult(Result.DENY);

            MobEffect replacement = getRandomVanillaEffect(effect.isBeneficial());
            if (replacement != null) {

                player.addEffect(new MobEffectInstance(
                        replacement,
                        instance.getDuration(),
                        instance.getAmplifier(),
                        instance.isAmbient(),
                        instance.isVisible(),
                        instance.showIcon()
                ));
            }
        }
    }

    private static MobEffect getRandomVanillaEffect(boolean beneficial) {
        if (VANILLA_BENEFICIAL == null || VANILLA_HARMFUL == null) {
            initializeEffectLists();
        }

        List<MobEffect> pool = beneficial ? VANILLA_BENEFICIAL : VANILLA_HARMFUL;
        if (pool.isEmpty()) return beneficial ? MobEffects.REGENERATION : MobEffects.POISON;

        return pool.get(RANDOM.nextInt(pool.size()));
    }

    private static void initializeEffectLists() {
        List<MobEffect> vanillaEffects = ForgeRegistries.MOB_EFFECTS.getValues().stream()
                .filter(e -> {
                    ResourceLocation key = ForgeRegistries.MOB_EFFECTS.getKey(e);
                    return key != null && key.getNamespace().equals("minecraft");
                })
                .toList();

        VANILLA_BENEFICIAL = vanillaEffects.stream().filter(MobEffect::isBeneficial).collect(Collectors.toList());
        VANILLA_HARMFUL = vanillaEffects.stream().filter(e -> !e.isBeneficial()).collect(Collectors.toList());
    }
}