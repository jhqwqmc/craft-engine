package net.momirealms.craftengine.core.loot;

import net.momirealms.craftengine.core.plugin.context.Condition;
import net.momirealms.craftengine.core.plugin.context.condition.*;
import net.momirealms.craftengine.core.plugin.locale.LocalizedResourceConfigException;
import net.momirealms.craftengine.core.registry.BuiltInRegistries;
import net.momirealms.craftengine.core.registry.Registries;
import net.momirealms.craftengine.core.registry.WritableRegistry;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;
import net.momirealms.craftengine.core.util.ResourceKey;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class LootConditions {

    static {
        register(CommonConditions.MATCH_ITEM, MatchItemCondition.factory());
        register(CommonConditions.MATCH_BLOCK_PROPERTY, MatchBlockPropertyCondition.factory());
        register(CommonConditions.TABLE_BONUS, TableBonusCondition.factory());
        register(CommonConditions.SURVIVES_EXPLOSION, SurvivesExplosionCondition.factory());
        register(CommonConditions.ANY_OF, AnyOfCondition.factory(LootConditions::fromMap));
        register(CommonConditions.ALL_OF, AllOfCondition.factory(LootConditions::fromMap));
        register(CommonConditions.HAS_PLAYER, HasPlayerCondition.factory());
        register(CommonConditions.HAS_ITEM, HasItemCondition.factory());
        register(CommonConditions.ENCHANTMENT, EnchantmentCondition.factory());
        register(CommonConditions.INVERTED, InvertedCondition.factory(LootConditions::fromMap));
        register(CommonConditions.FALLING_BLOCK, FallingBlockCondition.factory());
        register(CommonConditions.RANDOM, RandomCondition.factory());
        register(CommonConditions.DISTANCE, DistanceCondition.factory());
        register(CommonConditions.PERMISSION, PermissionCondition.factory());
        register(CommonConditions.EQUALS, StringEqualsCondition.factory());
        register(CommonConditions.STRING_REGEX, StringRegexCondition.factory());
        register(CommonConditions.STRING_EQUALS, StringEqualsCondition.factory());
        register(CommonConditions.STRING_CONTAINS, StringContainsCondition.factory());
        register(CommonConditions.EXPRESSION, ExpressionCondition.factory());
        register(CommonConditions.IS_NULL, IsNullCondition.factory());
        register(CommonConditions.HAND, HandCondition.factory());
        register(CommonConditions.ON_COOLDOWN, OnCooldownCondition.factory());
        register(CommonConditions.INVENTORY_HAS_ITEM, InventoryHasItemCondition.factory());
        register(CommonConditions.MATCH_FURNITURE_VARIANT, MatchFurnitureVariantCondition.factory());
    }

    public static void register(Key key, ConditionFactory<LootContext> factory) {
        ((WritableRegistry<ConditionFactory<LootContext>>) BuiltInRegistries.LOOT_CONDITION_FACTORY)
                .register(ResourceKey.create(Registries.LOOT_CONDITION_FACTORY.location(), key), factory);
    }

    public static <T> Predicate<T> andConditions(List<? extends Predicate<T>> predicates) {
        List<Predicate<T>> list = List.copyOf(predicates);
        return switch (list.size()) {
            case 0 -> ctx -> true;
            case 1 -> list.get(0);
            case 2 -> list.get(0).and(list.get(1));
            default -> (ctx -> {
                for (Predicate<T> predicate : list) {
                    if (!predicate.test(ctx)) {
                        return false;
                    }
                }
                return true;
            });
        };
    }

    public static <T> Predicate<T> orConditions(List<? extends Predicate<T>> predicates) {
        List<Predicate<T>> list = List.copyOf(predicates);
        return switch (list.size()) {
            case 0 -> ctx -> false;
            case 1 -> list.get(0);
            case 2 -> list.get(0).or(list.get(1));
            default -> (ctx -> {
                for (Predicate<T> predicate : list) {
                    if (predicate.test(ctx)) {
                        return true;
                    }
                }
                return false;
            });
        };
    }

    public static List<Condition<LootContext>> fromMapList(List<Map<String, Object>> mapList) {
        if (mapList == null || mapList.isEmpty()) return List.of();
        List<Condition<LootContext>> functions = new ArrayList<>();
        for (Map<String, Object> map : mapList) {
            functions.add(fromMap(map));
        }
        return functions;
    }

    public static Condition<LootContext> fromMap(Map<String, Object> map) {
        String type = ResourceConfigUtils.requireNonEmptyStringOrThrow(map.get("type"), "warning.config.loot_table.condition.missing_type");
        Key key = Key.withDefaultNamespace(type, Key.DEFAULT_NAMESPACE);
        if (key.value().charAt(0) == '!') {
            ConditionFactory<LootContext> factory = BuiltInRegistries.LOOT_CONDITION_FACTORY.getValue(new Key(key.namespace(), key.value().substring(1)));
            if (factory == null) {
                throw new LocalizedResourceConfigException("warning.config.loot_table.condition.invalid_type", type);
            }
            return new InvertedCondition<>(factory.create(map));
        } else {
            ConditionFactory<LootContext> factory = BuiltInRegistries.LOOT_CONDITION_FACTORY.getValue(key);
            if (factory == null) {
                throw new LocalizedResourceConfigException("warning.config.loot_table.condition.invalid_type", type);
            }
            return factory.create(map);
        }
    }
}
