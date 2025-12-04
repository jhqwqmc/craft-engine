package net.momirealms.craftengine.bukkit.block.behavior;

import net.momirealms.craftengine.bukkit.block.entity.BukkitBlockEntityTypes;
import net.momirealms.craftengine.bukkit.block.entity.SeatBlockEntity;
import net.momirealms.craftengine.bukkit.plugin.user.BukkitServerPlayer;
import net.momirealms.craftengine.core.block.BlockBehavior;
import net.momirealms.craftengine.core.block.CustomBlock;
import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.block.behavior.BlockBehaviorFactory;
import net.momirealms.craftengine.core.block.behavior.EntityBlockBehavior;
import net.momirealms.craftengine.core.block.entity.BlockEntity;
import net.momirealms.craftengine.core.block.entity.BlockEntityType;
import net.momirealms.craftengine.core.block.properties.Property;
import net.momirealms.craftengine.core.entity.player.InteractionResult;
import net.momirealms.craftengine.core.entity.seat.SeatConfig;
import net.momirealms.craftengine.core.item.context.UseOnContext;
import net.momirealms.craftengine.core.util.HorizontalDirection;
import net.momirealms.craftengine.core.world.BlockPos;
import net.momirealms.craftengine.core.world.CEWorld;

import java.util.Map;

public class SeatBlockBehavior extends BukkitBlockBehavior implements EntityBlockBehavior {
    public static final Factory FACTORY = new Factory();
    private final Property<HorizontalDirection> directionProperty;
    private final SeatConfig[] seats;

    public SeatBlockBehavior(CustomBlock customBlock, Property<HorizontalDirection> directionProperty, SeatConfig[] seats) {
        super(customBlock);
        this.seats = seats;
        this.directionProperty = directionProperty;
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, ImmutableBlockState state) {
        return new SeatBlockEntity(pos, state, this.seats);
    }

    @Override
    public <T extends BlockEntity> BlockEntityType<T> blockEntityType(ImmutableBlockState state) {
        return EntityBlockBehavior.blockEntityTypeHelper(BukkitBlockEntityTypes.SEAT);
    }

    public Property<HorizontalDirection> directionProperty() {
        return this.directionProperty;
    }

    @Override
    public InteractionResult useWithoutItem(UseOnContext context, ImmutableBlockState state) {
        BukkitServerPlayer player = (BukkitServerPlayer) context.getPlayer();
        if (player == null || player.isSecondaryUseActive()) {
            return InteractionResult.PASS;
        }
        player.swingHand(context.getHand());
        CEWorld world = context.getLevel().storageWorld();
        BlockEntity blockEntity = world.getBlockEntityAtIfLoaded(context.getClickedPos());
        if (!(blockEntity instanceof SeatBlockEntity seatBlockEntity)) {
            return InteractionResult.PASS;
        }
        if (seatBlockEntity.spawnSeat(player)) {
            return InteractionResult.SUCCESS_AND_CANCEL;
        } else {
            return InteractionResult.PASS;
        }
    }

    public static class Factory implements BlockBehaviorFactory {

        @SuppressWarnings("unchecked")
        @Override
        public BlockBehavior create(CustomBlock block, Map<String, Object> arguments) {
            Property<HorizontalDirection> directionProperty = null;
            Property<?> facing = block.getProperty("facing");
            if (facing != null && facing.valueClass() == HorizontalDirection.class) {
                directionProperty = (Property<HorizontalDirection>) facing;
            }
            return new SeatBlockBehavior(block, directionProperty, SeatConfig.fromObj(arguments.get("seats")));
        }
    }
}
