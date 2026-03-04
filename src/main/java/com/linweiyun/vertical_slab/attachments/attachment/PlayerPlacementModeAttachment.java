package com.linweiyun.vertical_slab.attachments.attachment;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;

public class PlayerPlacementModeAttachment implements IAttachmentSerializer<PlayerPlacementModeAttachment> {
    private boolean placementMode = false;

    public PlayerPlacementModeAttachment(boolean vanillaPlacementMode) {
        this.placementMode = vanillaPlacementMode;
    }
    public PlayerPlacementModeAttachment(IAttachmentHolder iAttachmentHolder) {
        this.placementMode = false;
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, PlayerPlacementModeAttachment> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.BOOL,
                    PlayerPlacementModeAttachment::isVanillaPlacementMode,
                    PlayerPlacementModeAttachment::new
            );




    @Override
    public PlayerPlacementModeAttachment read(IAttachmentHolder holder, ValueInput input) {
        PlayerPlacementModeAttachment attachment = new PlayerPlacementModeAttachment(holder);
        attachment.placementMode = input.getBooleanOr("placement_mode", false);
        return attachment;
    }

    @Override
    public boolean write(PlayerPlacementModeAttachment attachment, ValueOutput output) {
        output.putBoolean("placement_mode", attachment.placementMode);
        return true;
    }

    public boolean isVanillaPlacementMode() {
        return placementMode;
    }
}
