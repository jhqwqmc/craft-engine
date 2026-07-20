package net.momirealms.craftengine.core.attribute;

import com.google.common.collect.ImmutableMap;
import net.momirealms.craftengine.core.entity.Entity;
import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.plugin.context.Context;
import net.momirealms.craftengine.core.plugin.context.PlayerOptionalContext;
import net.momirealms.craftengine.core.util.Key;

import java.util.HashMap;
import java.util.Map;

public final class AttributeContainer implements AttributeGetter {
    private final AttributeManager manager;
    private final Entity entity;
    private final Map<Key, AttributeInstance> instances = new HashMap<>();
    private final EntityEquipments equipments;
    private final Context context;

    public AttributeContainer(AttributeManager manager, Entity entity) {
        this.manager = manager;
        this.entity = entity;
        this.context = entity instanceof Player player ? PlayerOptionalContext.of(player) : PlayerOptionalContext.emptyImmutable();
        this.equipments = new EntityEquipments(this);
    }

    public Entity entity() {
        return this.entity;
    }

    public EntityEquipments equipments() {
        return this.equipments;
    }

    public AttributeInstance getOrCreateInstance(Key attribute) {
        Attribute attr = this.manager.getAttribute(attribute).orElseThrow(() -> new IllegalStateException("Attribute " + attribute + " not found"));
        return getOrCreateInstance(attr);
    }

    public AttributeInstance getOrCreateInstance(Attribute attribute) {
        return this.instances.computeIfAbsent(attribute.id(), k -> new AttributeInstance(attribute, this.context));
    }

    @Override
    public double getAttributeValue(Attribute attribute) {
        return getOrCreateInstance(attribute).getValue();
    }

    public AttributeContainerSnapshot createSnapshot() {
        ImmutableMap.Builder<Key, Double> builder = ImmutableMap.builder();
        for (Map.Entry<Key, AttributeInstance> entry : this.instances.entrySet()) {
            builder.put(entry.getKey(), entry.getValue().getValue());
        }
        return new AttributeContainerSnapshot(this, builder.build());
    }
}
