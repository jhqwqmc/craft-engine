package net.momirealms.craftengine.core.item.recipe;

import com.google.gson.JsonObject;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.item.ItemBuildContext;
import net.momirealms.craftengine.core.item.recipe.input.CraftingInput;
import net.momirealms.craftengine.core.item.recipe.input.RecipeInput;
import net.momirealms.craftengine.core.item.recipe.result.CustomRecipeResult;
import net.momirealms.craftengine.core.item.recipe.transform.ItemTransformDataProcessor;
import net.momirealms.craftengine.core.item.recipe.transform.ItemTransformDataProcessors;
import net.momirealms.craftengine.core.plugin.config.ConfigConstants;
import net.momirealms.craftengine.core.plugin.config.ConfigSection;
import net.momirealms.craftengine.core.plugin.config.ConfigValue;
import net.momirealms.craftengine.core.plugin.config.KnownResourceException;
import net.momirealms.craftengine.core.plugin.context.CommonConditions;
import net.momirealms.craftengine.core.plugin.context.CommonFunctions;
import net.momirealms.craftengine.core.plugin.context.Context;
import net.momirealms.craftengine.core.plugin.context.function.Function;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.MiscUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public final class CustomShapelessTransformRecipe extends CustomShapelessRecipe {
    public static final Serializer SERIALIZER = new Serializer();

    private final Ingredient sourceIngredient;
    private final boolean mergeComponents;
    private final List<ItemTransformDataProcessor> processors;

    public CustomShapelessTransformRecipe(Key id,
                                          boolean showNotification,
                                          CustomRecipeResult result,
                                          CustomRecipeResult visualResult,
                                          String group,
                                          CraftingRecipeCategory category,
                                          List<Ingredient> ingredients,
                                          Ingredient sourceIngredient,
                                          Function<Context>[] craftingFunctions,
                                          Predicate<Context> craftingCondition,
                                          boolean ingredientCountSupport,
                                          List<ItemTransformDataProcessor> processors,
                                          boolean mergeComponents
    ) {
        super(id, showNotification, result, visualResult, group, category, ingredients, craftingFunctions, craftingCondition, true, ingredientCountSupport);
        this.sourceIngredient = sourceIngredient;
        this.processors = processors;
        this.mergeComponents = mergeComponents;
    }

    public boolean mergeComponents() {
        return this.mergeComponents;
    }

    public Ingredient sourceIngredient() {
        return this.sourceIngredient;
    }

    @Override
    public boolean requiresInput() {
        return true;
    }

    @Override
    public @NotNull Key serializerType() {
        return RecipeSerializers.SHAPELESS_TRANSFORM;
    }

    @Override
    public Item assembleVisual(RecipeInput input, ItemBuildContext context) {
        Item source = findSource((CraftingInput) input);
        if (source == null) {
            return super.assembleVisual(input, context);
        }
        Item result = this.visualResult().buildItem(context);
        return createTransformResult(source, result);
    }

    @Override
    public Item assemble(RecipeInput input, ItemBuildContext context) {
        Item source = findSource((CraftingInput) input);
        if (source == null) {
            return super.assemble(input, context);
        }
        Item result = this.result(context);
        return createTransformResult(source, result);
    }

    private @Nullable Item findSource(CraftingInput input) {
        for (UniqueIdItem uniqueIdItem : input) {
            if (!uniqueIdItem.isEmpty() && this.sourceIngredient.test(uniqueIdItem)) {
                return uniqueIdItem.item();
            }
        }
        return null;
    }

    private Item createTransformResult(Item base, Item result) {
        Item finalResult;
        if (this.mergeComponents) {
            finalResult = base.mergeCopy(result);
        } else {
            finalResult = result.copy();
        }
        if (this.processors != null) {
            for (ItemTransformDataProcessor processor : this.processors) {
                processor.accept(base, result, finalResult);
            }
        }
        return finalResult;
    }

    public static class Serializer extends AbstractRecipeSerializer<CustomShapelessTransformRecipe> {
        private static final String[] MERGE_COMPONENTS = new String[]{"merge-components", "merge_components"};

        @SuppressWarnings({"unchecked", "DuplicatedCode"})
        @Override
        public CustomShapelessTransformRecipe readConfig(Key id, ConfigSection section) {
            List<Ingredient> ingredients = new ArrayList<>(4);
            boolean hasAdditionalInput = false;

            Ingredient sourceIngredient = null;
            ConfigValue ingredientsValue = section.getNonNullValue(INGREDIENTS, ConfigConstants.ARGUMENT_LIST);
            if (ingredientsValue.is(Map.class)) {
                ConfigSection ingredientSection = ingredientsValue.getAsSection();
                for (String key : ingredientSection.keySet()) {
                    ConfigValue value = ingredientSection.getValue(key);
                    Ingredient ingredient = ingredientSection.getNonNullValue(key, ConfigConstants.ARGUMENT_LIST, super::parseIngredient);
                    if (isSource(value)) {
                        if (sourceIngredient == null) {
                            sourceIngredient = ingredient;
                        } else {
                            throw new KnownResourceException("resource.recipe.shapeless_transform.ambigious_source", ingredientSection.path(), key);
                        }
                    }
                    ingredients.add(ingredient);
                    if (ingredient.count() > 1) {
                        hasAdditionalInput = true;
                    }
                }
            } else if (ingredientsValue.is(List.class)) {
                List<ConfigValue> values = ingredientsValue.getAsList(v -> v);
                for (ConfigValue value : values) {
                    Ingredient ingredient = super.parseIngredient(value);
                    if (isSource(value)) {
                        if (sourceIngredient == null) {
                            sourceIngredient = ingredient;
                        } else {
                            throw new KnownResourceException("resource.recipe.shapeless_transform.ambigious_source", value.path());
                        }
                    }
                    ingredients.add(ingredient);
                    if (ingredient.count() > 1) {
                        hasAdditionalInput = true;
                    }
                }
            } else {
                Ingredient ingredient = super.parseIngredient(ingredientsValue);
                sourceIngredient = ingredient;
                ingredients.add(ingredient);
                if (ingredient.count() > 1) {
                    hasAdditionalInput = true;
                }
            }

            if (sourceIngredient == null) {
                throw new KnownResourceException("resource.recipe.shapeless_transform.source_not_found", section.assemblePath("ingredients"));
            }

            if (hasAdditionalInput) {
                ingredients.sort((o1, o2) -> Integer.compare(o2.count(), o1.count()));
            }

            return new CustomShapelessTransformRecipe(
                    id,
                    section.getBoolean(SHOW_NOTIFICATIONS, true),
                    super.parseResult(section.getNonNullValue("result", ConfigConstants.ARGUMENT_SECTION)),
                    section.getValue(VISUAL_RESULT, super::parseResult),
                    section.getString("group"),
                    section.getEnum("category", CraftingRecipeCategory.class),
                    ingredients,
                    sourceIngredient,
                    section.getList(FUNCTIONS, CommonFunctions::fromConfig).toArray(new Function[0]),
                    MiscUtils.allOf(section.getList(CONDITIONS, CommonConditions::fromConfig)),
                    hasAdditionalInput,
                    section.getList(TRANSFORM_PROCESSOR, ItemTransformDataProcessors::fromConfig),
                    section.getBoolean(MERGE_COMPONENTS, true)
            );
        }

        @Override
        public CustomShapelessTransformRecipe readJson(Key id, JsonObject json) {
            throw new IllegalArgumentException("unsupported recipe type for datapack: shapeless_transform");
        }

        private boolean isSource(ConfigValue value) {
            return value != null && value.is(Map.class) && value.getAsSection().getBoolean("source", false);
        }
    }
}
