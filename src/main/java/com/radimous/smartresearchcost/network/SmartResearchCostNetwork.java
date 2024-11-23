package com.radimous.smartresearchcost.network;

import com.radimous.smartresearchcost.network.packets.GetSharedResearchTreesC2S;
import com.radimous.smartresearchcost.network.packets.GetSharedResearchTreesS2C;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class SmartResearchCostNetwork {

    /* TODO: debug
    * [Render thread/ERROR] [minecraft/BlockableEventLoop]: Error executing task on Client
    * java.lang.IndexOutOfBoundsException: null
    */
    //TODO: check how it behaves when only on single side

    private static final String PROTOCOL_VERSION = "1";

    private static SimpleChannel CHANNEL;

    private static int packetId = 0;

    private static int id() {
        return packetId++;
    }


    public static void register() {
        CHANNEL = NetworkRegistry.ChannelBuilder.
            named(new ResourceLocation("smartresearchcost", "messages")).
            networkProtocolVersion(() -> PROTOCOL_VERSION).
            clientAcceptedVersions(PROTOCOL_VERSION::equals).
            serverAcceptedVersions(version -> true).
            simpleChannel();

        CHANNEL.messageBuilder(GetSharedResearchTreesC2S.class, id(), NetworkDirection.PLAY_TO_SERVER).
            decoder(GetSharedResearchTreesC2S::decode).
            encoder(GetSharedResearchTreesC2S::encode).
            consumer(GetSharedResearchTreesC2S::handle).
            add();

        CHANNEL.messageBuilder(GetSharedResearchTreesS2C.class, id(), NetworkDirection.PLAY_TO_CLIENT).
            decoder(GetSharedResearchTreesS2C::decode).
            encoder(GetSharedResearchTreesS2C::encode).
            consumer(GetSharedResearchTreesS2C::handle).
            add();
    }

     public static <T> void sendToServer(T message) {
        CHANNEL.sendToServer(message);
    }

    public static <T> void sendToPlayer(T message, ServerPlayer player) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), message);
    }
}
