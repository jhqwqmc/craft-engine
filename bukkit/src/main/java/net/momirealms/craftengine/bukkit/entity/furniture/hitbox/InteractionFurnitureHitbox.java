package net.momirealms.craftengine.bukkit.entity.furniture.hitbox;

import net.momirealms.craftengine.bukkit.entity.data.InteractionEntityData;
import net.momirealms.craftengine.bukkit.nms.FastNMS;
import net.momirealms.craftengine.bukkit.plugin.reflection.minecraft.CoreReflections;
import net.momirealms.craftengine.bukkit.plugin.reflection.minecraft.MEntityTypes;
import net.momirealms.craftengine.core.entity.furniture.Collider;
import net.momirealms.craftengine.core.entity.furniture.Furniture;
import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.world.Vec3d;
import net.momirealms.craftengine.core.world.WorldPosition;
import net.momirealms.craftengine.core.world.collision.AABB;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class InteractionFurnitureHitbox extends AbstractFurnitureHitBox {
    private final InteractionFurnitureHitboxConfig config;
    private final Vec3d position;
    private final AABB aabb;
    private final Collider collider;
    private final int[] entityIds;
    private final Object spawnPacket;
    private final Object despawnPacket;

    public InteractionFurnitureHitbox(Furniture furniture, InteractionFurnitureHitboxConfig config) {
        super(furniture, config);
        this.config = config;
        WorldPosition position = furniture.position();
        this.position = Furniture.getRelativePosition(position, config.position());
        this.aabb = AABB.fromInteraction(this.position, config.size.x, config.size.y);
        this.collider = createCollider(furniture.world(), this.position, this.aabb, false, config.blocksBuilding(), config.canBeHitByProjectile());
        int interactionId = CoreReflections.instance$Entity$ENTITY_COUNTER.incrementAndGet();
        this.entityIds = new int[] {interactionId};
        List<Object> values = new ArrayList<>(3);
        InteractionEntityData.Height.addEntityDataIfNotDefaultValue(config.size.y, values);
        InteractionEntityData.Width.addEntityDataIfNotDefaultValue(config.size.x, values);
        InteractionEntityData.Responsive.addEntityDataIfNotDefaultValue(config.responsive, values);
        this.spawnPacket = FastNMS.INSTANCE.constructor$ClientboundBundlePacket(List.of(
                FastNMS.INSTANCE.constructor$ClientboundAddEntityPacket(
                        interactionId, UUID.randomUUID(), position.x, position.y, position.z, 0, position.yRot,
                        MEntityTypes.INTERACTION, 0, CoreReflections.instance$Vec3$Zero, 0
                ),
                FastNMS.INSTANCE.constructor$ClientboundSetEntityDataPacket(interactionId, values)
        ));
        this.despawnPacket = createDespawnPacket(this.entityIds);
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
    public AABB aabb() {
        return this.aabb;
    }

    @Override
    public Vec3d position() {
        return this.position;
    }

    @Override
    public Collider collider() {
        return this.collider;
    }

    @Override
    public int[] virtualEntityIds() {
        return this.entityIds;
    }

    public InteractionFurnitureHitboxConfig config() {
        return this.config;
    }
}
