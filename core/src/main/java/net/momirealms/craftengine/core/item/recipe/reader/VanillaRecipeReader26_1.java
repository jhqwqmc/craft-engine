package net.momirealms.craftengine.core.item.recipe.reader;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import net.momirealms.craftengine.core.item.recipe.DatapackRecipeResult;
import org.jetbrains.annotations.NotNull;

public class VanillaRecipeReader26_1 extends VanillaRecipeReader1_21_2 {

    @Override
    public @NotNull DatapackRecipeResult craftingResult(JsonElement je) {
        if (je instanceof JsonPrimitive primitive) {
            return new DatapackRecipeResult(primitive.getAsString(), 1, null);
        }
        return super.craftingResult(je);
    }
}
