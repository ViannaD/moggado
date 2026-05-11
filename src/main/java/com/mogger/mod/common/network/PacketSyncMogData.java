package com.mogger.mod.common.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketSyncMogData {

    public final int mogLevel;
    public final long mogXP;

    public PacketSyncMogData(int mogLevel, long mogXP) {
        this.mogLevel = mogLevel;
        this.mogXP = mogXP;
    }

    public static PacketSyncMogData decode(FriendlyByteBuf buf) {
        return new PacketSyncMogData(buf.readInt(), buf.readLong());
    }

    public static void encode(PacketSyncMogData pkt, FriendlyByteBuf buf) {
        buf.writeInt(pkt.mogLevel);
        buf.writeLong(pkt.mogXP);
    }

    public static void handle(PacketSyncMogData pkt, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            com.mogger.mod.client.MoggerClientState.syncData(pkt.mogLevel, pkt.mogXP);
        });
        ctx.get().setPacketHandled(true);
    }
}
