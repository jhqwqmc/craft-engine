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

public final class CommonConditions {
    public static final ConditionType<Context> HAS_PLAYER = register(Key.ce("has_player"), HasPlayerCondition.factory());
    public static final ConditionType<Context> HAS_ITEM = register(Key.ce("has_item"), HasItemCondition.factory());
    public static final ConditionType<Context> MATCH_ITEM = register(Key.ce("match_item"), MatchItemCondition.factory());
    public static final ConditionType<Context> MATCH_ENTITY = register(Key.ce("match_entity"), MatchEntityCondition.factory());
    public static final ConditionType<Context> MATCH_BLOCK = register(Key.ce("match_block"), MatchBlockCondition.factory());
    public static final ConditionType<Context> MATCH_BLOCK_PROPERTY = register(Key.ce("match_block_property"), MatchBlockPropertyCondition.factory());
    public static final ConditionType<Context> TABLE_BONUS = register(Key.ce("table_bonus"), TableBonusCondition.factory());
    public static final ConditionType<Context> SURVIVES_EXPLOSION = register(Key.ce("survives_explosion"), SurvivesExplosionCondition.factory());
    public static final ConditionType<Context> ANY_OF = register(Key.ce("any_of"), AnyOfCondition.factory(CommonConditions::fromMap));
    public static final ConditionType<Context> ALL_OF = register(Key.ce("all_of"), AllOfCondition.factory(CommonConditions::fromMap));
    public static final ConditionType<Context> ENCHANTMENT = register(Key.ce("enchantment"), EnchantmentCondition.factory());
    public static final ConditionType<Context> INVERTED = register(Key.ce("inverted"), InvertedCondition.factory(CommonConditions::fromMap));
    public static final ConditionType<Context> FALLING_BLOCK = register(Key.ce("falling_block"), FallingBlockCondition.factory());
    public static final ConditionType<Context> RANDOM = register(Key.ce("random"), RandomCondition.factory());
    public static final ConditionType<Context> DISTANCE = register(Key.ce("distance"), DistanceCondition.factory());
    public static final ConditionType<Context> PERMISSION = register(Key.ce("permission"), PermissionCondition.factory());
    public static final ConditionType<Context> EQUALS = register(Key.ce("equals"), StringEqualsCondition.factory());
    public static final ConditionType<Context> REGEX = register(Key.ce("regex"), StringRegexCondition.factory());
    public static final ConditionType<Context> STRING_EQUALS = register(Key.ce("string_equals"), StringEqualsCondition.factory());
    public static final ConditionType<Context> STRING_CONTAINS = register(Key.ce("string_contains"), StringContainsCondition.factory());
    public static final ConditionType<Context> EXPRESSION = register(Key.ce("expression"), ExpressionCondition.factory());
    public static final ConditionType<Context> IS_NULL = register(Key.ce("is_null"), IsNullCondition.factory());
    public static final ConditionType<Context> HAND = register(Key.ce("hand"), HandCondition.factory());
    public static final ConditionType<Context> ON_COOLDOWN = register(Key.ce("on_cooldown"), OnCooldownCondition.factory());
    public static final ConditionType<Context> INVENTORY_HAS_ITEM = register(Key.ce("inventory_has_item"), InventoryHasItemCondition.factory());
    public static final ConditionType<Context> MATCH_FURNITURE_VARIANT = register(Key.ce("match_furniture_variant"), MatchFurnitureVariantCondition.factory());

    private CommonConditions() {}

    public static <CTX extends Context> ConditionType<CTX> register(Key key, ConditionFactory<CTX> factory) {
        ConditionType<CTX> type = new ConditionType<>(key, factory);
        ((WritableRegistry<ConditionType<?>>) BuiltInRegistries.COMMON_CONDITION_TYPE)
                .register(ResourceKey.create(Registries.COMMON_CONDITION_TYPE.location(), key), type);
        return type;
    }

    @SuppressWarnings("unchecked")
    public static <CTX extends Context> Condition<CTX> fromMap(Map<String, Object> map) {
        String type = ResourceConfigUtils.requireNonEmptyStringOrThrow(map.get("type"), "warning.config.event.condition.missing_type");
        boolean inverted = type.charAt(0) == '!';
        if (inverted) {
            type = type.substring(1);
        }
        Key key = Key.withDefaultNamespace(type, Key.DEFAULT_NAMESPACE);
        ConditionType<Context> conditionType = (ConditionType<Context>) BuiltInRegistries.COMMON_CONDITION_TYPE.getValue(key);
        if (conditionType == null) {
            throw new LocalizedResourceConfigException("warning.config.event.condition.invalid_type", type);
        }
        return inverted ? new InvertedCondition<>((Condition<CTX>) conditionType.factory().create(map)) : (Condition<CTX>) conditionType.factory().create(map);
    }
}
