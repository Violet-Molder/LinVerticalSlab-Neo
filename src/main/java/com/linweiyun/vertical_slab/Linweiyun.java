package com.linweiyun.vertical_slab;


import com.linweiyun.vertical_slab.events.ModResourcePack;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
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

import java.nio.file.Files;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.linweiyun.vertical_slab.events.ModResourcePack.lastResourceBlockPath;
import static com.linweiyun.vertical_slab.events.ModResourcePack.requireReloadResourcePack;

@Mod(Linweiyun.MOD_ID)
public class Linweiyun
{
    public static final String MOD_ID = "vertical_slab";
    private static final Logger LOGGER = LogUtils.getLogger();
    public Linweiyun(IEventBus modEventBus, ModContainer modContainer)
    {
        modEventBus.addListener(this::commonSetup);
        NeoForge.EVENT_BUS.register(this);
        modEventBus.addListener(this::addCreative);


    }

    @SubscribeEvent
    public void onServerStarted(ServerStartedEvent event)
    {
        System.out.println("服务器启动了");
        if (requireReloadResourcePack){
            if (lastResourceBlockPath != null){
                if (Files.exists(lastResourceBlockPath)){
                    // 创建定时器
                    ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

// 延迟执行任务
                    scheduler.schedule(() -> {
                        Minecraft.getInstance().reloadResourcePacks();
                        requireReloadResourcePack = false;
                    }, 7, TimeUnit.SECONDS);

                }
            }
        }
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
