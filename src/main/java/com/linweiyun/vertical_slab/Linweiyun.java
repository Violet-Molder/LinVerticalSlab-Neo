package com.linweiyun.vertical_slab;


import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Blocks;
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
import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;



@Mod(Linweiyun.MOD_ID)
public class Linweiyun
{
    public static final String RESOURCE_PACK_NAME = "LinVerticalSlab-MOD-ResourcePack";
    public static final String MOD_ID = "vertical_slab";
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
