package com.linweiyun.vertical_slab.net.attachment;

import com.linweiyun.vertical_slab.attachments.attachment.PlayerPlacementModeAttachment;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.attachment.AttachmentSyncHandler;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import org.jetbrains.annotations.Nullable;

public class PlacementModeSyncHandler implements AttachmentSyncHandler<PlayerPlacementModeAttachment> {
    @Override
    public void write(RegistryFriendlyByteBuf buf, PlayerPlacementModeAttachment attachment, boolean initialSync) {
        if (initialSync) {
            // Write entire attachment
            PlayerPlacementModeAttachment.STREAM_CODEC.encode(buf, attachment);
        } else {
            // Write update data
        }
    }

    @Override
    @Nullable
    public PlayerPlacementModeAttachment read(IAttachmentHolder holder, RegistryFriendlyByteBuf buf, @Nullable PlayerPlacementModeAttachment previousValue) {
        if (previousValue == null) {
            // Read entire attachment
            return PlayerPlacementModeAttachment.STREAM_CODEC.decode(buf);
        } else {
            // Read update data and merge to previous value
            return previousValue;
        }
    }

    @Override
    public boolean sendToPlayer(IAttachmentHolder holder, ServerPlayer to) {

        return holder == to;
    }
}
