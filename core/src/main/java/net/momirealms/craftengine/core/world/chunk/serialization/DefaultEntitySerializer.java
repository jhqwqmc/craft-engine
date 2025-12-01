package net.momirealms.craftengine.core.world.chunk.serialization;

import net.momirealms.craftengine.core.entity.CustomEntity;
import net.momirealms.craftengine.core.entity.CustomEntityType;
import net.momirealms.craftengine.core.entity.InactiveCustomEntity;
import net.momirealms.craftengine.core.registry.BuiltInRegistries;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.world.CEWorld;
import net.momirealms.craftengine.core.world.WorldPosition;
import net.momirealms.sparrow.nbt.CompoundTag;
import net.momirealms.sparrow.nbt.ListTag;
import net.momirealms.sparrow.nbt.Tag;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class DefaultEntitySerializer {

    public static ListTag serialize(@NotNull Collection<CustomEntity> entity) {
        ListTag entities = new ListTag();
        for (CustomEntity customEntity : entity) {
            if (customEntity.isValid()) {
                entities.add(customEntity.saveAsTag());
            }
        }
        return entities;
    }

    public static List<CustomEntity> deserialize(CEWorld world, ListTag entitiesTag) {
        List<CustomEntity> entities = new ArrayList<>(entitiesTag.size());
        for (Tag tag : entitiesTag) {
            if (tag instanceof CompoundTag entityTag) {
                WorldPosition worldPosition = CustomEntity.readPos(world, entityTag);
                UUID uuid = CustomEntity.readUUID(entityTag);
                Key type = Key.of(entityTag.getString("type"));
                CustomEntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.getValue(type);
                if (entityType == null) {
                    InactiveCustomEntity entity = new InactiveCustomEntity(uuid, worldPosition, entityTag);
                    entities.add(entity);
                } else {
                    CustomEntity entity = entityType.factory().create(uuid, worldPosition);
                    entity.loadCustomData(entityTag);
                    // 加载时无效则直接放弃
                    if (entity.isValid()) {
                        entities.add(entity);
                    }
                }
            }
        }
        return entities;
    }
}
