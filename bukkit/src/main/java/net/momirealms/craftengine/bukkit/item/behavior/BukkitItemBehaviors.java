package net.momirealms.craftengine.bukkit.item.behavior;

import net.momirealms.craftengine.core.item.behavior.ItemBehaviorType;
import net.momirealms.craftengine.core.item.behavior.ItemBehaviors;
import net.momirealms.craftengine.core.util.Key;

public final class BukkitItemBehaviors extends ItemBehaviors {
    private BukkitItemBehaviors() {}

    public static final ItemBehaviorType BLOCK_ITEM = register(Key.ce("block_item"), BlockItemBehavior.FACTORY);
    public static final ItemBehaviorType ON_LIQUID_BLOCK_ITEM = register(Key.ce("liquid_collision_block_item"), LiquidCollisionBlockItemBehavior.FACTORY);
    public static final ItemBehaviorType FURNITURE_ITEM = register(Key.ce("furniture_item"), FurnitureItemBehavior.FACTORY);
    public static final ItemBehaviorType ON_LIQUID_FURNITURE_ITEM = register(Key.ce("liquid_collision_furniture_item"), LiquidCollisionFurnitureItemBehavior.FACTORY);
    public static final ItemBehaviorType FLINT_AND_STEEL_ITEM = register(Key.ce("flint_and_steel_item"), FlintAndSteelItemBehavior.FACTORY);
    public static final ItemBehaviorType COMPOSTABLE_ITEM = register(Key.ce("compostable_item"), CompostableItemBehavior.FACTORY);
    public static final ItemBehaviorType AXE_ITEM = register(Key.ce("axe_item"), AxeItemBehavior.FACTORY);
    public static final ItemBehaviorType DOUBLE_HIGH_BLOCK_ITEM = register(Key.ce("double_high_block_item"), DoubleHighBlockItemBehavior.FACTORY);
    public static final ItemBehaviorType WALL_BLOCK_ITEM = register(Key.ce("wall_block_item"), WallBlockItemBehavior.FACTORY);
    public static final ItemBehaviorType CEILING_BLOCK_ITEM = register(Key.ce("ceiling_block_item"), CeilingBlockItemBehavior.FACTORY);
    public static final ItemBehaviorType GROUND_BLOCK_ITEM = register(Key.ce("ground_block_item"), GroundBlockItemBehavior.FACTORY);
    public static final ItemBehaviorType MULTI_HIGH_BLOCK_ITEM = register(Key.ce("multi_high_block_item"), MultiHighBlockItemBehavior.FACTORY);

    public static void init() {
    }
}
