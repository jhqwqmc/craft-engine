package net.momirealms.craftengine.bukkit.api.event;

import net.momirealms.craftengine.bukkit.plugin.user.BukkitServerPlayer;
import net.momirealms.craftengine.core.block.CustomBlock;
import net.momirealms.craftengine.core.block.ImmutableBlockState;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

public final class CustomBlockBreakEvent extends PlayerEvent implements Cancellable {
    private static final HandlerList HANDLER_LIST = new HandlerList();
    private boolean cancelled;
    private final CustomBlock customBlock;
    private final ImmutableBlockState state;
    private final Location location;
    private final Block bukkitBlock;
    private boolean dropItems;
    private final BukkitServerPlayer player;

    public CustomBlockBreakEvent(@NotNull BukkitServerPlayer player,
                                 @NotNull Location location,
                                 @NotNull Block bukkitBlock,
                                 @NotNull ImmutableBlockState state) {
        super(player.platformPlayer());
        this.customBlock = state.owner().value();
        this.state = state;
        this.bukkitBlock = bukkitBlock;
        this.location = location;
        this.dropItems = true;
        this.player = player;
    }

    public BukkitServerPlayer player() {
        return player;
    }

    public boolean dropItems() {
        return dropItems;
    }

    public void setDropItems(boolean dropItems) {
        this.dropItems = dropItems;
    }

    @NotNull
    public Block bukkitBlock() {
        return bukkitBlock;
    }

    @NotNull
    public CustomBlock customBlock() {
        return this.customBlock;
    }

    @NotNull
    public Location location() {
        return this.location.clone();
    }

    @NotNull
    public ImmutableBlockState blockState() {
        return this.state;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    @NotNull
    public HandlerList getHandlers() {
        return getHandlerList();
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }
}
