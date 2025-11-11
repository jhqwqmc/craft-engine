package net.momirealms.craftengine.core.item.recipe;

import net.momirealms.craftengine.core.util.Key;

public sealed interface IngredientElement permits IngredientElement.Item, IngredientElement.Tag {

    record Item(Key id) implements IngredientElement {
    }

    record Tag(Key tag) implements IngredientElement {
    }
}
