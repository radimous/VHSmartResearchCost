package com.radimous.smartresearchcost.network.packets;

import com.radimous.smartresearchcost.Smartresearchcost;
import iskallia.vault.research.ResearchTree;
import iskallia.vault.util.PlayerReference;
import iskallia.vault.world.data.PlayerResearchesData;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class GetSharedResearchTreesS2C {

    Map<PlayerReference,ResearchTree> researchTrees;
    public GetSharedResearchTreesS2C(Map<PlayerReference,ResearchTree> researchTrees) {
        this.researchTrees = researchTrees;

    }


    public static void encode(GetSharedResearchTreesS2C msg, FriendlyByteBuf packetBuffer) {
        msg.researchTrees.forEach((playerReference, researchTree) -> {
            packetBuffer.writeNbt(playerReference.serialize());
            packetBuffer.writeNbt(researchTree.serializeNBT());
        });
    }


    public static GetSharedResearchTreesS2C decode(FriendlyByteBuf packetBuffer) {
        Map<PlayerReference, ResearchTree> researchTrees = new HashMap<>();
        while (packetBuffer.isReadable()) {
            var nbt = packetBuffer.readNbt();
            if (nbt == null) {
                continue;
            }
            PlayerReference playerReference = new PlayerReference(nbt);
            ResearchTree researchTree = new ResearchTree(packetBuffer.readNbt());
            researchTrees.put(playerReference, researchTree);
        }
        return new GetSharedResearchTreesS2C(researchTrees);
    }


    public static void handle(GetSharedResearchTreesS2C msg, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> handleResearchTeamTrees(msg, context.getSender()));
        context.setPacketHandled(true);
    }



    private static void handleResearchTeamTrees(GetSharedResearchTreesS2C msg, ServerPlayer sender) {
        Smartresearchcost.teamResearches = msg.researchTrees;
        var pl = Minecraft.getInstance().player;
        if (pl == null) {
            System.out.println("Received research trees - player is null");
            return;
        }
        pl.displayClientMessage(new TextComponent("Received research trees"), false);
    }

}
