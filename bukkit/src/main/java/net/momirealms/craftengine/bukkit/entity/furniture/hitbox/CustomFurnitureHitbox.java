package net.momirealms.craftengine.bukkit.entity.furniture.hitbox;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.momirealms.craftengine.bukkit.nms.FastNMS;
import net.momirealms.craftengine.bukkit.plugin.reflection.minecraft.CoreReflections;
import net.momirealms.craftengine.bukkit.plugin.reflection.minecraft.MAttributeHolders;
import net.momirealms.craftengine.bukkit.plugin.reflection.minecraft.NetworkReflections;
import net.momirealms.craftengine.core.entity.furniture.Collider;
import net.momirealms.craftengine.core.entity.furniture.Furniture;
import net.momirealms.craftengine.core.entity.furniture.hitbox.FurnitureHitboxPart;
import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.util.VersionHelper;
import net.momirealms.craftengine.core.world.Vec3d;
import net.momirealms.craftengine.core.world.WorldPosition;
import net.momirealms.craftengine.core.world.collision.AABB;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public class CustomFurnitureHitbox extends AbstractFurnitureHitBox {
    private final CustomFurnitureHitboxConfig config;
    private final Collider collider;
    private final Object spawnPacket;
    private final Object despawnPacket;
    private final FurnitureHitboxPart part;

    public CustomFurnitureHitbox(Furniture furniture, CustomFurnitureHitboxConfig config) {
        super(furniture, config);
        this.config = config;
        WorldPosition position = furniture.position();
        Vec3d pos = Furniture.getRelativePosition(position, config.position());
        AABB aabb = AABB.makeBoundingBox(pos, config.width(), config.height());
        this.collider = createCollider(furniture.world(), pos, aabb, false, config.blocksBuilding(), config.canBeHitByProjectile());
        int entityId = CoreReflections.instance$Entity$ENTITY_COUNTER.incrementAndGet();
        List<Object> packets = new ArrayList<>(3);
        packets.add(FastNMS.INSTANCE.constructor$ClientboundAddEntityPacket(
                entityId, UUID.randomUUID(), position.x, position.y, position.z, 0, position.yRot,
                config.entityType(), 0, CoreReflections.instance$Vec3$Zero, 0
        ));
        packets.add(FastNMS.INSTANCE.constructor$ClientboundSetEntityDataPacket(entityId, config.cachedValues()));
        if (VersionHelper.isOrAbove1_20_5()) {
            try {
                Object attributeInstance = CoreReflections.constructor$AttributeInstance.newInstance(MAttributeHolders.SCALE, (Consumer<?>) (o) -> {});
                CoreReflections.method$AttributeInstance$setBaseValue.invoke(attributeInstance, config.scale());
                packets.add(NetworkReflections.constructor$ClientboundUpdateAttributesPacket0.newInstance(entityId, Collections.singletonList(attributeInstance)));
            } catch (ReflectiveOperationException e) {
                CraftEngine.instance().logger().warn("Failed to apply scale attribute", e);
            }
        }
        this.spawnPacket = FastNMS.INSTANCE.constructor$ClientboundBundlePacket(packets);
        this.part = new FurnitureHitboxPart(entityId, aabb, pos, false);
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
        player.sendPacket(this.spawnPacket, false);
    }

    @Override
    public void hide(Player player) {
        player.sendPacket(this.despawnPacket, false);
    }

    @Override
    public CustomFurnitureHitboxConfig config() {
        return this.config;
    }
}
