package net.momirealms.craftengine.core.item.recipe;

import net.momirealms.craftengine.core.item.recipe.input.RecipeInput;
import net.momirealms.craftengine.core.item.recipe.input.SingleItemInput;
import net.momirealms.craftengine.core.item.recipe.result.CustomRecipeResult;
import net.momirealms.craftengine.core.util.Key;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public abstract class CustomCookingRecipe<T> extends AbstractGroupedRecipe<T> {
    protected final CookingRecipeCategory category;
    protected final Ingredient<T> ingredient;
    protected final float experience;
    protected final int cookingTime;

    protected CustomCookingRecipe(Key id,
                                  boolean showNotification,
                                  CustomRecipeResult<T> result,
                                  String group,
                                  CookingRecipeCategory category,
                                  Ingredient<T> ingredient,
                                  int cookingTime,
                                  float experience) {
        super(id, showNotification, result, group);
        this.category = category == null ? CookingRecipeCategory.MISC : category;
        this.ingredient = ingredient;
        this.experience = experience;
        this.cookingTime = cookingTime;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean matches(RecipeInput input) {
        return this.ingredient.test(((SingleItemInput<T>) input).input());
    }

    @SuppressWarnings("unchecked")
    @Override
    public void takeInput(@NotNull RecipeInput in, int ignore) {
        SingleItemInput<T> input = (SingleItemInput<T>) in;
        takeIngredient(this.ingredient, input.input().item(), ignore);
    }

    public CookingRecipeCategory category() {
        return this.category;
    }

    public Ingredient<T> ingredient() {
        return this.ingredient;
    }

    public float experience() {
        return this.experience;
    }

    public int cookingTime() {
        return this.cookingTime;
    }

    @Override
    public List<Ingredient<T>> ingredientsInUse() {
        return List.of(this.ingredient);
    }
}
