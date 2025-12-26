package net.momirealms.craftengine.bukkit.block.behavior;

import net.momirealms.craftengine.bukkit.item.BukkitItemManager;
import net.momirealms.craftengine.bukkit.nms.FastNMS;
import net.momirealms.craftengine.bukkit.util.BlockStateUtils;
import net.momirealms.craftengine.bukkit.util.LocationUtils;
import net.momirealms.craftengine.bukkit.world.BukkitWorldManager;
import net.momirealms.craftengine.core.block.BlockSettings;
import net.momirealms.craftengine.core.block.CustomBlock;
import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.block.behavior.BlockBehavior;
import net.momirealms.craftengine.core.block.behavior.BlockBehaviorFactory;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.loot.LootContext;
import net.momirealms.craftengine.core.plugin.context.Condition;
import net.momirealms.craftengine.core.plugin.context.Context;
import net.momirealms.craftengine.core.plugin.context.ContextHolder;
import net.momirealms.craftengine.core.plugin.context.event.EventConditions;
import net.momirealms.craftengine.core.plugin.context.number.NumberProvider;
import net.momirealms.craftengine.core.plugin.context.number.NumberProviders;
import net.momirealms.craftengine.core.plugin.context.parameter.DirectContextParameters;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.MiscUtils;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;
import net.momirealms.craftengine.core.world.BlockPos;
import net.momirealms.craftengine.core.world.Vec3d;
import net.momirealms.craftengine.core.world.World;
import net.momirealms.craftengine.core.world.WorldPosition;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.Predicate;

public class DropExperienceBlockBehavior extends BukkitBlockBehavior {
    public static final Key ID = Key.from("craftengine:drop_experience_block");
    public static final BlockBehaviorFactory FACTORY = new Factory();
    private final NumberProvider amount;
    private final Predicate<Context> condition;

    public DropExperienceBlockBehavior(CustomBlock customBlock, NumberProvider amount, Predicate<Context> condition) {
        super(customBlock);
        this.amount = amount;
        this.condition = condition;
    }

    @Override
    public void spawnAfterBreak(Object thisBlock, Object[] args, Callable<Object> superMethod) {
        boolean dropExperience = (boolean) args[4]; // 通常来说是 false
        Item<ItemStack> item = BukkitItemManager.instance().wrap(FastNMS.INSTANCE.method$CraftItemStack$asCraftMirror(args[3]));
        if (!dropExperience) {
            ImmutableBlockState state = BlockStateUtils.getOptionalCustomBlockState(args[0]).orElse(null);
            if (state == null) {
                return;
            }
            BlockSettings settings = state.settings();
            if (settings.requireCorrectTool()) {
                if (item.isEmpty()) {
                    return;
                }
                boolean cannotBreak = !settings.isCorrectTool(item.id())
                        && (!settings.respectToolComponent()
                        || !FastNMS.INSTANCE.method$ItemStack$isCorrectToolForDrops(args[3], state.customBlockState().literalObject()));
                if (cannotBreak) {
                    return;
                }
            }
        }
        World world = BukkitWorldManager.instance().wrap(FastNMS.INSTANCE.method$Level$getCraftWorld(args[1]));
        BlockPos pos = LocationUtils.fromBlockPos(args[2]);
        tryDropExperience(world, pos, item);
    }

    private void tryDropExperience(World world, BlockPos pos, Item<ItemStack> item) {
        Vec3d dropPos = Vec3d.atCenterOf(pos);
        ContextHolder holder = ContextHolder.builder()
                .withParameter(DirectContextParameters.POSITION, new WorldPosition(world, dropPos))
                .withParameter(DirectContextParameters.ITEM_IN_HAND, item)
                .build();
        LootContext context = new LootContext(world, null, 1.0f, holder);
        if (!this.condition.test(context)) {
            return;
        }
        int finalAmount = this.amount.getInt(context);
        if (finalAmount <= 0) {
            return;
        }
        world.dropExp(dropPos, finalAmount);
    }

    private static class Factory implements BlockBehaviorFactory {

        @Override
        public BlockBehavior create(CustomBlock block, Map<String, Object> arguments) {
            NumberProvider amount = NumberProviders.fromObject(ResourceConfigUtils.get(arguments, "amount", "count"));
            List<Condition<Context>> conditionList = ResourceConfigUtils.parseConfigAsList(ResourceConfigUtils.get(arguments, "conditions", "condition"), EventConditions::fromMap);
            return new DropExperienceBlockBehavior(block, amount, MiscUtils.allOf(conditionList));
        }
    }
}
