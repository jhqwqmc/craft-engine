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
import net.momirealms.craftengine.core.util.QuaternionUtils;
import net.momirealms.craftengine.core.world.Vec3d;
import net.momirealms.craftengine.core.world.WorldPosition;
import net.momirealms.craftengine.core.world.collision.AABB;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public class HappyGhastFurnitureHitbox extends AbstractFurnitureHitBox {
    private final HappyGhastFurnitureHitboxConfig config;
    private final Collider collider;
    private final Object spawnPacket;
    private final Object despawnPacket;
    private final FurnitureHitboxPart part;

    public HappyGhastFurnitureHitbox(Furniture furniture, HappyGhastFurnitureHitboxConfig config) {
        super(furniture, config);
        this.config = config;
        WorldPosition position = furniture.position();
        Quaternionf conjugated = QuaternionUtils.toQuaternionf(0f, (float) Math.toRadians(180 - position.yRot()), 0f).conjugate();
        Vector3f offset = conjugated.transform(new Vector3f(config.position()));
        Vec3d pos = Furniture.getRelativePosition(position, config.position());
        AABB aabb = AABB.fromInteraction(pos, 3 * config.scale(), 3 * config.scale());
        int happyGhastId = CoreReflections.instance$Entity$ENTITY_COUNTER.incrementAndGet();
        List<Object> packets = new ArrayList<>(3);
        packets.add(FastNMS.INSTANCE.constructor$ClientboundAddEntityPacket(
                happyGhastId, UUID.randomUUID(), position.x + offset.x, position.y + offset.y, position.z + offset.z, 0, position.yRot,
                MEntityTypes.HAPPY_GHAST, 0, CoreReflections.instance$Vec3$Zero, 0
        ));
        packets.add(FastNMS.INSTANCE.constructor$ClientboundSetEntityDataPacket(happyGhastId, config.cachedValues()));
        if (config.scale() != 1) {
            try {
                Object attributeInstance = CoreReflections.constructor$AttributeInstance.newInstance(MAttributeHolders.SCALE, (Consumer<?>) (o) -> {});
                CoreReflections.method$AttributeInstance$setBaseValue.invoke(attributeInstance, config.scale());
                packets.add(NetworkReflections.constructor$ClientboundUpdateAttributesPacket0.newInstance(happyGhastId, Collections.singletonList(attributeInstance)));
            } catch (ReflectiveOperationException e) {
                CraftEngine.instance().logger().warn("Failed to apply scale attribute", e);
            }
        }
        this.collider = createCollider(furniture.world(), pos, aabb, config.hardCollision(), config.blocksBuilding(), config.canBeHitByProjectile());
        this.part = new FurnitureHitboxPart(happyGhastId, aabb, pos);
        this.spawnPacket = FastNMS.INSTANCE.constructor$ClientboundBundlePacket(packets);
        this.despawnPacket = FastNMS.INSTANCE.constructor$ClientboundRemoveEntitiesPacket(new IntArrayList() {{ add(happyGhastId); }});
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
    public HappyGhastFurnitureHitboxConfig config() {
        return this.config;
    }
}
