package net.momirealms.craftengine.core.attribute;

import net.momirealms.craftengine.core.block.BlockStateWrapper;
import net.momirealms.craftengine.core.entity.Entity;
import net.momirealms.craftengine.core.util.Key;
import org.jetbrains.annotations.Nullable;

public interface DamageSource {

    Key type();

    boolean isCritical();

    default boolean isDirect() {
        return causingEntity() == directEntity();
    }

    @Nullable
    Entity causingEntity();

    @Nullable
    Entity directEntity();

    @Nullable
    BlockStateWrapper blockSnapshot();
}
