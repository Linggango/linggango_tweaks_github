package com.misanthropy.linggango.linggango_tweaks.network.ring;

import com.misanthropy.linggango.linggango_tweaks.config.TweaksConfig;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.function.Supplier;

@SuppressWarnings("unused")
public class RingSelectionPacket {
    private final String ringId;

    public RingSelectionPacket(String ringId) { this.ringId = ringId; }
    public RingSelectionPacket(FriendlyByteBuf buf) { this.ringId = buf.readUtf(256); }

    public static void encode(RingSelectionPacket msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.ringId);
    }

    public static void handle(RingSelectionPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            CompoundTag persistentData = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);

            if (persistentData.contains("linggango_setup_stage") && persistentData.getInt("linggango_setup_stage") >= 3) return;

            persistentData.putInt("linggango_setup_stage", 3);
            player.getPersistentData().put(Player.PERSISTED_NBT_TAG, persistentData);

            if (msg.ringId != null) {
                CuriosApi.getCuriosInventory(player).ifPresent(handler -> handler.getStacksHandler("ring").ifPresent(stacks -> {
                    var itemHandler = stacks.getStacks();

                    Item cursedItem = null;
                    ResourceLocation cursedLoc = ResourceLocation.tryParse(TweaksConfig.RING_CURSED_ID.get());
                    if (cursedLoc != null) cursedItem = ForgeRegistries.ITEMS.getValue(cursedLoc);

                    Item virtueItem = null;
                    ResourceLocation virtueLoc = ResourceLocation.tryParse(TweaksConfig.RING_VIRTUE_ID.get());
                    if (virtueLoc != null) virtueItem = ForgeRegistries.ITEMS.getValue(virtueLoc);

                    for (int i = 0; i < itemHandler.getSlots(); i++) {
                        ItemStack stack = itemHandler.getStackInSlot(i);
                        if (!stack.isEmpty()) {
                            Item item = stack.getItem();
                            if ((cursedItem != null && item == cursedItem) || (virtueItem != null && item == virtueItem)) {
                                itemHandler.setStackInSlot(i, ItemStack.EMPTY);
                            }
                        }
                    }

                    if (msg.ringId.equals("both")) {
                        if (cursedItem != null && cursedItem != Items.AIR) {
                            for (int i = 0; i < itemHandler.getSlots(); i++) {
                                if (itemHandler.getStackInSlot(i).isEmpty()) {
                                    itemHandler.setStackInSlot(i, new ItemStack(cursedItem));
                                    break;
                                }
                            }
                        }
                        if (virtueItem != null && virtueItem != Items.AIR) {
                            for (int i = 0; i < itemHandler.getSlots(); i++) {
                                if (itemHandler.getStackInSlot(i).isEmpty()) {
                                    itemHandler.setStackInSlot(i, new ItemStack(virtueItem));
                                    break;
                                }
                            }
                        }
                    } else if (msg.ringId.equals("cursed") && cursedItem != null && cursedItem != Items.AIR) {
                        for (int i = 0; i < itemHandler.getSlots(); i++) {
                            if (itemHandler.getStackInSlot(i).isEmpty()) {
                                itemHandler.setStackInSlot(i, new ItemStack(cursedItem));
                                break;
                            }
                        }
                    } else if (msg.ringId.equals("virtue") && virtueItem != null && virtueItem != Items.AIR) {
                        for (int i = 0; i < itemHandler.getSlots(); i++) {
                            if (itemHandler.getStackInSlot(i).isEmpty()) {
                                itemHandler.setStackInSlot(i, new ItemStack(virtueItem));
                                break;
                            }
                        }
                    }
                }));
            }
        });
        ctx.get().setPacketHandled(true);
    }
}