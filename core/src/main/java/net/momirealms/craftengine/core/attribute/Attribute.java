package net.momirealms.craftengine.core.attribute;

import net.momirealms.craftengine.core.entity.Entity;
import net.momirealms.craftengine.core.util.Key;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

public final class Attribute {
    public final Key id;
    public final double defaultValue;
    public final ValueConstraint constraint;
    public final Predicate<Entity> predicate;
    @Nullable
    public final VanillaAttributeSync sync;

    public Attribute(Key id, double defaultValue, ValueConstraint constraint, Predicate<Entity> predicate, @Nullable VanillaAttributeSync sync) {
        this.id = id;
        this.defaultValue = defaultValue;
        this.constraint = constraint;
        this.sync = sync;
        this.predicate = predicate;
    }

    public Key id() {
        return this.id;
    }

    public double limit(double value) {
        return this.constraint.limit(value);
    }

    public double defaultValue(Entity entity) {
        // TODO per entity default value
        return this.defaultValue;
    }

    public ValueConstraint constraint() {
        return this.constraint;
    }

    @Nullable
    public VanillaAttributeSync sync() {
        return this.sync;
    }
}
