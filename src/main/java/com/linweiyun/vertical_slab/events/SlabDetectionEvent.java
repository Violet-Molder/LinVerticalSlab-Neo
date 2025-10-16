// SlabDetectionEvent.java
package com.linweiyun.vertical_slab.events;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Optional;

@EventBusSubscriber(modid = "vertical_slab", value = Dist.CLIENT)
public class SlabDetectionEvent {
    private static final Gson GSON = new GsonBuilder().create();
    private static boolean detectionCompleted = false;

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            if (!detectionCompleted) {
                SlabConfigManager.loadConfig();
                detectAllSlabBlocks();
                detectionCompleted = true;
            }
        });
        // 检测完成后生成资源
        ResourcePackGenerator.generateAllResources();
    }

    private static void detectAllSlabBlocks() {
        System.out.println("=== Starting slab detection ===");
        ResourceManager resourceManager = Minecraft.getInstance().getResourceManager();

        int detectedCount = 0;
        int skippedCount = 0;

        for (Block block : BuiltInRegistries.BLOCK) {
            if (block instanceof SlabBlock) {
                ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(block);
                System.out.println("Processing slab: " + blockId);

                // 如果配置中已有记录，跳过检测
                if (SlabConfigManager.hasModels(blockId)) {
                    System.out.println("  Already in config, skipping");
                    skippedCount++;
                    continue;
                }

                ResourceLocation blockstateLocation = ResourceLocation.fromNamespaceAndPath(
                        blockId.getNamespace(), "blockstates/" + blockId.getPath() + ".json"
                );

                try {
                    Optional<Resource> resource = resourceManager.getResource(blockstateLocation);
                    if (resource.isPresent()) {
                        try (InputStream stream = resource.get().open();
                             Reader reader = new InputStreamReader(stream)) {

                            JsonObject originalData = GSON.fromJson(reader, JsonObject.class);

                            // 跳过已经标记为自定义的blockstate
                            if (isCustomBlockstate(originalData)) {
                                System.out.println("  Custom blockstate, skipping");
                                SlabConfigManager.setHasModels(blockId, false, "", "", "");
                                continue;
                            }

                            JsonObject variants = originalData.getAsJsonObject("variants");

                            if (variants != null) {
                                JsonObject bottomVariant = variants.getAsJsonObject("type=bottom");
                                JsonObject topVariant = variants.getAsJsonObject("type=top");
                                JsonObject doubleVariant = variants.getAsJsonObject("type=double");

                                if (bottomVariant != null && bottomVariant.has("model") &&
                                        topVariant != null && topVariant.has("model") &&
                                        doubleVariant != null && doubleVariant.has("model")) {

                                    String bottomModel = bottomVariant.get("model").getAsString();
                                    String topModel = topVariant.get("model").getAsString();
                                    String doubleModel = doubleVariant.get("model").getAsString();

                                    System.out.println("  Found valid models:");
                                    System.out.println("    Bottom: " + bottomModel);
                                    System.out.println("    Top: " + topModel);
                                    System.out.println("    Double: " + doubleModel);

                                    SlabConfigManager.setHasModels(blockId, true, bottomModel, topModel, doubleModel);
                                    detectedCount++;
                                    System.out.println("  ✓ Added to config: " + blockId);
                                } else {
                                    System.out.println("  Missing required variants, skipping");
                                    SlabConfigManager.setHasModels(blockId, false, "", "", "");
                                }
                            } else {
                                System.out.println("  No variants found, skipping");
                                SlabConfigManager.setHasModels(blockId, false, "", "", "");
                            }
                        }
                    } else {
                        System.out.println("  No blockstate resource found");
                        SlabConfigManager.setHasModels(blockId, false, "", "", "");
                    }
                } catch (Exception e) {
                    System.out.println("  Error processing blockstate: " + e.getMessage());
                    SlabConfigManager.setHasModels(blockId, false, "", "", "");
                }
            }
        }

        System.out.println("=== Slab detection completed ===");
        System.out.println("Detected " + detectedCount + " new slabs with models");
        System.out.println("Skipped " + skippedCount + " already configured slabs");

        // 检测完成后生成资源
        System.out.println("=== Calling resource generation ===");
        ResourcePackGenerator.generateAllResources();
    }

    private static boolean isCustomBlockstate(JsonObject blockstateData) {
        return blockstateData.has("is_mod_pack") &&
                blockstateData.get("is_mod_pack").getAsBoolean();
    }

    public static boolean hasModels(ResourceLocation blockId) {
        return SlabConfigManager.hasModels(blockId);
    }
}