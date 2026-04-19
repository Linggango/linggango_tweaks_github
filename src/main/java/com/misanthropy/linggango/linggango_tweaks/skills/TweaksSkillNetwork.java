package com.misanthropy.linggango.linggango_tweaks.skills;

import com.misanthropy.linggango.linggango_tweaks.LinggangoTweaks;
import com.misanthropy.linggango.linggango_tweaks.skills.client.ClientSkillEvents;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import org.jspecify.annotations.NonNull;

import java.util.function.Supplier;

@SuppressWarnings("unused")
public class TweaksSkillNetwork {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(LinggangoTweaks.MOD_ID, "skills"),
            () -> PROTOCOL_VERSION, PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);

    public static void register() {
        int id = 0;
        INSTANCE.registerMessage(id++, SkillUseC2SPacket.class, SkillUseC2SPacket::encode, SkillUseC2SPacket::decode, SkillUseC2SPacket::handle);
        INSTANCE.registerMessage(id, SkillSyncS2CPacket.class, SkillSyncS2CPacket::encode, SkillSyncS2CPacket::decode, SkillSyncS2CPacket::handle);
    }

    public static class SkillUseC2SPacket {
        public SkillUseC2SPacket() {}
        public static void encode(SkillUseC2SPacket msg, FriendlyByteBuf buf) {}
        public static @NonNull SkillUseC2SPacket decode(FriendlyByteBuf buf) { return new SkillUseC2SPacket(); }
        public static void handle(SkillUseC2SPacket msg, @NonNull Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                ServerPlayer player = ctx.get().getSender();
                if (player != null) {
                    SkillManager.useActiveSkill(player);
                }
            });
            ctx.get().setPacketHandled(true);
        }
    }

    public record SkillSyncS2CPacket(String classId, int cdRemaining, int maxCd, boolean isActive) {

        public static void encode(@NonNull SkillSyncS2CPacket msg, @NonNull FriendlyByteBuf buf) {
                buf.writeUtf(msg.classId);
                buf.writeInt(msg.cdRemaining);
                buf.writeInt(msg.maxCd);
                buf.writeBoolean(msg.isActive);
            }

            public static @NonNull SkillSyncS2CPacket decode(@NonNull FriendlyByteBuf buf) {
                return new SkillSyncS2CPacket(buf.readUtf(), buf.readInt(), buf.readInt(), buf.readBoolean());
            }

            public static void handle(@NonNull SkillSyncS2CPacket msg, @NonNull Supplier<NetworkEvent.Context> ctx) {
                ctx.get().enqueueWork(() -> ClientSkillEvents.syncSkillData(
                        msg.classId, msg.cdRemaining, msg.maxCd, msg.isActive));
                ctx.get().setPacketHandled(true);
            }
        }
}