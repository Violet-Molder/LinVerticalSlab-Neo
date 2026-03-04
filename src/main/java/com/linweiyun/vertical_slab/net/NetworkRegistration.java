package com.linweiyun.vertical_slab.net;

import com.linweiyun.vertical_slab.net.packet.PlacementModeSyncPacket;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber
public class NetworkRegistration {

    @SubscribeEvent
    public static void registerPayloadHandlers(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");

        registrar.playBidirectional(
                PlacementModeSyncPacket.TYPE,
                PlacementModeSyncPacket.STREAM_CODEC,
                PlacementModeSyncPacket::serverHandle,
                PlacementModeSyncPacket::clientHandle
        );

    }
}