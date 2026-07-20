package net.momirealms.craftengine.core.attribute;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.context.Context;
import net.momirealms.craftengine.core.util.Key;

import java.util.HashMap;
import java.util.Map;

public class AttributeInstance {
    private final Attribute attribute;
    private final Map<Key, Map<Key, AttributeModifier>> byOperation = new HashMap<>();
    private final Map<Key, AttributeModifier> byId = new Object2ObjectArrayMap<>();
    private final Context context;
    private double baseValue;
    private double cachedValue;
    private boolean dirty = true;

    public AttributeInstance(Attribute attribute, Context context) {
        this.attribute = attribute;
        this.baseValue = attribute.defaultValue;
        this.context = context;
    }

    public double getValue() {
        if (this.dirty) {
            this.cachedValue = this.recalculate();
            this.dirty = false;
        }
        return this.cachedValue;
    }

    public boolean hasModifier(Key id) {
        return this.byId.containsKey(id);
    }

    public AttributeModifier getModifier(Key id) {
        return this.byId.get(id);
    }

    public void removeModifier(Key id) {
        AttributeModifier removed = this.byId.remove(id);
        if (removed != null) {
            Map<Key, AttributeModifier> operations = this.getModifiersByOperation(removed.operation());
            if (operations != null) {
                operations.remove(id);
            }
        }
        this.setDirty();
    }

    public void removeModifier(AttributeModifier modifier) {
        this.removeModifier(modifier.id());
    }

    public void addModifier(AttributeModifier modifier) {
        AttributeModifier previous = this.byId.putIfAbsent(modifier.id(), modifier);
        if (previous != null) {
            throw new IllegalArgumentException("Modifier is already applied on this attribute!");
        } else {
            this.getModifiersByOperation(modifier.operation()).put(modifier.id(), modifier);
            this.setDirty();
        }
    }

    public void addOrUpdateModifier(AttributeModifier modifier) {
        AttributeModifier oldModifier = this.byId.put(modifier.id(), modifier);
        if (modifier != oldModifier) {
            this.getModifiersByOperation(modifier.operation()).put(modifier.id(), modifier);
            this.setDirty();
        }
    }

    public void setBaseValue(double baseValue) {
        this.baseValue = baseValue;
        this.setDirty();
    }

    public void setDirty() {
        this.dirty = true;
    }

    public Map<Key, AttributeModifier> getModifiersByOperation(Key operation) {
        return this.byOperation.computeIfAbsent(operation, k -> new Object2ObjectOpenHashMap<>());
    }

    public double recalculate() {
        double value = this.baseValue;
        for (AttributeOperation operation : CraftEngine.instance().attributeManager().sortedOperations()) {
            Map<Key, AttributeModifier> attributeModifiers = this.byOperation.get(operation.id());
            if (attributeModifiers != null) {
                double phaseBase = value;
                for (AttributeModifier modifier : attributeModifiers.values()) {
                    if (modifier.condition().test(this.context)) {
                        value = operation.apply(phaseBase, value, modifier.amount(this.context));
                    }
                }
            }
        }
        return value;
    }
}
