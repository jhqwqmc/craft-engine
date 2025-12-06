package net.momirealms.craftengine.bukkit.entity.furniture.element;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.momirealms.craftengine.bukkit.entity.data.BaseEntityData;
import net.momirealms.craftengine.bukkit.entity.data.ItemDisplayEntityData;
import net.momirealms.craftengine.bukkit.item.BukkitItemManager;
import net.momirealms.craftengine.core.entity.display.Billboard;
import net.momirealms.craftengine.core.entity.display.ItemDisplayContext;
import net.momirealms.craftengine.core.entity.furniture.Furniture;
import net.momirealms.craftengine.core.entity.furniture.FurnitureColorSource;
import net.momirealms.craftengine.core.entity.furniture.element.FurnitureElementConfig;
import net.momirealms.craftengine.core.entity.furniture.element.FurnitureElementConfigFactory;
import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.item.ItemKeys;
import net.momirealms.craftengine.core.item.data.FireworkExplosion;
import net.momirealms.craftengine.core.util.Color;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;

public class ItemDisplayFurnitureElementConfig implements FurnitureElementConfig<ItemDisplayFurnitureElement> {
    public static final Factory FACTORY = new Factory();
    public final BiFunction<Player, FurnitureColorSource, List<Object>> metadata;
    public final Key itemId;
    public final Vector3f scale;
    public final Vector3f position;
    public final Vector3f translation;
    public final float xRot;
    public final float yRot;
    public final Quaternionf rotation;
    public final ItemDisplayContext displayContext;
    public final Billboard billboard;
    public final float shadowRadius;
    public final float shadowStrength;
    public final boolean applyDyedColor;
    public final Color glowColor;
    public final int blockLight;
    public final int skyLight;
    public final float viewRange;

    public ItemDisplayFurnitureElementConfig(Key itemId,
                                             Vector3f scale,
                                             Vector3f position,
                                             Vector3f translation,
                                             float xRot,
                                             float yRot,
                                             Quaternionf rotation,
                                             ItemDisplayContext displayContext,
                                             Billboard billboard,
                                             float shadowRadius,
                                             float shadowStrength,
                                             boolean applyDyedColor,
                                             @Nullable Color glowColor,
                                             int blockLight,
                                             int skyLight,
                                             float viewRange) {
        this.scale = scale;
        this.position = position;
        this.translation = translation;
        this.xRot = xRot;
        this.yRot = yRot;
        this.rotation = rotation;
        this.displayContext = displayContext;
        this.billboard = billboard;
        this.shadowRadius = shadowRadius;
        this.shadowStrength = shadowStrength;
        this.applyDyedColor = applyDyedColor;
        this.itemId = itemId;
        this.glowColor = glowColor;
        this.blockLight = blockLight;
        this.skyLight = skyLight;
        this.viewRange = viewRange;
        BiFunction<Player, FurnitureColorSource, Item<?>> itemFunction = (player, colorSource) -> {
            Item<ItemStack> wrappedItem = BukkitItemManager.instance().createWrappedItem(itemId, player);
            if (applyDyedColor && colorSource != null && wrappedItem != null) {
                Optional.ofNullable(colorSource.dyedColor()).ifPresent(wrappedItem::dyedColor);
                Optional.ofNullable(colorSource.fireworkColors()).ifPresent(colors -> wrappedItem.fireworkExplosion(new FireworkExplosion(
                        FireworkExplosion.Shape.SMALL_BALL,
                        new IntArrayList(colors),
                        new IntArrayList(),
                        false,
                        false
                )));
            }
            return Optional.ofNullable(wrappedItem).orElseGet(() -> BukkitItemManager.instance().createWrappedItem(ItemKeys.BARRIER, null));
        };
        this.metadata = (player, source) -> {
            List<Object> dataValues = new ArrayList<>();
            if (glowColor != null) {
                ItemDisplayEntityData.SharedFlags.addEntityData((byte) 0x40, dataValues);
                ItemDisplayEntityData.GlowColorOverride.addEntityData(glowColor.color(), dataValues);
            }
            ItemDisplayEntityData.DisplayedItem.addEntityData(itemFunction.apply(player, source).getLiteralObject(), dataValues);
            ItemDisplayEntityData.Scale.addEntityData(this.scale, dataValues);
            ItemDisplayEntityData.RotationLeft.addEntityData(this.rotation, dataValues);
            ItemDisplayEntityData.BillboardConstraints.addEntityData(this.billboard.id(), dataValues);
            ItemDisplayEntityData.Translation.addEntityData(this.translation, dataValues);
            ItemDisplayEntityData.DisplayType.addEntityData(this.displayContext.id(), dataValues);
            ItemDisplayEntityData.ShadowRadius.addEntityData(this.shadowRadius, dataValues);
            ItemDisplayEntityData.ShadowStrength.addEntityData(this.shadowStrength, dataValues);
            if (this.blockLight != -1 && this.skyLight != -1) {
                ItemDisplayEntityData.BrightnessOverride.addEntityData(this.blockLight << 4 | this.skyLight << 20, dataValues);
            }
            ItemDisplayEntityData.ViewRange.addEntityData(this.viewRange, dataValues);
            return dataValues;
        };
    }

