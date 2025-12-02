package net.momirealms.craftengine.bukkit.entity.furniture.hitbox;

import net.momirealms.craftengine.bukkit.entity.data.BaseEntityData;
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
import java.util.function.Consumer;

public class InteractionFurnitureHitbox extends AbstractFurnitureHitBox {
    private final InteractionFurnitureHitboxConfig config;
    private final Vec3d position;
    private final AABB aabb;
    private final Collider collider;
    private final int interactionId;
    private final Object spawnPacket;

    public InteractionFurnitureHitbox(Furniture furniture, InteractionFurnitureHitboxConfig config) {
        super(furniture, config);
        this.config = config;
        WorldPosition position = furniture.position();
        this.position = Furniture.getRelativePosition(position, config.position());
        this.aabb = AABB.fromInteraction(this.position, config.size.x, config.size.y);
        this.collider = createCollider(furniture.world(), this.position, this.aabb, false, config.blocksBuilding(), config.canBeHitByProjectile());
        this.interactionId = CoreReflections.instance$Entity$ENTITY_COUNTER.incrementAndGet();
        List<Object> values = new ArrayList<>(4);
        InteractionEntityData.Height.addEntityDataIfNotDefaultValue(config.size.y, values);
        InteractionEntityData.Width.addEntityDataIfNotDefaultValue(config.size.x, values);
        InteractionEntityData.Responsive.addEntityDataIfNotDefaultValue(config.responsive, values);
        if (config.invisible) {
            BaseEntityData.SharedFlags.addEntityDataIfNotDefaultValue((byte) 0x20, values);
        }
        this.spawnPacket = FastNMS.INSTANCE.constructor$ClientboundBundlePacket(List.of(
                FastNMS.INSTANCE.constructor$ClientboundAddEntityPacket(
                        this.interactionId, UUID.randomUUID(), position.x, position.y, position.z, 0, position.yRot,
                        MEntityTypes.INTERACTION, 0, CoreReflections.instance$Vec3$Zero, 0
                ),
                FastNMS.INSTANCE.constructor$ClientboundSetEntityDataPacket(this.interactionId, values)
        ));
    }

    @Override
    public void show(Player player) {
        player.sendPacket(this.spawnPacket, false);
    }

    @Override
    public AABB[] aabb() {
        return new AABB[] { this.aabb };
    }

    @Override
    public Vec3d position() {
        return this.position;
    }

    @Override
    public List<Collider> colliders() {
        return List.of(this.collider);
    }

    @Override
    public int[] virtualEntityIds() {
        return new int[] { this.interactionId };
    }

    @Override
    public void collectVirtualEntityIds(Consumer<Integer> collector) {
        collector.accept(this.interactionId);
    }

    public InteractionFurnitureHitboxConfig config() {
        return this.config;
    }
}
