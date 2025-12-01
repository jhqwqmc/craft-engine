package net.momirealms.craftengine.core.entity.furniture;

import net.momirealms.craftengine.core.entity.CustomEntity;
import net.momirealms.craftengine.core.entity.CustomEntityType;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.world.WorldPosition;
import net.momirealms.sparrow.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;

public abstract class Furniture extends CustomEntity {
    protected FurnitureConfig config;
    protected FurnitureDataAccessor dataAccessor;

    protected Key furnitureId;
    protected CompoundTag inactiveFurnitureData;

    protected Furniture(CustomEntityType<?> type, UUID uuid, WorldPosition position) {
        super(type, uuid, position);
    }

    @Override
    protected void saveCustomData(CompoundTag tag) {
        tag.putString("id", this.config.id().asMinimalString());

    }

    @Override
    public void loadCustomData(CompoundTag tag) {
        this.furnitureId = readFurnitureId(tag);
        this.dataAccessor = new FurnitureDataAccessor(tag.getCompound("data"));
        Optional<FurnitureConfig> furnitureConfig = CraftEngine.instance().furnitureManager().furnitureById(this.furnitureId);
        if (furnitureConfig.isPresent()) {
            this.config = furnitureConfig.get();
        } else {
            this.inactiveFurnitureData = tag;
        }
    }

    @Override
    public CompoundTag saveAsTag() {
        // 如果家具数据只是不活跃，则返回不活跃家具数据
        if (this.inactiveFurnitureData != null) {
            return this.inactiveFurnitureData;
        }
        return super.saveAsTag();
    }

    @NotNull
    public FurnitureConfig config() {
        return this.config;
    }

    @NotNull
    public FurnitureDataAccessor dataAccessor() {
        return this.dataAccessor;
    }

    protected final Key readFurnitureId(CompoundTag tag) {
        return Key.of(tag.getString("id"));
    }
}
