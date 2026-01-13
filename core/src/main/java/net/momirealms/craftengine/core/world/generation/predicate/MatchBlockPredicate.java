package net.momirealms.craftengine.core.world.generation.predicate;

import net.momirealms.craftengine.core.block.BlockStateWrapper;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.world.Vec3i;

import java.util.Set;

final class MatchBlockPredicate extends OffsetBlockPredicate {
    private final Set<Key> blocks;

    private MatchBlockPredicate(Vec3i offset, Set<Key> blocks) {
        super(offset);
        this.blocks = blocks;
    }

    @Override
    protected boolean test(BlockStateWrapper state) {
        return this.blocks.contains(state.ownerId());
    }
}
