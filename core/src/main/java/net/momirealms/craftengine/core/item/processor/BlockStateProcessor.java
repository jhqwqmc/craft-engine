package net.momirealms.craftengine.core.item.processor;

import net.momirealms.craftengine.core.block.BlockStateWrapper;
import net.momirealms.craftengine.core.block.CustomBlockStateWrapper;
import net.momirealms.craftengine.core.item.DataComponentKeys;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.item.ItemBuildContext;
import net.momirealms.craftengine.core.item.ItemProcessorFactory;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.LazyReference;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public final class BlockStateProcessor implements SimpleNetworkItemProcessor {
    public static final ItemProcessorFactory<BlockStateProcessor> FACTORY = new Factory();
    private static final Object[] NBT_PATH = new Object[]{"BlockStateTag"};
    private final LazyReference<Map<String, String>> wrapper;

    public BlockStateProcessor(LazyReference<Map<String, String>> wrapper) {
        this.wrapper = wrapper;
    }

    @Override
    public <I> Item<I> apply(Item<I> item, ItemBuildContext context) {
        return item.blockState(this.wrapper.get());
    }

    @Override
    public <I> Object[] nbtPath(Item<I> item, ItemBuildContext context) {
        return NBT_PATH;
    }

    @Override
    public <I> String nbtPathString(Item<I> item, ItemBuildContext context) {
        return "BlockStateTag";
    }

    @Override
    public <I> Key componentType(Item<I> item, ItemBuildContext context) {
        return DataComponentKeys.BLOCK_STATE;
    }

    private static class Factory implements ItemProcessorFactory<BlockStateProcessor> {

        @Override
        public BlockStateProcessor create(Object arg) {
            if (arg instanceof Map<?, ?> map) {
                Map<String, String> properties = new HashMap<>();
                for (Map.Entry<?, ?> entry : map.entrySet()) {
                    properties.put(entry.getKey().toString(), entry.getValue().toString());
                }
                return new BlockStateProcessor(LazyReference.lazyReference(() -> properties));
            } else {
                String stateString = arg.toString();
                return new BlockStateProcessor(LazyReference.lazyReference(() -> {
                    BlockStateWrapper blockState = CraftEngine.instance().blockManager().createBlockState(stateString);
                    if (blockState instanceof CustomBlockStateWrapper customBlockStateWrapper) {
                        blockState = customBlockStateWrapper.visualBlockState();
                    }
                    if (blockState != null) {
                        Map<String, String> properties = new HashMap<>(4);
                        for (String property : blockState.getPropertyNames()) {
                            Object value = blockState.getProperty(property);
                            properties.put(property, String.valueOf(value).toLowerCase(Locale.ROOT));
                        }
                        return properties;
                    }
                    return Collections.emptyMap();
                }));
            }
        }
    }
}
