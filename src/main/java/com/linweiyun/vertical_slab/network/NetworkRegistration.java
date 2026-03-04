package com.linweiyun.vertical_slab.network;

import com.linweiyun.vertical_slab.network.packet.PlacementModeSyncPacket;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.DirectionalPayloadHandler;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
public class NetworkRegistration {

    @SubscribeEvent
    public static void registerPayloadHandlers(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");

        registrar.playBidirectional(
                PlacementModeSyncPacket.TYPE,
                PlacementModeSyncPacket.STREAM_CODEC,
                new DirectionalPayloadHandler<>(
                        PlacementModeSyncPacket::clientHandle,
                        PlacementModeSyncPacket::serverHandle
                )

        );
    }
}
