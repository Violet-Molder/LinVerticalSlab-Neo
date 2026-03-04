package com.linweiyun.vertical_slab;

import com.google.gson.annotations.SerializedName;

import java.util.HashMap;
import java.util.Map;

import static com.linweiyun.vertical_slab.LinVerticalSlab.VANILLA_PLACE_MODE_NAME;

public class SlabBlockstate {
    @SerializedName("variants")
    public Map<String, LinVariant> variants = new HashMap<>();
    @SerializedName("is_mod_pack")
    public boolean isModPack = true;

    public SlabBlockstate(String bottomModel, String topModel, String doubleModel) {
        String modeName = VANILLA_PLACE_MODE_NAME;
        variants.put(modeName + "=true,type=bottom", new LinVariant(bottomModel, null, null));
        variants.put(modeName + "=true,type=top", new LinVariant(topModel, null, null));
        variants.put(modeName + "=false,type=double", new LinVariant(doubleModel, null, null));
        variants.put(modeName + "=true,type=double", new LinVariant(doubleModel, null, null));

        variants.put(modeName + "=false,place_direction=east,type=top", new LinVariant(bottomModel, 90, 270));
        variants.put(modeName + "=false,place_direction=west,type=top", new LinVariant(bottomModel, 90, 90));
        variants.put(modeName + "=false,place_direction=south,type=top", new LinVariant(bottomModel, 90, null));
        variants.put(modeName + "=false,place_direction=north,type=top", new LinVariant(bottomModel, 90, 180));

        variants.put(modeName + "=false,place_direction=east,type=bottom", new LinVariant(bottomModel, 90, 270));
        variants.put(modeName + "=false,place_direction=west,type=bottom", new LinVariant(bottomModel, 90, 90));
        variants.put(modeName + "=false,place_direction=south,type=bottom", new LinVariant(bottomModel, 90, null));
        variants.put(modeName + "=false,place_direction=north,type=bottom", new LinVariant(bottomModel, 90, 180));


    }

    public static class LinVariant {
        public String model;
        public Integer x;
        public Integer y;
        public boolean uvlock;

        public LinVariant(String model, Integer x, Integer y) {
            this.model = model;
            this.x = x;
            this.y = y;
            this.uvlock = true;
        }
    }
}
