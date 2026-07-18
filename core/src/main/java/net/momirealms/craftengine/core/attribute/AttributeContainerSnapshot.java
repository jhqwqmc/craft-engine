package net.momirealms.craftengine.core.attribute;

import net.momirealms.craftengine.core.util.Key;

import java.util.Map;

public final class AttributeContainerSnapshot implements AttributeGetter {
    private final AttributeContainer source;
    private final Map<Key, Double> snapshots;

    AttributeContainerSnapshot(AttributeContainer source, Map<Key, Double> snapshots) {
        this.source = source;
        this.snapshots = snapshots;
    }

    public AttributeContainer source() {
        return this.source;
    }

    public Map<Key, Double> snapshots() {
        return this.snapshots;
    }

    @Override
    public double getAttributeValue(Attribute attribute) {
        return this.snapshots.get(attribute.id());
    }
}
