package net.momirealms.craftengine.bukkit.entity.furniture.hitbox;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.momirealms.craftengine.bukkit.entity.data.BaseEntityData;
import net.momirealms.craftengine.bukkit.entity.data.InteractionEntityData;
import net.momirealms.craftengine.bukkit.nms.FastNMS;
import net.momirealms.craftengine.bukkit.plugin.reflection.minecraft.CoreReflections;
import net.momirealms.craftengine.bukkit.plugin.reflection.minecraft.MEntityTypes;
import net.momirealms.craftengine.core.entity.furniture.Collider;
import net.momirealms.craftengine.core.entity.furniture.Furniture;
import net.momirealms.craftengine.core.entity.furniture.hitbox.FurnitureHitboxPart;
import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.world.Vec3d;
import net.momirealms.craftengine.core.world.WorldPosition;
import net.momirealms.craftengine.core.world.collision.AABB;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class InteractionFurnitureHitbox extends AbstractFurnitureHitBox {
    private final InteractionFurnitureHitboxConfig config;
    private final Collider collider;
    private final Object spawnPacket;
    private final Object despawnPacket;
    private final FurnitureHitboxPart part;

    public InteractionFurnitureHitbox(Furniture furniture, InteractionFurnitureHitboxConfig config) {
        super(furniture, config);
        this.config = config;
        WorldPosition position = furniture.position();
        Vec3d pos = Furniture.getRelativePosition(position, config.position());
        AABB aabb = AABB.fromInteraction(pos, config.size.x, config.size.y);
        this.collider = createCollider(furniture.world(), pos, aabb, false, config.blocksBuilding(), config.canBeHitByProjectile());
        int interactionId = CoreReflections.instance$Entity$ENTITY_COUNTER.incrementAndGet();
        List<Object> values = new ArrayList<>(4);
        InteractionEntityData.Height.addEntityDataIfNotDefaultValue(config.size.y, values);
        InteractionEntityData.Width.addEntityDataIfNotDefaultValue(config.size.x, values);
        InteractionEntityData.Responsive.addEntityDataIfNotDefaultValue(config.responsive, values);
        if (config.invisible) {
            BaseEntityData.SharedFlags.addEntityDataIfNotDefaultValue((byte) 0x20, values);
        }
        this.spawnPacket = FastNMS.INSTANCE.constructor$ClientboundBundlePacket(List.of(
                FastNMS.INSTANCE.constructor$ClientboundAddEntityPacket(
                        interactionId, UUID.randomUUID(), position.x, position.y, position.z, 0, position.yRot,
                        MEntityTypes.INTERACTION, 0, CoreReflections.instance$Vec3$Zero, 0
                ),
                FastNMS.INSTANCE.constructor$ClientboundSetEntityDataPacket(interactionId, values)
        ));
        this.part = new FurnitureHitboxPart(interactionId, aabb, pos);
        this.despawnPacket = FastNMS.INSTANCE.constructor$ClientboundRemoveEntitiesPacket(new IntArrayList() {{ add(interactionId); }});
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
    public InteractionFurnitureHitboxConfig config() {
        return this.config;
    }

    @Override
    public void show(Player player) {
        player.sendPacket(this.spawnPacket, false);
    }

    @Override
    public void hide(Player player) {
        player.sendPacket(this.despawnPacket, false);
    }
}
