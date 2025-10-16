package net.momirealms.craftengine.core.item.recipe.remainder;

import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.MiscUtils;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecipeBasedCraftRemainder implements CraftRemainder {
    public static final Factory FACTORY = new Factory();
    private final Map<Key, CraftRemainder> remainders;

    public RecipeBasedCraftRemainder(Map<Key, CraftRemainder> remainders) {
        this.remainders = remainders;
    }

    @Override
    public <T> Item<T> remainder(Key recipeId, Item<T> item) {
        CraftRemainder remainder = this.remainders.get(recipeId);
        if (remainder != null) {
            return remainder.remainder(recipeId, item);
        }
        return null;
    }

    public static class Factory implements CraftRemainderFactory {

        @Override
        public CraftRemainder create(Map<String, Object> args) {
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
            return new RecipeBasedCraftRemainder(remainders);
        }

        public record GroupedRemainder(List<Key> recipes, CraftRemainder remainder) {
        }
    }
}
