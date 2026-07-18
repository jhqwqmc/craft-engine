package net.momirealms.craftengine.core.attribute;

import net.momirealms.craftengine.core.util.Key;
import org.jetbrains.annotations.Nullable;

public interface Attribute {

    Key id();

    double defaultValue();

    default double limit(double value) {
        return this.constraint().limit(value);
    }

    ValueConstraint constraint();

    @Nullable
    VanillaAttributeSync sync();
}
