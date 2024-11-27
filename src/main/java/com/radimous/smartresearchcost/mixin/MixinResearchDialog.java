package com.radimous.smartresearchcost.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.radimous.smartresearchcost.Smartresearchcost;
import com.radimous.smartresearchcost.network.SmartResearchCostNetwork;
import com.radimous.smartresearchcost.network.packets.GetSharedResearchTreesC2S;
import iskallia.vault.client.gui.screen.player.legacy.tab.split.dialog.ResearchDialog;
import iskallia.vault.init.ModConfigs;
import iskallia.vault.research.ResearchTree;
import iskallia.vault.research.type.Research;
import iskallia.vault.util.PlayerReference;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@Mixin(value = ResearchDialog.class, remap = false)
public class MixinResearchDialog {
    @Shadow private String researchName;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void initResearchDialog(CallbackInfo ci) {
        SmartResearchCostNetwork.sendToServer(new GetSharedResearchTreesC2S());
    }

    @Redirect(method = "update", at = @At(value = "INVOKE", target = "Liskallia/vault/research/ResearchTree;getResearchCost(Liskallia/vault/research/type/Research;)I"))
    private int changeResearchCost(ResearchTree instance, Research research) {
        if (Smartresearchcost.teamResearches == null
            // server swapped or it is outdated
            || !Smartresearchcost.teamResearches.keySet().equals(new HashSet<PlayerReference>(instance.getResearchShares()))) {
            return instance.getResearchCost(research);
        }
        var treeCopy = new ResearchTree(instance.serializeNBT());
        treeCopy.resetShares();
        for (var player : Smartresearchcost.teamResearches.keySet()) {
            var resTree = Smartresearchcost.teamResearches.get(player);
            if (!resTree.isResearched(research)) {
                treeCopy.addShare(player);
            }
        }
        return treeCopy.getResearchCost(research);
    }

    @Redirect(method = "update", at = @At(value = "INVOKE", target = "Liskallia/vault/research/ResearchTree;getTeamResearchCostIncreaseMultiplier()F"))
    private float changeMultiplierInBrackets(ResearchTree instance) {
        Research research = ModConfigs.RESEARCHES.getByName(this.researchName);
        if (research == null || ResearchTree.isPenalty || Smartresearchcost.teamResearches == null
            // server swapped or it is outdated
            || !Smartresearchcost.teamResearches.keySet().equals(new HashSet<PlayerReference>(instance.getResearchShares()))){
            return instance.getTeamResearchCostIncreaseMultiplier();
        }
        int notUnlocked = 0;

        for (ResearchTree researchTree : Smartresearchcost.teamResearches.values()) {
            if (!researchTree.isResearched(research)) {
                notUnlocked++;
            }
        }
        return notUnlocked * 0.5F;
    }

    @Inject(method = "lambda$update$2", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;disableDepthTest()V", remap = true), locals = LocalCapture.CAPTURE_FAILSOFT)
    public void showResearchedInPlayerList(Button btn, PoseStack poseStack, int mouseX, int mouseY, CallbackInfo ci, List<Component> shareList) {
        if (Smartresearchcost.teamResearches != null) {
            shareList.clear();
            shareList.add(new TextComponent("Sharing new researches with:"));
            for (var player : Smartresearchcost.teamResearches.keySet()) {
                var resTree = Smartresearchcost.teamResearches.get(player);
                var researched = resTree.isResearched(ModConfigs.RESEARCHES.getByName(this.researchName));
                var component = new TextComponent(player.getName() + (researched ? " (Researched)" : ""));
                if (researched) {
                    component.withStyle(ChatFormatting.GRAY);
                }
                shareList.add(component);
            }
        }
    }
}
