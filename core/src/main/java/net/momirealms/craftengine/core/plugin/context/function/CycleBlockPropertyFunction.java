package net.momirealms.craftengine.core.plugin.context.function;

import net.momirealms.craftengine.core.block.BlockStateWrapper;
import net.momirealms.craftengine.core.block.UpdateOption;
import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.plugin.context.Condition;
import net.momirealms.craftengine.core.plugin.context.Context;
import net.momirealms.craftengine.core.plugin.context.number.NumberProvider;
import net.momirealms.craftengine.core.plugin.context.number.NumberProviders;
import net.momirealms.craftengine.core.plugin.context.parameter.DirectContextParameters;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.MiscUtils;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;
import net.momirealms.craftengine.core.world.ExistingBlock;
import net.momirealms.craftengine.core.world.World;
import net.momirealms.craftengine.core.world.WorldPosition;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class CycleBlockPropertyFunction<CTX extends Context> extends AbstractConditionalFunction<CTX> {
    private final String property;
    private final NumberProvider x;
    private final NumberProvider y;
    private final NumberProvider z;
    private final NumberProvider updateFlags;

    public CycleBlockPropertyFunction(List<Condition<CTX>> predicates, String property, NumberProvider x, NumberProvider y, NumberProvider z, NumberProvider updateFlags) {
        super(predicates);
        this.property = property;
        this.x = x;
        this.y = y;
        this.z = z;
        this.updateFlags = updateFlags;
    }

    @Override
    protected void runInternal(CTX ctx) {
        Optional<WorldPosition> optionalWorldPosition = ctx.getOptionalParameter(DirectContextParameters.POSITION);
        if (optionalWorldPosition.isEmpty()) return;
        World world = optionalWorldPosition.get().world();
        int x = MiscUtils.fastFloor(this.x.getDouble(ctx));
        int y = MiscUtils.fastFloor(this.y.getDouble(ctx));
        int z = MiscUtils.fastFloor(this.z.getDouble(ctx));
        ExistingBlock blockAt = world.getBlockAt(x, y, z);
        boolean isSecondaryUseActive = ctx.getOptionalParameter(DirectContextParameters.PLAYER)
                .map(Player::isSecondaryUseActive)
                .orElse(false);
        BlockStateWrapper wrapper = blockAt.blockState().cycleProperty(this.property, isSecondaryUseActive);
        world.setBlockAt(x, y, z, wrapper, this.updateFlags.getInt(ctx));
    }

    @Override
    public Key type() {
        return CommonFunctions.CYCLE_BLOCK_PROPERTY;
    }

    public static class FactoryImpl<CTX extends Context> extends AbstractFactory<CTX> {

        public FactoryImpl(java.util.function.Function<Map<String, Object>, Condition<CTX>> factory) {
            super(factory);
        }

        @Override
        public Function<CTX> create(Map<String, Object> arguments) {
            return new CycleBlockPropertyFunction<>(getPredicates(arguments),
                    ResourceConfigUtils.requireNonEmptyStringOrThrow(arguments.get("property"), "warning.config.function.cycle_block_property.missing_property"),
                    NumberProviders.fromObject(arguments.getOrDefault("x", "<arg:position.x>")),
                    NumberProviders.fromObject(arguments.getOrDefault("y", "<arg:position.y>")),
                    NumberProviders.fromObject(arguments.getOrDefault("z", "<arg:position.z>")),
                    Optional.ofNullable(arguments.get("update-flags")).map(NumberProviders::fromObject).orElse(NumberProviders.direct(UpdateOption.UPDATE_ALL.flags())));
        }
    }
}
