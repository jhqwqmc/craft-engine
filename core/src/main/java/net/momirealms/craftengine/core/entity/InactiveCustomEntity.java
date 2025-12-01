package net.momirealms.craftengine.core.entity;

import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.plugin.entityculling.CullingData;
import net.momirealms.craftengine.core.world.WorldPosition;
import net.momirealms.sparrow.nbt.CompoundTag;
import net.momirealms.sparrow.nbt.NBT;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;

public class InactiveCustomEntity extends CustomEntity {
    public static final CompoundTag INVALID_TAG = new CompoundTag(Map.of("type", NBT.createString(CustomEntityTypes.INACTIVE.id().asMinimalString())));
    private final CompoundTag data;

    public InactiveCustomEntity(UUID uuid, WorldPosition position) {
        super(CustomEntityTypes.INACTIVE, uuid, position);
        this.data = INVALID_TAG;
    }

    public InactiveCustomEntity(UUID uuid, WorldPosition position, CompoundTag data) {
        super(CustomEntityTypes.INACTIVE, uuid, position);
        this.data = data;
    }

    @Override
    public CompoundTag saveAsTag() {
        return this.data;
    }

    @Override
    public boolean isValid() {
        // 不正常的数据不要存储
        return this.data != INVALID_TAG;
    }

    @Override
    public void destroy() {
    }

    @Override
    public void show(Player player) {
    }

    @Override
    public void hide(Player player) {
    }

    @Override
    public @Nullable CullingData cullingData() {
        return null;
    }
}
