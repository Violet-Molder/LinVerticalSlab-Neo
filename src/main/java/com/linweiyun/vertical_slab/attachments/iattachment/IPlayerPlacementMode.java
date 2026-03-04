package com.linweiyun.vertical_slab.attachments.iattachment;

import net.minecraft.server.level.ServerPlayer;

public interface IPlayerPlacementMode {
    boolean isVanillaPlacementMode();
    void setPlacementMode(boolean mode);
    void setServerPlacementMode(boolean mode, ServerPlayer player);
    void setClientPlacementMode(boolean mode);
}
