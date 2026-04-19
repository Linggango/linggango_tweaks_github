package com.misanthropy.linggango.linggango_tweaks.skills;

import com.misanthropy.linggango.linggango_tweaks.LinggangoTweaks;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

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
        public static SkillUseC2SPacket decode(FriendlyByteBuf buf) { return new SkillUseC2SPacket(); }
        public static void handle(SkillUseC2SPacket msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                ServerPlayer player = ctx.get().getSender();
                if (player != null) {
                    SkillManager.useActiveSkill(player);
                }
            });
            ctx.get().setPacketHandled(true);
        }
    }

    public static class SkillSyncS2CPacket {
        public final String classId;
        public final int cdRemaining;
        public final int maxCd;
        public final boolean isActive;

        public SkillSyncS2CPacket(String classId, int cdRemaining, int maxCd, boolean isActive) {
            this.classId = classId;
            this.cdRemaining = cdRemaining;
            this.maxCd = maxCd;
            this.isActive = isActive;
        }

        public static void encode(SkillSyncS2CPacket msg, FriendlyByteBuf buf) {
            buf.writeUtf(msg.classId);
            buf.writeInt(msg.cdRemaining);
            buf.writeInt(msg.maxCd);
            buf.writeBoolean(msg.isActive);
        }

        public static SkillSyncS2CPacket decode(FriendlyByteBuf buf) {
            return new SkillSyncS2CPacket(buf.readUtf(), buf.readInt(), buf.readInt(), buf.readBoolean());
        }

        public static void handle(SkillSyncS2CPacket msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> com.misanthropy.linggango.linggango_tweaks.skills.client.ClientSkillEvents.syncSkillData(
                    msg.classId, msg.cdRemaining, msg.maxCd, msg.isActive));
            ctx.get().setPacketHandled(true);
        }
    }
}