package net.momirealms.craftengine.bukkit.item.behavior;

import net.momirealms.craftengine.core.entity.player.InteractionResult;
import net.momirealms.craftengine.core.item.behavior.ItemBehavior;
import net.momirealms.craftengine.core.item.behavior.ItemBehaviorFactory;
import net.momirealms.craftengine.core.item.context.BlockPlaceContext;
import net.momirealms.craftengine.core.item.context.UseOnContext;
import net.momirealms.craftengine.core.pack.Pack;
import net.momirealms.craftengine.core.plugin.locale.LocalizedResourceConfigException;
import net.momirealms.craftengine.core.util.Key;

import java.nio.file.Path;
import java.util.Map;

public class CeilingBlockItemBehavior extends BlockItemBehavior {
    public static final Factory FACTORY = new Factory();

    public CeilingBlockItemBehavior(Key ceilingBlockId) {
        super(ceilingBlockId);
    }

    @Override
    public InteractionResult useOnBlock(UseOnContext context) {
        return this.place(new BlockPlaceContext(context));
    }

    @Override
    public InteractionResult place(BlockPlaceContext context) {
        if (context.getClickedFace().stepY() != -1) {
            return InteractionResult.PASS;
        }
        return super.place(context);
    }

    public static class Factory implements ItemBehaviorFactory {
        @Override
        public ItemBehavior create(Pack pack, Path path, String node, Key key, Map<String, Object> arguments) {
            Object id = arguments.get("block");
            if (id == null) {
                throw new LocalizedResourceConfigException("warning.config.item.behavior.ceiling_block.missing_block", new IllegalArgumentException("Missing required parameter 'block' for ceiling_block_item behavior"));
            }
            if (id instanceof Map<?, ?> map) {
                addPendingSection(pack, path, node, key, map);
                return new CeilingBlockItemBehavior(key);
            } else {
                return new CeilingBlockItemBehavior(Key.of(id.toString()));
            }
        }
    }
}
