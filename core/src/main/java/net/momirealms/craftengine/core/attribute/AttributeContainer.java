package net.momirealms.craftengine.core.attribute;

import com.google.common.collect.ImmutableMap;
import net.momirealms.craftengine.core.entity.Entity;
import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.plugin.context.PlayerOptionalContext;
import net.momirealms.craftengine.core.util.Key;

import java.util.HashMap;
import java.util.Map;

public final class AttributeContainer implements AttributeGetter {
    private final Entity entity;
    private final Map<Key, AttributeInstance> instances = new HashMap<>();

    public AttributeContainer(Entity entity) {
        this.entity = entity;
    }

    public Entity entity() {
        return this.entity;
    }

    public AttributeInstance getOrCreateInstance(Attribute attribute) {
        return this.instances.computeIfAbsent(attribute.id(), k -> {
            if (this.entity instanceof Player player) {
                return new AttributeInstance(attribute, PlayerOptionalContext.of(player));
            } else {
                return new AttributeInstance(attribute, PlayerOptionalContext.of(null));
            }
        });
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
