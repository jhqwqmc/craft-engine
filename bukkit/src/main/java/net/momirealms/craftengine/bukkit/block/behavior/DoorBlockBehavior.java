package net.momirealms.craftengine.bukkit.block.behavior;

import net.momirealms.craftengine.bukkit.block.BukkitBlockManager;
import net.momirealms.craftengine.bukkit.util.BlockStateUtils;
import net.momirealms.craftengine.bukkit.util.LocationUtils;
import net.momirealms.craftengine.bukkit.util.Reflections;
import net.momirealms.craftengine.bukkit.world.BukkitWorld;
import net.momirealms.craftengine.core.block.CustomBlock;
import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.block.behavior.BlockBehaviorFactory;
import net.momirealms.craftengine.core.block.properties.BooleanProperty;
import net.momirealms.craftengine.core.block.properties.EnumProperty;
import net.momirealms.craftengine.core.util.*;
import net.momirealms.craftengine.core.world.World;
import net.momirealms.craftengine.shared.block.BlockBehavior;
import org.bukkit.event.block.BlockRedstoneEvent;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.Callable;

public class DoorBlockBehavior extends BlockBehavior {
    public static final Factory FACTORY = new Factory();
    public final EnumProperty<HorizontalDirection> directionProperty;
    public final BooleanProperty openProperty;
    public final EnumProperty<DoorHingeSide> hingeProperty;
    public final BooleanProperty poweredProperty;
    public final EnumProperty<DoubleBlockHalf> halfProperty;
    public final Key openSound;
    public final Key closeSound;
    public final Boolean canOpenByHand;
    public final Key upperBlock;

    public DoorBlockBehavior(EnumProperty<HorizontalDirection> directionProperty, BooleanProperty openProperty, EnumProperty<DoorHingeSide> hingeProperty, BooleanProperty poweredProperty, EnumProperty<DoubleBlockHalf> halfProperty, Key openSound, Key closeSound, Boolean canOpenByHand, Key upperBlock) {
        this.directionProperty = directionProperty;
        this.openProperty = openProperty;
        this.hingeProperty = hingeProperty;
        this.poweredProperty = poweredProperty;
        this.halfProperty = halfProperty;
        this.openSound = openSound;
        this.closeSound = closeSound;
        this.canOpenByHand = canOpenByHand;
        this.upperBlock = upperBlock;
    }

    public HorizontalDirection getDirection(ImmutableBlockState state) {
        return state.get(this.directionProperty);
    }

    public Boolean getOpen(ImmutableBlockState state) {
        return state.get(this.openProperty);
    }

    public DoorHingeSide getHinge(ImmutableBlockState state) {
        return state.get(this.hingeProperty);
    }

    public Boolean getPowered(ImmutableBlockState state) {
        return state.get(this.poweredProperty);
    }

    public DoubleBlockHalf getHalf(ImmutableBlockState state) {
        return state.get(this.halfProperty);
    }

    @Override
    public Object updateShape(Object thisBlock, Object[] args, Callable<Object> superMethod) throws Exception {
        Object level;
        Object blockPos;
        Object neighborState;
        Object state = args[0];
        if (VersionHelper.isVersionNewerThan1_21_2()) {
            level = args[1];
            blockPos = args[3];
            neighborState = args[6];
        } else {
            level = args[3];
            blockPos = args[4];
            neighborState = args[2];
        }
        ImmutableBlockState thisState = BukkitBlockManager.instance().getImmutableBlockState(BlockStateUtils.blockStateToId(state));
        ImmutableBlockState thisNeighborState = BukkitBlockManager.instance().getImmutableBlockState(BlockStateUtils.blockStateToId(neighborState));
        if (thisState != null && thisNeighborState != null) {
            DoubleBlockHalf doubleBlockHalf = getHalf(thisState);
            if (getDirection(thisState).toDirection().axis() == Direction.Axis.Y && doubleBlockHalf == DoubleBlockHalf.LOWER == (getDirection(thisState).toDirection() == Direction.UP)) {
                return thisNeighborState.behavior() instanceof DoorBlockBehavior && getHalf(thisNeighborState) != doubleBlockHalf ? thisNeighborState.with(this.halfProperty, doubleBlockHalf) : Reflections.instance$Blocks$AIR$defaultState;
            } else {
                return doubleBlockHalf == DoubleBlockHalf.LOWER && getDirection(thisState).toDirection() == Direction.DOWN && !canSurvive(state, level, blockPos) ? Reflections.instance$Blocks$AIR$defaultState : super.updateShape(thisBlock, args, superMethod);
            }
        }
        return super.updateShape(thisBlock, args, superMethod);
    }

