package net.momirealms.craftengine.bukkit.api.event;

import org.bukkit.event.HandlerList;
import org.bukkit.event.server.ServerEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an event that is fired immediately before the server shuts down.
 * <p>
 * This event provides an opportunity to perform operations involving CraftEngine API
 * during the server's shutdown sequence.
 * <p>
 * <b>Priority Guidelines:</b><br>
 * - If you need to interact with CraftEngine components <b>before</b> they are unloaded,
 *   set your listener priority to {@link org.bukkit.event.EventPriority#LOW} or
 *   {@link org.bukkit.event.EventPriority#LOWEST}.<br>
 * - If your operations should run <b>after</b> CraftEngine has been unloaded
 *   (e.g., for logging, metrics, or final cleanup), use
 *   {@link org.bukkit.event.EventPriority#HIGH},
 *   {@link org.bukkit.event.EventPriority#HIGHEST}, or
 *   {@link org.bukkit.event.EventPriority#MONITOR}.
 *
 * @see org.bukkit.event.EventPriority
 */
public final class ServerPreShutdownEvent extends ServerEvent {
    private static final HandlerList HANDLER_LIST = new HandlerList();

    public ServerPreShutdownEvent() {
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    @NotNull
    public HandlerList getHandlers() {
        return getHandlerList();
    }
}
