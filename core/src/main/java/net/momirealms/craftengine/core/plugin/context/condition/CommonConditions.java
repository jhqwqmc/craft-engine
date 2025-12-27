package net.momirealms.craftengine.core.plugin.context.condition;

import net.momirealms.craftengine.core.util.Key;

public final class CommonConditions {
    private CommonConditions() {}

    public static final Key ALWAYS_TRUE = Key.ce("always_true");
    public static final Key ALWAYS_FALSE = Key.ce("always_false");
    public static final Key ALL_OF = Key.ce("all_of");
    public static final Key ANY_OF = Key.ce("any_of");
    public static final Key INVERTED = Key.ce("inverted");
    public static final Key MATCH_ITEM = Key.ce("match_item");
    public static final Key HAS_ITEM = Key.ce("has_item");
    public static final Key MATCH_ENTITY = Key.ce("match_entity");
    public static final Key MATCH_BLOCK = Key.ce("match_block");
    public static final Key MATCH_BLOCK_PROPERTY = Key.ce("match_block_property");
    public static final Key MATCH_FURNITURE_VARIANT = Key.ce("match_furniture_variant");
    public static final Key TABLE_BONUS = Key.ce("table_bonus");
    public static final Key SURVIVES_EXPLOSION = Key.ce("survives_explosion");
    public static final Key RANDOM = Key.ce("random");
    public static final Key ENCHANTMENT = Key.ce("enchantment");
    public static final Key FALLING_BLOCK = Key.ce("falling_block");
    public static final Key DISTANCE = Key.ce("distance");
    public static final Key PERMISSION = Key.ce("permission");
    public static final Key ON_COOLDOWN = Key.ce("on_cooldown");
    public static final Key EQUALS = Key.ce("equals");
    public static final Key STRING_EQUALS = Key.ce("string_equals");
    public static final Key STRING_CONTAINS = Key.ce("string_contains");
    public static final Key STRING_REGEX = Key.ce("regex");
    public static final Key EXPRESSION = Key.ce("expression");
    public static final Key IS_NULL = Key.ce("is_null");
    public static final Key HAND = Key.ce("hand");
    public static final Key HAS_PLAYER = Key.ce("has_player");
    public static final Key INVENTORY_HAS_ITEM = Key.ce("inventory_has_item");
}
