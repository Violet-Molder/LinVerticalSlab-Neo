
package com.linweiyun.vertical_slab.mixin;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.Mixins;

import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.*;

public class SlabMixinPlugin implements org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin {

    private static final String SLAB_BLOCK = "net.minecraft.world.level.block.SlabBlock";
    private static final String BASE_MIXIN = "com.linweiyun.vertical_slab.mixin.BlockMixin.SlabBlockMixin";
    private static final String GENERATED_PACKAGE = "com.linweiyun.vertical_slab.mixin.generated.";
    private static final String GENERATED_PACKAGE_PATH = "com/linweiyun/vertical_slab/mixin/generated/";

    private final List<String> generatedMixinNames = new ArrayList<>();

    @Override
    public void onLoad(String mixinPackage) {
        MixinBootstrap.init();
        scanAndGenerateSubclassMixins();
        if (!generatedMixinNames.isEmpty()) {
            Mixins.addConfiguration("vertical_slab_generated.mixins.json");
        }
    }

    private void scanAndGenerateSubclassMixins() {
        Set<Class<?>> subclasses = findSlabBlockSubclasses();
        for (Class<?> subclass : subclasses) {
            String className = subclass.getName().replace('.', '/');
            String simpleName = subclass.getSimpleName();
            String generatedClassName = GENERATED_PACKAGE_PATH + simpleName + "Mixin";
            String generatedFullName = GENERATED_PACKAGE + simpleName + "Mixin";

            try {
                byte[] bytecode = generateMixinClass(className, simpleName, generatedClassName);
                registerClass(generatedFullName.replace('.', '/'), bytecode);
                generatedMixinNames.add(simpleName + "Mixin");
                System.out.println("[LVS] Generated mixin for subclass: " + subclass.getName());
            } catch (Exception e) {
                System.err.println("[LVS] Failed to generate mixin for " + subclass.getName() + ": " + e.getMessage());
            }
        }
    }

    private Set<Class<?>> findSlabBlockSubclasses() {
        Set<Class<?>> result = new LinkedHashSet<>();
        try {
            Class<?> slabClass = Class.forName(SLAB_BLOCK);
            ClassLoader cl = slabClass.getClassLoader();

            Enumeration<URL> urls = cl.getResources("");
            while (urls.hasMoreElements()) {
                URL url = urls.nextElement();
                if (url.getProtocol().equals("file")) {
                    java.io.File dir = new java.io.File(url.toURI());
                    scanDirectory(dir, "", slabClass, result, cl);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    private void scanDirectory(java.io.File dir, String packagePrefix, Class<?> slabClass, Set<Class<?>> result, ClassLoader cl) {
        if (!dir.exists()) return;
        java.io.File[] files = dir.listFiles();
        if (files == null) return;

        for (java.io.File file : files) {
            if (file.isDirectory()) {
                scanDirectory(file, packagePrefix + file.getName() + ".", slabClass, result, cl);
            } else if (file.getName().endsWith(".class")) {
                String className = packagePrefix + file.getName().replace(".class", "");
                try {
                    Class<?> clazz = Class.forName(className, false, cl);
                    if (slabClass.isAssignableFrom(clazz) && clazz != slabClass
                            && !Modifier.isAbstract(clazz.getModifiers())
                            && !result.contains(clazz)) {
                        result.add(clazz);
                    }
                } catch (Throwable ignored) {}
            }
        }
    }

    private byte[] generateMixinClass(String targetInternalName, String simpleName, String generatedInternalName) {
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        String baseMixinInternal = BASE_MIXIN.replace('.', '/');

        cw.visit(org.objectweb.asm.Opcodes.V21,
                org.objectweb.asm.Opcodes.ACC_PUBLIC | org.objectweb.asm.Opcodes.ACC_ABSTRACT,
                generatedInternalName, null, baseMixinInternal, null);

        // @Mixin(SubclassSlab.class)
        {
            var av = cw.visitAnnotation("Lorg/spongepowered/asm/mixin/Mixin;", true);
            var visitArray = av.visitArray("value");
            visitArray.visit(null, Type.getType("L" + targetInternalName + ";"));
            visitArray.visitEnd();
            av.visitEnd();
        }

        cw.visitEnd();
        return cw.toByteArray();
    }

    private void registerClass(String name, byte[] bytecode) {
        try {
            var method = ClassLoader.class.getDeclaredMethod("defineClass",
                    String.class, byte[].class, int.class, int.class);
            method.setAccessible(true);
            method.invoke(Thread.currentThread().getContextClassLoader(),
                    name, bytecode, 0, bytecode.length);
        } catch (Exception e) {
            throw new RuntimeException("Failed to register class: " + name, e);
        }
    }

    @Override
    public String getRefMapperConfig() { return null; }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) { return true; }

    @Override
    public void acceptTargets(Set<String> set, Set<String> set1) {

    }

    @Override
    public void preApply(String targetClassName, org.objectweb.asm.tree.ClassNode targetClass,
                         String mixinClassName, org.spongepowered.asm.mixin.extensibility.IMixinInfo mixinInfo) {}

    @Override
    public void postApply(String targetClassName, org.objectweb.asm.tree.ClassNode targetClass,
                          String mixinClassName, org.spongepowered.asm.mixin.extensibility.IMixinInfo mixinInfo) {}

    @Override
    public List<String> getMixins() { return generatedMixinNames; }
}