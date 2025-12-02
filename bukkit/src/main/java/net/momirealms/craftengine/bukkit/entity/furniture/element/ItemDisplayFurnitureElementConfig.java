package net.momirealms.craftengine.bukkit.entity.furniture.element;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.momirealms.craftengine.bukkit.entity.data.ItemDisplayEntityData;
import net.momirealms.craftengine.bukkit.entity.furniture.ItemColorSource;
import net.momirealms.craftengine.bukkit.item.BukkitItemManager;
import net.momirealms.craftengine.core.entity.display.Billboard;
import net.momirealms.craftengine.core.entity.display.ItemDisplayContext;
import net.momirealms.craftengine.core.entity.furniture.Furniture;
import net.momirealms.craftengine.core.entity.furniture.element.FurnitureElementConfig;
import net.momirealms.craftengine.core.entity.furniture.element.FurnitureElementConfigFactory;
import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.item.ItemKeys;
import net.momirealms.craftengine.core.item.data.FireworkExplosion;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;

public class ItemDisplayFurnitureElementConfig implements FurnitureElementConfig<ItemDisplayFurnitureElement> {
    public static final Factory FACTORY = new Factory();
    private final BiFunction<Player, ItemColorSource, List<Object>> lazyMetadataPacket;
    private final BiFunction<Player, ItemColorSource, Item<?>> item;
    private final Vector3f scale;
    private final Vector3f position;
    private final Vector3f translation;
    private final float xRot;
    private final float yRot;
    private final Quaternionf rotation;
    private final ItemDisplayContext displayContext;
    private final Billboard billboard;
    private final float shadowRadius;
    private final float shadowStrength;
    private final boolean applyDyedColor;

    public ItemDisplayFurnitureElementConfig(BiFunction<Player, ItemColorSource, Item<?>> item,
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
                                             boolean applyDyedColor) {
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
        this.item = item;
        this.lazyMetadataPacket = (player, source) -> {
            List<Object> dataValues = new ArrayList<>();
            ItemDisplayEntityData.DisplayedItem.addEntityData(item.apply(player, source).getLiteralObject(), dataValues);
            ItemDisplayEntityData.Scale.addEntityData(this.scale, dataValues);
            ItemDisplayEntityData.RotationLeft.addEntityData(this.rotation, dataValues);
            ItemDisplayEntityData.BillboardConstraints.addEntityData(this.billboard.id(), dataValues);
            ItemDisplayEntityData.Translation.addEntityData(this.translation, dataValues);
            ItemDisplayEntityData.DisplayType.addEntityData(this.displayContext.id(), dataValues);
            ItemDisplayEntityData.ShadowRadius.addEntityData(this.shadowRadius, dataValues);
            ItemDisplayEntityData.ShadowStrength.addEntityData(this.shadowStrength, dataValues);
            return dataValues;
        };
    }

    public Vector3f scale() {
        return scale;
    }

    public Vector3f position() {
        return position;
    }

    public Vector3f translation() {
        return translation;
    }

    public float xRot() {
        return xRot;
    }

    public float yRot() {
        return yRot;
    }

    public Quaternionf rotation() {
        return rotation;
    }

    public ItemDisplayContext displayContext() {
        return displayContext;
    }

    public Billboard billboard() {
        return billboard;
    }

    public float shadowRadius() {
        return shadowRadius;
    }

    public float shadowStrength() {
        return shadowStrength;
    }

    public boolean applyDyedColor() {
        return applyDyedColor;
    }

    @Override
    public ItemDisplayFurnitureElement create(@NotNull Furniture furniture) {
        return new ItemDisplayFurnitureElement(this);
    }

    public static class Factory implements FurnitureElementConfigFactory<ItemDisplayFurnitureElement> {

        @Override
        public ItemDisplayFurnitureElementConfig create(Map<String, Object> arguments) {
            Key itemId = Key.of(ResourceConfigUtils.requireNonEmptyStringOrThrow(arguments.get("item"), "warning.config.furniture.element.item_display.missing_item"));
            boolean applyDyedColor = ResourceConfigUtils.getAsBoolean(arguments.getOrDefault("apply-dyed-color", true), "apply-dyed-color");
            return new ItemDisplayFurnitureElementConfig(
                    (player, colorSource) -> {
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
                    },
                    ResourceConfigUtils.getAsVector3f(arguments.getOrDefault("scale", 1f), "scale"),
                    ResourceConfigUtils.getAsVector3f(arguments.getOrDefault("position", 0.5f), "position"),
                    ResourceConfigUtils.getAsVector3f(arguments.get("translation"), "translation"),
                    ResourceConfigUtils.getAsFloat(arguments.getOrDefault("pitch", 0f), "pitch"),
                    ResourceConfigUtils.getAsFloat(arguments.getOrDefault("yaw", 0f), "yaw"),
                    ResourceConfigUtils.getAsQuaternionf(arguments.getOrDefault("rotation", 0f), "rotation"),
                    ResourceConfigUtils.getAsEnum(ResourceConfigUtils.get(arguments, "display-context", "display-transform"), ItemDisplayContext.class, ItemDisplayContext.NONE),
                    ResourceConfigUtils.getAsEnum(arguments.get("billboard"), Billboard.class, Billboard.FIXED),
                    ResourceConfigUtils.getAsFloat(arguments.getOrDefault("shadow-radius", 0f), "shadow-radius"),
                    ResourceConfigUtils.getAsFloat(arguments.getOrDefault("shadow-strength", 1f), "shadow-strength"),
                    applyDyedColor
            );
        }
    }
}
