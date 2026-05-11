package com.mogger.mod.common.network;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class PacketDuelState {

    public final boolean active;
    public final UUID targetUUID;
    public final int durationTicks;
    public final int playerPower;
    public final int entityPower;

    public PacketDuelState(boolean active, UUID targetUUID, int durationTicks, int playerPower, int entityPower) {
        this.active = active;
        this.targetUUID = targetUUID;
        this.durationTicks = durationTicks;
        this.playerPower = playerPower;
        this.entityPower = entityPower;
    }

    public static PacketDuelState decode(FriendlyByteBuf buf) {
        return new PacketDuelState(
            buf.readBoolean(),
            buf.readUUID(),
            buf.readInt(),
            buf.readInt(),
            buf.readInt()
        );
    }

    public static void encode(PacketDuelState pkt, FriendlyByteBuf buf) {
        buf.writeBoolean(pkt.active);
        buf.writeUUID(pkt.targetUUID);
        buf.writeInt(pkt.durationTicks);
        buf.writeInt(pkt.playerPower);
        buf.writeInt(pkt.entityPower);
    }

    public static void handle(PacketDuelState pkt, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // Client-side: update the duel HUD state
            com.mogger.mod.client.MoggerClientState.setDuelState(
                pkt.active, pkt.targetUUID, pkt.durationTicks, pkt.playerPower, pkt.entityPower
            );
        });
        ctx.get().setPacketHandled(true);
    }
}
