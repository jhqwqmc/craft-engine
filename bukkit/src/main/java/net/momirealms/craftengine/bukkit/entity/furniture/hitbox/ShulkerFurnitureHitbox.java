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
import net.momirealms.craftengine.core.util.VersionHelper;
import net.momirealms.craftengine.core.world.WorldPosition;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ShulkerFurnitureHitbox extends AbstractFurnitureHitBox {
    private final ShulkerFurnitureHitboxConfig config;
    private final List<FurnitureHitboxPart> parts;
    private final List<Collider> colliders;
    private final Object spawnPacket;
    private final Object despawnPacket;

    public ShulkerFurnitureHitbox(Furniture furniture, ShulkerFurnitureHitboxConfig config) {
        super(furniture, config);
        this.config = config;
        int[] entityIds = acquireEntityIds(CoreReflections.instance$Entity$ENTITY_COUNTER::incrementAndGet);
        WorldPosition position = furniture.position();
        Quaternionf conjugated = QuaternionUtils.toQuaternionf(0f, (float) Math.toRadians(180 - position.yRot()), 0f).conjugate();
        Vector3f offset = conjugated.transform(new Vector3f(config.position()));
        double x = position.x();
        double y = position.y();
        double z = position.z();
        float yaw = position.yRot();
        double originalY = y + offset.y;
        double integerPart = Math.floor(originalY);
        double fractionalPart = originalY - integerPart;
        double processedY = (fractionalPart >= 0.5) ? integerPart + 1 : originalY;
        List<Object> packets = new ArrayList<>();
        List<Collider> colliders = new ArrayList<>();
        List<FurnitureHitboxPart> parts = new ArrayList<>();

        packets.add(FastNMS.INSTANCE.constructor$ClientboundAddEntityPacket(
                entityIds[0], UUID.randomUUID(), x + offset.x, originalY, z - offset.z, 0, yaw,
                MEntityTypes.ITEM_DISPLAY, 0, CoreReflections.instance$Vec3$Zero, 0
        ));
        packets.add(FastNMS.INSTANCE.constructor$ClientboundAddEntityPacket(
                entityIds[1], UUID.randomUUID(), x + offset.x, processedY, z - offset.z, 0, yaw,
                MEntityTypes.SHULKER, 0, CoreReflections.instance$Vec3$Zero, 0
        ));
        packets.add(FastNMS.INSTANCE.constructor$ClientboundSetEntityDataPacket(entityIds[1], List.copyOf(config.cachedShulkerValues)));
        packets.add(FastNMS.INSTANCE.constructor$ClientboundSetPassengersPacket(entityIds[0], entityIds[1]));

        // fix some special occasions
        if (originalY != processedY) {
            double deltaY = originalY - processedY;
            short ya = (short) (deltaY * 8192);
            try {
                packets.add(NetworkReflections.constructor$ClientboundMoveEntityPacket$Pos.newInstance(
                        entityIds[1], (short) 0, ya, (short) 0, true
                ));
            } catch (ReflectiveOperationException e) {
                CraftEngine.instance().logger().warn("Failed to construct ClientboundMoveEntityPacket$Pos", e);
            }
        }
        if (VersionHelper.isOrAbove1_20_5() && config.scale != 1) {
            try {
                Object attributeInstance = CoreReflections.constructor$AttributeInstance.newInstance(MAttributeHolders.SCALE, (Consumer<?>) (o) -> {});
                CoreReflections.method$AttributeInstance$setBaseValue.invoke(attributeInstance, config.scale);
                packets.add(NetworkReflections.constructor$ClientboundUpdateAttributesPacket0.newInstance(entityIds[1], Collections.singletonList(attributeInstance)));
            } catch (ReflectiveOperationException e) {
                CraftEngine.instance().logger().warn("Failed to apply scale attribute", e);
            }
        }
        config.spawner.accept(entityIds, position.world(), x, y, z, yaw, offset, packets::add, colliders::add, parts::add);
        this.parts = parts;
        this.colliders = colliders;
        this.spawnPacket = FastNMS.INSTANCE.constructor$ClientboundBundlePacket(packets);
        this.despawnPacket = FastNMS.INSTANCE.constructor$ClientboundRemoveEntitiesPacket(new IntArrayList(entityIds));
    }

    @Override
    public List<Collider> colliders() {
        return this.colliders;
    }

    @Override
    public List<FurnitureHitboxPart> parts() {
        return this.parts;
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
    public ShulkerFurnitureHitboxConfig config() {
        return this.config;
    }

    public int[] acquireEntityIds(Supplier<Integer> entityIdSupplier) {
        if (config.interactionEntity) {
            if (config.direction.stepY() != 0) {
                // 展示实体                 // 潜影贝               // 交互实体
                return new int[] {entityIdSupplier.get(), entityIdSupplier.get(), entityIdSupplier.get()};
            } else {
                // 展示实体                 // 潜影贝               // 交互实体1              // 交互实体2
                return new int[] {entityIdSupplier.get(), entityIdSupplier.get(), entityIdSupplier.get(), entityIdSupplier.get()};
            }
        } else {
            // 展示实体                 // 潜影贝
            return new int[] {entityIdSupplier.get(), entityIdSupplier.get()};
        }
    }
}
