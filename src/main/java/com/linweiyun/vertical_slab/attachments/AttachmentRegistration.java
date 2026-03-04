package com.linweiyun.vertical_slab.attachments;

import com.linweiyun.vertical_slab.LinVerticalSlab;
import com.linweiyun.vertical_slab.attachments.attachment.PlayerPlacementModeAttachment;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;


public class AttachmentRegistration {

    // 在 Mod 主类或专用注册类
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, LinVerticalSlab.MOD_ID);

    public static final Supplier<AttachmentType<PlayerPlacementModeAttachment>> PLACEMENT_MODE_ATTACHMENT =
            ATTACHMENTS.register("placement_genshin_mode",
                    () -> AttachmentType.builder(PlayerPlacementModeAttachment::new).build());


    public static void register(IEventBus modEventBus) {
        ATTACHMENTS.register(modEventBus);
    }
}
