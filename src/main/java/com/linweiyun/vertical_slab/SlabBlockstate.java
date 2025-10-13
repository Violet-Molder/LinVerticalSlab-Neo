package com.linweiyun.vertical_slab;

import com.google.gson.annotations.SerializedName;

import java.util.HashMap;
import java.util.Map;

public class SlabBlockstate {
    @SerializedName("variants")
    public Map<String, LinVariant> variants = new HashMap<>();
    @SerializedName("is_mod_pack")
    public boolean isModPack = true;

    public SlabBlockstate(String bottomModel, String topModel, String doubleModel) {
        variants.put("shift_mode=true,type=bottom", new LinVariant(bottomModel, null, null));
        variants.put("shift_mode=true,type=top", new LinVariant(topModel, null, null));
        variants.put("shift_mode=false,clicked_face=up,type=top", new LinVariant(bottomModel, null, null));
        variants.put("shift_mode=false,clicked_face=east,type=top", new LinVariant(bottomModel, 90, 90));
        variants.put("shift_mode=false,clicked_face=west,type=top", new LinVariant(bottomModel, 90, 270));
        variants.put("shift_mode=false,clicked_face=south,type=top", new LinVariant(bottomModel, 90, 180));
        variants.put("shift_mode=false,clicked_face=north,type=top", new LinVariant(bottomModel, 90, null));
        variants.put("shift_mode=false,clicked_face=up,type=bottom", new LinVariant(bottomModel, null, null));
        variants.put("shift_mode=false,clicked_face=down,type=bottom", new LinVariant(bottomModel, 90, null));
        variants.put("shift_mode=false,clicked_face=east,type=bottom", new LinVariant(bottomModel, 90, 90));
        variants.put("shift_mode=false,clicked_face=west,type=bottom", new LinVariant(bottomModel, 90, 270));
        variants.put("shift_mode=false,clicked_face=south,type=bottom", new LinVariant(bottomModel, 90, 180));
        variants.put("shift_mode=false,clicked_face=north,type=bottom", new LinVariant(bottomModel, 90, null));
        variants.put("shift_mode=false,type=double", new LinVariant(doubleModel, null, null));
        variants.put("shift_mode=true,type=double", new LinVariant(doubleModel, null, null));

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
