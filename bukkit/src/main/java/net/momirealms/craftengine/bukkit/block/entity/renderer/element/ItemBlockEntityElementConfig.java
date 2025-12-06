package net.momirealms.craftengine.bukkit.block.entity.renderer.element;

import net.momirealms.craftengine.bukkit.entity.data.ItemEntityData;
import net.momirealms.craftengine.bukkit.item.BukkitItemManager;
import net.momirealms.craftengine.core.block.entity.render.element.BlockEntityElementConfig;
import net.momirealms.craftengine.core.block.entity.render.element.BlockEntityElementConfigFactory;
import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;
import net.momirealms.craftengine.core.world.BlockPos;
import net.momirealms.craftengine.core.world.World;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class ItemBlockEntityElementConfig implements BlockEntityElementConfig<ItemBlockEntityElement> {
    public static final Factory FACTORY = new Factory();
    private final Function<Player, List<Object>> lazyMetadataPacket;
    private final Function<Player, Item<?>> item;
    private final Vector3f position;

    public ItemBlockEntityElementConfig(Function<Player, Item<?>> item, Vector3f position) {
        this.item = item;
        this.position = position;
        this.lazyMetadataPacket = player -> {
            List<Object> dataValues = new ArrayList<>();
            ItemEntityData.Item.addEntityData(item.apply(player).getLiteralObject(), dataValues);
            ItemEntityData.NoGravity.addEntityData(true, dataValues);
            return dataValues;
        };
    }

    @Override
    public ItemBlockEntityElement create(World world, BlockPos pos) {
        return new ItemBlockEntityElement(this, pos);
    }

    @Override
    public ItemBlockEntityElement create(World world, BlockPos pos, ItemBlockEntityElement previous) {
        return new ItemBlockEntityElement(this, pos, previous.entityId1, previous.entityId2, !previous.config.position.equals(this.position));
    }

    @Override
    public ItemBlockEntityElement createExact(World world, BlockPos pos, ItemBlockEntityElement previous) {
        if (!previous.config.isSamePosition(this)) {
            return null;
        }
        return new ItemBlockEntityElement(this, pos, previous.entityId1, previous.entityId2, false);
    }

    @Override
    public Class<ItemBlockEntityElement> elementClass() {
        return ItemBlockEntityElement.class;
    }

    public Vector3f position() {
        return position;
    }

    public Item<?> item(Player player) {
        return this.item.apply(player);
    }

    public List<Object> metadataValues(Player player) {
        return this.lazyMetadataPacket.apply(player);
    }

    public boolean isSamePosition(ItemBlockEntityElementConfig that) {
        return this.position.equals(that.position);
    }

    public static class Factory implements BlockEntityElementConfigFactory<ItemBlockEntityElement> {

        @Override
        public ItemBlockEntityElementConfig create(Map<String, Object> arguments) {
            Key itemId = Key.of(ResourceConfigUtils.requireNonEmptyStringOrThrow(arguments.get("item"), "warning.config.block.state.entity_renderer.item_display.missing_item"));
            return new ItemBlockEntityElementConfig(
                    player -> BukkitItemManager.instance().createWrappedItem(itemId, player),
                    ResourceConfigUtils.getAsVector3f(arguments.getOrDefault("position", 0.5f), "position")
            );
        }
    }
}
