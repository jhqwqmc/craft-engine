package net.momirealms.craftengine.bukkit.block.behavior;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.momirealms.craftengine.bukkit.block.BukkitBlockManager;
import net.momirealms.craftengine.bukkit.item.BukkitItemManager;
import net.momirealms.craftengine.bukkit.item.behavior.BlockItemBehavior;
import net.momirealms.craftengine.bukkit.nms.FastNMS;
import net.momirealms.craftengine.bukkit.plugin.reflection.minecraft.CoreReflections;
import net.momirealms.craftengine.bukkit.plugin.reflection.minecraft.MEntityTypes;
import net.momirealms.craftengine.bukkit.util.BlockStateUtils;
import net.momirealms.craftengine.bukkit.util.DirectionUtils;
import net.momirealms.craftengine.bukkit.util.LocationUtils;
import net.momirealms.craftengine.bukkit.world.BukkitWorldManager;
import net.momirealms.craftengine.core.block.BlockBehavior;
import net.momirealms.craftengine.core.block.CustomBlock;
import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.block.behavior.BlockBehaviorFactory;
import net.momirealms.craftengine.core.block.properties.Property;
import net.momirealms.craftengine.core.entity.player.InteractionHand;
import net.momirealms.craftengine.core.entity.player.InteractionResult;
import net.momirealms.craftengine.core.item.CustomItem;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.item.behavior.ItemBehavior;
import net.momirealms.craftengine.core.item.context.PlaceBlockBlockPlaceContext;
import net.momirealms.craftengine.core.util.Direction;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.MiscUtils;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;
import net.momirealms.craftengine.core.world.BlockHitResult;
import net.momirealms.craftengine.core.world.BlockPos;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.stream.Collectors;

public class PlaceBlockBehavior extends FacingTriggerableBlockBehavior {
    public static final Factory FACTORY = new Factory();

    public PlaceBlockBehavior(CustomBlock customBlock, Property<Direction> facing, Property<Boolean> triggered, Set<Key> blocks, boolean whitelistMode) {
        super(customBlock, facing, triggered, blocks, whitelistMode);
    }

    @Override
    protected Object getTickPriority() {
        return CoreReflections.instance$TickPriority$EXTREMELY_LOW;
    }

    @Override
    public void tick(Object thisBlock, Object[] args, Callable<Object> superMethod) throws Exception {
        Object state = args[0];
        Object level = args[1];
        Object pos = args[2];
        tick(state, level, pos);
    }

