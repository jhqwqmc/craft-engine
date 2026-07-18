package net.momirealms.craftengine.bukkit.api;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.momirealms.craftengine.bukkit.entity.BukkitEntity;
import net.momirealms.craftengine.bukkit.entity.BukkitItemEntity;
import net.momirealms.craftengine.bukkit.entity.BukkitLivingEntity;
import net.momirealms.craftengine.bukkit.item.BukkitItem;
import net.momirealms.craftengine.bukkit.item.BukkitItemManager;
import net.momirealms.craftengine.bukkit.plugin.network.BukkitNetworkManager;
import net.momirealms.craftengine.bukkit.plugin.user.BukkitServerPlayer;
import net.momirealms.craftengine.bukkit.world.BukkitExistingBlock;
import net.momirealms.craftengine.bukkit.world.BukkitWorld;
import net.momirealms.craftengine.bukkit.world.BukkitWorldManager;
import net.momirealms.craftengine.core.util.MiscUtils;
import net.momirealms.craftengine.core.world.CEWorld;
import net.momirealms.craftengine.proxy.bukkit.craftbukkit.entity.CraftEntityProxy;
import net.momirealms.craftengine.proxy.minecraft.server.level.ServerPlayerProxy;
import net.momirealms.craftengine.proxy.minecraft.world.entity.EntityProxy;
import net.momirealms.craftengine.proxy.minecraft.world.entity.LivingEntityProxy;
import net.momirealms.craftengine.proxy.minecraft.world.entity.item.ItemEntityProxy;
import net.momirealms.craftengine.proxy.minecraft.world.entity.player.PlayerProxy;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.Function;

public final class BukkitAdaptor {
    private static final Map<Class<?>, Function<Object, net.momirealms.craftengine.core.entity.Entity>> ENTITY_ADAPTORS = MiscUtils.init(new Object2ObjectOpenHashMap<>(), m -> {
        m.put(ServerPlayerProxy.CLASS, e -> BukkitNetworkManager.instance().getOnlineUser(EntityProxy.INSTANCE.getUUID(e)));
        m.put(PlayerProxy.CLASS, e -> BukkitNetworkManager.instance().getOnlineUser(EntityProxy.INSTANCE.getUUID(e)));
        m.put(LivingEntityProxy.CLASS, BukkitLivingEntity::new);
        m.put(ItemEntityProxy.CLASS, BukkitItemEntity::new);
    });

    private BukkitAdaptor() {}

    /**
     * Adapts a Bukkit Player to a CraftEngine BukkitServerPlayer.
     * This provides access to CraftEngine-specific player functionality and data.
     *
     * @param player the Bukkit Player to adapt, must not be null
     * @return a BukkitServerPlayer instance wrapping the provided player, null if the player is not online
     */
    @Nullable
    public static BukkitServerPlayer adapt(@NotNull final Player player) {
        return BukkitNetworkManager.instance().getOnlineUser(player.getUniqueId());
    }

    /**
     * Adapts a Bukkit World to a CraftEngine BukkitWorld.
     * This enables CraftEngine world operations on Bukkit world instances.
     *
     * @param world the Bukkit World to adapt, must not be null
     * @return the BukkitWorld instance wrapping the provided world
     */
    @NotNull
    public static BukkitWorld adapt(@NotNull final World world) {
        CEWorld ceWorld = BukkitWorldManager.instance().getWorld(world);
        if (ceWorld == null) return new BukkitWorld(world);
        return (BukkitWorld) ceWorld.world;
    }

    /**
     * Adapts a Bukkit Entity to a CraftEngine BukkitEntity.
     * This provides CraftEngine entity functionality for Bukkit entities.
     *
     * @param entity the Bukkit Entity to adapt, must not be null
     * @return a non-null BukkitEntity instance wrapping the provided entity
     */
    @NotNull
    public static BukkitEntity adapt(@NotNull final Entity entity) {
        Object handle = CraftEntityProxy.INSTANCE.getEntity(entity);
        Class<?> clazz = handle.getClass();
        while (clazz != null) {
            Function<Object, net.momirealms.craftengine.core.entity.Entity> adaptor = ENTITY_ADAPTORS.get(clazz);
            if (adaptor != null) {
                return (BukkitEntity) adaptor.apply(handle);
            }
            clazz = clazz.getSuperclass();
        }
        return new BukkitEntity(handle);
    }

    /**
     * Adapts a Bukkit Block to a CraftEngine BukkitExistingBlock.
     * This enables CraftEngine block operations on Bukkit block instances.
     *
     * @param block the Bukkit Block to adapt, must not be null
     * @return a non-null BukkitExistingBlock instance wrapping the provided block
     */
    @NotNull
    public static BukkitExistingBlock adapt(@NotNull final Block block) {
        return new BukkitExistingBlock(block);
    }

    /**
     * Adapts a Bukkit ItemStack to a CraftEngine wrapped item
     *
     * @param item the Bukkit ItemStack to adapt, must not be null
     * @return a non-null Item instance wrapping the provided item
     */
    @NotNull
    public static BukkitItem adapt(@NotNull final ItemStack item) {
        return BukkitItemManager.instance().wrap(item);
    }
}
