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
        int yRot = 0;
        Optional<SeatBlockBehavior> behavior = super.blockState.behavior().getAs(SeatBlockBehavior.class);
        if (behavior.isEmpty()) return false;
        Property<HorizontalDirection> facing = behavior.get().directionProperty();
        if (facing != null) {
            HorizontalDirection direction = super.blockState.get(facing);
            yRot = switch (direction) {
                case NORTH -> 0;
                case SOUTH -> 180;
                case WEST -> 270;
                case EAST -> 90;
            };
        }
        for (Seat<SeatBlockEntity> seat : this.seats) {
            if (!seat.isOccupied()) {
                if (seat.spawnSeat(player, new WorldPosition(super.world.world(), super.pos.x() + 0.5, super.pos.y(), super.pos.z() + 0.5, 0, yRot))) {
                    return true;
                }
            }
        }
        return false;
    }
}
