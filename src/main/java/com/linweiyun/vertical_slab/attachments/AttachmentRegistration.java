package com.linweiyun.vertical_slab.attachments;

import com.linweiyun.vertical_slab.LinVerticalSlab;
import com.mojang.serialization.Codec;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class AttachmentRegistration {
    // 在 Mod 主类或专用注册类
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, LinVerticalSlab.MOD_ID);

    public static final Supplier<AttachmentType<Boolean>> PLACEMENT_MODE_ATTACHMENT =
            ATTACHMENTS.register("player_boolean", // 唯一名称
                    () -> AttachmentType.builder(() -> false) // 默认值 false
                            // 官方要求：序列化必须用 MapCodec → .fieldOf("xxx")
                            .serialize(Codec.BOOL.fieldOf("enabled"))
                            // 官方要求：同步使用 StreamCodec
                            .sync(StreamCodec.of(
                                    (buf, value) -> buf.writeBoolean(value),
                                    (buf) -> buf.readBoolean()
                            ))
                            // 死亡自动复制数据
                            .copyOnDeath()
                            .build()
            );



    public static void register(IEventBus modEventBus) {
        ATTACHMENTS.register(modEventBus);
    }
}
