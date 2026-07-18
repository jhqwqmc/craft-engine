package net.momirealms.craftengine.core.attribute;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.context.Context;
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
    private final Context context;

    public AttributeInstance(Attribute attribute, Context context) {
        this.attribute = attribute;
        this.baseValue = attribute.defaultValue;
        this.context = context;
    }

    public double getValue() {
        if (this.dirty) {
            this.recalculate();
        }
        return this.cachedValue;
    }

    public void setBaseValue(double baseValue) {
        this.baseValue = baseValue;
        this.dirty = true;
    }

    public void recalculate() {
        double value = this.baseValue;
        for (AttributeOperation operation : CraftEngine.instance().attributeManager().sortedOperations()) {
            Map<Key, AttributeModifier> attributeModifiers = this.byOperation.get(operation.id());
            if (attributeModifiers != null) {
                double phaseBase = value;
                for (AttributeModifier modifier : attributeModifiers.values()) {
                    if (modifier.condition().test(this.context)) {
                        value = operation.apply(phaseBase, value, modifier.amount());
                    }
                }
            }
        }
        this.cachedValue = value;
        this.dirty = false;
    }
}
