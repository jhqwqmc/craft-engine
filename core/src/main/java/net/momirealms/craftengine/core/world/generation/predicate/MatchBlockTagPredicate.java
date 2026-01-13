package net.momirealms.craftengine.core.world.generation.predicate;

import net.momirealms.craftengine.core.block.BlockStateWrapper;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.world.Vec3i;

final class MatchBlockTagPredicate extends OffsetBlockPredicate {
    private final Key[] tags;

    private MatchBlockTagPredicate(Vec3i offset, Key[] tags) {
        super(offset);
        this.tags = tags;
    }

    @Override
    protected boolean test(BlockStateWrapper state) {
        for (Key key : this.tags) {
            if (state.hasTag(key)) {
                return true;
            }
        }
        return false;
    }
}
