package net.momirealms.craftengine.core.entity;

import net.momirealms.craftengine.core.world.CEWorld;
import net.momirealms.craftengine.core.world.Cullable;
import net.momirealms.craftengine.core.world.WorldPosition;
import net.momirealms.sparrow.nbt.CompoundTag;

import java.util.UUID;

public abstract class CustomEntity implements Cullable {
    protected final CustomEntityType<?> type;
    protected final UUID uuid;
    protected WorldPosition position;
    protected boolean valid = true;

    protected CustomEntity(CustomEntityType<?> type, UUID uuid, WorldPosition position) {
        this.position = position;
        this.type = type;
        this.uuid = uuid;
    }

    public CompoundTag saveAsTag() {
        CompoundTag tag = new CompoundTag();
        this.saveId(tag);
        this.savePos(tag);
        this.saveCustomData(tag);
        return tag;
    }

    private void savePos(CompoundTag tag) {
        tag.putDouble("x", this.position.x());
        tag.putDouble("y", this.position.y());
        tag.putDouble("z", this.position.z());
        tag.putFloat("x_rot", this.position.xRot());
        tag.putFloat("y_rot", this.position.yRot());
    }

    public boolean isValid() {
        return this.valid;
    }

    public UUID uuid() {
        return uuid;
    }

    public double x() {
        return this.position.x();
    }

    public double y() {
        return this.position.y();
    }

    public double z() {
        return this.position.z();
    }

    public float yRot() {
        return this.position.yRot();
    }

    public float xRot() {
        return this.position.xRot();
    }

    public CustomEntityType<?> entityType() {
        return this.type;
    }

    protected void saveCustomData(CompoundTag tag) {
    }

    public void loadCustomData(CompoundTag tag) {
    }

    private void saveId(CompoundTag tag) {
        tag.putString("id", this.type.id().asString());
    }

    public void destroy() {
        this.valid = false;
    }

    public static UUID readUUID(CompoundTag tag) {
        return tag.getUUID("uuid");
    }

    public static WorldPosition readPos(CEWorld world, CompoundTag tag) {
        double x = tag.getDouble("x");
        double y = tag.getDouble("y");
        double z = tag.getDouble("z");
        float xRot = tag.getFloat("x_rot");
        float yRot = tag.getFloat("y_rot");
        return new WorldPosition(world.world, x, y, z, xRot, yRot);
    }
}
