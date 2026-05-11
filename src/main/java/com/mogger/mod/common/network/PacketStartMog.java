package com.mogger.mod.common.network;

import com.mogger.mod.MoggerMod;
import com.mogger.mod.common.MogDuelManager;
import com.mogger.mod.common.capability.IMoggerData;
import com.mogger.mod.common.capability.MoggerCapability;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class PacketStartMog {

    private final UUID targetUUID;

    public PacketStartMog(UUID targetUUID) {
        this.targetUUID = targetUUID;
    }

    public static PacketStartMog decode(FriendlyByteBuf buf) {
        return new PacketStartMog(buf.readUUID());
    }

    public static void encode(PacketStartMog pkt, FriendlyByteBuf buf) {
        buf.writeUUID(pkt.targetUUID);
    }

    public static void handle(PacketStartMog pkt, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            // Already in a duel?
            if (MogDuelManager.isInDuel(player.getUUID())) return;

            // Find target entity
            ServerLevel level = player.serverLevel();
            Entity entity = null;
            for (Entity e : level.getAllEntities()) {
                if (e.getUUID().equals(pkt.targetUUID)) { entity = e; break; }
            }
            if (!(entity instanceof LivingEntity target)) return;

            // Distance check (max 8 blocks)
            if (player.distanceTo(target) > 8.0) return;

            // Level check
            MoggerCapability.get(player).ifPresent(data -> {
                int playerPower = data.getMogLevel() * 2; // player power scales with level
                int entityPower = IMoggerData.calculateEntityMogPower(target.getMaxHealth());

                // Can only start if player level >= entity power * 0.5 (some chance even if weaker)
                MogDuelManager.startDuel(player, target);

                // Freeze both
                target.setNoAi(true);

                // Notify client to enter duel state
                MoggerNetwork.sendToPlayer(player,
                    new PacketDuelState(true, pkt.targetUUID, MogDuelManager.DUEL_DURATION_TICKS, playerPower, entityPower));

                MoggerMod.LOGGER.info("[Mogger] Duel started: {} vs {} (player power: {}, entity power: {})",
                    player.getName().getString(), target.getName().getString(), playerPower, entityPower);
            });
        });
        ctx.get().setPacketHandled(true);
    }
}
