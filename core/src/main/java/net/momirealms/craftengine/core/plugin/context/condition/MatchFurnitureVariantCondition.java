package net.momirealms.craftengine.core.plugin.context.condition;

import net.momirealms.craftengine.core.entity.furniture.Furniture;
import net.momirealms.craftengine.core.plugin.context.Condition;
import net.momirealms.craftengine.core.plugin.context.Context;
import net.momirealms.craftengine.core.plugin.context.parameter.DirectContextParameters;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.MiscUtils;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;

import java.util.*;

public class MatchFurnitureVariantCondition<CTX extends Context> implements Condition<CTX> {
    private final Set<String> variants;

    public MatchFurnitureVariantCondition(Collection<String> variants) {
        this.variants = new HashSet<>(variants);
    }

    @Override
    public Key type() {
        return CommonConditions.MATCH_FURNITURE_VARIANT;
    }

    @Override
    public boolean test(CTX ctx) {
        Optional<Furniture> furniture = ctx.getOptionalParameter(DirectContextParameters.FURNITURE);
        return furniture.filter(value -> this.variants.contains(value.getCurrentVariant().name())).isPresent();
    }

    public static class FactoryImpl<CTX extends Context> implements ConditionFactory<CTX> {

        @Override
        public Condition<CTX> create(Map<String, Object> arguments) {
            List<String> variants = MiscUtils.getAsStringList(ResourceConfigUtils.get(arguments, "variant", "variants"));
            return new MatchFurnitureVariantCondition<>(variants);
        }
    }
}
