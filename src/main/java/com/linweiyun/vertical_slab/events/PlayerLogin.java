package com.linweiyun.vertical_slab.events;

import com.linweiyun.vertical_slab.attachments.AttachmentRegistration;
import com.linweiyun.vertical_slab.attachments.attachment.PlayerPlacementModeAttachment;
import com.linweiyun.vertical_slab.network.packet.PlacementModeSyncPacket;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
public class PlayerLogin {
    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!event.getEntity().level().isClientSide){
            PlayerPlacementModeAttachment attachment = event.getEntity().getData(AttachmentRegistration.PLACEMENT_MODE_ATTACHMENT);
            PacketDistributor.sendToPlayer((ServerPlayer) event.getEntity(), new PlacementModeSyncPacket(attachment.isVanillaPlacementMode()));
        }

    }
}
