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
        register(net.momirealms.craftengine.core.plugin.context.condition.CommonConditions.HAS_PLAYER, HasPlayerCondition.factory());
        register(net.momirealms.craftengine.core.plugin.context.condition.CommonConditions.HAS_ITEM, HasItemCondition.factory());
        register(net.momirealms.craftengine.core.plugin.context.condition.CommonConditions.MATCH_ITEM, MatchItemCondition.factory());
        register(net.momirealms.craftengine.core.plugin.context.condition.CommonConditions.MATCH_ENTITY, MatchEntityCondition.factory());
        register(net.momirealms.craftengine.core.plugin.context.condition.CommonConditions.MATCH_BLOCK, MatchBlockCondition.factory());
        register(net.momirealms.craftengine.core.plugin.context.condition.CommonConditions.MATCH_BLOCK_PROPERTY, MatchBlockPropertyCondition.factory());
        register(net.momirealms.craftengine.core.plugin.context.condition.CommonConditions.TABLE_BONUS, TableBonusCondition.factory());
        register(net.momirealms.craftengine.core.plugin.context.condition.CommonConditions.SURVIVES_EXPLOSION, SurvivesExplosionCondition.factory());
        register(net.momirealms.craftengine.core.plugin.context.condition.CommonConditions.ANY_OF, AnyOfCondition.factory(CommonConditions::fromMap));
        register(net.momirealms.craftengine.core.plugin.context.condition.CommonConditions.ALL_OF, AllOfCondition.factory(CommonConditions::fromMap));
        register(net.momirealms.craftengine.core.plugin.context.condition.CommonConditions.ENCHANTMENT, EnchantmentCondition.factory());
        register(net.momirealms.craftengine.core.plugin.context.condition.CommonConditions.INVERTED, InvertedCondition.factory(CommonConditions::fromMap));
        register(net.momirealms.craftengine.core.plugin.context.condition.CommonConditions.FALLING_BLOCK, FallingBlockCondition.factory());
        register(net.momirealms.craftengine.core.plugin.context.condition.CommonConditions.RANDOM, RandomCondition.factory());
        register(net.momirealms.craftengine.core.plugin.context.condition.CommonConditions.DISTANCE, DistanceCondition.factory());
        register(net.momirealms.craftengine.core.plugin.context.condition.CommonConditions.PERMISSION, PermissionCondition.factory());
        register(net.momirealms.craftengine.core.plugin.context.condition.CommonConditions.EQUALS, StringEqualsCondition.factory());
        register(net.momirealms.craftengine.core.plugin.context.condition.CommonConditions.STRING_REGEX, StringRegexCondition.factory());
        register(net.momirealms.craftengine.core.plugin.context.condition.CommonConditions.STRING_EQUALS, StringEqualsCondition.factory());
        register(net.momirealms.craftengine.core.plugin.context.condition.CommonConditions.STRING_CONTAINS, StringContainsCondition.factory());
        register(net.momirealms.craftengine.core.plugin.context.condition.CommonConditions.EXPRESSION, ExpressionCondition.factory());
        register(net.momirealms.craftengine.core.plugin.context.condition.CommonConditions.IS_NULL, IsNullCondition.factory());
        register(net.momirealms.craftengine.core.plugin.context.condition.CommonConditions.HAND, HandCondition.factory());
        register(net.momirealms.craftengine.core.plugin.context.condition.CommonConditions.ON_COOLDOWN, OnCooldownCondition.factory());
        register(net.momirealms.craftengine.core.plugin.context.condition.CommonConditions.INVENTORY_HAS_ITEM, InventoryHasItemCondition.factory());
        register(net.momirealms.craftengine.core.plugin.context.condition.CommonConditions.MATCH_FURNITURE_VARIANT, MatchFurnitureVariantCondition.factory());
    }

    public static void register(Key key, ConditionFactory<Context> factory) {
        ((WritableRegistry<ConditionFactory<Context>>) BuiltInRegistries.EVENT_CONDITION_FACTORY)
                .register(ResourceKey.create(Registries.EVENT_CONDITION_FACTORY.location(), key), factory);
    }

    @SuppressWarnings("unchecked")
    public static <CTX extends Context> Condition<CTX> fromMap(Map<String, Object> map) {
        String type = ResourceConfigUtils.requireNonEmptyStringOrThrow(map.get("type"), "warning.config.event.condition.missing_type");
        boolean inverted = type.charAt(0) == '!';
        if (inverted) {
            type = type.substring(1);
        }
        Key key = Key.withDefaultNamespace(type, Key.DEFAULT_NAMESPACE);
        ConditionFactory<Context> factory = BuiltInRegistries.EVENT_CONDITION_FACTORY.getValue(key);
        if (factory == null) {
            throw new LocalizedResourceConfigException("warning.config.event.condition.invalid_type", type);
        }
        return inverted ? new InvertedCondition<>((Condition<CTX>) factory.create(map)) : (Condition<CTX>) factory.create(map);
    }
}
