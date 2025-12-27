package net.momirealms.craftengine.core.plugin.context.selector;

import net.momirealms.craftengine.core.plugin.context.Condition;
import net.momirealms.craftengine.core.plugin.context.Context;
import net.momirealms.craftengine.core.plugin.locale.LocalizedResourceConfigException;
import net.momirealms.craftengine.core.registry.BuiltInRegistries;
import net.momirealms.craftengine.core.registry.Registries;
import net.momirealms.craftengine.core.registry.WritableRegistry;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.MiscUtils;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;
import net.momirealms.craftengine.core.util.ResourceKey;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.Function;

public final class PlayerSelectors {
    public static final PlayerSelectorType<? extends Context> ALL = register(Key.ce("all"), AllPlayerSelector.factory());
    public static final PlayerSelectorType<? extends Context> SELF = register(Key.ce("self"), SelfPlayerSelector.factory());

    private PlayerSelectors() {}

    public static <CTX extends Context> PlayerSelectorType<CTX> register(Key key, PlayerSelectorFactory<CTX> factory) {
        PlayerSelectorType<CTX> type = new PlayerSelectorType<>(key, factory);
        ((WritableRegistry<PlayerSelectorType<?>>) BuiltInRegistries.PLAYER_SELECTOR_TYPE)
                .register(ResourceKey.create(Registries.PLAYER_SELECTOR_TYPE.location(), key), type);
        return type;
    }

    @Nullable
    public static <CTX extends Context> PlayerSelector<CTX> fromObject(Object object, Function<Map<String, Object>, Condition<CTX>> conditionFactory) {
        switch (object) {
            case null -> {
                return null;
            }
            case Map<?, ?> map -> {
                Map<String, Object> selectorMap = MiscUtils.castToMap(map, false);
                return fromMap(selectorMap, conditionFactory);
            }
            case String target -> {
                if (target.equals("all") || target.equals("@a")) {
                    return AllPlayerSelector.all();
                } else if (target.equals("self") || target.equals("@s")) {
                    return SelfPlayerSelector.self();
                }
            }
            default -> {
            }
        }
        throw new LocalizedResourceConfigException("warning.config.selector.invalid_target", object.toString());
    }

    @SuppressWarnings("unchecked")
    public static <CTX extends Context> PlayerSelector<CTX> fromMap(Map<String, Object> map, Function<Map<String, Object>, Condition<CTX>> conditionFactory) {
        String type = ResourceConfigUtils.requireNonEmptyStringOrThrow(map.get("type"), "warning.config.selector.missing_type");
        Key key = Key.withDefaultNamespace(type, Key.DEFAULT_NAMESPACE);
        PlayerSelectorType<CTX> selectorType = (PlayerSelectorType<CTX>) BuiltInRegistries.PLAYER_SELECTOR_TYPE.getValue(key);
        if (selectorType == null) {
            throw new LocalizedResourceConfigException("warning.config.selector.invalid_type", type);
        }
        return selectorType.factory().create(map, conditionFactory);
    }
}
