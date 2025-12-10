package net.momirealms.craftengine.bukkit.plugin.network.payload.protocol;


import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslationArgument;
import net.momirealms.craftengine.bukkit.block.BukkitBlockManager;
import net.momirealms.craftengine.bukkit.nms.FastNMS;
import net.momirealms.craftengine.bukkit.plugin.network.payload.PayloadHelper;
import net.momirealms.craftengine.bukkit.plugin.reflection.minecraft.CoreReflections;
import net.momirealms.craftengine.bukkit.plugin.reflection.minecraft.NetworkReflections;
import net.momirealms.craftengine.bukkit.plugin.reflection.paper.PaperReflections;
import net.momirealms.craftengine.bukkit.util.BlockStateUtils;
import net.momirealms.craftengine.bukkit.util.RegistryUtils;
import net.momirealms.craftengine.core.plugin.logger.Debugger;
import net.momirealms.craftengine.core.plugin.network.ModPacket;
import net.momirealms.craftengine.core.plugin.network.NetWorkUser;
import net.momirealms.craftengine.core.plugin.network.codec.NetworkCodec;
import net.momirealms.craftengine.core.registry.BuiltInRegistries;
import net.momirealms.craftengine.core.util.*;
import org.bukkit.entity.Player;

public record ClientCustomBlockPacket(int vanillaSize, int currentSize) implements ModPacket {
    public static final ResourceKey<NetworkCodec<FriendlyByteBuf, ? extends ModPacket>> TYPE = ResourceKey.create(
            BuiltInRegistries.MOD_PACKET.key().location(), Key.of("craftengine", "client_custom_block")
    );
    public static final NetworkCodec<FriendlyByteBuf, ClientCustomBlockPacket> CODEC = ModPacket.codec(
            ClientCustomBlockPacket::encode,
            ClientCustomBlockPacket::new
    );

    private ClientCustomBlockPacket(FriendlyByteBuf buf) {
        this(buf.readInt(), buf.readInt());
    }

    private void encode(FriendlyByteBuf buf) {
        buf.writeInt(this.vanillaSize);
        buf.writeInt(this.currentSize);
    }

    @Override
    public ResourceKey<NetworkCodec<FriendlyByteBuf, ? extends ModPacket>> type() {
        return TYPE;
    }

    @Override
    public void handle(NetWorkUser user) {
        if (user.clientModEnabled()) return; // 防止滥用
        int vanillaBlockRegistrySize = BlockStateUtils.vanillaBlockStateCount();
        if (this.vanillaSize != vanillaBlockRegistrySize) {
            user.kick(Component.translatable(
                    "disconnect.craftengine.vanilla_block_registry_mismatch",
                    TranslationArgument.numeric(this.vanillaSize),
                    TranslationArgument.numeric(vanillaBlockRegistrySize)
            ));
            return;
        }
        int serverBlockRegistrySize = RegistryUtils.currentBlockRegistrySize();
        if (this.currentSize < serverBlockRegistrySize) {
            user.kick(Component.translatable(
                    "disconnect.craftengine.current_block_registry_mismatch",
                    TranslationArgument.numeric(this.currentSize),
                    TranslationArgument.numeric(serverBlockRegistrySize)
            ));
            return;
        }
        user.setClientModState(true);
        user.setClientBlockList(new IntIdentityList(this.currentSize));
        PayloadHelper.sendData(user, BukkitBlockManager.instance().cachedVisualBlockStatePacket());
        if (!VersionHelper.isOrAbove1_20_2()) {
            // 因为旧版本没有配置阶段需要重新发送区块
            try {
                Object chunkLoader = PaperReflections.field$ServerPlayer$chunkLoader.get(user.serverPlayer());
                LongOpenHashSet sentChunks = (LongOpenHashSet) PaperReflections.field$RegionizedPlayerChunkLoader$PlayerChunkLoaderData$sentChunks.get(chunkLoader);
                Object serverLevel = FastNMS.INSTANCE.field$CraftWorld$ServerLevel(((Player) user.platformPlayer()).getWorld());
                Object lightEngine = CoreReflections.method$BlockAndTintGetter$getLightEngine.invoke(serverLevel);
                Object chunkSource = FastNMS.INSTANCE.method$ServerLevel$getChunkSource(serverLevel);
                for (long chunkPos : sentChunks) { // 这里可能出现玄学报错，故将错误打印移动至debug输出
                    int chunkX = (int) chunkPos;
                    int chunkZ = (int) (chunkPos >> 32);
                    Object levelChunk = FastNMS.INSTANCE.method$ServerChunkCache$getChunk(chunkSource, chunkX, chunkZ, false);
                    Object packet = NetworkReflections.constructor$ClientboundLevelChunkWithLightPacket.newInstance(levelChunk, lightEngine, null, null);
                    user.sendPacket(packet, true);
                }
            } catch (Exception e) {
                Debugger.COMMON.warn(() -> "Failed to refresh chunk for player " + user.name(), e);
            }
        }
    }
}
