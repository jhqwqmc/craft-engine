package net.momirealms.craftengine.bukkit.block.behavior;

import net.momirealms.craftengine.bukkit.block.entity.BukkitBlockEntityTypes;
import net.momirealms.craftengine.bukkit.block.entity.DisplayItemBlockEntity;
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
import org.joml.Vector3f;

import java.util.Map;
import java.util.Optional;

public class DisplayItemBlockBehavior extends BukkitBlockBehavior implements EntityBlockBehavior {
    public static final BlockBehaviorFactory<DisplayItemBlockBehavior> FACTORY = new Factory();
    public final Vector3f position;
    public final boolean isGlow;
    public final boolean invisible;
    public final boolean renderMapItem;
    public final SoundData addItemSound;
    public final SoundData removeItemSound;
    public final SoundData rotateItemSound;
    public final Property<Direction> directionProperty;

    public DisplayItemBlockBehavior(
            CustomBlock customBlock,
            Vector3f position,
            boolean isGlow,
            boolean invisible,
            boolean renderMapItem,
            SoundData addItemSound,
            SoundData removeItemSound,
            SoundData rotateItemSound,
            Property<Direction> directionProperty) {
        super(customBlock);
        this.position = position;
        this.isGlow = isGlow;
        this.invisible = invisible;
        this.renderMapItem = renderMapItem;
        this.addItemSound = addItemSound;
        this.removeItemSound = removeItemSound;
        this.rotateItemSound = rotateItemSound;
        this.directionProperty = directionProperty;
    }

    @Override
    public <T extends BlockEntity> BlockEntityType<T> blockEntityType(ImmutableBlockState state) {
        return EntityBlockBehavior.blockEntityTypeHelper(BukkitBlockEntityTypes.DISPLAY_ITEM);
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, ImmutableBlockState state) {
        return new DisplayItemBlockEntity(pos, state);
    }

    @Override
    public InteractionResult useOnBlock(UseOnContext context, ImmutableBlockState state) {
        Player player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;
        World world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockEntity blockEntity = world.storageWorld().getBlockEntityAtIfLoaded(pos);
        if (!(blockEntity instanceof DisplayItemBlockEntity itemDisplay && itemDisplay.isValid())) {
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

    private static class Factory implements BlockBehaviorFactory<DisplayItemBlockBehavior> {

        @Override
        public DisplayItemBlockBehavior create(CustomBlock block, Map<String, Object> arguments) {
            @SuppressWarnings("unchecked")
            Property<Direction> directionProperty = (Property<Direction>) ResourceConfigUtils.requireNonNullOrThrow(block.getProperty("facing"), "warning.config.block.behavior.display_item.missing_facing");
            Vector3f position = ResourceConfigUtils.getAsVector3f(arguments.getOrDefault("position", 0), "position");
            boolean isGlow = ResourceConfigUtils.getAsBoolean(arguments.getOrDefault("is-glow", false), "is-glow");
            boolean invisible = ResourceConfigUtils.getAsBoolean(arguments.getOrDefault("invisible", false), "invisible");
            boolean renderMapItem = ResourceConfigUtils.getAsBoolean(arguments.getOrDefault("render-map-item", true), "render-map-item"); // 地图渲染有少量开销可选启用
            Map<String, Object> sounds = ResourceConfigUtils.getAsMapOrNull(arguments.get("sounds"), "sounds");
            SoundData addItemSound = null;
            SoundData removeItemSound = null;
            SoundData rotateItemSound = null;
            if (sounds != null) {
                addItemSound = Optional.ofNullable(sounds.get("add-item")).map(obj -> SoundData.create(obj, SoundData.SoundValue.FIXED_1, SoundData.SoundValue.ranged(0.9f, 1f))).orElse(null);
                removeItemSound = Optional.ofNullable(sounds.get("remove-item")).map(obj -> SoundData.create(obj, SoundData.SoundValue.FIXED_1, SoundData.SoundValue.ranged(0.9f, 1f))).orElse(null);
                rotateItemSound = Optional.ofNullable(sounds.get("rotate-item")).map(obj -> SoundData.create(obj, SoundData.SoundValue.FIXED_1, SoundData.SoundValue.ranged(0.9f, 1f))).orElse(null);
            }
            return new DisplayItemBlockBehavior(block, position, isGlow, invisible, renderMapItem, addItemSound, removeItemSound, rotateItemSound, directionProperty);
        }
    }
}
