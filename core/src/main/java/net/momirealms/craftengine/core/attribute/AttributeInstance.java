package net.momirealms.craftengine.core.attribute;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import net.momirealms.craftengine.core.util.Key;

import java.util.HashMap;
import java.util.Map;

public class AttributeInstance {
    private final Attribute attribute;
    private final Map<Key, Map<Key, AttributeModifier>> byOperation = new HashMap<>();
    private final Map<Key, AttributeModifier> byId = new Object2ObjectArrayMap<>();
    private double baseValue;
    private double cachedValue;
    private boolean dirty = true;

    public AttributeInstance(Attribute attribute) {
        this.attribute = attribute;
    }

    public double getValue() {
        return this.baseValue;
    }
}
