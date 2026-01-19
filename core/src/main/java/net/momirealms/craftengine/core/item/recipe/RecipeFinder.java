package net.momirealms.craftengine.core.item.recipe;

import java.util.List;

public class RecipeFinder<T> {
    private final StackedContents<UniqueIdItem<T>> stackedContents = new StackedContents<>();

    public void addInput(UniqueIdItem<T> item) {
        if (!item.isEmpty()) {
            this.stackedContents.add(item, 1);
        }
    }

    public boolean canCraft(CustomShapelessRecipe<T> recipe) {
        PlacementInfo<T> placementInfo = recipe.placementInfo();
        return !placementInfo.isImpossibleToPlace() && canCraft(placementInfo.ingredients());
    }

    private boolean canCraft(List<? extends StackedContents.IngredientInfo<UniqueIdItem<T>>> rawIngredients) {
        return this.stackedContents.tryPick(rawIngredients);
    }
}
