package net.momirealms.craftengine.bukkit.block.entity.renderer.element;

import com.google.common.base.Objects;
import net.momirealms.craftengine.bukkit.entity.data.ArmorStandData;
import net.momirealms.craftengine.bukkit.entity.data.BaseEntityData;
import net.momirealms.craftengine.bukkit.item.BukkitItemManager;
import net.momirealms.craftengine.core.block.entity.render.element.BlockEntityElementConfig;
import net.momirealms.craftengine.core.block.entity.render.element.BlockEntityElementConfigFactory;
import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.item.ItemKeys;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.LegacyChatFormatter;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;
import net.momirealms.craftengine.core.world.BlockPos;
import net.momirealms.craftengine.core.world.World;
import org.bukkit.inventory.ItemStack;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class ArmorStandBlockEntityElementConfig implements BlockEntityElementConfig<ArmorStandBlockEntityElement> {
    public static final Factory FACTORY = new Factory();
    public final Function<Player, List<Object>> lazyMetadataPacket;
    public final Key itemId;
    public final float scale;
    public final Vector3f position;
    public final float xRot;
    public final float yRot;
    public final boolean small;
    public final LegacyChatFormatter glowColor;

    public ArmorStandBlockEntityElementConfig(Key itemId,
                                              float scale,
                                              Vector3f position,
                                              float xRot,
                                              float yRot,
                                              boolean small,
                                              LegacyChatFormatter glowColor) {
        this.itemId = itemId;
        this.glowColor = glowColor;
        this.scale = scale;
        this.position = position;
        this.xRot = xRot;
        this.yRot = yRot;
        this.small = small;
        this.lazyMetadataPacket = player -> {
            List<Object> dataValues = new ArrayList<>(2);
            if (glowColor != null) {
                BaseEntityData.SharedFlags.addEntityData((byte) 0x60, dataValues);
            } else {
                BaseEntityData.SharedFlags.addEntityData((byte) 0x20, dataValues);
            }
            if (small) {
                ArmorStandData.ArmorStandFlags.addEntityData((byte) 0x01, dataValues);
            }
            return dataValues;
        };
    }

    @Override
    public ArmorStandBlockEntityElement create(World world, BlockPos pos) {
        return new ArmorStandBlockEntityElement(this, pos);
    }

    @Override
    public ArmorStandBlockEntityElement create(World world, BlockPos pos, ArmorStandBlockEntityElement previous) {
        if (previous.config.scale != scale || previous.config.glowColor != glowColor) {
            return null;
        }
        return new ArmorStandBlockEntityElement(this, pos, previous.entityId,
                previous.config.yRot != this.yRot ||
                previous.config.xRot != this.xRot ||
                !previous.config.position.equals(this.position)
        );
    }

    @Override
    public ArmorStandBlockEntityElement createExact(World world, BlockPos pos, ArmorStandBlockEntityElement previous) {
        if (!previous.config.isSamePosition(this)) {
            return null;
        }
        return new ArmorStandBlockEntityElement(this, pos, previous.entityId, false);
    }

    @Override
    public Class<ArmorStandBlockEntityElement> elementClass() {
        return ArmorStandBlockEntityElement.class;
    }

    public Item<?> item(Player player) {
        Item<ItemStack> wrappedItem = BukkitItemManager.instance().createWrappedItem(this.itemId, player);
        return wrappedItem == null ? BukkitItemManager.instance().createWrappedItem(ItemKeys.BARRIER, player) : wrappedItem ;
    }

    public Key itemId() {
        return this.itemId;
    }

    public float scale() {
        return this.scale;
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

    public boolean small() {
        return this.small;
    }

    public List<Object> metadataValues(Player player) {
        return this.lazyMetadataPacket.apply(player);
    }

    public boolean isSamePosition(ArmorStandBlockEntityElementConfig that) {
        return Float.compare(xRot, that.xRot) == 0 &&
                Float.compare(yRot, that.yRot) == 0 &&
                Objects.equal(position, that.position);
    }

    public static class Factory implements BlockEntityElementConfigFactory<ArmorStandBlockEntityElement> {

        @Override
        public ArmorStandBlockEntityElementConfig create(Map<String, Object> arguments) {
            return new ArmorStandBlockEntityElementConfig(
                    Key.of(ResourceConfigUtils.requireNonEmptyStringOrThrow(arguments.get("item"), "warning.config.block.state.entity_renderer.armor_stand.missing_item")),
                    ResourceConfigUtils.getAsFloat(arguments.getOrDefault("scale", 1f), "scale"),
                    ResourceConfigUtils.getAsVector3f(arguments.getOrDefault("position", 0.5f), "position"),
                    ResourceConfigUtils.getAsFloat(arguments.getOrDefault("pitch", 0f), "pitch"),
                    ResourceConfigUtils.getAsFloat(arguments.getOrDefault("yaw", 0f), "yaw"),
                    ResourceConfigUtils.getAsBoolean(arguments.getOrDefault("small", false), "small"),
                    ResourceConfigUtils.getAsEnum(arguments.get("glow-color"), LegacyChatFormatter.class, null)
            );
        }
    }
}
