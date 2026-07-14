package com.misanthropy.linggango.linggango_tweaks.network.macabre;

import com.mojang.logging.LogUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.lang.reflect.Method;
import java.util.function.Supplier;

@SuppressWarnings("unused")
public class PitTeleportPacket {

    private static Method executeMethod = null;
    private static boolean reflectionSearched = false;

    private static Item crackedBloodItem = null;
    private static boolean crackedBloodSearched = false;

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
                initReflection();
                if (executeMethod != null) {
                    try {
                        executeMethod.invoke(null, player.level(), player);
                    } catch (Exception e) {
                        LogUtils.getLogger().error("Failed to trigger Macabre teleport procedure via reflection", e);
                        player.server.getCommands().performPrefixedCommand(
                                player.createCommandSourceStack().withSuppressedOutput().withPermission(4),
                                "execute as " + player.getScoreboardName() + " in macabre:the_pit run tp @s ~ ~ ~"
                        );
                    }
                } else {
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

    private static void initReflection() {
        if (!reflectionSearched) {
            try {
                Class<?> procedureClass = Class.forName("com.curseforge.macabre.procedures.CrystalizedBloodRightclickedProcedure");
                executeMethod = procedureClass.getMethod("execute", net.minecraft.world.level.LevelAccessor.class, net.minecraft.world.entity.Entity.class);
            } catch (Exception e) {
                executeMethod = null;
            }
            reflectionSearched = true;
        }
    }

    private static void initCrackedBlood() {
        if (!crackedBloodSearched) {
            crackedBloodItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation("macabre", "cracked_crystalized_blood"));
            crackedBloodSearched = true;
        }
    }

    private void clearCrackedBlood(ServerPlayer player) {
        initCrackedBlood();
        if (crackedBloodItem == null) return;

        boolean changed = false;
        var inventory = player.getInventory();
        int size = inventory.getContainerSize();
        for (int i = 0; i < size; i++) {
            ItemStack stack = inventory.getItem(i);
            if (!stack.isEmpty() && stack.getItem() == crackedBloodItem) {
                inventory.setItem(i, ItemStack.EMPTY);
                changed = true;
            }
        }
        if (changed) {
            player.containerMenu.broadcastChanges();
            player.inventoryMenu.broadcastChanges();
        }
    }
}