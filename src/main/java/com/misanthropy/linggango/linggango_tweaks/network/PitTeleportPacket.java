package com.misanthropy.linggango.linggango_tweaks.network;

import com.mojang.logging.LogUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.lang.reflect.Method;
import java.util.function.Supplier;

@SuppressWarnings("unused")
public class PitTeleportPacket {

    public PitTeleportPacket() {
    }

    public PitTeleportPacket(FriendlyByteBuf buf) {
    }

    public void encode(FriendlyByteBuf buf) {
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                try {
                    Class<?> procedureClass = Class.forName("com.curseforge.macabre.procedures.CrystalizedBloodRightclickedProcedure");
                    Method executeMethod = procedureClass.getMethod("execute", net.minecraft.world.level.LevelAccessor.class, net.minecraft.world.entity.Entity.class);
                    executeMethod.invoke(null, player.level(), player);
                } catch (Exception e) {
                    LogUtils.getLogger().error("Failed to trigger Macabre teleport procedure via reflection", e);
                    player.server.getCommands().performPrefixedCommand(
                            player.createCommandSourceStack().withSuppressedOutput().withPermission(4),
                            "execute as " + player.getScoreboardName() + " in macabre:the_pit run tp @s ~ ~ ~"
                    );
                }
                clearCrackedBlood(player);
            }
        });
        ctx.get().setPacketHandled(true);
    }

    private void clearCrackedBlood(ServerPlayer player) {
        boolean changed = false;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty()) {
                ResourceLocation key = net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(stack.getItem());
                if (key != null && key.toString().equals("macabre:cracked_crystalized_blood")) {
                    player.getInventory().setItem(i, ItemStack.EMPTY);
                    changed = true;
                }
            }
        }
        if (changed) {
            player.containerMenu.broadcastChanges();
            player.inventoryMenu.broadcastChanges();
        }
    }
}