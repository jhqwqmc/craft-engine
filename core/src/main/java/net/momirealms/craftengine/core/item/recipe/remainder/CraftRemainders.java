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

public final class CraftRemainders {
    public static final CraftRemainderType<FixedCraftRemainder> FIXED = register(Key.ce("fixed"), FixedCraftRemainder.FACTORY);
    public static final CraftRemainderType<RecipeBasedCraftRemainder> RECIPE_BASED = register(Key.ce("recipe_based"), RecipeBasedCraftRemainder.FACTORY);
    public static final CraftRemainderType<HurtAndBreakRemainder> HURT_AND_BREAK = register(Key.ce("hurt_and_break"), HurtAndBreakRemainder.FACTORY);

    private CraftRemainders() {}

    public static <T extends CraftRemainder> CraftRemainderType<T> register(Key key, CraftRemainderFactory<T> factory) {
        CraftRemainderType<T> type = new CraftRemainderType<>(key, factory);
        ((WritableRegistry<CraftRemainderType<?>>) BuiltInRegistries.CRAFT_REMAINDER_TYPE)
                .register(ResourceKey.create(Registries.CRAFT_REMAINDER_TYPE.location(), key), type);
        return type;
    }

    public static CraftRemainder fromMap(Map<String, Object> map) {
        String type = ResourceConfigUtils.requireNonEmptyStringOrThrow(map.get("type"), "warning.config.item.settings.craft_remainder.missing_type");
        Key key = Key.withDefaultNamespace(type, Key.DEFAULT_NAMESPACE);
        CraftRemainderType<?> craftRemainderType = BuiltInRegistries.CRAFT_REMAINDER_TYPE.getValue(key);
        if (craftRemainderType == null) {
            throw new LocalizedResourceConfigException("warning.config.item.settings.craft_remainder.invalid_type", type);
        }
        return craftRemainderType.factory().create(map);
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
