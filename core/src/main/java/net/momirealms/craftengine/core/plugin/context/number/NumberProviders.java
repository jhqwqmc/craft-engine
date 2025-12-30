package net.momirealms.craftengine.core.plugin.context.number;

import net.momirealms.craftengine.core.plugin.locale.LocalizedResourceConfigException;
import net.momirealms.craftengine.core.registry.BuiltInRegistries;
import net.momirealms.craftengine.core.registry.Registries;
import net.momirealms.craftengine.core.registry.WritableRegistry;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;
import net.momirealms.craftengine.core.util.ResourceKey;

import java.util.Map;

public final class NumberProviders {
    public static final NumberProviderType<FixedNumberProvider> FIXED = register(Key.ce("fixed"), FixedNumberProvider.FACTORY);
    public static final NumberProviderType<FixedNumberProvider> CONSTANT = register(Key.ce("constant"), FixedNumberProvider.FACTORY);
    public static final NumberProviderType<UniformNumberProvider> UNIFORM = register(Key.ce("uniform"), UniformNumberProvider.FACTORY);
    public static final NumberProviderType<ExpressionNumberProvider> EXPRESSION = register(Key.ce("expression"), ExpressionNumberProvider.FACTORY);
    public static final NumberProviderType<GaussianNumberProvider> NORMAL = register(Key.ce("normal"), GaussianNumberProvider.FACTORY);
    public static final NumberProviderType<GaussianNumberProvider> GAUSSIAN = register(Key.ce("gaussian"), GaussianNumberProvider.FACTORY);
    public static final NumberProviderType<LogNormalNumberProvider> LOG_NORMAL = register(Key.ce("log_normal"), LogNormalNumberProvider.FACTORY);
    public static final NumberProviderType<SkewNormalNumberProvider> SKEW_NORMAL = register(Key.ce("skew_normal"), SkewNormalNumberProvider.FACTORY);
    public static final NumberProviderType<BinomialNumberProvider> BINOMIAL = register(Key.ce("binomial"), BinomialNumberProvider.FACTORY);
    public static final NumberProviderType<WeightedNumberProvider> WEIGHTED = register(Key.ce("weighted"), WeightedNumberProvider.FACTORY);
    public static final NumberProviderType<TriangleNumberProvider> TRIANGLE = register(Key.ce("triangle"), TriangleNumberProvider.FACTORY);
    public static final NumberProviderType<ExponentialNumberProvider> EXPONENTIAL = register(Key.ce("exponential"), ExponentialNumberProvider.FACTORY);
    public static final NumberProviderType<BetaNumberProvider> BETA = register(Key.ce("beta"), BetaNumberProvider.FACTORY);

    private NumberProviders() {}

    public static <T extends NumberProvider> NumberProviderType<T> register(Key key, NumberProviderFactory<T> factory) {
        NumberProviderType<T> type = new NumberProviderType<>(key, factory);
        ((WritableRegistry<NumberProviderType<? extends NumberProvider>>) BuiltInRegistries.NUMBER_PROVIDER_TYPE)
                .register(ResourceKey.create(Registries.NUMBER_PROVIDER_TYPE.location(), key), type);
        return type;
    }

    public static NumberProvider direct(double value) {
        return new FixedNumberProvider(value);
    }

    public static NumberProvider fromMap(Map<String, Object> map) {
        String type = ResourceConfigUtils.requireNonEmptyStringOrThrow(map.get("type"), "warning.config.number.missing_type");
        Key key = Key.withDefaultNamespace(type, Key.DEFAULT_NAMESPACE);
        NumberProviderType<? extends NumberProvider> providerType = BuiltInRegistries.NUMBER_PROVIDER_TYPE.getValue(key);
        if (providerType == null) {
            throw new LocalizedResourceConfigException("warning.config.number.invalid_type", type);
        }
        return providerType.factory().create(map);
    }

    @SuppressWarnings("unchecked")
    public static NumberProvider fromObject(Object object) {
        switch (object) {
            case null -> throw new LocalizedResourceConfigException("warning.config.number.missing_argument");
            case Number number -> {
                return new FixedNumberProvider(number.floatValue());
            }
            case Boolean bool -> {
                return new FixedNumberProvider(bool ? 1 : 0);
            }
            case Map<?, ?> map -> {
                return fromMap((Map<String, Object>) map);
            }
            default -> {
                String string = object.toString();
                if (string.contains("~")) {
                    int first = string.indexOf('~');
                    int second = string.indexOf('~', first + 1);
                    if (second == -1) {
                        NumberProvider min = fromObject(string.substring(0, first));
                        NumberProvider max = fromObject(string.substring(first + 1));
                        return new UniformNumberProvider(min, max);
                    } else {
                        throw new LocalizedResourceConfigException("warning.config.number.invalid_format", string);
                    }
                } else if (string.contains("<") && string.contains(">") && string.contains(":")) {
                    return new ExpressionNumberProvider(string);
                } else {
                    try {
                        return new FixedNumberProvider(Float.parseFloat(string));
                    } catch (NumberFormatException e) {
                        throw new LocalizedResourceConfigException("warning.config.number.invalid_format", e, string);
                    }
                }
            }
        }
    }
}
