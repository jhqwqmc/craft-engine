package net.momirealms.craftengine.core.block.behavior;

import net.momirealms.craftengine.core.block.CustomBlock;

public abstract class AbstractBlockBehavior extends BlockBehavior {
    protected final CustomBlock customBlock;

    public AbstractBlockBehavior(CustomBlock customBlock) {
        this.customBlock = customBlock;
    }

    @Override
    public CustomBlock block() {
        return this.customBlock;
    }
}
