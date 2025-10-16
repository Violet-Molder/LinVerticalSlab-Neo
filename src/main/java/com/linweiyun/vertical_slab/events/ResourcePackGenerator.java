// ResourcePackGenerator.java
package com.linweiyun.vertical_slab.events;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.linweiyun.vertical_slab.Linweiyun;
import com.linweiyun.vertical_slab.SlabBlockstate;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.neoforged.fml.loading.FMLPaths;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;

import static com.linweiyun.vertical_slab.Linweiyun.RESOURCE_PACK_NAME;

public class ResourcePackGenerator {
    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
    private static final Path resourcePackPath = FMLPaths.GAMEDIR.get().resolve("resourcepacks").resolve(RESOURCE_PACK_NAME);

    public static void createResourcePack() {
        try {
            if (!Files.exists(resourcePackPath)) {
                Files.createDirectories(resourcePackPath);
            }

            // 创建 pack.mcmeta
            Path packMetaPath = resourcePackPath.resolve("pack.mcmeta");
            if (!Files.exists(packMetaPath)) {
                String packMetaContent = """
                {
                  "pack": {
                    "min_format": 65,
                    "max_format": 69,
                    "description": "竖半砖MOD资源包"
                  }
                }""";
                Files.writeString(packMetaPath, packMetaContent);
            }
            Path iconPath = resourcePackPath.resolve("pack.png");
            if (!Files.exists(iconPath)) {
                ResourceManager resourceManager = Minecraft.getInstance().getResourceManager();
                ResourceLocation iconLocation = ResourceLocation.fromNamespaceAndPath(Linweiyun.MOD_ID, "textures/icon/pack.png");

                Optional<Resource> resourceOptional = resourceManager.getResource(iconLocation);
                if (resourceOptional.isPresent()) {
                    try (InputStream inputStream = resourceOptional.get().open()) {
                        Files.copy(inputStream, iconPath);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void generateAllResources() {
        createResourcePack();

        Set<ResourceLocation> allBlocks = SlabConfigManager.getAllBlocks();

        int generatedCount = 0;
        for (ResourceLocation blockId : allBlocks) {
            // 现在通过检查文件是否存在来判断是否需要生成资源
            if (SlabConfigManager.needsResourceGeneration(blockId)) {
                generateBlockstateFile(blockId);
                generatedCount++;
            }
        }
    }

    private static void generateBlockstateFile(ResourceLocation blockId) {
        try {
            SlabConfigManager.SlabConfig config = SlabConfigManager.getConfig(blockId);
            if (config == null) return;

            SlabBlockstate customBlockstate = new SlabBlockstate(
                    config.bottomModel, config.topModel, config.doubleModel
            );

            String newJson = GSON.toJson(customBlockstate);
            Path blockstatePath = resourcePackPath.resolve("assets")
                    .resolve(blockId.getNamespace())
                    .resolve("blockstates")
                    .resolve(blockId.getPath() + ".json");

            Files.createDirectories(blockstatePath.getParent());
            Files.writeString(blockstatePath, newJson);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
