package net.momirealms.craftengine.bukkit.block.entity;

import net.momirealms.craftengine.bukkit.api.BukkitAdaptors;
import net.momirealms.craftengine.bukkit.block.behavior.DisplayItemBlockBehavior;
import net.momirealms.craftengine.bukkit.block.entity.renderer.DynamicItemFrameRenderer;
import net.momirealms.craftengine.bukkit.entity.data.ItemFrameData;
import net.momirealms.craftengine.bukkit.item.BukkitItemManager;
import net.momirealms.craftengine.bukkit.nms.FastNMS;
import net.momirealms.craftengine.bukkit.plugin.reflection.minecraft.CoreReflections;
import net.momirealms.craftengine.bukkit.plugin.reflection.minecraft.MRegistryOps;
import net.momirealms.craftengine.bukkit.util.DirectionUtils;
import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.block.entity.BlockEntity;
import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.util.ItemUtils;
import net.momirealms.craftengine.core.util.VersionHelper;
import net.momirealms.craftengine.core.world.BlockPos;
import net.momirealms.craftengine.core.world.Vec3d;
import net.momirealms.craftengine.core.world.chunk.CEChunk;
import net.momirealms.sparrow.nbt.CompoundTag;
import net.momirealms.sparrow.nbt.Tag;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class DisplayItemBlockEntity extends BlockEntity {
    public final DynamicItemFrameRenderer.Config config;
    public final DisplayItemBlockBehavior behavior;
    private int rotation = 0;
    private @NotNull Item<ItemStack> item = BukkitItemManager.instance().uniqueEmptyItem().item();
    private @NotNull List<Object> cacheMetadata = List.of();
    private @Nullable Object mapId;
    private @Nullable Object mapItemSavedData;

    public DisplayItemBlockEntity(BlockPos pos, ImmutableBlockState blockState, DynamicItemFrameRenderer.Config config) {
        super(BukkitBlockEntityTypes.DISPLAY_ITEM, pos, blockState);
        this.config = config;
        this.behavior = blockState.behavior().getAs(DisplayItemBlockBehavior.class).orElseThrow();
        super.blockEntityRenderer = new DynamicItemFrameRenderer(this, super.pos);
        this.updateMetadata();
    }

    @Override
    protected void saveCustomData(CompoundTag tag) {
        tag.putInt("rotation", this.rotation);
        if (ItemUtils.isEmpty(this.item)) return; // 无法保存空的物品
        if (VersionHelper.isOrAbove1_20_5()) {
            CoreReflections.instance$ItemStack$CODEC.encodeStart(MRegistryOps.SPARROW_NBT, item.getLiteralObject())
                    .ifSuccess(success -> tag.put("item", success))
                    .ifError(error -> CraftEngine.instance().logger().severe("Error while saving item: " + error));
        } else {
            Object nmsTag = FastNMS.INSTANCE.method$itemStack$save(item.getLiteralObject(), FastNMS.INSTANCE.constructor$CompoundTag());
            tag.put("item", MRegistryOps.NBT.convertTo(MRegistryOps.SPARROW_NBT, nmsTag));
        }
    }

    @Override
    public void loadCustomData(CompoundTag tag) {
        this.rotation = tag.getInt("rotation");
        Tag itemTag = tag.get("item");
        if (itemTag == null) return;
        if (VersionHelper.isOrAbove1_20_5()) {
            CoreReflections.instance$ItemStack$CODEC.parse(MRegistryOps.SPARROW_NBT, itemTag)
                    .resultOrPartial(error -> CraftEngine.instance().logger().severe("Tried to load invalid item: '" + itemTag + "'. " + error))
                    .ifPresent(itemStack -> this.item = BukkitAdaptors.adapt(FastNMS.INSTANCE.method$CraftItemStack$asCraftMirror(itemStack)));
        } else {
            Object nmsTag = MRegistryOps.SPARROW_NBT.convertTo(MRegistryOps.NBT, itemTag);
            Object itemStack = FastNMS.INSTANCE.method$ItemStack$of(nmsTag);
            this.item = BukkitAdaptors.adapt(FastNMS.INSTANCE.method$CraftItemStack$asCraftMirror(itemStack));
        }
        this.updateMetadata();
    }

    @Override
    public void preRemove() {
        super.world.world().dropItemNaturally(Vec3d.atCenterOf(this.pos), this.item);
    }

    public void updateItem(Item<ItemStack> item) {
        if (item == null) {
            item = BukkitItemManager.instance().uniqueEmptyItem().item();
        }
        this.item = item;
        this.update();
    }

    public void rotation(int rotation) {
        this.rotation = rotation % 8;
        this.update();
    }

    public Item<ItemStack> item() {
        return this.item;
    }

    public int rotation() {
        return this.rotation;
    }

    public List<Object> cacheMetadata() {
        return this.cacheMetadata;
    }

    public @Nullable Object mapId() {
        return this.mapId;
    }

    public @Nullable Object mapItemSavedData() {
        return this.mapItemSavedData;
    }

    public void mapItemSavedData(@Nullable Object data) {
        this.mapItemSavedData = data;
    }

    private void update() {
        CEChunk chunk = super.world.getChunkAtIfLoaded(super.pos.x >> 4, super.pos.z >> 4);
        if (chunk == null) return;
        chunk.setDirty(true); // 一定要标记脏数据不然不会保存
        if (super.blockEntityRenderer == null) return;
        this.updateMetadata();
        for (Player player : chunk.getTrackedBy()) {
            super.blockEntityRenderer.update(player);
        }
    }

    private void updateMetadata() {
        Object direction = DirectionUtils.toNMSDirection(super.blockState.get(this.behavior.directionProperty));
        List<Object> metadataValues = new ArrayList<>();
        ItemFrameData.Item.addEntityData(this.item.getLiteralObject(), metadataValues);
        ItemFrameData.Rotation.addEntityData(this.rotation, metadataValues);
        if (VersionHelper.isOrAbove1_21_6()) {
            ItemFrameData.Direction.addEntityData(direction, metadataValues);
        }
        if (this.config.invisible()) {
            ItemFrameData.SharedFlags.addEntityData((byte) 0x20, metadataValues);
        }
        this.cacheMetadata = metadataValues;
        if (this.config.renderMapItem()) {
            this.mapId = FastNMS.INSTANCE.method$MapItem$getMapId(this.item.getLiteralObject());
            this.mapItemSavedData = this.mapId != null && super.world != null
                    ? FastNMS.INSTANCE.method$MapItem$getSavedData(mapId, super.world.world.serverWorld())
                    : null;
        }
    }
}
