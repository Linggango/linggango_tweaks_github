package com.misanthropy.linggango.linggango_tweaks.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.DimensionArgument;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Mod.EventBusSubscriber
public class LinggangoCommands {

    @SubscribeEvent
    public static void onRegisterCommands(@NonNull RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> d = event.getDispatcher();

        d.register(Commands.literal("gm1").requires(s -> s.hasPermission(2)).executes(c -> setGameMode(c.getSource(), GameType.SURVIVAL)));
        d.register(Commands.literal("gm3").requires(s -> s.hasPermission(2)).executes(c -> setGameMode(c.getSource(), GameType.CREATIVE)));
        d.register(Commands.literal("gm2").requires(s -> s.hasPermission(2)).executes(c -> setGameMode(c.getSource(), GameType.ADVENTURE)));
        d.register(Commands.literal("gm4").requires(s -> s.hasPermission(2)).executes(c -> setGameMode(c.getSource(), GameType.SPECTATOR)));

        d.register(Commands.literal("feed").requires(s -> s.hasPermission(2)).executes(c -> {
            ServerPlayer p = c.getSource().getPlayerOrException();
            p.getFoodData().setFoodLevel(20);
            p.getFoodData().setSaturation(20.0f);
            return 1;
        }));

        d.register(Commands.literal("heal").requires(s -> s.hasPermission(2)).executes(c -> {
            ServerPlayer p = c.getSource().getPlayerOrException();
            p.setHealth(p.getMaxHealth());
            return 1;
        }));

        d.register(Commands.literal("fly").requires(s -> s.hasPermission(2)).executes(c -> {
            ServerPlayer p = c.getSource().getPlayerOrException();
            p.getAbilities().mayfly = !p.getAbilities().mayfly;
            p.onUpdateAbilities();
            return 1;
        }));

        d.register(Commands.literal("god").requires(s -> s.hasPermission(2)).executes(c -> {
            ServerPlayer p = c.getSource().getPlayerOrException();
            p.getAbilities().invulnerable = !p.getAbilities().invulnerable;
            p.onUpdateAbilities();
            return 1;
        }));

        d.register(Commands.literal("repair").requires(s -> s.hasPermission(2)).executes(c -> {
            c.getSource().getPlayerOrException().getMainHandItem().setDamageValue(0);
            return 1;
        }));

        d.register(Commands.literal("repairall").requires(s -> s.hasPermission(2)).executes(c -> {
            ServerPlayer p = c.getSource().getPlayerOrException();
            p.getInventory().items.forEach(i -> i.setDamageValue(0));
            p.getInventory().armor.forEach(i -> i.setDamageValue(0));
            p.getInventory().offhand.forEach(i -> i.setDamageValue(0));
            return 1;
        }));

        d.register(Commands.literal("top").requires(s -> s.hasPermission(2)).executes(c -> {
            ServerPlayer p = c.getSource().getPlayerOrException();
            BlockPos pos = p.blockPosition();
            int y = p.serverLevel().getHeight(Heightmap.Types.MOTION_BLOCKING, pos.getX(), pos.getZ());
            p.teleportTo(pos.getX() + 0.5, y + 0.1, pos.getZ() + 0.5);
            return 1;
        }));

        d.register(Commands.literal("bottom").requires(s -> s.hasPermission(2)).executes(c -> {
            ServerPlayer p = c.getSource().getPlayerOrException();
            int y = p.serverLevel().getMinBuildHeight();
            BlockPos pos = p.blockPosition();
            p.teleportTo(pos.getX() + 0.5, y + 1.1, pos.getZ() + 0.5);
            return 1;
        }));

        d.register(Commands.literal("spawn").requires(s -> s.hasPermission(2)).executes(c -> {
            ServerPlayer p = c.getSource().getPlayerOrException();
            ServerLevel overworld = Objects.requireNonNull(p.getServer()).getLevel(Level.OVERWORLD);
            assert overworld != null;
            BlockPos spawn = overworld.getSharedSpawnPos();
            p.teleportTo(overworld, spawn.getX() + 0.5, spawn.getY() + 0.1, spawn.getZ() + 0.5, p.getYRot(), p.getXRot());
            return 1;
        }));

        d.register(Commands.literal("craft").requires(s -> s.hasPermission(2)).executes(c -> {
            ServerPlayer p = c.getSource().getPlayerOrException();
            p.openMenu(new SimpleMenuProvider((id, inv, pl) -> new CraftingMenu(id, inv, ContainerLevelAccess.create(p.level(), p.blockPosition())) {
                @Override
                public boolean stillValid(@NonNull Player playerIn) {
                    return true;
                }
            }, Component.literal("Crafting")));
            return 1;
        }));

        d.register(Commands.literal("enderchest").requires(s -> s.hasPermission(2)).executes(c -> {
            ServerPlayer p = c.getSource().getPlayerOrException();
            p.openMenu(new SimpleMenuProvider((id, inv, pl) -> ChestMenu.threeRows(id, inv, p.getEnderChestInventory()), Component.literal("Ender Chest")));
            return 1;
        }));

        d.register(Commands.literal("clearinv").requires(s -> s.hasPermission(2)).executes(c -> {
            c.getSource().getPlayerOrException().getInventory().clearContent();
            return 1;
        }));

        d.register(Commands.literal("invsee").requires(s -> s.hasPermission(2))
                .then(Commands.argument("target", EntityArgument.player())
                        .executes(c -> {
                            ServerPlayer p = c.getSource().getPlayerOrException();
                            ServerPlayer target = EntityArgument.getPlayer(c, "target");

                            SimpleContainer combinedInventory = new SimpleContainer(54) {
                                @Override
                                public void setChanged() {
                                    super.setChanged();
                                    for (int i = 0; i < 36; i++) {
                                        target.getInventory().items.set(i, this.getItem(i));
                                    }
                                    for (int i = 0; i < 4; i++) {
                                        target.getInventory().armor.set(i, this.getItem(45 + i));
                                    }
                                    target.getInventory().offhand.set(0, this.getItem(49));
                                }
                            };

                            for (int i = 0; i < 36; i++) {
                                combinedInventory.setItem(i, target.getInventory().items.get(i));
                            }
                            for (int i = 0; i < 4; i++) {
                                combinedInventory.setItem(45 + i, target.getInventory().armor.get(i));
                            }
                            combinedInventory.setItem(49, target.getInventory().offhand.get(0));

                            p.openMenu(new SimpleMenuProvider((id, inv, pl) ->
                                    new ChestMenu(MenuType.GENERIC_9x6, id, inv, combinedInventory, 6),
                                    Component.literal(target.getName().getString() + "'s Inventory")));

                            return 1;
                        })));

        d.register(Commands.literal("speed").requires(s -> s.hasPermission(2))
                .then(Commands.argument("value", FloatArgumentType.floatArg(0, 10))
                        .executes(c -> {
                            ServerPlayer p = c.getSource().getPlayerOrException();
                            p.getAbilities().setWalkingSpeed(FloatArgumentType.getFloat(c, "value") / 10f);
                            p.onUpdateAbilities();
                            return 1;
                        })));

        d.register(Commands.literal("flyspeed").requires(s -> s.hasPermission(2))
                .then(Commands.argument("value", FloatArgumentType.floatArg(0, 10))
                        .executes(c -> {
                            ServerPlayer p = c.getSource().getPlayerOrException();
                            p.getAbilities().setFlyingSpeed(FloatArgumentType.getFloat(c, "value") / 10f);
                            p.onUpdateAbilities();
                            return 1;
                        })));

        d.register(Commands.literal("lightning").requires(s -> s.hasPermission(2)).executes(c -> {
            ServerPlayer p = c.getSource().getPlayerOrException();
            HitResult ray = p.pick(100.0D, 0.0F, false);
            if (ray.getType() == HitResult.Type.BLOCK) {
                LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(p.serverLevel());
                assert bolt != null;
                bolt.moveTo(ray.getLocation());
                p.serverLevel().addFreshEntity(bolt);
            }
            return 1;
        }));

        d.register(Commands.literal("launch").requires(s -> s.hasPermission(2)).executes(c -> {
            ServerPlayer p = c.getSource().getPlayerOrException();
            p.setDeltaMovement(p.getDeltaMovement().add(0, 5.0, 0));
            p.hurtMarked = true;
            return 1;
        }));

        d.register(Commands.literal("explode").requires(s -> s.hasPermission(2)).executes(c -> {
            ServerPlayer p = c.getSource().getPlayerOrException();
            p.serverLevel().explode(null, p.getX(), p.getY(), p.getZ(), 4.0F, Level.ExplosionInteraction.NONE);
            return 1;
        }));

        d.register(Commands.literal("burn").requires(s -> s.hasPermission(2)).executes(c -> {
            c.getSource().getPlayerOrException().setSecondsOnFire(10);
            return 1;
        }));

        d.register(Commands.literal("dimtp").requires(s -> s.hasPermission(2))
                .then(Commands.argument("dim", DimensionArgument.dimension())
                        .executes(c -> {
                            ServerPlayer p = c.getSource().getPlayerOrException();
                            ServerLevel lvl = DimensionArgument.getDimension(c, "dim");
                            p.teleportTo(lvl, p.getX(), p.getY(), p.getZ(), p.getYRot(), p.getXRot());
                            return 1;
                        })));

        d.register(Commands.literal("structurename").requires(s -> s.hasPermission(2)).executes(c -> {
            ServerPlayer p = c.getSource().getPlayerOrException();
            ServerLevel level = p.serverLevel();
            BlockPos pos = p.blockPosition();

            var registry = level.registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.STRUCTURE);
            var starts = level.structureManager().startsForStructure(new net.minecraft.world.level.ChunkPos(pos), s -> true);

            List<String> found = new ArrayList<>();
            for (var start : starts) {
                if (start != null && start.isValid() && start.getBoundingBox().isInside(pos)) {
                    net.minecraft.resources.ResourceLocation id = registry.getKey(start.getStructure());
                    if (id != null) found.add(id.toString());
                }
            }

            if (found.isEmpty()) {
                p.sendSystemMessage(Component.literal("§e[Linggango] §cNo structures detected here."));
            } else {
                p.sendSystemMessage(Component.literal("§e[Linggango] §aYou are currently inside:"));
                for (String s : found) {
                    p.sendSystemMessage(Component.literal(" §7> §b" + s));
                }
            }
            return 1;
        }));

        d.register(Commands.literal("structurecount").requires(s -> s.hasPermission(2)).executes(ctx -> {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            var registry = player.serverLevel().registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.STRUCTURE);
            int count = registry.size();
            player.sendSystemMessage(Component.literal("§e[Linggango] §aThere are §b" + count + " §astructures registered."));
            return 1;
        }));
    }

    private static int setGameMode(@NonNull CommandSourceStack source, @NonNull GameType type) {
        try {
            source.getPlayerOrException().setGameMode(type);
        } catch (Exception ignored) {}
        return 1;
    }
}