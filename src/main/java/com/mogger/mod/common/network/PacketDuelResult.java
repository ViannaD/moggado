package com.mogger.mod.common.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketDuelResult {

    public final boolean won;
    public final long xpGained;
    public final int newLevel;

    public PacketDuelResult(boolean won, long xpGained, int newLevel) {
        this.won = won;
        this.xpGained = xpGained;
        this.newLevel = newLevel;
    }

    public static PacketDuelResult decode(FriendlyByteBuf buf) {
        return new PacketDuelResult(buf.readBoolean(), buf.readLong(), buf.readInt());
    }

    public static void encode(PacketDuelResult pkt, FriendlyByteBuf buf) {
        buf.writeBoolean(pkt.won);
        buf.writeLong(pkt.xpGained);
        buf.writeInt(pkt.newLevel);
    }

    public static void handle(PacketDuelResult pkt, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            com.mogger.mod.client.MoggerClientState.onDuelResult(pkt.won, pkt.xpGained, pkt.newLevel);
        });
        ctx.get().setPacketHandled(true);
    }
}
