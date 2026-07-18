package net.momirealms.craftengine.core.attribute;

import net.momirealms.craftengine.core.entity.Entity;
import net.momirealms.craftengine.core.util.Key;
import org.jetbrains.annotations.Nullable;

public interface DamageSource {

    Key type();

    boolean isCritical();

    boolean isDirect();

    @Nullable
    Entity causingEntity();

    @Nullable
    Entity directEntity();
}
