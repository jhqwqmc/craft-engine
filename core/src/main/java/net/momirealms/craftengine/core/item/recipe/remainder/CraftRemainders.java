package net.momirealms.craftengine.core.item.recipe.remainder;

import net.momirealms.craftengine.core.plugin.locale.LocalizedResourceConfigException;
import net.momirealms.craftengine.core.registry.BuiltInRegistries;
import net.momirealms.craftengine.core.registry.Registries;
import net.momirealms.craftengine.core.registry.WritableRegistry;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.MiscUtils;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;
import net.momirealms.craftengine.core.util.ResourceKey;

import java.util.List;
import java.util.Map;

public class CraftRemainders {
    public static final Key FIXED = Key.of("craftengine:fixed");
    public static final Key RECIPE_BASED = Key.of("craftengine:recipe_based");
    public static final Key HURT_AND_BREAK = Key.of("craftengine:hurt_and_break");

    static {
        register(FIXED, FixedCraftRemainder.FACTORY);
        register(RECIPE_BASED, RecipeBasedCraftRemainder.FACTORY);
        register(HURT_AND_BREAK, HurtAndBreakRemainder.FACTORY);
    }

    public static void register(Key key, CraftRemainderFactory<?> factory) {
        ((WritableRegistry<CraftRemainderFactory<?>>) BuiltInRegistries.CRAFT_REMAINDER_FACTORY)
                .register(ResourceKey.create(Registries.CRAFT_REMAINDER_FACTORY.location(), key), factory);
    }

    public static CraftRemainder fromMap(Map<String, Object> map) {
        String type = ResourceConfigUtils.requireNonEmptyStringOrThrow(map.get("type"), "warning.config.item.settings.craft_remainder.missing_type");
        Key key = Key.withDefaultNamespace(type, Key.DEFAULT_NAMESPACE);
        CraftRemainderFactory<?> factory = BuiltInRegistries.CRAFT_REMAINDER_FACTORY.getValue(key);
        if (factory == null) {
            throw new LocalizedResourceConfigException("warning.config.item.settings.craft_remainder.invalid_type", type);
        }
        return factory.create(map);
    }

    public static CraftRemainder fromObject(Object obj) {
        if (obj instanceof Map<?,?> map) {
            return fromMap(MiscUtils.castToMap(map, false));
        } else if (obj instanceof List<?> list) {
            List<CraftRemainder> remainderList = ResourceConfigUtils.parseConfigAsList(list, map -> fromMap(MiscUtils.castToMap(map, false)));
            return new CompositeCraftRemainder(remainderList.toArray(new CraftRemainder[0]));
        } else if (obj != null) {
            return new FixedCraftRemainder(Key.of(obj.toString()));
        } else {
            return null;
        }
    }
}
