package com.misanthropy.linggango.linggango_tweaks.tweaks;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

@Mod.EventBusSubscriber(modid = "linggango_tweaks", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class DungeonSwordTweak {

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onDungeonSoulCap(LivingDeathEvent event) {
        if (event.getSource().getEntity() instanceof Player player) {
            ItemStack mainHand = player.getMainHandItem();

            if (mainHand.getItem().getClass().getSimpleName().equals("DungeonSwordReinforced")) {
                CompoundTag tag = mainHand.getOrCreateTag();

                if (!tag.contains("DungeonAddition", 9)) {
                    tag.put("DungeonAddition", new ListTag());
                }

                ListTag list = tag.getList("DungeonAddition", 8);
                ResourceLocation victimId = ForgeRegistries.ENTITY_TYPES.getKey(event.getEntity().getType());

                if (victimId != null) {
                    String victimName = victimId.toString();

                    boolean isUnique = true;
                    for (int i = 0; i < list.size(); i++) {
                        if (list.getString(i).equals(victimName)) {
                            isUnique = false;
                            break;
                        }
                    }

                    if (isUnique && list.size() < 40) {
                        list.add(StringTag.valueOf(victimName));
                    }
                }

                while (list.size() > 40) {
                    list.remove(list.size() - 1);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (stack.isEmpty()) return;

        if (stack.getItem().getClass().getSimpleName().equals("DungeonSwordReinforced")) {
            List<Component> tooltip = event.getToolTip();
            if (tooltip.size() > 3) {
                tooltip.remove(3);
                tooltip.remove(2);
                tooltip.remove(1);
            }

            tooltip.add(1, Component.literal("A scythe that remembers every unique foe it has slain.").withStyle(ChatFormatting.DARK_AQUA));
            CompoundTag tag = stack.getTag();
            int count = 0;
            if (tag != null && tag.contains("DungeonAddition", 9)) {
                count = tag.getList("DungeonAddition", 8).size();
            }

            float bonus = (float)count * 0.25F;
            tooltip.add(2, Component.literal(String.format("Unique Souls: %d/40 (+%.1f Damage)", count, bonus)).withStyle(ChatFormatting.DARK_PURPLE));
            tooltip.add(3, Component.literal("Every 2 unique souls grant +0.5 Attack Damage.").withStyle(ChatFormatting.GRAY));
            if (count >= 40) {
                tooltip.add(4, Component.literal("The blade's memory is at its limit.").withStyle(ChatFormatting.GOLD).withStyle(ChatFormatting.ITALIC));
            } else {
                tooltip.add(4, Component.literal("Slay new types of entities to increase power.").withStyle(ChatFormatting.GRAY).withStyle(ChatFormatting.ITALIC));
            }
        }
    }
}