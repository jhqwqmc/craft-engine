package net.momirealms.craftengine.core.item.recipe.remainder;

import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.MiscUtils;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecipeBasedCraftRemainder implements CraftRemainder {
    public static final Factory FACTORY = new Factory();
    private final Map<Key, CraftRemainder> remainders;
    @Nullable
    private final CraftRemainder fallback;

    public RecipeBasedCraftRemainder(Map<Key, CraftRemainder> remainders, @Nullable CraftRemainder fallback) {
        this.remainders = remainders;
        this.fallback = fallback;
    }

    @Override
    public <T> Item<T> remainder(Key recipeId, Item<T> item) {
        CraftRemainder remainder = this.remainders.get(recipeId);
        if (remainder != null) {
            return remainder.remainder(recipeId, item);
        }
        return this.fallback != null ? this.fallback.remainder(recipeId, item) : null;
    }

    public static class Factory implements CraftRemainderFactory<RecipeBasedCraftRemainder> {

        @Override
        public RecipeBasedCraftRemainder create(Map<String, Object> args) {
            Map<Key, CraftRemainder> remainders = new HashMap<>();
            List<GroupedRemainder> remainderList = ResourceConfigUtils.parseConfigAsList(ResourceConfigUtils.requireNonNullOrThrow(args.get("terms"), "warning.config.item.settings.craft_remainder.recipe_based.missing_terms"), map -> {
                List<Key> recipes = MiscUtils.getAsStringList(map.get("recipes")).stream().map(Key::of).toList();
                CraftRemainder remainder = CraftRemainders.fromObject(ResourceConfigUtils.get(map, "craft-remainder", "craft-remaining-item"));
                return new GroupedRemainder(recipes, remainder);
            });
            for (GroupedRemainder remainder : remainderList) {
                for (Key recipeId : remainder.recipes) {
                    remainders.put(recipeId, remainder.remainder());
                }
            }
            return new RecipeBasedCraftRemainder(remainders, CraftRemainders.fromObject(args.get("fallback")));
        }

        public record GroupedRemainder(List<Key> recipes, CraftRemainder remainder) {
        }
    }
}
