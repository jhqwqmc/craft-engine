package net.momirealms.craftengine.bukkit.entity.furniture.element;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.ints.IntList;
import net.momirealms.craftengine.bukkit.nms.FastNMS;
import net.momirealms.craftengine.bukkit.plugin.reflection.minecraft.CoreReflections;
import net.momirealms.craftengine.bukkit.plugin.reflection.minecraft.MEntityTypes;
import net.momirealms.craftengine.core.entity.furniture.Furniture;
import net.momirealms.craftengine.core.entity.furniture.FurnitureColorSource;
import net.momirealms.craftengine.core.entity.furniture.element.FurnitureElement;
import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.world.Vec3d;
import net.momirealms.craftengine.core.world.WorldPosition;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public class ArmorStandFurnitureElement implements FurnitureElement {
    private final ArmorStandFurnitureElementConfig config;
    public final int entityId;
    private final FurnitureColorSource colorSource;
    public final Object cachedSpawnPacket;
    public final Object cachedDespawnPacket;

    public ArmorStandFurnitureElement(Furniture furniture, ArmorStandFurnitureElementConfig config) {
        this.config = config;
        this.entityId = CoreReflections.instance$Entity$ENTITY_COUNTER.incrementAndGet();
        WorldPosition furniturePos = furniture.position();
        Vec3d position = Furniture.getRelativePosition(furniturePos, config.position());
        this.cachedSpawnPacket = FastNMS.INSTANCE.constructor$ClientboundAddEntityPacket(
                entityId, UUID.randomUUID(), position.x, position.y, position.z,
                furniturePos.xRot, furniturePos.yRot, MEntityTypes.ARMOR_STAND, 0, CoreReflections.instance$Vec3$Zero, furniturePos.yRot
        );
        this.colorSource = furniture.dataAccessor.getColorSource();
        this.cachedDespawnPacket = FastNMS.INSTANCE.constructor$ClientboundRemoveEntitiesPacket(IntList.of(entityId));
    }

    @Override
    public void show(Player player) {
        player.sendPackets(List.of(this.cachedSpawnPacket, FastNMS.INSTANCE.constructor$ClientboundSetEntityDataPacket(this.entityId, this.config.metadata.apply(player))), false);
        player.sendPacket(FastNMS.INSTANCE.constructor$ClientboundSetEquipmentPacket(this.entityId, List.of(
                Pair.of(CoreReflections.instance$EquipmentSlot$HEAD, this.config.item(player, this.colorSource).getLiteralObject())
        )), false);
    }

    @Override
    public void hide(Player player) {
        player.sendPacket(this.cachedDespawnPacket, false);
    }

    @Override
    public int[] virtualEntityIds() {
        return new int[] {this.entityId};
    }

    @Override
    public void collectVirtualEntityId(Consumer<Integer> collector) {
        collector.accept(this.entityId);
    }
}
