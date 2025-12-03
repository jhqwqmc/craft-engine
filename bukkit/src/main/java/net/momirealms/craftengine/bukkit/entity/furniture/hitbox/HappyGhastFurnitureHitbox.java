package net.momirealms.craftengine.bukkit.entity.furniture.hitbox;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.momirealms.craftengine.bukkit.nms.FastNMS;
import net.momirealms.craftengine.bukkit.plugin.reflection.minecraft.CoreReflections;
import net.momirealms.craftengine.bukkit.plugin.reflection.minecraft.MAttributeHolders;
import net.momirealms.craftengine.bukkit.plugin.reflection.minecraft.MEntityTypes;
import net.momirealms.craftengine.bukkit.plugin.reflection.minecraft.NetworkReflections;
import net.momirealms.craftengine.core.entity.furniture.Collider;
import net.momirealms.craftengine.core.entity.furniture.Furniture;
import net.momirealms.craftengine.core.entity.furniture.hitbox.FurnitureHitboxPart;
import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.world.Vec3d;
import net.momirealms.craftengine.core.world.WorldPosition;
import net.momirealms.craftengine.core.world.collision.AABB;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public class HappyGhastFurnitureHitbox extends AbstractFurnitureHitBox {
    private final HappyGhastFurnitureHitboxConfig config;
    private final Collider collider;
    private final Object despawnPacket;
    private final FurnitureHitboxPart part;
    private final Vec3d pos;
    private final List<Object> packets;
    private final int entityId;
    private final float yaw;

    public HappyGhastFurnitureHitbox(Furniture furniture, HappyGhastFurnitureHitboxConfig config) {
        super(furniture, config);
        this.config = config;
        WorldPosition position = furniture.position();
        this.pos = Furniture.getRelativePosition(position, config.position());
        double bbSize = 4 * config.scale();
        AABB aabb = AABB.fromInteraction(this.pos, bbSize, bbSize);
        this.yaw = position.yRot;
        this.entityId = CoreReflections.instance$Entity$ENTITY_COUNTER.incrementAndGet();
        this.packets = new ArrayList<>(3);
        this.packets.add(FastNMS.INSTANCE.constructor$ClientboundSetEntityDataPacket(this.entityId, config.cachedValues()));
        if (config.scale() != 1) {
            try {
                Object attributeInstance = CoreReflections.constructor$AttributeInstance.newInstance(MAttributeHolders.SCALE, (Consumer<?>) (o) -> {});
                CoreReflections.method$AttributeInstance$setBaseValue.invoke(attributeInstance, config.scale());
                this.packets.add(NetworkReflections.constructor$ClientboundUpdateAttributesPacket0.newInstance(this.entityId, Collections.singletonList(attributeInstance)));
            } catch (ReflectiveOperationException e) {
                CraftEngine.instance().logger().warn("Failed to apply scale attribute", e);
            }
        }
        this.packets.add(FastNMS.INSTANCE.constructor$ClientboundEntityPositionSyncPacket(this.entityId, this.pos.x, this.pos.y, this.pos.z, 0, position.yRot, false));
        this.collider = createCollider(furniture.world(), this.pos, aabb, config.hardCollision(), config.blocksBuilding(), config.canBeHitByProjectile());
        this.part = new FurnitureHitboxPart(this.entityId, aabb, this.pos, false);
        this.despawnPacket = FastNMS.INSTANCE.constructor$ClientboundRemoveEntitiesPacket(new IntArrayList() {{ add(entityId); }});
    }

    @Override
    public List<Collider> colliders() {
        return List.of(this.collider);
    }

    @Override
    public List<FurnitureHitboxPart> parts() {
        return List.of(this.part);
    }

    @Override
    public void show(Player player) {
        List<Object> packets = new ArrayList<>();
        packets.add(FastNMS.INSTANCE.constructor$ClientboundAddEntityPacket(
                this.entityId, UUID.randomUUID(), this.pos.x, player.y() - (this.config.scale() * 4 + 16), this.pos.z, 0, this.yaw,
                MEntityTypes.HAPPY_GHAST, 0, CoreReflections.instance$Vec3$Zero, 0
        ));
        packets.addAll(this.packets);
        player.sendPacket(FastNMS.INSTANCE.constructor$ClientboundBundlePacket(packets), false);
    }

    @Override
    public void hide(Player player) {
        player.sendPacket(this.despawnPacket, false);
    }

    @Override
    public HappyGhastFurnitureHitboxConfig config() {
        return this.config;
    }
}
