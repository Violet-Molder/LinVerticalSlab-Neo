package com.linweiyun.vertical_slab;

import com.linweiyun.vertical_slab.attachments.AttachmentRegistration;
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



@Mod(LinVerticalSlab.MOD_ID)
public class LinVerticalSlab
{

    public static final String MOD_ID = "vertical_slab";
    public static final String GAME_VERSION =  "1.21.9";
    public static final String MOD_VERSION = "21.9.13";
    public static final String RESOURCE_PACK_NAME = "LVS-MOD-Pack" + "-" + GAME_VERSION;
    public static final String VANILLA_PLACE_MODE_NAME = "vanilla_mode";
    private static final Logger LOGGER = LogUtils.getLogger();
    public LinVerticalSlab(IEventBus modEventBus, ModContainer modContainer) throws Exception {
        modEventBus.addListener(this::commonSetup);
        NeoForge.EVENT_BUS.register(this);

        AttachmentRegistration.register(modEventBus);
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
