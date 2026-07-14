package com.misanthropy.linggango.linggango_tweaks.features;

import com.misanthropy.linggango.linggango_tweaks.LinggangoTweaks;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.AdvancementEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid = LinggangoTweaks.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class DirkStabbings {

    private static final SoundEvent STAB_1_SOUND = SoundEvent.createVariableRangeEvent(new ResourceLocation(LinggangoTweaks.MOD_ID, "stab_1"));
    private static final SoundEvent STAB_2_SOUND = SoundEvent.createVariableRangeEvent(new ResourceLocation(LinggangoTweaks.MOD_ID, "stab_2"));
    private static final SoundEvent STAB_3_SOUND = SoundEvent.createVariableRangeEvent(new ResourceLocation(LinggangoTweaks.MOD_ID, "stab_3"));
    private static final SoundEvent STAB_4_SOUND = SoundEvent.createVariableRangeEvent(new ResourceLocation(LinggangoTweaks.MOD_ID, "stab_4"));
    private static final SoundEvent ACCEPTANCE_SOUND = SoundEvent.createVariableRangeEvent(new ResourceLocation(LinggangoTweaks.MOD_ID, "acceptance"));
    private static final SoundEvent DIM_LEAVE_SOUND = SoundEvent.createVariableRangeEvent(new ResourceLocation(LinggangoTweaks.MOD_ID, "macabre_dimension_leave"));

    @SubscribeEvent
    public static void onDirkRightClick(PlayerInteractEvent.RightClickItem event) {
        if (event.getHand() != InteractionHand.MAIN_HAND) return;

        ItemStack stack = event.getItemStack();
        if (stack.isEmpty()) return;

        ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (itemId != null && itemId.getNamespace().equals("macabre")) {
            Player player = event.getEntity();
            String path = itemId.getPath();

            if (!event.getLevel().isClientSide()) {
                switch (path) {
                    case "sacrificial_dirk" -> {
                        player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 200, 0));
                        event.getLevel().playSound(null, player.blockPosition(), STAB_1_SOUND, SoundSource.PLAYERS, 1.0F, 1.0F);
                    }
                    case "sac_dir_2" -> {
                        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 200, 2));
                        player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 200, 4));
                        event.getLevel().playSound(null, player.blockPosition(), STAB_2_SOUND, SoundSource.PLAYERS, 1.0F, 1.0F);
                    }
                    case "sac_dir_3" ->
                            event.getLevel().playSound(null, player.blockPosition(), STAB_3_SOUND, SoundSource.PLAYERS, 1.0F, 1.0F);
                    case "sac_dir_4" -> {
                        event.getLevel().playSound(null, player.blockPosition(), STAB_4_SOUND, SoundSource.PLAYERS, 1.0F, 1.0F);
                        event.getLevel().playSound(null, player.blockPosition(), ACCEPTANCE_SOUND, SoundSource.PLAYERS, 1.0F, 1.0F);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onAdvancementEarned(AdvancementEvent.AdvancementEarnEvent event) {
        ResourceLocation id = event.getAdvancement().getId();
        if (id.getNamespace().equals("macabre") && id.getPath().equals("ach_5")) {
            Player player = event.getEntity();
            if (!player.level().isClientSide()) {
                player.level().playSound(null, player.blockPosition(), DIM_LEAVE_SOUND, SoundSource.PLAYERS, 1.0F, 1.0F);
            }
        }
    }
}