package com.radimous.smartresearchcost.mixin;

import com.radimous.smartresearchcost.Smartresearchcost;
import iskallia.vault.research.ResearchTree;
import iskallia.vault.research.type.Research;
import iskallia.vault.util.PlayerReference;
import iskallia.vault.world.data.PlayerResearchesData;
import net.minecraft.client.Minecraft;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

@Mixin(value = ResearchTree.class, remap = false)
public abstract class MixinResearchTree {
    @Shadow @Final protected List<PlayerReference> researchShares;

    @Shadow public static boolean isPenalty;

    @Shadow public abstract float getTeamResearchCostIncreaseMultiplier();


    // Server side logic
    // Do not increase the cost if another players in group have it researched
    // If anything goes wrong, do not lower the cost and return the original value
    // I can't overwrite the getTeamResearchCostIncreaseMultiplier method, because I need access to the research
    @Redirect(method = "getResearchCost", at = @At(value = "INVOKE", target = "Liskallia/vault/research/ResearchTree;getTeamResearchCostIncreaseMultiplier()F"))
    private float doNotIncreaseIfAlreadyResearched(ResearchTree instance, Research research) {
        if (research == null || isPenalty) {
            return instance.getTeamResearchCostIncreaseMultiplier();
        }
        int notUnlocked = 0;
        var server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            return instance.getTeamResearchCostIncreaseMultiplier();
        }
        var ow = server.overworld();
        if (ow == null) {
            return instance.getTeamResearchCostIncreaseMultiplier();
        }
        for (PlayerReference playerReference : this.researchShares) {
            var playerId = playerReference.getId();
            if (playerId == null) {
                return instance.getTeamResearchCostIncreaseMultiplier();
            }
            if (!PlayerResearchesData.get(ow).getResearches(playerId).isResearched(research)) {
                notUnlocked++;
            }
        }
        return notUnlocked * 0.5F;
    }
}
