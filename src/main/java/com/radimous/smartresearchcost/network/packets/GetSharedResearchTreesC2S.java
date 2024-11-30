package com.radimous.smartresearchcost.network.packets;

import com.radimous.smartresearchcost.network.SmartResearchCostNetwork;
import iskallia.vault.research.ResearchTree;
import iskallia.vault.util.PlayerReference;
import iskallia.vault.world.data.PlayerResearchesData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class GetSharedResearchTreesC2S {

    public GetSharedResearchTreesC2S() {
    }


    public static void encode(GetSharedResearchTreesC2S msg, FriendlyByteBuf packetBuffer) {
    }


    public static GetSharedResearchTreesC2S decode(FriendlyByteBuf packetBuffer) {
        return new GetSharedResearchTreesC2S();
    }


    public static void handle(GetSharedResearchTreesC2S msg, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> handleResearchTeamTrees(msg, context.getSender()));
        context.setPacketHandled(true);
    }

    private static void handleResearchTeamTrees(GetSharedResearchTreesC2S msg, ServerPlayer sender) {
        if (sender != null) {
            var svr = sender.getServer();
            if (svr == null) {
                return;
            }
            var ow = svr.overworld();
            if (ow == null) {
                return;
            }
            Map<PlayerReference, ResearchTree> researchTrees = new HashMap<>();
            var researchData = PlayerResearchesData.get(ow);
            var resTree = researchData.getResearches(sender.getUUID());
            var shares = resTree.getResearchShares();
            for (PlayerReference playerReference : shares) {
                var researches = PlayerResearchesData.get(ow).getResearches(playerReference.getId());
                if (researches != null) {
                    researchTrees.put(playerReference, researches);
                }
            }
            SmartResearchCostNetwork.sendToPlayer(new GetSharedResearchTreesS2C(researchTrees), sender);
        }
    }

}
