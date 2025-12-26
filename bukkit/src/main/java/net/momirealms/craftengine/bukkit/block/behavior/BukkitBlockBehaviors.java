package net.momirealms.craftengine.bukkit.block.behavior;

import net.momirealms.craftengine.core.block.behavior.BlockBehaviorType;
import net.momirealms.craftengine.core.block.behavior.BlockBehaviors;
import net.momirealms.craftengine.core.util.Key;

public class BukkitBlockBehaviors extends BlockBehaviors {
    private BukkitBlockBehaviors() {}

    public static final BlockBehaviorType BUSH_BLOCK = register(BushBlockBehavior.ID, BushBlockBehavior.FACTORY);
    public static final BlockBehaviorType HANGING_BLOCK = register(HangingBlockBehavior.ID, HangingBlockBehavior.FACTORY);
    public static final BlockBehaviorType FALLING_BLOCK = register(FallingBlockBehavior.ID, FallingBlockBehavior.FACTORY);
    public static final BlockBehaviorType LEAVES_BLOCK = register(LeavesBlockBehavior.ID, LeavesBlockBehavior.FACTORY);
    public static final BlockBehaviorType STRIPPABLE_BLOCK = register(StrippableBlockBehavior.ID, StrippableBlockBehavior.FACTORY);
    public static final BlockBehaviorType SAPLING_BLOCK = register(SaplingBlockBehavior.ID, SaplingBlockBehavior.FACTORY);
    public static final BlockBehaviorType ON_LIQUID_BLOCK = register(OnLiquidBlockBehavior.ID, OnLiquidBlockBehavior.FACTORY);
    public static final BlockBehaviorType NEAR_LIQUID_BLOCK = register(NearLiquidBlockBehavior.ID, NearLiquidBlockBehavior.FACTORY);
    public static final BlockBehaviorType CONCRETE_POWDER_BLOCK = register(ConcretePowderBlockBehavior.ID, ConcretePowderBlockBehavior.FACTORY);
    public static final BlockBehaviorType VERTICAL_CROP_BLOCK = register(VerticalCropBlockBehavior.ID, VerticalCropBlockBehavior.FACTORY);
    public static final BlockBehaviorType CROP_BLOCK = register(CropBlockBehavior.ID, CropBlockBehavior.FACTORY);
    public static final BlockBehaviorType GRASS_BLOCK = register(GrassBlockBehavior.ID, GrassBlockBehavior.FACTORY);
    public static final BlockBehaviorType LAMP_BLOCK = register(LampBlockBehavior.ID, LampBlockBehavior.FACTORY);
    public static final BlockBehaviorType TRAPDOOR_BLOCK = register(TrapDoorBlockBehavior.ID, TrapDoorBlockBehavior.FACTORY);
    public static final BlockBehaviorType DOOR_BLOCK = register(DoorBlockBehavior.ID, DoorBlockBehavior.FACTORY);
    public static final BlockBehaviorType STACKABLE_BLOCK = register(StairsBlockBehavior.ID, StackableBlockBehavior.FACTORY);
    public static final BlockBehaviorType STURDY_BASE_BLOCK = register(StairsBlockBehavior.ID, SturdyBaseBlockBehavior.FACTORY);
    public static final BlockBehaviorType FENCE_GATE_BLOCK = register(FenceGateBlockBehavior.ID, FenceGateBlockBehavior.FACTORY);
    public static final BlockBehaviorType SLAB_BLOCK = register(SlabBlockBehavior.ID, SlabBlockBehavior.FACTORY);
    public static final BlockBehaviorType STAIRS_BLOCK = register(StairsBlockBehavior.ID, StairsBlockBehavior.FACTORY);
    public static final BlockBehaviorType PRESSURE_PLATE_BLOCK = register(PressurePlateBlockBehavior.ID, PressurePlateBlockBehavior.FACTORY);
    public static final BlockBehaviorType DOUBLE_HIGH_BLOCK = register(DoubleHighBlockBehavior.ID, DoubleHighBlockBehavior.FACTORY);
    public static final BlockBehaviorType CHANGE_OVER_TIME_BLOCK = register(ChangeOverTimeBlockBehavior.ID, ChangeOverTimeBlockBehavior.FACTORY);
    public static final BlockBehaviorType SIMPLE_STORAGE_BLOCK = register(SimpleParticleBlockBehavior.ID, SimpleStorageBlockBehavior.FACTORY);
    public static final BlockBehaviorType TOGGLEABLE_LAMP_BLOCK = register(ToggleableLampBlockBehavior.ID, ToggleableLampBlockBehavior.FACTORY);
    public static final BlockBehaviorType SOFA_BLOCK = register(SofaBlockBehavior.ID, SofaBlockBehavior.FACTORY);
    public static final BlockBehaviorType BOUNCING_BLOCK = register(BouncingBlockBehavior.ID, BouncingBlockBehavior.FACTORY);
    public static final BlockBehaviorType DIRECTIONAL_ATTACHED_BLOCK = register(DirectionalAttachedBlockBehavior.ID, DirectionalAttachedBlockBehavior.FACTORY);
    public static final BlockBehaviorType LIQUID_FLOWABLE_BLOCK = register(LiquidFlowableBlockBehavior.ID, LiquidFlowableBlockBehavior.FACTORY);
    public static final BlockBehaviorType SIMPLE_PARTICLE_BLOCK = register(SimpleParticleBlockBehavior.ID, SimpleParticleBlockBehavior.FACTORY);
    public static final BlockBehaviorType WALL_TORCH_PARTICLE_BLOCK = register(WallTorchParticleBlockBehavior.ID, WallTorchParticleBlockBehavior.FACTORY);
    public static final BlockBehaviorType FENCE_BLOCK = register(FenceBlockBehavior.ID, FenceBlockBehavior.FACTORY);
    public static final BlockBehaviorType BUTTON_BLOCK = register(ButtonBlockBehavior.ID, ButtonBlockBehavior.FACTORY);
    public static final BlockBehaviorType FACE_ATTACHED_HORIZONTAL_DIRECTIONAL_BLOCK = register(FaceAttachedHorizontalDirectionalBlockBehavior.ID, FaceAttachedHorizontalDirectionalBlockBehavior.FACTORY);
    public static final BlockBehaviorType STEM_BLOCK = register(StemBlockBehavior.ID, StemBlockBehavior.FACTORY);
    public static final BlockBehaviorType ATTACHED_STEM_BLOCK = register(AttachedStemBlockBehavior.ID, AttachedStemBlockBehavior.FACTORY);
    public static final BlockBehaviorType CHIME_BLOCK = register(ChimeBlockBehavior.ID, ChimeBlockBehavior.FACTORY);
    public static final BlockBehaviorType BUDDING_BLOCK = register(BuddingBlockBehavior.ID, BuddingBlockBehavior.FACTORY);
    public static final BlockBehaviorType SEAT_BLOCK = register(SeatBlockBehavior.ID, SeatBlockBehavior.FACTORY);
    public static final BlockBehaviorType SURFACE_SPREADING_BLOCK = register(SurfaceSpreadingBlockBehavior.ID, SurfaceSpreadingBlockBehavior.FACTORY);
    public static final BlockBehaviorType SNOWY_BLOCK = register(SnowyBlockBehavior.ID, SnowyBlockBehavior.FACTORY);
    public static final BlockBehaviorType HANGABLE_BLOCK = register(HangableBlockBehavior.ID, HangableBlockBehavior.FACTORY);
    public static final BlockBehaviorType DROP_EXPERIENCE_BLOCK = register(DropExperienceBlockBehavior.ID, DropExperienceBlockBehavior.FACTORY);
    public static final BlockBehaviorType DROP_EXP_BLOCK = register(Key.from("craftengine:drop_exp_block"), DropExperienceBlockBehavior.FACTORY);
    public static final BlockBehaviorType MULTI_HIGH_BLOCK = register(MultiHighBlockBehavior.ID, MultiHighBlockBehavior.FACTORY);

    public static void init() {
    }
}
