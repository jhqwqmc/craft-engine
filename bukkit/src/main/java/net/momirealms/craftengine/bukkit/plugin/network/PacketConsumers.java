package net.momirealms.craftengine.bukkit.plugin.network;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.ints.IntList;
import net.momirealms.craftengine.bukkit.api.CraftEngineFurniture;
import net.momirealms.craftengine.bukkit.api.event.FurnitureBreakEvent;
import net.momirealms.craftengine.bukkit.api.event.FurnitureInteractEvent;
import net.momirealms.craftengine.bukkit.block.BukkitBlockManager;
import net.momirealms.craftengine.bukkit.entity.furniture.BukkitFurnitureManager;
import net.momirealms.craftengine.bukkit.entity.furniture.LoadedFurniture;
import net.momirealms.craftengine.bukkit.plugin.BukkitCraftEngine;
import net.momirealms.craftengine.bukkit.plugin.user.BukkitServerPlayer;
import net.momirealms.craftengine.bukkit.util.*;
import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.entity.player.InteractionHand;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.config.ConfigManager;
import net.momirealms.craftengine.core.plugin.network.ConnectionState;
import net.momirealms.craftengine.core.plugin.network.NetWorkUser;
import net.momirealms.craftengine.core.util.*;
import net.momirealms.craftengine.core.world.BlockPos;
import net.momirealms.craftengine.core.world.chunk.Palette;
import net.momirealms.craftengine.core.world.chunk.PalettedContainer;
import net.momirealms.craftengine.core.world.chunk.packet.MCSection;
import org.bukkit.*;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.util.RayTraceResult;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;

public class PacketConsumers {
    private static int[] mappings;
    private static IntIdentityList BLOCK_LIST;
    private static IntIdentityList BIOME_LIST;

