package net.momirealms.craftengine.bukkit.block.behavior;

import net.momirealms.craftengine.bukkit.block.entity.BukkitBlockEntityTypes;
import net.momirealms.craftengine.bukkit.block.entity.ItemDisplayBlockEntity;
import net.momirealms.craftengine.bukkit.block.entity.renderer.DynamicItemFrameRenderer;
import net.momirealms.craftengine.core.block.CustomBlock;
import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.block.behavior.BlockBehaviorFactory;
import net.momirealms.craftengine.core.block.behavior.EntityBlockBehavior;
import net.momirealms.craftengine.core.block.entity.BlockEntity;
import net.momirealms.craftengine.core.block.entity.BlockEntityType;
import net.momirealms.craftengine.core.block.properties.Property;
import net.momirealms.craftengine.core.entity.player.InteractionHand;
import net.momirealms.craftengine.core.entity.player.InteractionResult;
import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.sound.SoundData;
import net.momirealms.craftengine.core.util.Direction;
import net.momirealms.craftengine.core.util.ItemUtils;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;
import net.momirealms.craftengine.core.world.BlockPos;
import net.momirealms.craftengine.core.world.Vec3d;
import net.momirealms.craftengine.core.world.World;
import net.momirealms.craftengine.core.world.context.UseOnContext;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.Optional;

public class ItemDisplayBlockBehavior extends BukkitBlockBehavior implements EntityBlockBehavior {
    public static final BlockBehaviorFactory<ItemDisplayBlockBehavior> FACTORY = new Factory();
    public final DynamicItemFrameRenderer.Config config;
    public final SoundData addItemSound;
    public final SoundData removeItemSound;
    public final SoundData rotateItemSound;
    public final Property<Direction> directionProperty;

    public ItemDisplayBlockBehavior(
            CustomBlock customBlock,
            DynamicItemFrameRenderer.Config config,
            SoundData addItemSound,
            SoundData removeItemSound,
            SoundData rotateItemSound,
            Property<Direction> directionProperty) {
        super(customBlock);
        this.config = config;
        this.addItemSound = addItemSound;
        this.removeItemSound = removeItemSound;
        this.rotateItemSound = rotateItemSound;
        this.directionProperty = directionProperty;
    }

    @Override
    public <T extends BlockEntity> BlockEntityType<T> blockEntityType(ImmutableBlockState state) {
        return EntityBlockBehavior.blockEntityTypeHelper(BukkitBlockEntityTypes.ITEM_DISPLAY);
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, ImmutableBlockState state) {
        return new ItemDisplayBlockEntity(pos, state, this.config);
    }

    @Override
    public InteractionResult useOnBlock(UseOnContext context, ImmutableBlockState state) {
        Player player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;
        World world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockEntity blockEntity = world.storageWorld().getBlockEntityAtIfLoaded(pos);
        if (!(blockEntity instanceof ItemDisplayBlockEntity itemDisplay && itemDisplay.isValid())) {
            return InteractionResult.PASS;
        }
        // 方块实体内部有物品的时候在shift时旋转
        if (player.isSecondaryUseActive() && !ItemUtils.isEmpty(itemDisplay.item())) {
            itemDisplay.rotation(itemDisplay.rotation() + 1);
            playSound(world, pos, this.rotateItemSound);
            player.swingHand(context.getHand());
            return InteractionResult.SUCCESS_AND_CANCEL;
        }
        // 当主手为空的时候右键取下
        if (context.getHand() == InteractionHand.MAIN_HAND && ItemUtils.isEmpty(context.getItem())) {
            Item<ItemStack> item = itemDisplay.item();
            if (ItemUtils.isEmpty(item)) { // 空的不管
                return InteractionResult.SUCCESS_AND_CANCEL;
            }
            itemDisplay.updateItem(null); // 先取出来
            if (!player.canInstabuild()) {
                player.setItemInHand(InteractionHand.MAIN_HAND, item); // 然后给玩家
            }
            playSound(world, pos, this.removeItemSound);
            player.swingHand(context.getHand());
            return InteractionResult.SUCCESS_AND_CANCEL;
        }
        // 当方块实体内部没有物品切换手上物品不为空则放入
        if (ItemUtils.isEmpty(itemDisplay.item()) && !ItemUtils.isEmpty(context.getItem())) {
            @SuppressWarnings("unchecked")
            Item<ItemStack> item = (Item<ItemStack>) context.getItem();
            Item<ItemStack> copied = item.copyWithCount(1);
            if (!player.canInstabuild()) {
                item.shrink(1); // 先扣物品
            }
            itemDisplay.updateItem(copied); // 然后放进去
            playSound(world, pos, this.addItemSound);
            player.swingHand(context.getHand());
            return InteractionResult.SUCCESS_AND_CANCEL;
        }
        return InteractionResult.SUCCESS_AND_CANCEL;
    }

    private static void playSound(World world, BlockPos pos, SoundData soundData) {
        if (soundData == null) return;
        Vec3d location = new Vec3d(pos.x() + 0.5, pos.y() + 0.5, pos.z() + 0.5);
        world.playBlockSound(location, soundData);
    }

    private static class Factory implements BlockBehaviorFactory<ItemDisplayBlockBehavior> {

        @Override
        public ItemDisplayBlockBehavior create(CustomBlock block, Map<String, Object> arguments) {
            @SuppressWarnings("unchecked")
            Property<Direction> directionProperty = (Property<Direction>) ResourceConfigUtils.requireNonNullOrThrow(block.getProperty("facing"), "warning.config.block.behavior.item_display.missing_facing");
            DynamicItemFrameRenderer.Config config = new DynamicItemFrameRenderer.Config(
                    ResourceConfigUtils.getAsVector3f(arguments.getOrDefault("position", 0), "position"),
                    ResourceConfigUtils.getAsBoolean(arguments.getOrDefault("is-glow", false), "is-glow"),
                    ResourceConfigUtils.getAsBoolean(arguments.getOrDefault("invisible", false), "invisible"),
                    ResourceConfigUtils.getAsBoolean(arguments.getOrDefault("render-map-item", true), "render-map-item") // 地图渲染有少量开销可选启用
            );
            Map<String, Object> sounds = ResourceConfigUtils.getAsMapOrNull(arguments.get("sounds"), "sounds");
            SoundData addItemSound = null;
            SoundData removeItemSound = null;
            SoundData rotateItemSound = null;
            if (sounds != null) {
                addItemSound = Optional.ofNullable(sounds.get("add-item")).map(obj -> SoundData.create(obj, SoundData.SoundValue.FIXED_1, SoundData.SoundValue.ranged(0.9f, 1f))).orElse(null);
                removeItemSound = Optional.ofNullable(sounds.get("remove-item")).map(obj -> SoundData.create(obj, SoundData.SoundValue.FIXED_1, SoundData.SoundValue.ranged(0.9f, 1f))).orElse(null);
                rotateItemSound = Optional.ofNullable(sounds.get("rotate-item")).map(obj -> SoundData.create(obj, SoundData.SoundValue.FIXED_1, SoundData.SoundValue.ranged(0.9f, 1f))).orElse(null);
            }
            return new ItemDisplayBlockBehavior(block, config, addItemSound, removeItemSound, rotateItemSound, directionProperty);
        }
    }
}
