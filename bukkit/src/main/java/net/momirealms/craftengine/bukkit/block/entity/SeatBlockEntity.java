package net.momirealms.craftengine.bukkit.block.entity;

import net.momirealms.craftengine.bukkit.block.behavior.SeatBlockBehavior;
import net.momirealms.craftengine.bukkit.entity.seat.BukkitSeat;
import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.block.entity.BlockEntity;
import net.momirealms.craftengine.core.block.properties.Property;
import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.entity.seat.Seat;
import net.momirealms.craftengine.core.entity.seat.SeatConfig;
import net.momirealms.craftengine.core.entity.seat.SeatOwner;
import net.momirealms.craftengine.core.util.HorizontalDirection;
import net.momirealms.craftengine.core.world.BlockPos;
import net.momirealms.craftengine.core.world.WorldPosition;
import net.momirealms.sparrow.nbt.CompoundTag;

import java.util.Optional;

public class SeatBlockEntity extends BlockEntity implements SeatOwner {
    private final Seat<SeatBlockEntity>[] seats;

    @SuppressWarnings("unchecked")
    public SeatBlockEntity(BlockPos pos, ImmutableBlockState blockState, SeatConfig[] seats) {
        super(BukkitBlockEntityTypes.SEAT, pos, blockState);
        this.seats = new Seat[seats.length];
        for (int i = 0; i < seats.length; i++) {
            this.seats[i] = new BukkitSeat<>(this, seats[i]);
        }
    }

    @Override
    public void saveCustomData(CompoundTag data) {
        data.putString("type", "seat_block_entity");
    }

    @Override
    public void preRemove() {
        for (Seat<SeatBlockEntity> seat : this.seats) {
            seat.destroy();
        }
    }

    public boolean spawnSeat(Player player) {
        Optional<SeatBlockBehavior> seatBehavior = super.blockState.behavior().getAs(SeatBlockBehavior.class);
        if (seatBehavior.isEmpty()) {
            return false;
        }
        float yRot = 0;
        Property<HorizontalDirection> directionProperty = seatBehavior.get().directionProperty();
        if (directionProperty != null) {
            HorizontalDirection direction = super.blockState.get(directionProperty);
            if (direction == HorizontalDirection.NORTH) {
                yRot = 180;
            } else if (direction == HorizontalDirection.EAST) {
                yRot = -90;
            } else if (direction == HorizontalDirection.WEST) {
                yRot = 90;
            }
        }
        for (Seat<SeatBlockEntity> seat : this.seats) {
            if (!seat.isOccupied()) {
                if (seat.spawnSeat(player, new WorldPosition(super.world.world(), super.pos.x() + 0.5, super.pos.y(), super.pos.z() + 0.5, 180 - yRot, getYRot()))) {
                    return true;
                }
            }
        }
        return false;
    }

    private float getYRot() {
        Property<?> facing = super.blockState.owner().value().getProperty("facing");
        if (facing == null || facing.valueClass() != HorizontalDirection.class) return 0;
        HorizontalDirection direction = (HorizontalDirection) super.blockState.get(facing);
        return switch (direction) {
            case NORTH -> 0;
            case SOUTH -> 180;
            case WEST -> 270;
            case EAST -> 90;
        };
    }
}
