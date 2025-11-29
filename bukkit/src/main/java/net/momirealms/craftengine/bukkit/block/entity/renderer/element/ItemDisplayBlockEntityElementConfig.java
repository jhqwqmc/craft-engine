package net.momirealms.craftengine.bukkit.block.entity.renderer.element;

import com.google.common.base.Objects;
import net.momirealms.craftengine.bukkit.entity.data.ItemDisplayEntityData;
import net.momirealms.craftengine.bukkit.item.BukkitItemManager;
import net.momirealms.craftengine.core.block.entity.render.element.BlockEntityElement;
import net.momirealms.craftengine.core.block.entity.render.element.BlockEntityElementConfig;
import net.momirealms.craftengine.core.block.entity.render.element.BlockEntityElementConfigFactory;
import net.momirealms.craftengine.core.entity.Billboard;
import net.momirealms.craftengine.core.entity.ItemDisplayContext;
import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;
import net.momirealms.craftengine.core.world.BlockPos;
import net.momirealms.craftengine.core.world.World;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

public class ItemDisplayBlockEntityElementConfig implements BlockEntityElementConfig<ItemDisplayBlockEntityElement> {
    public static final Factory FACTORY = new Factory();
    private final Function<Player, List<Object>> lazyMetadataPacket;
    private final Function<Player, Item<?>> item;
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

    public ItemDisplayBlockEntityElementConfig(Function<Player, Item<?>> item,
                                               Vector3f scale,
                                               Vector3f position,
                                               Vector3f translation,
                                               float xRot,
                                               float yRot,
                                               Quaternionf rotation,
                                               ItemDisplayContext displayContext,
                                               Billboard billboard,
                                               float shadowRadius,
                                               float shadowStrength) {
        this.item = item;
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
        this.lazyMetadataPacket = player -> {
            List<Object> dataValues = new ArrayList<>();
            ItemDisplayEntityData.DisplayedItem.addEntityData(item.apply(player).getLiteralObject(), dataValues);
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

    @Override
    public ItemDisplayBlockEntityElement create(World world, BlockPos pos) {
        return new ItemDisplayBlockEntityElement(this, pos);
    }

    @Override
    public ItemDisplayBlockEntityElement create(World world, BlockPos pos, ItemDisplayBlockEntityElement previous) {
        return new ItemDisplayBlockEntityElement(this, pos, previous.entityId,
                previous.config.yRot != this.yRot ||
                        previous.config.xRot != this.xRot ||
                        !previous.config.position.equals(this.position)
        );
    }

    @Override
    public ItemDisplayBlockEntityElement createExact(World world, BlockPos pos, ItemDisplayBlockEntityElement previous) {
        if (!previous.config.equals(this)) {
            return null;
        }
        return new ItemDisplayBlockEntityElement(this, pos, previous.entityId, false);
    }

    @Override
    public Class<ItemDisplayBlockEntityElement> elementClass() {
        return ItemDisplayBlockEntityElement.class;
    }

    public Item<?> item(Player player) {
        return this.item.apply(player);
    }

    public Vector3f scale() {
        return this.scale;
    }

    public Vector3f translation() {
        return this.translation;
    }

    public Vector3f position() {
        return this.position;
    }

    public float yRot() {
        return this.yRot;
    }

    public float xRot() {
        return this.xRot;
    }

    public Billboard billboard() {
        return billboard;
    }

    public ItemDisplayContext displayContext() {
        return displayContext;
    }

    public Quaternionf rotation() {
        return rotation;
    }

    public float shadowRadius() {
        return shadowRadius;
    }

    public float shadowStrength() {
        return shadowStrength;
    }

    public List<Object> metadataValues(Player player) {
        return this.lazyMetadataPacket.apply(player);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ItemDisplayBlockEntityElementConfig that)) return false;
        return Float.compare(xRot, that.xRot) == 0 &&
                Float.compare(yRot, that.yRot) == 0 &&
                Objects.equal(position, that.position) &&
                Objects.equal(translation, that.translation) &&
                Objects.equal(rotation, that.rotation);
    }

    public static class Factory implements BlockEntityElementConfigFactory {

        @SuppressWarnings("unchecked")
        @Override
        public <E extends BlockEntityElement> BlockEntityElementConfig<E> create(Map<String, Object> arguments) {
            Key itemId = Key.of(ResourceConfigUtils.requireNonEmptyStringOrThrow(arguments.get("item"), "warning.config.block.state.entity_renderer.item_display.missing_item"));
            return (BlockEntityElementConfig<E>) new ItemDisplayBlockEntityElementConfig(
                    player -> BukkitItemManager.instance().createWrappedItem(itemId, player),
                    ResourceConfigUtils.getAsVector3f(arguments.getOrDefault("scale", 1f), "scale"),
                    ResourceConfigUtils.getAsVector3f(arguments.getOrDefault("position", 0.5f), "position"),
                    ResourceConfigUtils.getAsVector3f(arguments.get("translation"), "translation"),
                    ResourceConfigUtils.getAsFloat(arguments.getOrDefault("pitch", 0f), "pitch"),
                    ResourceConfigUtils.getAsFloat(arguments.getOrDefault("yaw", 0f), "yaw"),
                    ResourceConfigUtils.getAsQuaternionf(arguments.getOrDefault("rotation", 0f), "rotation"),
                    ItemDisplayContext.valueOf(arguments.getOrDefault("display-context", "none").toString().toUpperCase(Locale.ROOT)),
                    Billboard.valueOf(arguments.getOrDefault("billboard", "fixed").toString().toUpperCase(Locale.ROOT)),
                    ResourceConfigUtils.getAsFloat(arguments.getOrDefault("shadow-radius", 0f), "shadow-radius"),
                    ResourceConfigUtils.getAsFloat(arguments.getOrDefault("shadow-strength", 1f), "shadow-strength")
            );
        }
    }
}
