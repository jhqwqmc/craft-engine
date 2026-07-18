package net.momirealms.craftengine.core.attribute;

import net.momirealms.craftengine.core.util.Key;

public final class AttributeOperations {
    private AttributeOperations() {}

    public static final Key ADD_VALUE_ID = Key.ce("add_value");
    public static final Key ADD_MULTIPLIED_BASE_ID = Key.ce("add_multiplied_base");
    public static final Key ADD_MULTIPLIED_TOTAL_ID = Key.ce("add_multiplied_total");

    public static final AttributeOperation ADD_VALUE = AttributeOperation.of(ADD_VALUE_ID, 0,
            (base, current, amount) -> current + amount);
    public static final AttributeOperation ADD_MULTIPLIED_BASE = AttributeOperation.of(ADD_MULTIPLIED_BASE_ID, 100,
            (base, current, amount) -> current + base * amount);
    public static final AttributeOperation ADD_MULTIPLIED_TOTAL = AttributeOperation.of(ADD_MULTIPLIED_TOTAL_ID, 200,
            (base, current, amount) -> current * (1 + amount));
}
