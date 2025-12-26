package net.momirealms.craftengine.core.plugin.context.number;

import net.momirealms.craftengine.core.plugin.context.Context;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.MiscUtils;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class GaussianNumberProvider implements NumberProvider {
    public static final NumberProviderFactory FACTORY = new Factory();
    private final double min;
    private final double max;
    private final double mean;
    private final double stdDev;
    private final int maxAttempts;

    public GaussianNumberProvider(double min, double max, double mean, double stdDev, int maxAttempts) {
        this.min = min;
        this.max = max;
        this.mean = mean;
        this.stdDev = stdDev;
        this.maxAttempts = maxAttempts;
        validateParameters();
    }

    private void validateParameters() {
        if (this.min >= this.max) {
            throw new IllegalArgumentException("min must be less than max");
        }
        if (this.stdDev <= 0) {
            throw new IllegalArgumentException("std-dev must be greater than 0");
        }
        if (this.maxAttempts <= 0) {
            throw new IllegalArgumentException("max-attempts must be greater than 0");
        }
    }

    @Override
    public float getFloat(Context context) {
        return (float) getDouble(context);
    }

    @Override
    public double getDouble(Context context) {
        Random random = ThreadLocalRandom.current();
        int attempts = 0;
        while (attempts < maxAttempts) {
            double value = random.nextGaussian() * stdDev + mean;
            if (value >= min && value <= max) {
                return value;
            }
            attempts++;
        }
        return MiscUtils.clamp(this.mean, this.min, this.max);
    }

    @Override
    public Key type() {
        return NumberProviders.GAUSSIAN;
    }

    public double min() {
        return min;
    }

    public double max() {
        return max;
    }

    public int maxAttempts() {
        return maxAttempts;
    }

    public double mean() {
        return mean;
    }

    public double stdDev() {
        return stdDev;
    }

    private static class Factory implements NumberProviderFactory {

        @Override
        public NumberProvider create(Map<String, Object> arguments) {
            double min = ResourceConfigUtils.getAsDouble(ResourceConfigUtils.requireNonNullOrThrow(arguments.get("min"), "warning.config.number.gaussian.missing_min"), "min");
            double max = ResourceConfigUtils.getAsDouble(ResourceConfigUtils.requireNonNullOrThrow(arguments.get("max"), "warning.config.number.gaussian.missing_max"), "max");
            double mean = ResourceConfigUtils.getAsDouble(arguments.getOrDefault("mean", (min + max) / 2.0), "mean");
            double stdDev = ResourceConfigUtils.getAsDouble(arguments.getOrDefault("std-dev", (max - min) / 6.0), "std-dev");
            int maxAttempts = ResourceConfigUtils.getAsInt(arguments.getOrDefault("max-attempts", 128), "max-attempts");
            return new GaussianNumberProvider(min, max, mean, stdDev, maxAttempts);
        }
    }

    @Override
    public String toString() {
        return String.format("GaussianNumberProvider{min=%.2f, max=%.2f, mean=%.2f, stdDev=%.2f, maxAttempts=%d}",
                min, max, mean, stdDev, maxAttempts);
    }
}