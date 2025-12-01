package net.momirealms.craftengine.core.entity.furniture;

import net.momirealms.craftengine.core.entity.CustomEntity;
import net.momirealms.craftengine.core.entity.CustomEntityType;
import net.momirealms.craftengine.core.world.WorldPosition;
import net.momirealms.sparrow.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;

public abstract class Furniture extends CustomEntity {
    protected final FurnitureConfig config;
    protected final FurnitureDataAccessor dataAccessor;

    public Furniture(CustomEntityType<?> type, WorldPosition position, FurnitureConfig config, CompoundTag data) {
        super(type, position);
        this.dataAccessor = new FurnitureDataAccessor(data);
        this.config = config;
    }

    @NotNull
    public FurnitureConfig config() {
        return this.config;
    }

    @NotNull
    public FurnitureDataAccessor dataAccessor() {
        return this.dataAccessor;
    }
}