    @Override
    public void tick(Object state, Object level, Object nmsBlockPos) {
        BlockPos pos = LocationUtils.fromBlockPos(nmsBlockPos);
        ImmutableBlockState blockState = BukkitBlockManager.instance().getImmutableBlockState(BlockStateUtils.blockStateToId(state));
        if (blockState == null || blockState.isEmpty()) return;
        Direction direction = blockState.get(this.facingProperty);
        Direction opposite = direction.opposite();
        BlockPos blockPos = pos.relative(opposite);
        BlockPos blockPos1 = pos.relative(direction);
        getItemAndDoThings(level, blockPos, opposite, itemStack -> {
            if (FastNMS.INSTANCE.method$ItemStack$isEmpty(itemStack)) {
                return false;
            } else {
                Object itemStack1 = FastNMS.INSTANCE.method$ItemStack$getItem(itemStack);
                boolean flag = false;
                if (CoreReflections.clazz$BlockItem.isInstance(itemStack1)) {
                    Object block = FastNMS.INSTANCE.method$BlockItem$getBlock(itemStack1);
                    if (blockCheckByBlockState(FastNMS.INSTANCE.method$Block$defaultState(block))) {
                        Object blockHitResult = FastNMS.INSTANCE.constructor$BlockHitResult(
                                FastNMS.INSTANCE.method$BlockPos$getCenter(LocationUtils.toBlockPos(blockPos1)),
                                DirectionUtils.toNMSDirection(opposite),
                                LocationUtils.toBlockPos(blockPos1),
                                false
                        );
                        Object placeBlockBlockPlaceContext = FastNMS.INSTANCE.constructor$PlaceBlockBlockPlaceContext(
                                level, CoreReflections.instance$InteractionHand$MAIN_HAND, itemStack, blockHitResult
                        );
                        Object interactionResult = FastNMS.INSTANCE.method$BlockItem$place(itemStack1, placeBlockBlockPlaceContext);
                        flag = FastNMS.INSTANCE.method$InteractionResult$consumesAction(interactionResult);
                    }
                }
                if (!flag) {
                    Item<ItemStack> item = BukkitItemManager.instance().wrap(FastNMS.INSTANCE.method$CraftItemStack$asCraftMirror(itemStack));
                    Optional<CustomItem<ItemStack>> optionalCustomItem = item.getCustomItem();
                    if (optionalCustomItem.isPresent()) {
                        CustomItem<ItemStack> customItem = optionalCustomItem.get();
                        for (ItemBehavior itemBehavior : customItem.behaviors()) {
                            if (itemBehavior instanceof BlockItemBehavior blockItemBehavior) {
                                if (!blockCheckByKey(blockItemBehavior.block())) continue;
                                BlockHitResult hitResult = new BlockHitResult(
                                        LocationUtils.toVec3d(blockPos1),
                                        opposite,
                                        blockPos1,
                                        false
                                );
                                PlaceBlockBlockPlaceContext context = new PlaceBlockBlockPlaceContext(
                                        BukkitWorldManager.instance().wrap(level),
                                        InteractionHand.MAIN_HAND,
                                        BukkitItemManager.instance().wrap(FastNMS.INSTANCE.method$CraftItemStack$asCraftMirror(itemStack)),
                                        hitResult
                                );
                                InteractionResult result = blockItemBehavior.place(context);
                                if (result.success()) {
                                    return true;
                                }
                            }
                        }
                    }
                    double d = FastNMS.INSTANCE.method$EntityType$getHeight(MEntityTypes.ITEM) / 2.0;
                    double d1 = blockPos1.x() + 0.5;
                    double d2 = blockPos1.y() + 0.5 - d;
                    double d3 = blockPos1.z() + 0.5;
                    Object itemEntity = FastNMS.INSTANCE.constructor$ItemEntity(level, d1, d2, d3, itemStack);
                    FastNMS.INSTANCE.method$ItemEntity$setDefaultPickUpDelay(itemEntity);
                    FastNMS.INSTANCE.method$LevelWriter$addFreshEntity(level, itemEntity);
                }
                return true;
            }
        });

    }

