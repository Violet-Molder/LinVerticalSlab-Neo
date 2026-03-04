package com.linweiyun.vertical_slab.net.packet;

import com.linweiyun.vertical_slab.LinVerticalSlab;
import com.linweiyun.vertical_slab.attachments.AttachmentRegistration;
import com.linweiyun.vertical_slab.attachments.attachment.PlayerPlacementModeAttachment;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PlacementModeSyncPacket(boolean placementMode) implements CustomPacketPayload {
    public static final Type<PlacementModeSyncPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(LinVerticalSlab.MOD_ID, "placement_mode_sync")
    );


    public static final StreamCodec<ByteBuf, PlacementModeSyncPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL,
            PlacementModeSyncPacket::placementMode,
            PlacementModeSyncPacket::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    public static void serverHandle(PlacementModeSyncPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            context.player().setData(AttachmentRegistration.PLACEMENT_MODE_ATTACHMENT, packet.placementMode);
            System.out.println(context.player());
        });
    }
    public static void clientHandle(PlacementModeSyncPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            context.player().setData(AttachmentRegistration.PLACEMENT_MODE_ATTACHMENT, packet.placementMode);
            System.out.println(context);
        });
    }
}
