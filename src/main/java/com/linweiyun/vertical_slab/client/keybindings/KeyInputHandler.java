package com.linweiyun.vertical_slab.client.keybindings;


import com.linweiyun.vertical_slab.attachments.AttachmentRegistration;
import com.linweiyun.vertical_slab.attachments.attachment.PlayerPlacementModeAttachment;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(value = Dist.CLIENT)
public class KeyInputHandler {
    @SubscribeEvent
    public static void onKeyInput(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;

        if (player == null) return;


        // 获取玩家的附件实现类
        PlayerPlacementModeAttachment placementModeAttachment =
                player.getData(AttachmentRegistration.PLACEMENT_MODE_ATTACHMENT.get());
        // 检查是否处于原神模式
        boolean placeMode = placementModeAttachment.isVanillaPlacementMode();

        // G键：切换原神模式
        if (KeyMappingRegistry.G_KEY.consumeClick()) {
            placementModeAttachment.setClientPlacementMode(!placeMode);
            if (!placeMode) {
                player.sendSystemMessage(Component.translatable("message_switch_to_vanilla"));
            } else {
                player.sendSystemMessage(Component.translatable("message_switch_to_vertical"));
            }

        }
    }
}
