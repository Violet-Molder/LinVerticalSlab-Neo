package com.linweiyun.vertical_slab;


import net.minecraft.client.Minecraft;
import net.minecraft.client.main.GameConfig;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.network.PacketDistributor;
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
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;



@Mod(Linweiyun.MOD_ID)
public class Linweiyun
{
    public static final String RESOURCE_PACK_NAME = "LinVerticalSlab-MOD-ResourcePack";
    public static final String MOD_ID = "vertical_slab";
    private static final Logger LOGGER = LogUtils.getLogger();
    public Linweiyun(IEventBus modEventBus, ModContainer modContainer) throws Exception {
        modEventBus.addListener(this::commonSetup);
        NeoForge.EVENT_BUS.register(this);
        modEventBus.addListener(this::addCreative);



    }


    public static void init(Path minecraftPath) {
        try {
            // 直接应用资源包
            LVSGameConfig config = new LVSGameConfig(minecraftPath.resolve("options.txt"));
            config.addResourcePack(RESOURCE_PACK_NAME);
            config.writeToFile();
        } catch (Exception e) {
            // 简单的错误处理
            System.err.println("Failed to add resource pack: " + e.getMessage());
        }
    }
    private static Map<String, Boolean> playerRequireReloadStates = new HashMap<>();
//    @SubscribeEvent
//    @OnlyIn(Dist.CLIENT)
//    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
//        // 获取事件中的玩家对象
//        Player player = event.getEntity();
//        String playerName = player.getName().getString();
//
//        playerRequireReloadStates.putIfAbsent(playerName, true);
//        player.sendSystemMessage(Component.literal(playerRequireReloadStates.toString()));
//
//        // 再判断该玩家在map里是true还是false，如果是true则执行重载
//        if (playerRequireReloadStates.get(playerName)) {
//            ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
//
//            if (Files.exists(lastResourceBlockPath)) {
//                scheduler.schedule(() -> {
//                    if (player instanceof ServerPlayer serverPlayer) {
//                        Minecraft.getInstance().reloadResourcePacks();
//                        playerRequireReloadStates.put(playerName, false);
//                    }
//                }, 2, TimeUnit.SECONDS);
//            } else {
//                scheduler.schedule(() -> {
//                    if (player instanceof ServerPlayer serverPlayer) {
//                        Minecraft.getInstance().reloadResourcePacks();
//                        playerRequireReloadStates.put(playerName, false);
//                    }
//                }, 7, TimeUnit.SECONDS);
//            }
//        }
//
//    }
//    @SubscribeEvent
//    public void onServerStarted(ServerStartedEvent event)
//    {
//        if (requireReloadResourcePack) {
//            // 循环Map所有项，把所有玩家的都改为true
//            for (Map.Entry<String, Boolean> entry : playerRequireReloadStates.entrySet()) {
//                entry.setValue(true);
//            }
//            requireReloadResourcePack = false;
//        }
//    }

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
