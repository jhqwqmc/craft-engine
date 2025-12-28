package net.momirealms.craftengine.core.plugin.context.number;

import net.momirealms.craftengine.core.plugin.context.Context;
import net.momirealms.craftengine.core.util.RandomUtils;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;

import java.util.Map;

public record BinomialNumberProvider(NumberProvider trials, NumberProvider successProbability) implements NumberProvider {
    public static final NumberProviderFactory<BinomialNumberProvider> FACTORY = new Factory();

    @Override
    public float getFloat(Context context) {
        return getInt(context);
    }

    @Override
    public double getDouble(Context context) {
        return getInt(context);
    }

    @Override
    public int getInt(Context context) {
        int trialCount = this.trials.getInt(context);
        float probability = this.successProbability.getFloat(context);
        int successCount = 0;

        for (int i = 0; i < trialCount; i++) {
            if (RandomUtils.generateRandomFloat(0, 1) < probability) {
                successCount++;
            }
        }
        return successCount;
    }

    private static class Factory implements NumberProviderFactory<BinomialNumberProvider> {

        @Override
        public BinomialNumberProvider create(Map<String, Object> arguments) {
            Object trials = ResourceConfigUtils.requireNonNullOrThrow(arguments.get("extra"), "warning.config.number.binomial.missing_extra");
            Object successProbability = ResourceConfigUtils.requireNonNullOrThrow(arguments.get("probability"), "warning.config.number.binomial.missing_probability");
            return new BinomialNumberProvider(NumberProviders.fromObject(trials), NumberProviders.fromObject(successProbability));
        }
    }
}
