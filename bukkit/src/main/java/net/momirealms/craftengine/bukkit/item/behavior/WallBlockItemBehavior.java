package net.momirealms.craftengine.bukkit.item.behavior;

import net.momirealms.craftengine.core.entity.player.InteractionResult;
import net.momirealms.craftengine.core.item.behavior.ItemBehavior;
import net.momirealms.craftengine.core.item.behavior.ItemBehaviorFactory;
import net.momirealms.craftengine.core.item.context.BlockPlaceContext;
import net.momirealms.craftengine.core.item.context.UseOnContext;
import net.momirealms.craftengine.core.pack.Pack;
import net.momirealms.craftengine.core.plugin.locale.LocalizedResourceConfigException;
import net.momirealms.craftengine.core.util.Direction;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;
import net.momirealms.craftengine.core.util.Tristate;

import java.nio.file.Path;
import java.util.Map;

public class WallBlockItemBehavior extends BlockItemBehavior {
    public static final Factory FACTORY = new Factory();
    private final Tristate hanging;

    public WallBlockItemBehavior(Key wallBlockId, Tristate hanging) {
        super(wallBlockId);
        this.hanging = hanging;
    }

    @Override
    public InteractionResult useOnBlock(UseOnContext context) {
        return this.place(new BlockPlaceContext(context));
    }

    @Override
    public InteractionResult place(BlockPlaceContext context) {
        if (context.getClickedFace().stepY() != 0) {
            return InteractionResult.PASS;
        }
        if (this.hanging == Tristate.UNDEFINED) {
            return super.place(context);
        }
        for (Direction direction : context.getNearestLookingDirections()) {
            if (direction.axis() != Direction.Axis.Y) continue;
            if (this.hanging == Tristate.FALSE) {
                if (direction == Direction.DOWN) return super.place(context);
                if (direction == Direction.UP) break;
            } else if (this.hanging == Tristate.TRUE) {
                if (direction == Direction.UP) return super.place(context);
                if (direction == Direction.DOWN) break;
            }
        }
        return InteractionResult.PASS;
    }

    public static class Factory implements ItemBehaviorFactory {
        @Override
        public ItemBehavior create(Pack pack, Path path, String node, Key key, Map<String, Object> arguments) {
            Object id = arguments.get("block");
            if (id == null) {
                throw new LocalizedResourceConfigException("warning.config.item.behavior.wall_block.missing_block", new IllegalArgumentException("Missing required parameter 'block' for wall_block_item behavior"));
            }
            Tristate hanging = arguments.containsKey("hanging") ? Tristate.of(ResourceConfigUtils.getAsBoolean(arguments.get("hanging"), "hanging")) : Tristate.UNDEFINED;
            if (id instanceof Map<?, ?> map) {
                addPendingSection(pack, path, node, key, map);
                return new WallBlockItemBehavior(key, hanging);
            } else {
                return new WallBlockItemBehavior(Key.of(id.toString()), hanging);
            }
        }
    }
}
