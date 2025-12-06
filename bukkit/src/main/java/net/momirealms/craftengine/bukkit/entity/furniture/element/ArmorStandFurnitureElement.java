package net.momirealms.craftengine.bukkit.entity.furniture.element;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.ints.IntList;
import net.momirealms.craftengine.bukkit.nms.FastNMS;
import net.momirealms.craftengine.bukkit.plugin.reflection.minecraft.CoreReflections;
import net.momirealms.craftengine.bukkit.plugin.reflection.minecraft.MAttributeHolders;
import net.momirealms.craftengine.bukkit.plugin.reflection.minecraft.MEntityTypes;
import net.momirealms.craftengine.bukkit.world.score.BukkitTeamManager;
import net.momirealms.craftengine.core.entity.furniture.Furniture;
import net.momirealms.craftengine.core.entity.furniture.FurnitureColorSource;
import net.momirealms.craftengine.core.entity.furniture.element.FurnitureElement;
import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.util.VersionHelper;
import net.momirealms.craftengine.core.world.Vec3d;
import net.momirealms.craftengine.core.world.WorldPosition;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public class ArmorStandFurnitureElement implements FurnitureElement {
    private final ArmorStandFurnitureElementConfig config;
    private final FurnitureColorSource colorSource;
    public final Object cachedSpawnPacket;
    public final Object cachedDespawnPacket;
    public final Object cachedScalePacket;
    public final Object cachedTeamPacket;
    public final int entityId;
    public final UUID uuid = UUID.randomUUID();

    public ArmorStandFurnitureElement(Furniture furniture, ArmorStandFurnitureElementConfig config) {
        this.config = config;
        this.entityId = CoreReflections.instance$Entity$ENTITY_COUNTER.incrementAndGet();
        WorldPosition furniturePos = furniture.position();
        Vec3d position = Furniture.getRelativePosition(furniturePos, config.position());
        this.cachedSpawnPacket = FastNMS.INSTANCE.constructor$ClientboundAddEntityPacket(
                this.entityId, this.uuid, position.x, position.y, position.z,
                furniturePos.xRot, furniturePos.yRot, MEntityTypes.ARMOR_STAND, 0, CoreReflections.instance$Vec3$Zero, furniturePos.yRot
        );
        this.colorSource = furniture.dataAccessor.getColorSource();
        this.cachedDespawnPacket = FastNMS.INSTANCE.constructor$ClientboundRemoveEntitiesPacket(IntList.of(this.entityId));
        if (VersionHelper.isOrAbove1_20_5() && config.scale != 1) {
            Object attributeIns = FastNMS.INSTANCE.constructor$AttributeInstance(MAttributeHolders.SCALE, (Consumer<?>) (o) -> {});
            FastNMS.INSTANCE.method$AttributeInstance$setBaseValue(attributeIns, config.scale());
            this.cachedScalePacket = FastNMS.INSTANCE.constructor$ClientboundUpdateAttributesPacket(this.entityId, Collections.singletonList(attributeIns));
        } else {
            this.cachedScalePacket = null;
        }
        Object teamPacket = null;
        if (config.glowColor != null) {
            Object teamByColor = BukkitTeamManager.instance().getTeamByColor(config.glowColor);
            if (teamByColor != null) {
                teamPacket = FastNMS.INSTANCE.method$ClientboundSetPlayerTeamPacket$createMultiplePlayerPacket(teamByColor, List.of(this.uuid.toString()), true);
            }
        }
        this.cachedTeamPacket = teamPacket;
    }

    @Override
    public void show(Player player) {
        player.sendPackets(List.of(this.cachedSpawnPacket, FastNMS.INSTANCE.constructor$ClientboundSetEntityDataPacket(this.entityId, this.config.metadata.apply(player))), false);
        player.sendPacket(FastNMS.INSTANCE.constructor$ClientboundSetEquipmentPacket(this.entityId, List.of(
                Pair.of(CoreReflections.instance$EquipmentSlot$HEAD, this.config.item(player, this.colorSource).getLiteralObject())
        )), false);
        if (this.cachedScalePacket != null) {
            player.sendPacket(this.cachedScalePacket, false);
        }
        if (this.cachedTeamPacket != null) {
            player.sendPacket(this.cachedTeamPacket, false);
        }
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
