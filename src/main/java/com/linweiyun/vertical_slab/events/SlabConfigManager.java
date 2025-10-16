// SlabConfigManager.java
package com.linweiyun.vertical_slab.events;

import com.google.gson.*;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.loading.FMLPaths;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class SlabConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_DIR = FMLPaths.CONFIGDIR.get().resolve("vertical_slab");
    private static final Path RESOURCE_PACK_PATH = FMLPaths.GAMEDIR.get().resolve("resourcepacks").resolve("vertical_slab");
    private static final Map<ResourceLocation, SlabConfig> slabConfigs = new HashMap<>();

    public static class SlabConfig {
        public boolean hasModels = false;
        public String bottomModel = "";
        public String topModel = "";
        public String doubleModel = "";
    }

    public static void loadConfig() {
        try {
            if (!Files.exists(CONFIG_DIR)) {
                Files.createDirectories(CONFIG_DIR);
                return;
            }

            Files.list(CONFIG_DIR)
                    .filter(path -> path.toString().endsWith(".json"))
                    .forEach(path -> {
                        try {
                            String namespace = path.getFileName().toString().replace(".json", "");
                            String content = Files.readString(path);
                            JsonObject json = GSON.fromJson(content, JsonObject.class);

                            for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
                                ResourceLocation blockId = ResourceLocation.fromNamespaceAndPath(namespace, entry.getKey());
                                JsonObject configJson = entry.getValue().getAsJsonObject();
                                SlabConfig config = new SlabConfig();
                                config.hasModels = configJson.get("hasModels").getAsBoolean();
                                if (configJson.has("bottomModel")) {
                                    config.bottomModel = configJson.get("bottomModel").getAsString();
                                    config.topModel = configJson.get("topModel").getAsString();
                                    config.doubleModel = configJson.get("doubleModel").getAsString();
                                }
                                slabConfigs.put(blockId, config);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void saveConfig() {
        Map<String, JsonObject> namespaceConfigs = new HashMap<>();

        for (Map.Entry<ResourceLocation, SlabConfig> entry : slabConfigs.entrySet()) {
            String namespace = entry.getKey().getNamespace();
            String path = entry.getKey().getPath();
            SlabConfig config = entry.getValue();

            JsonObject configJson = new JsonObject();
            configJson.addProperty("hasModels", config.hasModels);
            configJson.addProperty("bottomModel", config.bottomModel);
            configJson.addProperty("topModel", config.topModel);
            configJson.addProperty("doubleModel", config.doubleModel);

            namespaceConfigs
                    .computeIfAbsent(namespace, k -> new JsonObject())
                    .add(path, configJson);
        }

        for (Map.Entry<String, JsonObject> entry : namespaceConfigs.entrySet()) {
            try {
                Path configFile = CONFIG_DIR.resolve(entry.getKey() + ".json");
                Files.writeString(configFile, GSON.toJson(entry.getValue()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean hasModels(ResourceLocation blockId) {
        SlabConfig config = slabConfigs.get(blockId);
        return config != null && config.hasModels;
    }

    public static Set<ResourceLocation> getAllBlocks() {
        return slabConfigs.keySet();
    }

    public static boolean needsResourceGeneration(ResourceLocation blockId) {
        SlabConfig config = slabConfigs.get(blockId);
        if (config == null || !config.hasModels) {
            return false;
        }

        // 检查资源包中是否已存在该方块的blockstate文件
        Path blockstatePath = RESOURCE_PACK_PATH.resolve("assets")
                .resolve(blockId.getNamespace())
                .resolve("blockstates")
                .resolve(blockId.getPath() + ".json");

        return !Files.exists(blockstatePath);
    }

    public static void setHasModels(ResourceLocation blockId, boolean hasModels,
                                    String bottomModel, String topModel, String doubleModel) {
        SlabConfig config = slabConfigs.computeIfAbsent(blockId, k -> new SlabConfig());
        config.hasModels = hasModels;
        if (hasModels) {
            config.bottomModel = bottomModel;
            config.topModel = topModel;
            config.doubleModel = doubleModel;
        }
        saveConfig();
    }

    public static SlabConfig getConfig(ResourceLocation blockId) {
        return slabConfigs.get(blockId);
    }
}
