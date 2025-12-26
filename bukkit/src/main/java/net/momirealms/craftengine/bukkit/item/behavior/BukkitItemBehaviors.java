package net.momirealms.craftengine.bukkit.item.behavior;

import net.momirealms.craftengine.core.item.behavior.ItemBehaviorType;
import net.momirealms.craftengine.core.item.behavior.ItemBehaviors;

public final class BukkitItemBehaviors extends ItemBehaviors {
    private BukkitItemBehaviors() {}

    public static final ItemBehaviorType BLOCK_ITEM = register(BlockItemBehavior.ID, BlockItemBehavior.FACTORY);
    public static final ItemBehaviorType ON_LIQUID_BLOCK_ITEM = register(LiquidCollisionBlockItemBehavior.ID, LiquidCollisionBlockItemBehavior.FACTORY);
    public static final ItemBehaviorType FURNITURE_ITEM = register(FurnitureItemBehavior.ID, FurnitureItemBehavior.FACTORY);
    public static final ItemBehaviorType ON_LIQUID_FURNITURE_ITEM = register(LiquidCollisionFurnitureItemBehavior.ID, LiquidCollisionFurnitureItemBehavior.FACTORY);
    public static final ItemBehaviorType FLINT_AND_STEEL_ITEM = register(FlintAndSteelItemBehavior.ID, FlintAndSteelItemBehavior.FACTORY);
    public static final ItemBehaviorType COMPOSTABLE_ITEM = register(CompostableItemBehavior.ID, CompostableItemBehavior.FACTORY);
    public static final ItemBehaviorType AXE_ITEM = register(AxeItemBehavior.ID, AxeItemBehavior.FACTORY);
    public static final ItemBehaviorType DOUBLE_HIGH_BLOCK_ITEM = register(DoubleHighBlockItemBehavior.ID, DoubleHighBlockItemBehavior.FACTORY);
    public static final ItemBehaviorType WALL_BLOCK_ITEM = register(WallBlockItemBehavior.ID, WallBlockItemBehavior.FACTORY);
    public static final ItemBehaviorType CEILING_BLOCK_ITEM = register(CeilingBlockItemBehavior.ID, CeilingBlockItemBehavior.FACTORY);
    public static final ItemBehaviorType GROUND_BLOCK_ITEM = register(GroundBlockItemBehavior.ID, GroundBlockItemBehavior.FACTORY);
    public static final ItemBehaviorType MULTI_HIGH_BLOCK_ITEM = register(MultiHighBlockItemBehavior.ID, MultiHighBlockItemBehavior.FACTORY);

    public static void init() {
    }
}
