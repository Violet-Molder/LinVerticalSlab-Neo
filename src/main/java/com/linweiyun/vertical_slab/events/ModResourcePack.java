package com.linweiyun.vertical_slab.events;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.linweiyun.vertical_slab.LVSGameConfig;
import com.linweiyun.vertical_slab.Linweiyun;
import com.linweiyun.vertical_slab.SlabBlockstate;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackCompatibility;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static com.linweiyun.vertical_slab.Linweiyun.RESOURCE_PACK_NAME;

@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(modid = "vertical_slab", bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ModResourcePack {


    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
    private static final Path resourcePackPath = FMLPaths.GAMEDIR.get().resolve("resourcepacks").resolve(RESOURCE_PACK_NAME);

//    public static Boolean requireReloadResourcePack = false;
//    public static Path lastResourceBlockPath = FMLPaths.GAMEDIR.get().resolve("linweiyun").resolve(RESOURCE_PACK_NAME);
    @SubscribeEvent
    public static void onAddPackFinders(FMLCommonSetupEvent event) throws Exception {
        // 在客户端设置完成后执行资源包生成
        createResourcePack();
        for (Block block : BuiltInRegistries.BLOCK) {
            // 判断方块是否为 SlabBlock
            if (block instanceof SlabBlock) {
                ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(block);
                ResourceLocation blockstateLocation = ResourceLocation.fromNamespaceAndPath(
                        blockId.getNamespace(),
                        "blockstates/" + blockId.getPath() + ".json"
                );
                Path blockstatePath = FMLPaths.GAMEDIR.get().resolve("resourcepacks").resolve(RESOURCE_PACK_NAME).resolve("assets").resolve(blockId.getNamespace()).resolve("blockstates")
                        .resolve(blockId.getPath() + ".json");
                if (!Files.exists(blockstatePath)){
                    loadBlockstateFromResource(blockstateLocation);
//                    requireReloadResourcePack = true;
//                    lastResourceBlockPath = blockstatePath;
                }


            }
        }
//        LVSGameConfig gameConfig = new LVSGameConfig(FMLPaths.GAMEDIR.get().resolve("options.txt"));
//        gameConfig.addResourcePack(RESOURCE_PACK_NAME);
//        gameConfig.writeToFile();
//        registerResourcePack(event);

    }




//    @SubscribeEvent
//    private static void registerResourcePack(AddPackFindersEvent event) throws Exception {

//        if (event.getPackType() != PackType.CLIENT_RESOURCES) {
//            return;
//        }
//        Path packPath = FMLPaths.GAMEDIR.get().resolve("linweiyun").resolve(RESOURCE_PACK_NAME);
//        if (!Files.exists(packPath)) {
//            return;
//        }
//        // 创建 PackLocationInfo
//        PackLocationInfo locationInfo = new PackLocationInfo(
//                "linweiyun:internal_resources", // 包的唯一标识符
//                Component.literal("内置资源包示例"), // 在游戏界面中显示的名称
//                PackSource.DEFAULT, // 包来源，DEFUALT 表示默认来源
//                Optional.empty() // 可选固定版本信息
//        );
//
//        // 创建 ResourcesSupplier
//        Pack.ResourcesSupplier resourcesSupplier = new Pack.ResourcesSupplier() {
//            @Override
//            public PathPackResources openPrimary(PackLocationInfo locationInfo) {
//                // 提供主要的资源包内容
//                return new PathPackResources(locationInfo, packPath);
//            }
//
//            @Override
//            public PathPackResources openFull(PackLocationInfo locationInfo, Pack.Metadata metadata) {
//                // 对于大多数情况，使用与 openPrimary 相同的逻辑
//                return openPrimary(locationInfo);
//            }
//        };
//
//        Pack.Metadata metadata;
//        try {
//            // 尝试从资源包目录读取 pack.mcmeta
//            metadata = Pack.readPackMetadata(locationInfo, resourcesSupplier, 34);
//        } catch (Exception e) {
//            // 如果读取失败（例如元数据不存在），创建一个默认的 Metadata
//            // 请根据你的实际情况调整默认的 Metadata 构造参数
//            // 下面的参数是示例，你需要查看你的游戏版本中 Pack.Metadata 的正确构造方法
//            metadata = new Pack.Metadata(
//                    Component.literal("KFC强制材质包"),
//                    PackCompatibility.COMPATIBLE,
//                    FeatureFlagSet.of(),
//                    List.of(),
//                    false
//            );
//        }
//        // 创建 PackSelectionConfig
//        PackSelectionConfig selectionConfig = new PackSelectionConfig(
//                true,// 标题
//                Pack.Position.TOP, // 位置固定位置
//                true // 来源
//        );
//        Pack pack = new Pack(
//                locationInfo,
//                resourcesSupplier,
//                metadata,
//                selectionConfig  // 指定包在列表中的位置，TOP 会使其出现在顶部
//
//        );
//        event.addRepositorySource((consumer) -> consumer.accept(pack));
//        Minecraft.getInstance().reloadResourcePacks();
//    }
//    @SubscribeEvent
//    public static void onCommonSetup(FMLCommonSetupEvent event) {
//        // 在此事件中调用 loadBlockstateData
//        // 此时所有方块已注册，但 game 尚未进入
//        createResourcePack();
//        for (Block block : BuiltInRegistries.BLOCK) {
//            // 判断方块是否为 SlabBlock
//            if (block instanceof SlabBlock) {
//                ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(block);
//                ResourceLocation blockstateLocation = ResourceLocation.fromNamespaceAndPath(
//                        blockId.getNamespace(),
//                        "blockstates/" + blockId.getPath() + ".json"
//                );
//                loadBlockstateFromResource(blockstateLocation);
//
//            }
//        }
////        packResourcePack();
//    }
    public static void createResourcePack() {

        try {
            if (!Files.exists(resourcePackPath)) {
                // 创建资源包目录
                Files.createDirectories(resourcePackPath);
            }

            // 先判断pack.mc文件是否存在
            Path packMetaPath = resourcePackPath.resolve("pack.mcmeta");
            if (!Files.exists(packMetaPath)) {
                // 写入资源包元数据
                String packMetaContent = "{\n" +
                        "  \"pack\": {\n" +
                        "    \"pack_format\": 34,\n" +
                        "    \"description\": \"竖半砖MOD资源包\"\n" +
                        "  }\n" +
                        "}";

                Files.write(packMetaPath, packMetaContent.getBytes(StandardCharsets.UTF_8));
            }

            // 添加: 复制 pack.png 到资源包根目录
            Path iconPath = resourcePackPath.resolve("pack.png");
            if (!Files.exists(iconPath)) {
                // 从 MOD 资源中读取图标文件
                ResourceManager resourceManager = Minecraft.getInstance().getResourceManager();
                ResourceLocation iconLocation = ResourceLocation.fromNamespaceAndPath(Linweiyun.MOD_ID, "textures/icon/pack.png");

                Optional<Resource> resourceOptional = resourceManager.getResource(iconLocation);
                if (resourceOptional.isPresent()) {
                    try (InputStream inputStream = resourceOptional.get().open()) {
                        Files.copy(inputStream, iconPath);
                    }
                } else {

                }
            }


        } catch (IOException e) {
        }
    }

    private static void loadBlockstateFromResource(ResourceLocation location) {
        // 这里需要获取Minecraft的资源管理器
        // 通常需要在资源重载监听器中执行

        ResourceManager resourceManager = Minecraft.getInstance().getResourceManager();
        if (resourceManager != null) {
            try {
                Optional<Resource> resource = resourceManager.getResource(location);
                if (resource.isPresent()) {
                    try (InputStream stream = resource.get().open();
                         Reader reader = new InputStreamReader(stream)) {
                        JsonObject originalData = GSON.fromJson(reader, JsonObject.class);
                        String bottomModel = "";
                        String topModel = "";
                        String doubleModel = "";
                        JsonObject variants = originalData.getAsJsonObject("variants");
                        if (!isMyCustomBlockstate(originalData)){
                            // 从原始JSON中提取三个模型路径

                            bottomModel = variants.getAsJsonObject("type=bottom").get("model").getAsString();
                            topModel = variants.getAsJsonObject("type=top").get("model").getAsString();
                            doubleModel = variants.getAsJsonObject("type=double").get("model").getAsString();
                        } else {
                            bottomModel = variants.getAsJsonObject("shift_mode=true,type=bottom").get("model").getAsString();
                            topModel = variants.getAsJsonObject("shift_mode=true,type=top").get("model").getAsString();
                            doubleModel = variants.getAsJsonObject("shift_mode=true,type=double").get("model").getAsString();

                        }
                        // 使用提取的模型路径创建SlabBlockstate并序列化
                        SlabBlockstate customBlockstate = new SlabBlockstate(bottomModel, topModel, doubleModel);
                        String newJson = GSON.toJson(customBlockstate);
                        Path blockstatePath = resourcePackPath.resolve("assets").resolve(location.getNamespace())
                                .resolve(location.getPath());
                        if (!Files.exists(blockstatePath)){
                            Files.createDirectories(blockstatePath.getParent());
                        }
                        Files.write(blockstatePath, newJson.getBytes());

                    }
                }
            } catch (IOException e) {
                // 处理异常
            }
        }
    }

    private static boolean isMyCustomBlockstate(JsonObject blockstateData) {
        // 检查是否存在自定义字段
        if (blockstateData.has("is_mod_pack")) {

            return blockstateData.get("is_mod_pack").getAsBoolean();
        }
        return false;
    }








    /*
      打包资源包
        private static void packResourcePack() {
        try {
            // 获取Minecraft的资源包目录
            Path resourcepacksDir = FMLPaths.GAMEDIR.get().resolve("resourcepacks");
            if (!Files.exists(resourcepacksDir)) {
                Files.createDirectories(resourcepacksDir);
            }

            // 定义ZIP文件路径
            Path zipPath = resourcepacksDir.resolve("ModResourcePack.zip");

            // 创建ZIP文件
            try (FileOutputStream fos = new FileOutputStream(zipPath.toFile());
                 ZipOutputStream zos = new ZipOutputStream(fos)) {

                // 遍历资源包文件夹中的所有文件
                Files.walk(resourcePackPath)
                        .filter(path -> !Files.isDirectory(path))
                        .forEach(path -> {
                            try {
                                // 计算在ZIP中的相对路径
                                String relativePath = resourcePackPath.relativize(path).toString().replace("\\", "/");
                                ZipEntry zipEntry = new ZipEntry(relativePath);
                                zos.putNextEntry(zipEntry);
                                Files.copy(path, zos);
                                zos.closeEntry();
                            } catch (IOException e) {
                                System.err.println("Failed to add file to ZIP: " + path + " - " + e.getMessage());
                            }
                        });

                System.out.println("Successfully packed resource pack to: " + zipPath);


            } catch (IOException e) {
                System.err.println("Failed to create ZIP file: " + e.getMessage());
            }

        } catch (IOException e) {
            System.err.println("Failed to pack resource pack: " + e.getMessage());
        }
    }
     */



}
