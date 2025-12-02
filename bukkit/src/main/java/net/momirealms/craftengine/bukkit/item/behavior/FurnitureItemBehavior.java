package net.momirealms.craftengine.bukkit.item.behavior;

import net.momirealms.craftengine.bukkit.api.event.FurnitureAttemptPlaceEvent;
import net.momirealms.craftengine.bukkit.api.event.FurniturePlaceEvent;
import net.momirealms.craftengine.bukkit.entity.furniture.BukkitFurniture;
import net.momirealms.craftengine.bukkit.entity.furniture.BukkitFurnitureManager;
import net.momirealms.craftengine.bukkit.nms.FastNMS;
import net.momirealms.craftengine.bukkit.plugin.BukkitCraftEngine;
import net.momirealms.craftengine.bukkit.util.DirectionUtils;
import net.momirealms.craftengine.bukkit.util.EventUtils;
import net.momirealms.craftengine.bukkit.util.LocationUtils;
import net.momirealms.craftengine.core.entity.furniture.FurnitureConfig;
import net.momirealms.craftengine.core.entity.furniture.FurnitureDataAccessor;
import net.momirealms.craftengine.core.entity.furniture.FurnitureVariant;
import net.momirealms.craftengine.core.entity.furniture.hitbox.FurnitureHitBox;
import net.momirealms.craftengine.core.entity.furniture.hitbox.FurnitureHitBoxConfig;
import net.momirealms.craftengine.core.entity.player.InteractionResult;
import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.item.behavior.ItemBehavior;
import net.momirealms.craftengine.core.item.behavior.ItemBehaviorFactory;
import net.momirealms.craftengine.core.item.context.UseOnContext;
import net.momirealms.craftengine.core.pack.Pack;
import net.momirealms.craftengine.core.pack.PendingConfigSection;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.context.ContextHolder;
import net.momirealms.craftengine.core.plugin.context.PlayerOptionalContext;
import net.momirealms.craftengine.core.plugin.context.event.EventTrigger;
import net.momirealms.craftengine.core.plugin.context.parameter.DirectContextParameters;
import net.momirealms.craftengine.core.plugin.locale.LocalizedResourceConfigException;
import net.momirealms.craftengine.core.util.*;
import net.momirealms.craftengine.core.world.Vec3d;
import net.momirealms.craftengine.core.world.WorldPosition;
import net.momirealms.craftengine.core.world.collision.AABB;
import net.momirealms.sparrow.nbt.CompoundTag;
import org.bukkit.Location;
import org.bukkit.World;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class FurnitureItemBehavior extends ItemBehavior {
    public static final Factory FACTORY = new Factory();
    private final Key id;

    public FurnitureItemBehavior(Key id) {
        this.id = id;
    }

    public Key furnitureId() {
        return this.id;
    }

    @Override
    public InteractionResult useOnBlock(UseOnContext context) {
        return this.place(context);
    }

    public InteractionResult place(UseOnContext context) {
        Optional<FurnitureConfig> optionalCustomFurniture = BukkitFurnitureManager.instance().furnitureById(this.id);
        if (optionalCustomFurniture.isEmpty()) {
            CraftEngine.instance().logger().warn("Furniture " + this.id + " not found");
            return InteractionResult.FAIL;
        }

        FurnitureConfig customFurniture = optionalCustomFurniture.get();
        FurnitureVariant variant = customFurniture.anyVariant();
        if (variant == null) {
            return InteractionResult.FAIL;
        }

        Player player = context.getPlayer();
        if (player != null && player.isAdventureMode()) {
            return InteractionResult.FAIL;
        }

        Vec3d clickedPosition = context.getClickLocation();

        // trigger event
        org.bukkit.entity.Player bukkitPlayer = player != null ? (org.bukkit.entity.Player) player.platformPlayer() : null;
        World world = (World) context.getLevel().platformWorld();

        // get position and rotation for placement
        Vec3d finalPlacePosition = clickedPosition;
        double furnitureYaw = 180 + (player != null ? player.yRot() : 0);

        Location furnitureLocation = new Location(world, finalPlacePosition.x(), finalPlacePosition.y(), finalPlacePosition.z(), (float) furnitureYaw, 0);
        WorldPosition furniturePos = LocationUtils.toWorldPosition(furnitureLocation);
        List<AABB> aabbs = new ArrayList<>();
        // 收集阻挡的碰撞箱
        for (FurnitureHitBoxConfig<?> hitBoxConfig : variant.hitBoxConfigs()) {
            hitBoxConfig.prepareForPlacement(furniturePos, aabbs::add);
        }
        // 检查方块、实体阻挡
        if (!aabbs.isEmpty()) {
            if (!FastNMS.INSTANCE.checkEntityCollision(context.getLevel().serverWorld(), aabbs.stream().map(it -> FastNMS.INSTANCE.constructor$AABB(it.minX, it.minY, it.minZ, it.maxX, it.maxY, it.maxZ)).toList())) {
                return InteractionResult.FAIL;
            }
        }
        // 检查其他插件兼容性
        if (!BukkitCraftEngine.instance().antiGriefProvider().canPlace(bukkitPlayer, furnitureLocation)) {
            return InteractionResult.FAIL;
        }
        // 触发尝试放置的事件
        if (player != null) {
            FurnitureAttemptPlaceEvent attemptPlaceEvent = new FurnitureAttemptPlaceEvent(bukkitPlayer, customFurniture, variant, furnitureLocation.clone(), context.getHand(), world.getBlockAt(context.getClickedPos().x(), context.getClickedPos().y(), context.getClickedPos().z()));
            if (EventUtils.fireAndCheckCancel(attemptPlaceEvent)) {
                return InteractionResult.FAIL;
            }
        }
        Item<?> item = context.getItem();
        if (ItemUtils.isEmpty(item)) return InteractionResult.FAIL;
        // 获取家具物品的一些属性
        FurnitureDataAccessor dataAccessor = FurnitureDataAccessor.of(new CompoundTag());
        dataAccessor.setVariant(variant.name());
        dataAccessor.setItem(item.copyWithCount(1));
        dataAccessor.setDyedColor(item.dyedColor().orElse(null));
        dataAccessor.setFireworkExplosionColors(item.fireworkExplosion().map(explosion -> explosion.colors().toIntArray()).orElse(null));
        // 放置家具
        BukkitFurniture bukkitFurniture = BukkitFurnitureManager.instance().place(furnitureLocation.clone(), customFurniture, dataAccessor, false);
        // 触发放置事件
        if (player != null) {
            FurniturePlaceEvent placeEvent = new FurniturePlaceEvent(bukkitPlayer, bukkitFurniture, furnitureLocation, context.getHand());
            if (EventUtils.fireAndCheckCancel(placeEvent)) {
                bukkitFurniture.destroy();
                return InteractionResult.FAIL;
            }
        }
        // 触发ce事件
        Cancellable dummy = Cancellable.dummy();
        PlayerOptionalContext functionContext = PlayerOptionalContext.of(player, ContextHolder.builder()
                .withParameter(DirectContextParameters.FURNITURE, bukkitFurniture)
                .withParameter(DirectContextParameters.POSITION, LocationUtils.toWorldPosition(furnitureLocation))
                .withParameter(DirectContextParameters.EVENT, dummy)
                .withParameter(DirectContextParameters.HAND, context.getHand())
                .withParameter(DirectContextParameters.ITEM_IN_HAND, item)
        );
        customFurniture.execute(functionContext, EventTrigger.PLACE);
        if (dummy.isCancelled()) {
            return InteractionResult.SUCCESS_AND_CANCEL;
        }
        // 后续处理
        if (player != null) {
            if (!player.canInstabuild()) {
                item.count(item.count() - 1);
            }
            player.swingHand(context.getHand());
        }
        context.getLevel().playBlockSound(finalPlacePosition, customFurniture.settings().sounds().placeSound());
        return InteractionResult.SUCCESS;
    }

    public static class Factory implements ItemBehaviorFactory {

        @Override
        public ItemBehavior create(Pack pack, Path path, String node, Key key, Map<String, Object> arguments) {
            Object id = arguments.get("furniture");
            if (id == null) {
                throw new LocalizedResourceConfigException("warning.config.item.behavior.furniture.missing_furniture", new IllegalArgumentException("Missing required parameter 'furniture' for furniture_item behavior"));
            }
            if (id instanceof Map<?,?> map) {
                if (map.containsKey(key.toString())) {
                    // 防呆
                    BukkitFurnitureManager.instance().parser().addPendingConfigSection(new PendingConfigSection(pack, path, node, key, MiscUtils.castToMap(map.get(key.toString()), false)));
                } else {
                    BukkitFurnitureManager.instance().parser().addPendingConfigSection(new PendingConfigSection(pack, path, node, key, MiscUtils.castToMap(map, false)));
                }
                return new FurnitureItemBehavior(key);
            } else {
                return new FurnitureItemBehavior(Key.of(id.toString()));
            }
        }
    }
}
