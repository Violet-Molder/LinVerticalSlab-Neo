// ResourcePackGenerator.java
package com.linweiyun.vertical_slab.events;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.linweiyun.vertical_slab.Linweiyun;
import com.linweiyun.vertical_slab.SlabBlockstate;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.neoforged.fml.loading.FMLPaths;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
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
            boolean needRegenerate = false;
            if (Files.exists(packMetaPath)) {
                try (Reader reader = Files.newBufferedReader(packMetaPath)) {
                    JsonObject metaData = JsonParser.parseReader(reader).getAsJsonObject();
                    if (metaData.has("version")) {
                        String existingVersion = metaData.get("version").getAsString();
                        if (!Linweiyun.MOD_VERSION.equals(existingVersion)) {
                            needRegenerate = true;
                        }
                    } else {
                        needRegenerate = true; // 没有版本信息也需要重新生成
                    }
                } catch (Exception e) {
                    needRegenerate = true; // 解析失败需要重新生成
                }
            } else {
                needRegenerate = true; // 文件不存在需要生成
            }

            // 如果需要重新生成，先删除旧的资源包内容
            if (needRegenerate) {
                deleteResourcePackContents();
                Files.createDirectories(resourcePackPath);
            }

            // 生成 pack.mcmeta 文件（无论是否需要重新生成都会执行）
            if (needRegenerate || !Files.exists(packMetaPath)) {
                String packMetaContent = """
                {
                  "pack": {
                    "pack_format": 34,
                    "description": "竖半砖MOD资源包"
                  },
                  "version": "%s"
                }""".formatted(Linweiyun.MOD_VERSION);
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

    /**
     * 删除资源包内容但保留目录结构
     */
    private static void deleteResourcePackContents() {
        try {
            // 删除资源包中的所有文件和文件夹，但保留根目录
            if (Files.exists(resourcePackPath)) {
                Files.walk(resourcePackPath)
                        .sorted(java.util.Comparator.reverseOrder())
                        .filter(path -> !path.equals(resourcePackPath)) // 不删除根目录
                        .forEach(path -> {
                            try {
                                Files.deleteIfExists(path);
                            } catch (IOException e) {
                                System.err.println("无法删除文件: " + path);
                            }
                        });
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
