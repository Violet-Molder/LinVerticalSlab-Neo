package com.linweiyun.vertical_slab;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import java.nio.file.Path;



@Mod(Linweiyun.MOD_ID)
public class Linweiyun
{

    public static final String MOD_ID = "vertical_slab";
    public static final String GAME_VERSION = "1.21.1";
    public static final String RESOURCE_PACK_NAME = "LVS-MOD-Pack" + "-" + GAME_VERSION;
    private static final Logger LOGGER = LogUtils.getLogger();
    public Linweiyun(IEventBus modEventBus, ModContainer modContainer) throws Exception {
        modEventBus.addListener(this::commonSetup);
        NeoForge.EVENT_BUS.register(this);
        modEventBus.addListener(this::addCreative);



    }

    private void commonSetup(final FMLCommonSetupEvent event)
    {
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event)
    {
    }


    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event)
    {
    }
    @EventBusSubscriber(modid = MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents
    {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event)
        {
            
        }
    }
}
