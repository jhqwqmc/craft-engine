package net.momirealms.craftengine.core.pack.model;

import com.google.gson.JsonObject;
import net.momirealms.craftengine.core.pack.revision.Revision;
import net.momirealms.craftengine.core.util.GsonHelper;
import net.momirealms.craftengine.core.util.MinecraftVersion;
import net.momirealms.craftengine.core.util.MinecraftVersions;

import java.util.List;

public class ModernItemModel {
    private final ItemModel itemModel;
    private final boolean oversizedInGui;
    private final boolean handAnimationOnSwap;
    private final float swapAnimationScale;

    public ModernItemModel(ItemModel itemModel, boolean handAnimationOnSwap, boolean oversizedInGui, float swapAnimationScale) {
        this.handAnimationOnSwap = handAnimationOnSwap;
        this.itemModel = itemModel;
        this.oversizedInGui = oversizedInGui;
        this.swapAnimationScale = swapAnimationScale;
    }

    public static ModernItemModel fromJson(JsonObject json) {
        ItemModel model = ItemModels.fromJson(json.getAsJsonObject("model"));
        return new ModernItemModel(
                model,
                GsonHelper.getAsBoolean(json.get("hand_animation_on_swap"), true),
                GsonHelper.getAsBoolean(json.get("oversized_in_gui"), false),
                GsonHelper.getAsFloat(json.get("swap_animation_scale"), 1.0f)
        );
    }

    public JsonObject toJson(MinecraftVersion version) {
        JsonObject json = new JsonObject();
        if (this.oversizedInGui && version.isAtOrAbove(MinecraftVersions.V1_21_6)) {
            json.addProperty("oversized_in_gui", true);
        }
        if (!this.handAnimationOnSwap) {
            json.addProperty("hand_animation_on_swap", false);
        }
        if (this.swapAnimationScale != 1.0f && version.isAtOrAbove(MinecraftVersions.V1_21_11)) {
            json.addProperty("swap_animation_scale", this.swapAnimationScale);
        }
        json.add("model", this.itemModel.apply(version));
        return json;
    }

    public List<Revision> revisions() {
        return this.itemModel.revisions().stream().distinct().toList();
    }

    public boolean handAnimationOnSwap() {
        return handAnimationOnSwap;
    }

    public ItemModel itemModel() {
        return itemModel;
    }

    public boolean oversizedInGui() {
        return oversizedInGui;
    }

    public float swapAnimationScale() {
        return swapAnimationScale;
    }
}
