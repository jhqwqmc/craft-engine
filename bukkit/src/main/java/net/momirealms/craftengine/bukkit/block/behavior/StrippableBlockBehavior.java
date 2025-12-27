package net.momirealms.craftengine.bukkit.block.behavior;

import net.momirealms.craftengine.core.block.BlockStateWrapper;
import net.momirealms.craftengine.core.block.CustomBlock;
import net.momirealms.craftengine.core.block.behavior.BlockBehavior;
import net.momirealms.craftengine.core.block.behavior.BlockBehaviorFactory;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.util.LazyReference;
import net.momirealms.craftengine.core.util.MiscUtils;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;
import net.momirealms.sparrow.nbt.CompoundTag;

import java.util.List;
import java.util.Map;

public class StrippableBlockBehavior extends BukkitBlockBehavior {
    public static final BlockBehaviorFactory FACTORY = new Factory();
    private final String stripped;
    private final LazyReference<BlockStateWrapper> lazyState;
    private final List<String> excludedProperties;

    public StrippableBlockBehavior(CustomBlock block, String stripped, List<String> excludedProperties) {
        super(block);
        this.stripped = stripped;
        this.lazyState = LazyReference.lazyReference(() -> CraftEngine.instance().blockManager().createBlockState(this.stripped));
        this.excludedProperties = excludedProperties;
    }

    public String stripped() {
        return this.stripped;
    }

    public BlockStateWrapper strippedState() {
        return this.lazyState.get();
    }

    public CompoundTag filter(CompoundTag properties) {
        for (String property : this.excludedProperties) {
            properties.remove(property);
        }
        return properties;
    }

    private static class Factory implements BlockBehaviorFactory {

        @Override
        public BlockBehavior create(CustomBlock block, Map<String, Object> arguments) {
            String stripped = ResourceConfigUtils.requireNonEmptyStringOrThrow(arguments.get("stripped"), "warning.config.block.behavior.strippable.missing_stripped");
            List<String> excludedProperties = MiscUtils.getAsStringList(arguments.get("excluded-properties"));
            return new StrippableBlockBehavior(block, stripped, excludedProperties);
        }
    }
}
