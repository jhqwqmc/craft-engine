package net.momirealms.craftengine.core.item.recipe;

import com.google.gson.JsonObject;
import net.momirealms.craftengine.core.item.recipe.input.CraftingInput;
import net.momirealms.craftengine.core.item.recipe.input.RecipeInput;
import net.momirealms.craftengine.core.item.recipe.result.CustomRecipeResult;
import net.momirealms.craftengine.core.plugin.context.Condition;
import net.momirealms.craftengine.core.plugin.context.Context;
import net.momirealms.craftengine.core.plugin.context.function.Function;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.MiscUtils;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class CustomShapelessRecipe<T> extends CustomCraftingTableRecipe<T> {
    public static final Serializer<?> SERIALIZER = new Serializer<>();
    private final List<Ingredient<T>> ingredients;
    private final PlacementInfo<T> placementInfo;
    private final boolean takeAdditionalIngredients;

    public CustomShapelessRecipe(Key id,
                                 boolean showNotification,
                                 CustomRecipeResult<T> result,
                                 CustomRecipeResult<T> visualResult,
                                 String group,
                                 CraftingRecipeCategory category,
                                 List<Ingredient<T>> ingredients,
                                 Function<Context>[] craftingFunctions,
                                 Condition<Context> craftingCondition,
                                 boolean alwaysRebuildOutput,
                                 boolean takeAdditionalIngredients) {
        super(id, showNotification, result, visualResult, group, category, craftingFunctions, craftingCondition, alwaysRebuildOutput);
        this.ingredients = ingredients;
        this.placementInfo = PlacementInfo.create(ingredients);
        this.takeAdditionalIngredients = takeAdditionalIngredients;
    }

    public PlacementInfo<T> placementInfo() {
        return this.placementInfo;
    }

    @Override
    public List<Ingredient<T>> ingredientsInUse() {
        return this.ingredients;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean matches(RecipeInput input) {
        return matches((CraftingInput<T>) input);
    }

    private boolean matches(CraftingInput<T> input) {
        if (input.ingredientCount() != this.ingredients.size()) {
            return false;
        }
        if (input.size() == 1 && this.ingredients.size() == 1) {
            return this.ingredients.getFirst().test(input.getItem(0));
        }
        return input.finder().canCraft(this);
    }

    public boolean takeAdditionalIngredients() {
        return this.takeAdditionalIngredients;
    }

    public void takeAdditionalIngredients(CraftingInput<T> input, int left) {
        if (input.ingredientCount() != this.ingredients.size()) {
            return;
        }
        if (input.size() == 1 && this.ingredients.size() == 1) {
            UniqueIdItem<T> item = input.getItem(0);
            Ingredient<T> first = this.ingredients.getFirst();
            item.item().shrink(first.count() - left);
            return;
        }
        List<UniqueIdItem<T>> inputItems = new ArrayList<>(input.items);
        // 数量多的排前面
        inputItems.sort((o1, o2) -> Integer.compare(o2.item().count(), o1.item().count()));
        boolean[] taken = new boolean[inputItems.size()];
        outer:
        for (Ingredient<T> ingredient : this.ingredients) {
            for (int j = 0; j < taken.length; j++) {
                if (!taken[j]) {
                    UniqueIdItem<T> inputItem = inputItems.get(j);
                    if (ingredient.test(inputItem)) {
                        int toShrink = ingredient.count() - left;
                        if (toShrink > 0) {
                            inputItem.item().shrink(toShrink);
                        }
                        taken[j] = true;
                        continue outer;
                    }
                }
            }
        }
    }

    @Override
    public @NotNull Key serializerType() {
        return RecipeSerializers.SHAPELESS;
    }

    public static class Serializer<A> extends AbstractRecipeSerializer<A, CustomShapelessRecipe<A>> {

        @SuppressWarnings({"unchecked", "rawtypes", "DuplicatedCode"})
        @Override
        public CustomShapelessRecipe<A> readMap(Key id, Map<String, Object> arguments) {
            List<Ingredient<A>> ingredients = new ArrayList<>();
            boolean hasAdditionalInput = false;
            Object ingredientsObject = getIngredientOrThrow(arguments);
            if (ingredientsObject instanceof Map<?,?> map) {
                for (Map.Entry<String, Object> entry : (MiscUtils.castToMap(map, false)).entrySet()) {
                    if (entry.getValue() == null) continue;
                    Ingredient<A> in = parseIngredient(entry.getValue());
                    ingredients.add(in);
                    if (in.count() > 1) {
                        hasAdditionalInput = true;
                    }
                }
            } else if (ingredientsObject instanceof List<?> list) {
                for (Object obj : list) {
                    Ingredient<A> in = parseIngredient(obj);
                    ingredients.add(in);
                    if (in.count() > 1) {
                        hasAdditionalInput = true;
                    }
                }
            } else {
                Ingredient<A> ingredient = parseIngredient(ingredientsObject);
                ingredients.add(ingredient);
                if (ingredient.count() > 1) {
                    hasAdditionalInput = true;
                }
            }
            // 按照数量从多到少排序
            if (hasAdditionalInput) {
                ingredients.sort((o1, o2) -> Integer.compare(o2.count(), o1.count()));
            }
            return new CustomShapelessRecipe(id,
                    showNotification(arguments),
                    parseResult(arguments),
                    parseVisualResult(arguments),
                    arguments.containsKey("group") ? arguments.get("group").toString() : null, craftingRecipeCategory(arguments),
                    ingredients,
                    functions(arguments),
                    conditions(arguments),
                    ResourceConfigUtils.getAsBoolean(arguments.getOrDefault("always-rebuild-result", true), "always-rebuild-result"),
                    hasAdditionalInput
            );
        }

        @Override
        public CustomShapelessRecipe<A> readJson(Key id, JsonObject json) {
            return new CustomShapelessRecipe<>(id,
                    true,
                    parseResult(VANILLA_RECIPE_HELPER.craftingResult(json.get("result"))),
                    null,
                    VANILLA_RECIPE_HELPER.readGroup(json), VANILLA_RECIPE_HELPER.craftingCategory(json),
                    VANILLA_RECIPE_HELPER.shapelessIngredients(json.getAsJsonArray("ingredients")).stream().map(this::toIngredient).toList(),
                    null,
                    null,
                    false,
                    false
            );
        }
    }
}
