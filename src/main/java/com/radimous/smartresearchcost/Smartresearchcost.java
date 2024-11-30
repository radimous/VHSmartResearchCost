package com.radimous.smartresearchcost;

import com.radimous.smartresearchcost.network.SmartResearchCostNetwork;
import iskallia.vault.research.ResearchTree;
import iskallia.vault.util.PlayerReference;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

import java.util.Map;

@Mod("smartresearchcost")
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class Smartresearchcost {

    public static Map<PlayerReference, ResearchTree> teamResearches;

    public Smartresearchcost() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void setupCommon(FMLCommonSetupEvent event) {
        event.enqueueWork(SmartResearchCostNetwork::register);
    }
}
