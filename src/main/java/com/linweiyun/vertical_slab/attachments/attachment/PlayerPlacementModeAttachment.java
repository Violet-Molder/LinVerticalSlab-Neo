package com.linweiyun.vertical_slab.attachments.attachment;

import com.linweiyun.vertical_slab.attachments.AttachmentRegistration;
import com.linweiyun.vertical_slab.attachments.iattachment.IPlayerPlacementMode;
import com.linweiyun.vertical_slab.network.packet.PlacementModeSyncPacket;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

public class PlayerPlacementModeAttachment implements IPlayerPlacementMode, IAttachmentSerializer<CompoundTag, PlayerPlacementModeAttachment> {
    private boolean placementMode = false;

    @Override
    public boolean isVanillaPlacementMode() {
        return placementMode;
    }

    @Override
    public void setPlacementMode(boolean mode) {
        this.placementMode = mode;
    }

    @Override
    public void setServerPlacementMode(boolean mode, ServerPlayer player) {
        this.placementMode = mode;
        PacketDistributor.sendToPlayer(player, new PlacementModeSyncPacket(mode));
    }

    @Override
    public void setClientPlacementMode(boolean mode) {
        this.placementMode = mode;
        PacketDistributor.sendToServer(new PlacementModeSyncPacket(mode));
    }


    @Override
    public PlayerPlacementModeAttachment read(IAttachmentHolder holder, CompoundTag tag, HolderLookup.Provider provider) {
        PlayerPlacementModeAttachment attachment = holder.getData(AttachmentRegistration.PLACEMENT_MODE_ATTACHMENT);
        if (tag.contains("placement_mode")) {
            attachment.placementMode = tag.getBoolean("placement_mode");
        }
        return attachment;
    }

    @Override
    public @Nullable CompoundTag write(PlayerPlacementModeAttachment attachment, HolderLookup.Provider provider) {
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.putBoolean("placement_mode", attachment.placementMode);
        return compoundTag;
    }

}
