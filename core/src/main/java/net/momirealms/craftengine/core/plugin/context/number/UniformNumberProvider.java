package net.momirealms.craftengine.core.plugin.context.number;

import net.momirealms.craftengine.core.plugin.context.Context;
import net.momirealms.craftengine.core.util.RandomUtils;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;

import java.util.Map;

public record UniformNumberProvider(NumberProvider min, NumberProvider max) implements NumberProvider {
    public static final NumberProviderFactory<UniformNumberProvider> FACTORY = new Factory();

    @Override
    public int getInt(Context context) {
        return RandomUtils.generateRandomInt(this.min.getInt(context), this.max.getInt(context) + 1);
    }

    @Override
    public double getDouble(Context context) {
        return RandomUtils.generateRandomDouble(this.min.getDouble(context), this.max.getDouble(context));
    }

    @Override
    public float getFloat(Context context) {
        return RandomUtils.generateRandomFloat(this.min.getFloat(context), this.max.getFloat(context));
    }

    private static class Factory implements NumberProviderFactory<UniformNumberProvider> {

        @Override
        public UniformNumberProvider create(Map<String, Object> arguments) {
            Object min = ResourceConfigUtils.requireNonNullOrThrow(arguments.get("min"), "warning.config.number.uniform.missing_min");
            Object max = ResourceConfigUtils.requireNonNullOrThrow(arguments.get("max"), "warning.config.number.uniform.missing_max");
            return new UniformNumberProvider(NumberProviders.fromObject(min), NumberProviders.fromObject(max));
        }
    }
}