    public Vector3f scale() {
        return this.scale;
    }

    public Vector3f position() {
        return this.position;
    }

    public Vector3f translation() {
        return this.translation;
    }

    public float xRot() {
        return this.xRot;
    }

    public float yRot() {
        return this.yRot;
    }

    public Quaternionf rotation() {
        return this.rotation;
    }

    public ItemDisplayContext displayContext() {
        return this.displayContext;
    }

    public Billboard billboard() {
        return this.billboard;
    }

    public float shadowRadius() {
        return this.shadowRadius;
    }

    public float shadowStrength() {
        return this.shadowStrength;
    }

    public boolean applyDyedColor() {
        return this.applyDyedColor;
    }

    public BiFunction<Player, FurnitureColorSource, List<Object>> metadata() {
        return this.metadata;
    }

    public Key itemId() {
        return this.itemId;
    }

    @Nullable
    public Color glowColor() {
        return this.glowColor;
    }

    public int blockLight() {
        return this.blockLight;
    }

    public int skyLight() {
        return this.skyLight;
    }

    public float viewRange() {
        return this.viewRange;
    }

    @Override
    public ItemDisplayFurnitureElement create(@NotNull Furniture furniture) {
        return new ItemDisplayFurnitureElement(furniture, this);
    }

    public static class Factory implements FurnitureElementConfigFactory<ItemDisplayFurnitureElement> {

        @Override
        public ItemDisplayFurnitureElementConfig create(Map<String, Object> arguments) {
            Key itemId = Key.of(ResourceConfigUtils.requireNonEmptyStringOrThrow(arguments.get("item"), "warning.config.furniture.element.item_display.missing_item"));
            boolean applyDyedColor = ResourceConfigUtils.getAsBoolean(arguments.getOrDefault("apply-dyed-color", true), "apply-dyed-color");
            Map<String, Object> brightness = ResourceConfigUtils.getAsMap(arguments.getOrDefault("brightness", Map.of()), "brightness");
            return new ItemDisplayFurnitureElementConfig(
                    itemId,
                    ResourceConfigUtils.getAsVector3f(arguments.getOrDefault("scale", 1f), "scale"),
                    ResourceConfigUtils.getAsVector3f(arguments.getOrDefault("position", 0f), "position"),
                    ResourceConfigUtils.getAsVector3f(arguments.get("translation"), "translation"),
                    ResourceConfigUtils.getAsFloat(arguments.getOrDefault("pitch", 0f), "pitch"),
                    ResourceConfigUtils.getAsFloat(arguments.getOrDefault("yaw", 0f), "yaw"),
                    ResourceConfigUtils.getAsQuaternionf(arguments.getOrDefault("rotation", 0f), "rotation"),
                    ResourceConfigUtils.getAsEnum(ResourceConfigUtils.get(arguments, "display-context", "display-transform"), ItemDisplayContext.class, ItemDisplayContext.NONE),
                    ResourceConfigUtils.getAsEnum(arguments.get("billboard"), Billboard.class, Billboard.FIXED),
                    ResourceConfigUtils.getAsFloat(arguments.getOrDefault("shadow-radius", 0f), "shadow-radius"),
                    ResourceConfigUtils.getAsFloat(arguments.getOrDefault("shadow-strength", 1f), "shadow-strength"),
                    applyDyedColor,
                    Optional.ofNullable(arguments.get("glow-color")).map(it -> Color.fromStrings(it.toString().split(","))).orElse(null),
                    ResourceConfigUtils.getAsInt(brightness.getOrDefault("block-light", -1), "block-light"),
                    ResourceConfigUtils.getAsInt(brightness.getOrDefault("sky-light", -1), "sky-light"),
                    ResourceConfigUtils.getAsFloat(arguments.getOrDefault("view-range", 1f), "view-range")
            );
        }
    }

    @Override
    public String toString() {
        return "ItemDisplayFurnitureElementConfig{" +
                "metadata=" + metadata +
                ", itemId=" + itemId +
                ", scale=" + scale +
                ", position=" + position +
                ", translation=" + translation +
                ", xRot=" + xRot +
                ", yRot=" + yRot +
                ", rotation=" + rotation +
                ", displayContext=" + displayContext +
                ", billboard=" + billboard +
                ", shadowRadius=" + shadowRadius +
                ", shadowStrength=" + shadowStrength +
                ", applyDyedColor=" + applyDyedColor +
                '}';
    }
}