    @Override
    public void neighborChanged(Object thisBlock, Object[] args, Callable<Object> superMethod) throws Exception {
        Object state = args[0];
        Object level = args[1];
        Object pos = args[2];
        ImmutableBlockState thisState = BukkitBlockManager.instance().getImmutableBlockState(BlockStateUtils.blockStateToId(state));
        if (thisState == null) return;
        Object otherHalf = Reflections.method$BlockPos$relative.invoke(pos, getHalf(thisState) == DoubleBlockHalf.LOWER ? Reflections.instance$Direction$UP : Reflections.instance$Direction$DOWN);
        org.bukkit.block.Block bukkitBlock = (org.bukkit.block.Block) Reflections.method$CraftBlock$at.invoke(null, level, pos);
        org.bukkit.block.Block blockTop = (org.bukkit.block.Block) Reflections.method$CraftBlock$at.invoke(null, level, otherHalf);

        int power = bukkitBlock.getBlockPower();
        int powerTop = blockTop.getBlockPower();
        if (powerTop > power) {
            power = powerTop;
        }


        int oldPower = getPowered(thisState) ? 15 : 0;
        if (oldPower == 0 ^ power == 0) {
            BlockRedstoneEvent event = new BlockRedstoneEvent(bukkitBlock, oldPower, power);
            event.callEvent();
            boolean flag = event.getNewCurrent() > 0;
            if (flag != getOpen(thisState)) {
                World world = new BukkitWorld((org.bukkit.World) Reflections.method$Level$getCraftWorld.invoke(level));
                float pitch = (float)Reflections.method$RandomSource$nextFloat.invoke(Reflections.method$RandomSource$getRandom.invoke(level)) * 0.1F + 0.9F;
                Key sound = flag ? this.openSound : this.closeSound;
                Field gameEvent = flag ? Reflections.field$GameEvent$BLOCK_OPEN : Reflections.field$GameEvent$BLOCK_CLOSE;
                world.playBlockSound(LocationUtils.toVec3d(bukkitBlock.getLocation()), sound, 1.0f, pitch);
                Reflections.method$LevelAccessor$gameEvent.invoke(level, null, gameEvent, pos);
            }
            Reflections.method$Level$setBlock.invoke(level, pos, thisState.with(this.poweredProperty, flag).with(this.openProperty, flag).customBlockState().handle(), 2);
        }
    }

    @Override
    public boolean canSurvive(Object thisBlock, Object[] args, Callable<Object> superMethod) throws Exception {
        Object state = args[0];
        Object world = args[1];
        Object pos = args[2];
        return canSurvive(state, world, pos);
    }

    private boolean canSurvive(Object state, Object world, Object pos) throws Exception {
        ImmutableBlockState thisState = BukkitBlockManager.instance().getImmutableBlockState(BlockStateUtils.blockStateToId(state));
        if (thisState == null) return false;
        return getHalf(thisState) == DoubleBlockHalf.LOWER
                ? (boolean) Reflections.method$BlockBehaviour$BlockStateBase$isFaceSturdy.invoke(state, world, pos, Reflections.instance$Direction$UP)
                : thisState.behavior() == this;
    }

    @SuppressWarnings("unchecked")
    public static class Factory implements BlockBehaviorFactory {

        @Override
        public BlockBehavior create(CustomBlock block, Map<String, Object> arguments) {
            String upperBlock = (String) arguments.get("upper-block");
            Boolean canOpenByHand = (Boolean) arguments.get("can-open-by-hand");
            String openSound = (String) arguments.get("open-sound");
            String closeSound = (String) arguments.get("close-sound");
            EnumProperty<HorizontalDirection> direction = (EnumProperty<HorizontalDirection>) block.getProperty("direction");
            if (direction == null) {
                throw new NullPointerException("direction property not set for block " + block.id());
            }
            BooleanProperty open = (BooleanProperty) block.getProperty("open");
            if (open == null) {
                throw new NullPointerException("open property not set for block " + block.id());
            }
            EnumProperty<DoorHingeSide> hinge = (EnumProperty<DoorHingeSide>) block.getProperty("hinge");
            if (hinge == null) {
                throw new NullPointerException("hinge property not set for block " + block.id());
            }
            BooleanProperty powered = (BooleanProperty) block.getProperty("powered");
            if (powered == null) {
                throw new NullPointerException("powered property not set for block " + block.id());
            }
            EnumProperty<DoubleBlockHalf> half = (EnumProperty<DoubleBlockHalf>) block.getProperty("half");
            if (half == null) {
                throw new NullPointerException("half property not set for block " + block.id());
            }
            return new DoorBlockBehavior(direction, open, hinge, powered, half, Key.of(openSound), Key.of(closeSound), canOpenByHand, Key.of(upperBlock));
        }
    }

}