    @SuppressWarnings("UnusedReturnValue")
    private static boolean getItemAndDoThings(Object level, BlockPos blockPos, Direction direction, Function<Object, Boolean> function) {
        for (Object container : getContainersAt(level, blockPos)) {
            boolean flag = FastNMS.INSTANCE.method$HopperBlockEntity$getSlots(container, DirectionUtils.toNMSDirection(direction)).anyMatch(i -> {
                Object itemStack = FastNMS.INSTANCE.method$Container$removeItem(container, i, 1);
                if (!FastNMS.INSTANCE.method$ItemStack$isEmpty(itemStack)) {
                    boolean flag1 = function.apply(FastNMS.INSTANCE.method$ItemStack$copy(itemStack));
                    if (flag1) {
                        FastNMS.INSTANCE.method$Container$setChanged(container);
                    } else {
                        FastNMS.INSTANCE.method$Container$setItem(container, i, itemStack);
                    }

                    return true;
                } else {
                    return false;
                }
            });
            if (flag) {
                return true;
            }
        }
        Object itemAt = getItemAt(level, LocationUtils.toBlockPos(blockPos));
        if (itemAt != null) {
            Object item = FastNMS.INSTANCE.method$ItemEntity$getItem(itemAt);
            if (!FastNMS.INSTANCE.method$ItemStack$isEmpty(item)) {
                boolean flag = function.apply(FastNMS.INSTANCE.method$ItemStack$copyWithCount(item, 1));
                if (flag) {
                    FastNMS.INSTANCE.method$ItemStack$shrink(item, 1);
                    if (FastNMS.INSTANCE.method$ItemStack$getCount(item) <= 0) {
                        FastNMS.INSTANCE.method$Entity$discard(itemAt);
                    }
                }

                return true;
            }
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    public static List<Object> getContainersAt(Object level, BlockPos blockPos) {
        Object nmsBlockPos = LocationUtils.toBlockPos(blockPos);
        Object blockState = FastNMS.INSTANCE.method$BlockGetter$getBlockState(level, nmsBlockPos);
        Object block = FastNMS.INSTANCE.method$BlockState$getBlock(blockState);
        if (CoreReflections.clazz$WorldlyContainerHolder.isInstance(block)) {
            Object container = FastNMS.INSTANCE.method$WorldlyContainerHolder$getContainer(block, blockState, level, nmsBlockPos);
            if (container != null) {
                return List.of(container);
            }
        } else if (FastNMS.INSTANCE.method$BlockStateBase$hasBlockEntity(blockState)) {
            Object blockEntity = FastNMS.INSTANCE.method$BlockGetter$getBlockEntity(level, nmsBlockPos);
            if (CoreReflections.clazz$Container.isInstance(blockEntity)) {
                if (!(CoreReflections.clazz$ChestBlockEntity.isInstance(blockEntity)) || !(CoreReflections.clazz$ChestBlock.isInstance(block))) {
                    return List.of(blockEntity);
                }
                Object container = FastNMS.INSTANCE.method$ChestBlock$getContainer(block, blockState, level, nmsBlockPos, true);
                if (container != null) {
                    return List.of(container);
                }
            }
        }
        List<Object> list = new ArrayList<>();
        for (Object entity : (List<Object>) FastNMS.INSTANCE.method$EntityGetter$getEntities(
                level, null, blockAABB(nmsBlockPos),
                entity -> CoreReflections.clazz$Container.isInstance(entity) && FastNMS.INSTANCE.method$Entity$isAlive(entity))
        ) {
            if (CoreReflections.clazz$Container.isInstance(entity)) {
                list.add(entity);
            }
        }
        return list;
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public static Object getItemAt(Object level, Object blockPos) {
        List<Object> entitiesOfClass = (List<Object>) FastNMS.INSTANCE.method$EntityGetter$getEntitiesOfClass(
                level, CoreReflections.clazz$ItemEntity, blockAABB(blockPos), FastNMS.INSTANCE::method$Entity$isAlive
        );
        return entitiesOfClass.isEmpty() ? null : entitiesOfClass.getFirst();
    }

    private static Object blockAABB(Object blockPos) {
        return FastNMS.INSTANCE.method$AABB$ofSize(FastNMS.INSTANCE.method$BlockPos$getCenter(blockPos), 0.9999999, 0.9999999, 0.9999999);
    }

    public static class Factory implements BlockBehaviorFactory {

        @Override
        @SuppressWarnings("unchecked")
        public BlockBehavior create(CustomBlock block, Map<String, Object> arguments) {
            Property<Direction> facing = (Property<Direction>) ResourceConfigUtils.requireNonNullOrThrow(block.getProperty("facing"), "warning.config.block.behavior.place_block.missing_facing");
            Property<Boolean> triggered = (Property<Boolean>) ResourceConfigUtils.requireNonNullOrThrow(block.getProperty("triggered"), "warning.config.block.behavior.place_block.missing_triggered");
            boolean whitelistMode = (boolean) arguments.getOrDefault("whitelist", false);
            Set<Key> blocks = MiscUtils.getAsStringList(arguments.get("blocks")).stream().map(Key::of).collect(Collectors.toCollection(ObjectOpenHashSet::new));
            if (blocks.isEmpty() && !whitelistMode) {
                blocks = FacingTriggerableBlockBehavior.DEFAULT_BLACKLIST_BLOCKS;
            }
            return new PlaceBlockBehavior(block, facing, triggered, blocks, whitelistMode);
        }
    }
}
