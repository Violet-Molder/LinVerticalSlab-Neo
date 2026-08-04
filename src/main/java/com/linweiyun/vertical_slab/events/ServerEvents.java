package com.linweiyun.vertical_slab.events;

import com.linweiyun.vertical_slab.attachments.AttachmentRegistration;
import com.linweiyun.vertical_slab.net.packet.PlacementModeSyncPacket;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.network.PacketDistributor;

public class ServerEvents {
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
    }

    @SubscribeEvent
    public void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        boolean mode = event.getEntity().getData(AttachmentRegistration.PLACEMENT_MODE_ATTACHMENT);
        PacketDistributor.sendToPlayer((ServerPlayer) event.getEntity(), new PlacementModeSyncPacket(mode));
    }
}
