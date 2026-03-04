package com.linweiyun.vertical_slab.client.keybindings;

import com.linweiyun.vertical_slab.LinVerticalSlab;
import com.linweiyun.vertical_slab.attachments.AttachmentRegistration;
import com.linweiyun.vertical_slab.net.packet.PlacementModeSyncPacket;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.client.settings.KeyConflictContext;

@EventBusSubscriber
public class KeyMappingRegistry {
    public static final KeyMapping.Category LVSKEY_CATEGORY = new KeyMapping.Category(ResourceLocation.fromNamespaceAndPath(LinVerticalSlab.MOD_ID, "category"));

    public static final KeyMapping G_KEY =
            new KeyMapping(
                    "key.lvs.place_mode",
                    KeyConflictContext.IN_GAME,
                    InputConstants.Type.KEYSYM,
                    InputConstants.KEY_G,
                    LVSKEY_CATEGORY
            );
    @SubscribeEvent // on the mod event bus only on the physical client
    public static void registerBindings(RegisterKeyMappingsEvent event) {
        event.registerCategory(LVSKEY_CATEGORY);
        event.register(G_KEY);
    }

    @SubscribeEvent
    public static void onKeyInput(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;

        if (player == null) return;


        // 获取玩家的附件实现类
        boolean  placementModeAttachment =
                player.getData(AttachmentRegistration.PLACEMENT_MODE_ATTACHMENT.get());

        // G键：切换原神模式
        if (KeyMappingRegistry.G_KEY.consumeClick()) {
//            player.setData(AttachmentRegistration.PLACEMENT_MODE_ATTACHMENT.get(), !placementModeAttachment);
            ClientPacketDistributor.sendToServer(new PlacementModeSyncPacket(!placementModeAttachment));
            if (!placementModeAttachment) {
                player.displayClientMessage(Component.translatable("message_switch_to_vanilla"), false);
            } else {
                player.displayClientMessage(Component.translatable("message_switch_to_vertical"), false);
            }

        }
    }
}
