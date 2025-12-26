package net.momirealms.craftengine.core.plugin.context;

import net.momirealms.craftengine.core.plugin.context.condition.*;
import net.momirealms.craftengine.core.plugin.locale.LocalizedResourceConfigException;
import net.momirealms.craftengine.core.registry.BuiltInRegistries;
import net.momirealms.craftengine.core.registry.Registries;
import net.momirealms.craftengine.core.registry.WritableRegistry;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;
import net.momirealms.craftengine.core.util.ResourceKey;

import java.util.Map;

public class CommonConditions {

    static {
        register(net.momirealms.craftengine.core.plugin.context.condition.CommonConditions.HAS_PLAYER, new HasPlayerCondition.FactoryImpl<>());
        register(net.momirealms.craftengine.core.plugin.context.condition.CommonConditions.HAS_ITEM, new HasItemCondition.FactoryImpl<>());
        register(net.momirealms.craftengine.core.plugin.context.condition.CommonConditions.MATCH_ITEM, new MatchItemCondition.FactoryImpl<>());
        register(net.momirealms.craftengine.core.plugin.context.condition.CommonConditions.MATCH_ENTITY, new MatchEntityCondition.FactoryImpl<>());
        register(net.momirealms.craftengine.core.plugin.context.condition.CommonConditions.MATCH_BLOCK, new MatchBlockCondition.FactoryImpl<>());
        register(net.momirealms.craftengine.core.plugin.context.condition.CommonConditions.MATCH_BLOCK_PROPERTY, new MatchBlockPropertyCondition.FactoryImpl<>());
        register(net.momirealms.craftengine.core.plugin.context.condition.CommonConditions.TABLE_BONUS, new TableBonusCondition.FactoryImpl<>());
        register(net.momirealms.craftengine.core.plugin.context.condition.CommonConditions.SURVIVES_EXPLOSION, new SurvivesExplosionCondition.FactoryImpl<>());
        register(net.momirealms.craftengine.core.plugin.context.condition.CommonConditions.ANY_OF, new AnyOfCondition.FactoryImpl<>(CommonConditions::fromMap));
        register(net.momirealms.craftengine.core.plugin.context.condition.CommonConditions.ALL_OF, new AllOfCondition.FactoryImpl<>(CommonConditions::fromMap));
        register(net.momirealms.craftengine.core.plugin.context.condition.CommonConditions.ENCHANTMENT, new EnchantmentCondition.Factory<>());
        register(net.momirealms.craftengine.core.plugin.context.condition.CommonConditions.INVERTED, new InvertedCondition.FactoryImpl<>(CommonConditions::fromMap));
        register(net.momirealms.craftengine.core.plugin.context.condition.CommonConditions.FALLING_BLOCK, new FallingBlockCondition.FactoryImpl<>());
        register(net.momirealms.craftengine.core.plugin.context.condition.CommonConditions.RANDOM, new RandomCondition.FactoryImpl<>());
        register(net.momirealms.craftengine.core.plugin.context.condition.CommonConditions.DISTANCE, new DistanceCondition.FactoryImpl<>());
        register(net.momirealms.craftengine.core.plugin.context.condition.CommonConditions.PERMISSION, new PermissionCondition.FactoryImpl<>());
        register(net.momirealms.craftengine.core.plugin.context.condition.CommonConditions.EQUALS, new StringEqualsCondition.FactoryImpl<>());
        register(net.momirealms.craftengine.core.plugin.context.condition.CommonConditions.STRING_REGEX, new StringRegexCondition.FactoryImpl<>());
        register(net.momirealms.craftengine.core.plugin.context.condition.CommonConditions.STRING_EQUALS, new StringEqualsCondition.FactoryImpl<>());
        register(net.momirealms.craftengine.core.plugin.context.condition.CommonConditions.STRING_CONTAINS, new StringContainsCondition.FactoryImpl<>());
        register(net.momirealms.craftengine.core.plugin.context.condition.CommonConditions.EXPRESSION, new ExpressionCondition.FactoryImpl<>());
        register(net.momirealms.craftengine.core.plugin.context.condition.CommonConditions.IS_NULL, new IsNullCondition.FactoryImpl<>());
        register(net.momirealms.craftengine.core.plugin.context.condition.CommonConditions.HAND, new HandCondition.FactoryImpl<>());
        register(net.momirealms.craftengine.core.plugin.context.condition.CommonConditions.ON_COOLDOWN, new OnCooldownCondition.FactoryImpl<>());
        register(net.momirealms.craftengine.core.plugin.context.condition.CommonConditions.INVENTORY_HAS_ITEM, new InventoryHasItemCondition.FactoryImpl<>());
        register(net.momirealms.craftengine.core.plugin.context.condition.CommonConditions.MATCH_FURNITURE_VARIANT, new MatchFurnitureVariantCondition.FactoryImpl<>());
    }

    public static void register(Key key, ConditionFactory<Context> factory) {
        ((WritableRegistry<ConditionFactory<Context>>) BuiltInRegistries.EVENT_CONDITION_FACTORY)
                .register(ResourceKey.create(Registries.EVENT_CONDITION_FACTORY.location(), key), factory);
    }

    @SuppressWarnings("unchecked")
    public static <CTX extends Context> Condition<CTX> fromMap(Map<String, Object> map) {
        String type = ResourceConfigUtils.requireNonEmptyStringOrThrow(map.get("type"), "warning.config.event.condition.missing_type");
        Key key = Key.withDefaultNamespace(type, Key.DEFAULT_NAMESPACE);
        if (type.charAt(0) == '!') {
            ConditionFactory<Context> factory = BuiltInRegistries.EVENT_CONDITION_FACTORY.getValue(new Key(key.namespace(), key.value().substring(1)));
            if (factory == null) {
                throw new LocalizedResourceConfigException("warning.config.event.condition.invalid_type", type);
            }
            return new InvertedCondition<>((Condition<CTX>) factory.create(map));
        } else {
            ConditionFactory<Context> factory = BuiltInRegistries.EVENT_CONDITION_FACTORY.getValue(key);
            if (factory == null) {
                throw new LocalizedResourceConfigException("warning.config.event.condition.invalid_type", type);
            }
            return (Condition<CTX>) factory.create(map);
        }
    }
}