    public static void init(Map<Integer, Integer> map, int registrySize) {
        mappings = new int[registrySize];
        Arrays.fill(mappings, -1);
        for (int i = 0; i < registrySize; i++) {
            mappings[i] = i;
        }
        for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
            mappings[entry.getKey()] = entry.getValue();
        }
        BLOCK_LIST = new IntIdentityList(registrySize);
        BIOME_LIST = new IntIdentityList(RegistryUtils.currentBiomeRegistrySize());
    }

    public static int remap(int stateId) {
        return mappings[stateId];
    }

    public static final TriConsumer<NetWorkUser, NMSPacketEvent, Object> LEVEL_CHUNK_WITH_LIGHT = (user, event, packet) -> {
        try {
            BukkitServerPlayer player = (BukkitServerPlayer) user;
            Object chunkData = Reflections.field$ClientboundLevelChunkWithLightPacket$chunkData.get(packet);
            byte[] buffer = (byte[]) Reflections.field$ClientboundLevelChunkPacketData$buffer.get(chunkData);
            ByteBuf buf = Unpooled.copiedBuffer(buffer);
            FriendlyByteBuf friendlyByteBuf = new FriendlyByteBuf(buf);
            FriendlyByteBuf newBuf = new FriendlyByteBuf(Unpooled.buffer());
            for (int i = 0, count = player.clientSideSectionCount(); i < count; i++) {
                try {
                    MCSection mcSection = new MCSection(BLOCK_LIST, BIOME_LIST);
                    mcSection.readPacket(friendlyByteBuf);
                    PalettedContainer<Integer> container = mcSection.blockStateContainer();
                    Palette<Integer> palette = container.data().palette();
                    if (palette.canRemap()) {
                        palette.remap(PacketConsumers::remap);
                    } else {
                        for (int j = 0; j < 4096; j ++) {
                            int state = container.get(j);
                            int newState = remap(state);
                            if (newState != state) {
                                container.set(j, newState);
                            }
                        }
                    }
                    mcSection.writePacket(newBuf);
                } catch (Exception e) {
                    break;
                }
            }
            Reflections.field$ClientboundLevelChunkPacketData$buffer.set(chunkData, newBuf.array());
        } catch (Exception e) {
            CraftEngine.instance().logger().warn("Failed to handle ClientboundLevelChunkWithLightPacket", e);
        }
    };

    public static final BiConsumer<NetWorkUser, ByteBufPacketEvent> SECTION_BLOCK_UPDATE = (user, event) -> {
        try {
            FriendlyByteBuf buf = event.getBuffer();
            long pos = buf.readLong();
            int blocks = buf.readVarInt();
            short[] positions = new short[blocks];
            int[] states = new int[blocks];
            for (int i = 0; i < blocks; i++) {
                long k = buf.readVarLong();
                positions[i] = (short) ((int) (k & 4095L));
                states[i] = remap((int) (k >>> 12));
            }
            buf.clear();
            buf.writeVarInt(event.packetID());
            buf.writeLong(pos);
            buf.writeVarInt(blocks);
            for (int i = 0; i < blocks; i ++) {
                buf.writeVarLong((long) states[i] << 12 | positions[i]);
            }
            event.setChanged(true);
        } catch (Exception e) {
            CraftEngine.instance().logger().warn("Failed to handle ClientboundSectionBlocksUpdatePacket", e);
        }
    };

    public static final BiConsumer<NetWorkUser, ByteBufPacketEvent> BLOCK_UPDATE = (user, event) -> {
        try {
            FriendlyByteBuf buf = event.getBuffer();
            BlockPos pos = buf.readBlockPos(buf);
            int before = buf.readVarInt();
            int state = remap(before);
            if (state == before) {
                return;
            }
            event.setChanged(true);
            buf.clear();
            buf.writeVarInt(event.packetID());
            buf.writeBlockPos(pos);
            buf.writeVarInt(state);
        } catch (Exception e) {
            CraftEngine.instance().logger().warn("Failed to handle ClientboundBlockUpdatePacket", e);
        }
    };

    public static final BiConsumer<NetWorkUser, ByteBufPacketEvent> LEVEL_EVENT = (user, event) -> {
        try {
            FriendlyByteBuf buf = event.getBuffer();
            int eventId = buf.readInt();
            if (eventId != 2001) return;
            BlockPos blockPos = buf.readBlockPos(buf);
            int state = buf.readInt();
            boolean global = buf.readBoolean();
            int newState = remap(state);
            if (newState == state) {
                return;
            }
            event.setChanged(true);
            buf.clear();
            buf.writeVarInt(event.packetID());
            buf.writeInt(eventId);
            buf.writeBlockPos(blockPos);
            buf.writeInt(newState);
            buf.writeBoolean(global);
        } catch (Exception e) {
            CraftEngine.instance().logger().warn("Failed to handle ClientboundLevelEventPacket", e);
        }
    };

    public static final BiConsumer<NetWorkUser, ByteBufPacketEvent> LEVEL_PARTICLE = (user, event) -> {
        try {
            FriendlyByteBuf buf = event.getBuffer();
            Object mcByteBuf;
            Method writeMethod;
            if (VersionHelper.isVersionNewerThan1_20_5()) {
                mcByteBuf = Reflections.constructor$RegistryFriendlyByteBuf.newInstance(buf, Reflections.instance$registryAccess);
                writeMethod = Reflections.method$ClientboundLevelParticlesPacket$write;
            } else {
                mcByteBuf = Reflections.constructor$FriendlyByteBuf.newInstance(event.getBuffer().source());
                writeMethod = Reflections.method$Packet$write;
            }
            Object packet = Reflections.constructor$ClientboundLevelParticlesPacket.newInstance(mcByteBuf);
            Object option = Reflections.field$ClientboundLevelParticlesPacket$particle.get(packet);
            if (option == null) return;
            if (!Reflections.clazz$BlockParticleOption.isInstance(option)) return;
            Object blockState = Reflections.field$BlockParticleOption$blockState.get(option);
            int id = BlockStateUtils.blockStateToId(blockState);
            int remapped = remap(id);
            if (remapped == id) return;
            Reflections.field$BlockParticleOption$blockState.set(option, BlockStateUtils.idToBlockState(remapped));
            event.setChanged(true);
            buf.clear();
            buf.writeVarInt(event.packetID());
            writeMethod.invoke(packet, mcByteBuf);
        } catch (Exception e) {
            CraftEngine.instance().logger().warn("Failed to handle ClientboundLevelParticlesPacket", e);
        }
    };

    public static final TriConsumer<NetWorkUser, NMSPacketEvent, Object> PLAYER_ACTION = (user, event, packet) -> {
        try {
            if (!user.isOnline()) return;
            BukkitServerPlayer player = (BukkitServerPlayer) user;
            Player platformPlayer = player.platformPlayer();
            World world = platformPlayer.getWorld();
            Object blockPos = Reflections.field$ServerboundPlayerActionPacket$pos.get(packet);
            BlockPos pos = new BlockPos(
                    (int) Reflections.field$Vec3i$x.get(blockPos),
                    (int) Reflections.field$Vec3i$y.get(blockPos),
                    (int) Reflections.field$Vec3i$z.get(blockPos)
            );
            if (VersionHelper.isFolia()) {
                BukkitCraftEngine.instance().scheduler().sync().run(() -> {
                    try {
                        handlePlayerActionPacketOnMainThread(player, world, pos, packet);
                    } catch (Exception e) {
                        CraftEngine.instance().logger().warn("Failed to handle ServerboundPlayerActionPacket", e);
                    }
                }, world, pos.x() >> 4, pos.z() >> 4);
            } else {
                handlePlayerActionPacketOnMainThread(player, world, pos, packet);
            }
        } catch (Exception e) {
            CraftEngine.instance().logger().warn("Failed to handle ServerboundPlayerActionPacket", e);
        }
    };

    private static void handlePlayerActionPacketOnMainThread(BukkitServerPlayer player, World world, BlockPos pos, Object packet) throws Exception {

        Object action = Reflections.field$ServerboundPlayerActionPacket$action.get(packet);
        if (action == Reflections.instance$ServerboundPlayerActionPacket$Action$START_DESTROY_BLOCK) {
            Object serverLevel = Reflections.field$CraftWorld$ServerLevel.get(world);
            Object blockState = Reflections.method$BlockGetter$getBlockState.invoke(serverLevel, LocationUtils.toBlockPos(pos));
            int stateId = BlockStateUtils.blockStateToId(blockState);
            // not a custom block
            if (BlockStateUtils.isVanillaBlock(stateId)) {
                if (ConfigManager.enableSoundSystem()) {
                    Object blockOwner = Reflections.field$StateHolder$owner.get(blockState);
                    if (BukkitBlockManager.instance().isBlockSoundRemoved(blockOwner)) {
                        player.startMiningBlock(world, pos, blockState, false, null);
                        return;
                    }
                }
                if (player.isMiningBlock() || player.shouldSyncAttribute()) {
                    player.stopMiningBlock();
                }
                return;
            }
            player.startMiningBlock(world, pos, blockState, true, BukkitBlockManager.instance().getImmutableBlockStateUnsafe(stateId));
        } else if (action == Reflections.instance$ServerboundPlayerActionPacket$Action$ABORT_DESTROY_BLOCK) {
            if (player.isMiningBlock()) {
                player.abortMiningBlock();
            }
        } else if (action == Reflections.instance$ServerboundPlayerActionPacket$Action$STOP_DESTROY_BLOCK) {
            if (player.isMiningBlock()) {
                player.stopMiningBlock();
            }
        }
    }

    public static final TriConsumer<NetWorkUser, NMSPacketEvent, Object> SWING_HAND = (user, event, packet) -> {
        try {
            if (!user.isOnline()) return;
            BukkitServerPlayer player = (BukkitServerPlayer) user;
            if (!player.isMiningBlock()) return;
            Object hand = Reflections.field$ServerboundSwingPacket$hand.get(packet);
            if (hand == Reflections.instance$InteractionHand$MAIN_HAND) {
                player.onSwingHand();
            }
        } catch (Exception e) {
            CraftEngine.instance().logger().warn("Failed to handle ServerboundSwingPacket", e);
        }
    };

    public static final TriConsumer<NetWorkUser, NMSPacketEvent, Object> USE_ITEM_ON = (user, event, packet) -> {
        try {
            if (!user.isOnline()) return;
            BukkitServerPlayer player = (BukkitServerPlayer) user;
            if (player.isMiningBlock()) {
                player.stopMiningBlock();
            }
        } catch (Exception e) {
            CraftEngine.instance().logger().warn("Failed to handle ServerboundUseItemOnPacket", e);
        }
    };

    public static final TriConsumer<NetWorkUser, NMSPacketEvent, Object> RESPAWN = (user, event, packet) -> {
        try {
            BukkitServerPlayer player = (BukkitServerPlayer) user;
            Object dimensionKey;
            if (!VersionHelper.isVersionNewerThan1_20_2()) {
                dimensionKey = Reflections.field$ClientboundRespawnPacket$dimension.get(packet);
            } else {
                Object commonInfo = Reflections.field$ClientboundRespawnPacket$commonPlayerSpawnInfo.get(packet);
                dimensionKey = Reflections.field$CommonPlayerSpawnInfo$dimension.get(commonInfo);
            }
            Object location = Reflections.field$ResourceKey$location.get(dimensionKey);
            World world = Bukkit.getWorld(Objects.requireNonNull(NamespacedKey.fromString(location.toString())));
            if (world != null) {
                int sectionCount = (world.getMaxHeight() - world.getMinHeight()) / 16;
                player.setClientSideSectionCount(sectionCount);
                player.setClientSideDimension(Key.of(location.toString()));
            } else {
                CraftEngine.instance().logger().warn("Failed to handle ClientboundRespawnPacket: World " + location + " does not exist");
            }
        } catch (Exception e) {
            CraftEngine.instance().logger().warn("Failed to handle ClientboundRespawnPacket", e);
        }
    };

    public static final TriConsumer<NetWorkUser, NMSPacketEvent, Object> LOGIN = (user, event, packet) -> {
        try {
            BukkitServerPlayer player = (BukkitServerPlayer) user;
            player.setConnectionState(ConnectionState.PLAY);
            Object dimensionKey;
            if (!VersionHelper.isVersionNewerThan1_20_2()) {
                dimensionKey = Reflections.field$ClientboundLoginPacket$dimension.get(packet);
            } else {
                Object commonInfo = Reflections.field$ClientboundLoginPacket$commonPlayerSpawnInfo.get(packet);
                dimensionKey = Reflections.field$CommonPlayerSpawnInfo$dimension.get(commonInfo);
            }
            Object location = Reflections.field$ResourceKey$location.get(dimensionKey);
            World world = Bukkit.getWorld(Objects.requireNonNull(NamespacedKey.fromString(location.toString())));
            if (world != null) {
                int sectionCount = (world.getMaxHeight() - world.getMinHeight()) / 16;
                player.setClientSideSectionCount(sectionCount);
                player.setClientSideDimension(Key.of(location.toString()));
            } else {
                CraftEngine.instance().logger().warn("Failed to handle ClientboundLoginPacket: World " + location + " does not exist");
            }
        } catch (Exception e) {
            CraftEngine.instance().logger().warn("Failed to handle ClientboundLoginPacket", e);
        }
    };

    // 1.21.4-
    // We can't find the best solution, we can only keep the feel as good as possible
    // When the hotbar is full, the latest creative mode inventory can only be accessed when the player opens the inventory screen. Currently, it is not worth further handling this issue.
    public static final TriConsumer<NetWorkUser, NMSPacketEvent, Object> SET_CREATIVE_SLOT = (user, event, packet) -> {
        try {
            if (VersionHelper.isVersionNewerThan1_21_4()) return;
            if (!user.isOnline()) return;
            BukkitServerPlayer player = (BukkitServerPlayer) user;
            if (VersionHelper.isFolia()) {
                BukkitCraftEngine.instance().scheduler().sync().run(() -> {
                    try {
                        handleSetCreativeSlotPacketOnMainThread(player, packet);
                    } catch (Exception e) {
                        CraftEngine.instance().logger().warn("Failed to handle ServerboundSetCreativeModeSlotPacket", e);
                    }
                }, (World) player.level().getHandle(), (MCUtils.fastFloor(player.x())) >> 4, (MCUtils.fastFloor(player.z())) >> 4);
            } else {
                handleSetCreativeSlotPacketOnMainThread(player, packet);
            }
        } catch (Exception e) {
            CraftEngine.instance().logger().warn("Failed to handle ServerboundSetCreativeModeSlotPacket", e);
        }
    };

    private static void handleSetCreativeSlotPacketOnMainThread(BukkitServerPlayer player, Object packet) throws Exception {
        Player bukkitPlayer = player.platformPlayer();
        if (bukkitPlayer == null) return;
        if (bukkitPlayer.getGameMode() != GameMode.CREATIVE) return;
        int slot = VersionHelper.isVersionNewerThan1_20_5() ? Reflections.field$ServerboundSetCreativeModeSlotPacket$slotNum.getShort(packet) : Reflections.field$ServerboundSetCreativeModeSlotPacket$slotNum.getInt(packet);
        if (slot < 36 || slot > 44) return;
        ItemStack item = (ItemStack) Reflections.method$CraftItemStack$asCraftMirror.invoke(null, Reflections.field$ServerboundSetCreativeModeSlotPacket$itemStack.get(packet));
        if (ItemUtils.isEmpty(item)) return;
        if (slot - 36 != bukkitPlayer.getInventory().getHeldItemSlot()) {
            return;
        }
        double interactionRange = player.getInteractionRange();
        // do ray trace to get current block
        RayTraceResult result = bukkitPlayer.rayTraceBlocks(interactionRange, FluidCollisionMode.NEVER);
        if (result == null) return;
        org.bukkit.block.Block hitBlock = result.getHitBlock();
        if (hitBlock == null) return;
        ImmutableBlockState state = BukkitBlockManager.instance().getImmutableBlockState(BlockStateUtils.blockDataToId(hitBlock.getBlockData()));
        // not a custom block
        if (state == null || state.isEmpty()) return;
        Key itemId = state.settings().itemId();
        // no item available
        if (itemId == null) return;
        BlockData data = BlockStateUtils.createBlockData(state.vanillaBlockState().handle());
        // compare item
        if (data == null || !data.getMaterial().equals(item.getType())) return;
        ItemStack itemStack = BukkitCraftEngine.instance().itemManager().buildCustomItemStack(itemId, player);
        if (ItemUtils.isEmpty(itemStack)) {
            CraftEngine.instance().logger().warn("Item: " + itemId + " is not a valid item");
            return;
        }
        PlayerInventory inventory = bukkitPlayer.getInventory();
        int sameItemSlot = -1;
        int emptySlot = -1;
        for (int i = 0; i < 9 + 27; i++) {
            ItemStack invItem = inventory.getItem(i);
            if (ItemUtils.isEmpty(invItem)) {
                if (emptySlot == -1 && i < 9) emptySlot = i;
                continue;
            }
            if (invItem.getType().equals(itemStack.getType()) && invItem.getItemMeta().equals(itemStack.getItemMeta())) {
                if (sameItemSlot == -1) sameItemSlot = i;
            }
        }
        if (sameItemSlot != -1) {
            if (sameItemSlot < 9) {
                inventory.setHeldItemSlot(sameItemSlot);
                ItemStack previousItem = inventory.getItem(slot - 36);
                BukkitCraftEngine.instance().scheduler().sync().runDelayed(() -> inventory.setItem(slot - 36, previousItem));
            } else {
                ItemStack sameItem = inventory.getItem(sameItemSlot);
                int finalSameItemSlot = sameItemSlot;
                BukkitCraftEngine.instance().scheduler().sync().runDelayed(() -> {
                    inventory.setItem(finalSameItemSlot, new ItemStack(Material.AIR));
                    inventory.setItem(slot - 36, sameItem);
                });
            }
        } else {
            if (item.getAmount() == 1) {
                if (ItemUtils.isEmpty(inventory.getItem(slot - 36))) {
                    BukkitCraftEngine.instance().scheduler().sync().runDelayed(() -> inventory.setItem(slot - 36, itemStack));
                    return;
                }
                if (emptySlot != -1) {
                    inventory.setHeldItemSlot(emptySlot);
                    inventory.setItem(emptySlot, itemStack);
                } else {
                    BukkitCraftEngine.instance().scheduler().sync().runDelayed(() -> inventory.setItem(slot - 36, itemStack));
                }
            }
        }
    }

    // 1.21.4+
    public static final TriConsumer<NetWorkUser, NMSPacketEvent, Object> PICK_ITEM_FROM_BLOCK = (user, event, packet) -> {
        try {
            if (!user.isOnline()) return;
            Player player = (Player) user.platformPlayer();
            if (player == null) return;
            Object pos = Reflections.field$ServerboundPickItemFromBlockPacket$pos.get(packet);
            if (VersionHelper.isFolia()) {
                int x = (int) Reflections.field$Vec3i$x.get(pos);
                int z = (int) Reflections.field$Vec3i$z.get(pos);
                BukkitCraftEngine.instance().scheduler().sync().run(() -> {
                    try {
                        handlePickItemFromBlockPacketOnMainThread(player, pos);
                    } catch (Exception e) {
                        CraftEngine.instance().logger().warn("Failed to handle ServerboundPickItemFromBlockPacket", e);
                    }
                }, player.getWorld(), x >> 4, z >> 4);
            } else {
                BukkitCraftEngine.instance().scheduler().sync().run(() -> {
                    try {
                        handlePickItemFromBlockPacketOnMainThread(player, pos);
                    } catch (Exception e) {
                        CraftEngine.instance().logger().warn("Failed to handle ServerboundPickItemFromBlockPacket", e);
                    }
                });
            }
        } catch (Exception e) {
            CraftEngine.instance().logger().warn("Failed to handle ServerboundPickItemFromBlockPacket", e);
        }
    };

    private static void handlePickItemFromBlockPacketOnMainThread(Player player, Object pos) throws Exception {
        Object serverLevel = Reflections.field$CraftWorld$ServerLevel.get(player.getWorld());
        Object blockState = Reflections.method$BlockGetter$getBlockState.invoke(serverLevel, pos);
        ImmutableBlockState state = BukkitBlockManager.instance().getImmutableBlockState(BlockStateUtils.blockStateToId(blockState));
        if (state == null) return;
        Key itemId = state.settings().itemId();
        if (itemId == null) return;
        pickItem(player, itemId);
    }

    // 1.21.4+
    public static final TriConsumer<NetWorkUser, NMSPacketEvent, Object> PICK_ITEM_FROM_ENTITY = (user, event, packet) -> {
        try {
            int entityId = (int) Reflections.field$ServerboundPickItemFromEntityPacket$id.get(packet);
            LoadedFurniture furniture = BukkitFurnitureManager.instance().getLoadedFurnitureByInteractionEntityId(entityId);
            if (furniture == null) return;
            Player player = (Player) user.platformPlayer();
            if (player == null) return;
            if (VersionHelper.isFolia()) {
                Location location = player.getLocation();
                int x = location.getBlockX();
                int z = location.getBlockZ();
                BukkitCraftEngine.instance().scheduler().sync().run(() -> {
                    try {
                        handlePickItemFromEntityOnMainThread(player, furniture);
                    } catch (Exception e) {
                        CraftEngine.instance().logger().warn("Failed to handle ServerboundPickItemFromEntityPacket", e);
                    }
                }, player.getWorld(), x >> 4, z >> 4);
            } else {
                BukkitCraftEngine.instance().scheduler().sync().run(() -> {
                    try {
                        handlePickItemFromEntityOnMainThread(player, furniture);
                    } catch (Exception e) {
                        CraftEngine.instance().logger().warn("Failed to handle ServerboundPickItemFromEntityPacket", e);
                    }
                });
            }
        } catch (Exception e) {
            CraftEngine.instance().logger().warn("Failed to handle ServerboundPickItemFromEntityPacket", e);
        }
    };

    private static void handlePickItemFromEntityOnMainThread(Player player, LoadedFurniture furniture) throws Exception {
        Key itemId = furniture.furniture().settings().itemId();
        if (itemId == null) return;
        pickItem(player, itemId);
    }

    private static void pickItem(Player player, Key itemId) throws IllegalAccessException, InvocationTargetException {
        ItemStack itemStack = BukkitCraftEngine.instance().itemManager().buildCustomItemStack(itemId, BukkitCraftEngine.instance().adapt(player));
        if (itemStack == null) {
            CraftEngine.instance().logger().warn("Item: " + itemId + " is not a valid item");
            return;
        }
        assert Reflections.method$ServerGamePacketListenerImpl$tryPickItem != null;
        Reflections.method$ServerGamePacketListenerImpl$tryPickItem.invoke(
                Reflections.field$ServerPlayer$connection.get(Reflections.method$CraftPlayer$getHandle.invoke(player)), Reflections.method$CraftItemStack$asNMSMirror.invoke(null, itemStack));
    }

    public static final TriConsumer<NetWorkUser, NMSPacketEvent, Object> ADD_ENTITY = (user, event, packet) -> {
        try {
            Object entityType = Reflections.field$ClientboundAddEntityPacket$type.get(packet);
            // Falling blocks
            if (entityType == Reflections.instance$EntityType$FALLING_BLOCK) {
                int data = Reflections.field$ClientboundAddEntityPacket$data.getInt(packet);
                int remapped = remap(data);
                if (remapped != data) {
                    Reflections.field$ClientboundAddEntityPacket$data.set(packet, remapped);
                }
            } else if (entityType == Reflections.instance$EntityType$ITEM_DISPLAY) {
                // Furniture
                int entityId = (int) Reflections.field$ClientboundAddEntityPacket$entityId.get(packet);
                LoadedFurniture furniture = BukkitFurnitureManager.instance().getLoadedFurnitureByBaseEntityId(entityId);
                if (furniture != null) {
                    user.sendPacket(furniture.spawnPacket(), false);
                }
            }
        } catch (Exception e) {
            CraftEngine.instance().logger().warn("Failed to handle ClientboundAddEntityPacket", e);
        }
    };

    public static final TriConsumer<NetWorkUser, NMSPacketEvent, Object> SYNC_ENTITY_POSITION = (user, event, packet) -> {
        try {
            int entityId = (int) Reflections.field$ClientboundEntityPositionSyncPacket$id.get(packet);
            if (BukkitFurnitureManager.instance().isFurnitureBaseEntity(entityId)) {
                event.setCancelled(true);
            }
        } catch (Exception e) {
            CraftEngine.instance().logger().warn("Failed to handle ClientboundEntityPositionSyncPacket", e);
        }
    };

    public static final TriConsumer<NetWorkUser, NMSPacketEvent, Object> MOVE_ENTITY = (user, event, packet) -> {
        try {
            int entityId = (int) Reflections.field$ClientboundMoveEntityPacket$entityId.get(packet);
            if (BukkitFurnitureManager.instance().isFurnitureBaseEntity(entityId)) {
                event.setCancelled(true);
            }
        } catch (Exception e) {
            CraftEngine.instance().logger().warn("Failed to handle ClientboundMoveEntityPacket$Pos", e);
        }
    };

    public static final TriConsumer<NetWorkUser, NMSPacketEvent, Object> REMOVE_ENTITY = (user, event, packet) -> {
        try {
            IntList intList = (IntList) Reflections.field$ClientboundRemoveEntitiesPacket$entityIds.get(packet);
            for (int i = 0, size = intList.size(); i < size; i++) {
                int[] entities = BukkitFurnitureManager.instance().getSubEntityIdsByBaseEntityId(intList.getInt(i));
                if (entities == null) continue;
                for (int entityId : entities) {
                    intList.add(entityId);
                }
            }
        } catch (Exception e) {
            CraftEngine.instance().logger().warn("Failed to handle ClientboundRemoveEntitiesPacket", e);
        }
    };

    public static final TriConsumer<NetWorkUser, NMSPacketEvent, Object> INTERACT_ENTITY = (user, event, packet) -> {
        try {
            Player player = (Player) user.platformPlayer();
            if (player == null) return;
            int entityId = (int) Reflections.field$ServerboundInteractPacket$entityId.get(packet);
            Object action = Reflections.field$ServerboundInteractPacket$action.get(packet);
            Object actionType = Reflections.method$ServerboundInteractPacket$Action$getType.invoke(action);
            if (actionType == null) return;
            LoadedFurniture furniture = BukkitFurnitureManager.instance().getLoadedFurnitureByInteractionEntityId(entityId);
            if (furniture == null) return;
            Location location = furniture.baseEntity().getLocation();
            BukkitServerPlayer serverPlayer = (BukkitServerPlayer) user;
            if (serverPlayer.isSpectatorMode() || serverPlayer.isAdventureMode()) return;
            BukkitCraftEngine.instance().scheduler().sync().run(() -> {
                if (actionType == Reflections.instance$ServerboundInteractPacket$ActionType$ATTACK) {
                    if (furniture.isValid()) {
                        if (!BukkitCraftEngine.instance().antiGrief().canBreak(player, location)) {
                            return;
                        }
                        FurnitureBreakEvent breakEvent = new FurnitureBreakEvent(serverPlayer.platformPlayer(), furniture);
                        if (EventUtils.fireAndCheckCancel(breakEvent)) {
                            return;
                        }
                        CraftEngineFurniture.remove(furniture, serverPlayer, !serverPlayer.isCreativeMode(), true);
                    }
                } else if (actionType == Reflections.instance$ServerboundInteractPacket$ActionType$INTERACT_AT) {
                    InteractionHand hand;
                    Location interactionPoint;
                    try {
                        Object interactionHand = Reflections.field$ServerboundInteractPacket$InteractionAtLocationAction$hand.get(action);
                        hand = interactionHand == Reflections.instance$InteractionHand$MAIN_HAND ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
                        Object vec3 = Reflections.field$ServerboundInteractPacket$InteractionAtLocationAction$location.get(action);
                        double x = (double) Reflections.field$Vec3$x.get(vec3);
                        double y = (double) Reflections.field$Vec3$y.get(vec3);
                        double z = (double) Reflections.field$Vec3$z.get(vec3);
                        interactionPoint = new Location(location.getWorld(), x, y, z);
                    } catch (ReflectiveOperationException e) {
                        throw new RuntimeException("Failed to get interaction hand from interact packet", e);
                    }
                    FurnitureInteractEvent interactEvent = new FurnitureInteractEvent(serverPlayer.platformPlayer(), furniture, hand, interactionPoint);
                    if (EventUtils.fireAndCheckCancel(interactEvent)) {
                        return;
                    }
                    if (player.isSneaking())
                        return;
                    furniture.getAvailableSeat(entityId).ifPresent(seatPos -> {
                        if (furniture.occupySeat(seatPos)) {
                            furniture.mountSeat(Objects.requireNonNull(player.getPlayer()), seatPos);
                        }
                    });
                }
            }, player.getWorld(), location.getBlockX() >> 4,location.getBlockZ() >> 4);
        } catch (Exception e) {
            CraftEngine.instance().logger().warn("Failed to handle ServerboundInteractPacket", e);
        }
    };

    public static final TriConsumer<NetWorkUser, NMSPacketEvent, Object> SOUND = (user, event, packet) -> {
        try {
            Object sound = Reflections.field$ClientboundSoundPacket$sound.get(packet);
            Object soundEvent = Reflections.method$Holder$value.invoke(sound);
            Key mapped = BukkitBlockManager.instance().replaceSoundIfExist(Key.of(Reflections.field$SoundEvent$location.get(soundEvent).toString()));
            if (mapped != null) {
                event.setCancelled(true);
                Object newId = Reflections.method$ResourceLocation$fromNamespaceAndPath.invoke(null, mapped.namespace(), mapped.value());
                Object newSoundEvent = VersionHelper.isVersionNewerThan1_21_2() ?
                        Reflections.constructor$SoundEvent.newInstance(newId, Reflections.field$SoundEvent$fixedRange.get(soundEvent)) :
                        Reflections.constructor$SoundEvent.newInstance(newId, Reflections.field$SoundEvent$range.get(soundEvent), Reflections.field$SoundEvent$newSystem.get(soundEvent));
                Object newSoundPacket = Reflections.constructor$ClientboundSoundPacket.newInstance(
                        Reflections.method$Holder$direct.invoke(null, newSoundEvent),
                        Reflections.field$ClientboundSoundPacket$source.get(packet),
                        (double) Reflections.field$ClientboundSoundPacket$x.getInt(packet) / 8,
                        (double) Reflections.field$ClientboundSoundPacket$y.getInt(packet) / 8,
                        (double) Reflections.field$ClientboundSoundPacket$z.getInt(packet) / 8,
                        Reflections.field$ClientboundSoundPacket$volume.get(packet),
                        Reflections.field$ClientboundSoundPacket$pitch.get(packet),
                        Reflections.field$ClientboundSoundPacket$seed.get(packet)
                );
                user.sendPacket(newSoundPacket, true);
            }
        } catch (Exception e) {
            CraftEngine.instance().logger().warn("Failed to handle ClientboundSoundPacket", e);
        }
    };

    public static final TriConsumer<NetWorkUser, NMSPacketEvent, Object> BUNDLE_SELECT_ITEM = (user, event, packet) -> {
        try {
            Player player = (Player) user.platformPlayer();
            BukkitServerPlayer serverPlayer = BukkitCraftEngine.instance().adapt(player);
            int slotId = (int) Reflections.field$ServerboundSelectBundleItemPacket$slotId.get(packet);
            int selectedItemIndex = (int) Reflections.field$ServerboundSelectBundleItemPacket$selectedItemIndex.get(packet);
            if (selectedItemIndex != -1) {
                if (user.bundleSelectedItemIndex() - selectedItemIndex == -1 || user.bundleSelectedItemIndex() - selectedItemIndex == 2) {
                    player.sendMessage("向下");
                } else {
                    player.sendMessage("向上");
                }
            }
            serverPlayer.setBundleSelectedItemIndex(selectedItemIndex);
        } catch (Exception e) {
            CraftEngine.instance().logger().warn("Failed to handle ServerboundSelectBundleItemPacket", e);
        }
    };
}
