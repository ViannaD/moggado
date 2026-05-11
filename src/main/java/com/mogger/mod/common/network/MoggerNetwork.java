package com.mogger.mod.common.network;

import com.mogger.mod.MoggerMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class MoggerNetwork {

    private static final String PROTOCOL_VERSION = "1";
    private static SimpleChannel INSTANCE;
    private static int id = 0;

    public static void register() {
        INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(MoggerMod.MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
        );

        INSTANCE.messageBuilder(PacketStartMog.class, id++, NetworkDirection.PLAY_TO_SERVER)
            .encoder(PacketStartMog::encode)
            .decoder(PacketStartMog::decode)
            .consumerMainThread(PacketStartMog::handle)
            .add();

        INSTANCE.messageBuilder(PacketDuelState.class, id++, NetworkDirection.PLAY_TO_CLIENT)
            .encoder(PacketDuelState::encode)
            .decoder(PacketDuelState::decode)
            .consumerMainThread(PacketDuelState::handle)
            .add();

        INSTANCE.messageBuilder(PacketDuelResult.class, id++, NetworkDirection.PLAY_TO_CLIENT)
            .encoder(PacketDuelResult::encode)
            .decoder(PacketDuelResult::decode)
            .consumerMainThread(PacketDuelResult::handle)
            .add();

        INSTANCE.messageBuilder(PacketSyncMogData.class, id++, NetworkDirection.PLAY_TO_CLIENT)
            .encoder(PacketSyncMogData::encode)
            .decoder(PacketSyncMogData::decode)
            .consumerMainThread(PacketSyncMogData::handle)
            .add();
    }

    public static <MSG> void sendToServer(MSG message) {
        INSTANCE.sendToServer(message);
    }

    public static <MSG> void sendToPlayer(ServerPlayer player, MSG message) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), message);
    }
}
