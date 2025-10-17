package com.linweiyun.vertical_slab;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;



@Mod(Linweiyun.MOD_ID)
public class Linweiyun
{

    public static final String MOD_ID = "vertical_slab";
    public static final String GAME_VERSION =  "1.21.9";
    public static final String MOD_VERSION = "21.9.10";
    public static final String RESOURCE_PACK_NAME = "LVS-MOD-Pack" + "-" + GAME_VERSION;
    private static final Logger LOGGER = LogUtils.getLogger();
    public Linweiyun(IEventBus modEventBus, ModContainer modContainer) throws Exception {
        modEventBus.addListener(this::commonSetup);
        NeoForge.EVENT_BUS.register(this);

    }

    private void addCreative(BuildCreativeModeTabContentsEvent event)
    {
    }

    private void commonSetup(FMLCommonSetupEvent event) {

    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
    }
}
