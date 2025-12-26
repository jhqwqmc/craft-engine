package net.momirealms.craftengine.core.registry;

import net.momirealms.craftengine.core.block.CustomBlock;
import net.momirealms.craftengine.core.block.behavior.BlockBehaviorType;
import net.momirealms.craftengine.core.block.entity.BlockEntityType;
import net.momirealms.craftengine.core.block.entity.render.element.BlockEntityElementConfigFactory;
import net.momirealms.craftengine.core.block.properties.PropertyFactory;
import net.momirealms.craftengine.core.entity.furniture.behavior.FurnitureBehaviorType;
import net.momirealms.craftengine.core.entity.furniture.element.FurnitureElementConfigFactory;
import net.momirealms.craftengine.core.entity.furniture.hitbox.FurnitureHitBoxConfigFactory;
import net.momirealms.craftengine.core.item.behavior.ItemBehaviorType;
import net.momirealms.craftengine.core.item.equipment.EquipmentFactory;
import net.momirealms.craftengine.core.item.processor.ItemProcessorType;
import net.momirealms.craftengine.core.item.recipe.CustomSmithingTransformRecipe;
import net.momirealms.craftengine.core.item.recipe.Recipe;
import net.momirealms.craftengine.core.item.recipe.RecipeSerializer;
import net.momirealms.craftengine.core.item.recipe.network.legacy.LegacyRecipe;
import net.momirealms.craftengine.core.item.recipe.network.modern.display.RecipeDisplay;
import net.momirealms.craftengine.core.item.recipe.network.modern.display.slot.SlotDisplay;
import net.momirealms.craftengine.core.item.recipe.remainder.CraftRemainderFactory;
import net.momirealms.craftengine.core.item.recipe.result.PostProcessor;
import net.momirealms.craftengine.core.item.updater.ItemUpdaterType;
import net.momirealms.craftengine.core.loot.LootContext;
import net.momirealms.craftengine.core.loot.entry.LootEntryContainerFactory;
import net.momirealms.craftengine.core.loot.function.ApplyBonusCountFunction;
import net.momirealms.craftengine.core.loot.function.LootFunctionFactory;
import net.momirealms.craftengine.core.pack.conflict.PathContext;
import net.momirealms.craftengine.core.pack.conflict.resolution.ResolutionFactory;
import net.momirealms.craftengine.core.pack.host.ResourcePackHostFactory;
import net.momirealms.craftengine.core.pack.model.ItemModelFactory;
import net.momirealms.craftengine.core.pack.model.ItemModelReader;
import net.momirealms.craftengine.core.pack.model.condition.ConditionPropertyFactory;
import net.momirealms.craftengine.core.pack.model.condition.ConditionPropertyReader;
import net.momirealms.craftengine.core.pack.model.rangedisptach.RangeDispatchPropertyFactory;
import net.momirealms.craftengine.core.pack.model.rangedisptach.RangeDispatchPropertyReader;
import net.momirealms.craftengine.core.pack.model.select.SelectPropertyFactory;
import net.momirealms.craftengine.core.pack.model.select.SelectPropertyReader;
import net.momirealms.craftengine.core.pack.model.special.SpecialModelFactory;
import net.momirealms.craftengine.core.pack.model.special.SpecialModelReader;
import net.momirealms.craftengine.core.pack.model.tint.TintFactory;
import net.momirealms.craftengine.core.pack.model.tint.TintReader;
import net.momirealms.craftengine.core.plugin.config.template.argument.TemplateArgumentFactory;
import net.momirealms.craftengine.core.plugin.context.Context;
import net.momirealms.craftengine.core.plugin.context.condition.ConditionFactory;
import net.momirealms.craftengine.core.plugin.context.function.FunctionFactory;
import net.momirealms.craftengine.core.plugin.context.number.NumberProviderFactory;
import net.momirealms.craftengine.core.plugin.context.selector.PlayerSelectorFactory;
import net.momirealms.craftengine.core.plugin.network.ModPacket;
import net.momirealms.craftengine.core.plugin.network.codec.NetworkCodec;
import net.momirealms.craftengine.core.util.FriendlyByteBuf;
import net.momirealms.craftengine.core.util.ResourceKey;

public class BuiltInRegistries {
    public static final Registry<CustomBlock> BLOCK = createDynamicBoundRegistry(Registries.BLOCK, 512);
    public static final Registry<BlockBehaviorType> BLOCK_BEHAVIOR_TYPE = createConstantBoundRegistry(Registries.BLOCK_BEHAVIOR_TYPE, 64);
    public static final Registry<ItemProcessorType<?>> ITEM_PROCESSOR_TYPE = createConstantBoundRegistry(Registries.ITEM_PROCESSOR_TYPE, 64);
    public static final Registry<ItemBehaviorType> ITEM_BEHAVIOR_TYPE = createConstantBoundRegistry(Registries.ITEM_BEHAVIOR_TYPE, 64);
    public static final Registry<PropertyFactory> PROPERTY_FACTORY = createConstantBoundRegistry(Registries.PROPERTY_FACTORY, 16);
    public static final Registry<LootFunctionFactory<?>> LOOT_FUNCTION_FACTORY = createConstantBoundRegistry(Registries.LOOT_FUNCTION_FACTORY, 32);
    public static final Registry<ConditionFactory<LootContext>> LOOT_CONDITION_FACTORY = createConstantBoundRegistry(Registries.LOOT_CONDITION_FACTORY, 32);
    public static final Registry<LootEntryContainerFactory<?>> LOOT_ENTRY_CONTAINER_FACTORY = createConstantBoundRegistry(Registries.LOOT_ENTRY_CONTAINER_FACTORY, 16);
    public static final Registry<NumberProviderFactory> NUMBER_PROVIDER_FACTORY = createConstantBoundRegistry(Registries.NUMBER_PROVIDER_FACTORY, 16);
    public static final Registry<TemplateArgumentFactory> TEMPLATE_ARGUMENT_FACTORY = createConstantBoundRegistry(Registries.TEMPLATE_ARGUMENT_FACTORY, 16);
    public static final Registry<ItemModelFactory> ITEM_MODEL_FACTORY = createConstantBoundRegistry(Registries.ITEM_MODEL_FACTORY, 16);
    public static final Registry<ItemModelReader> ITEM_MODEL_READER = createConstantBoundRegistry(Registries.ITEM_MODEL_READER, 16);
    public static final Registry<TintFactory> TINT_FACTORY = createConstantBoundRegistry(Registries.TINT_FACTORY, 16);
    public static final Registry<TintReader> TINT_READER = createConstantBoundRegistry(Registries.TINT_READER, 16);
    public static final Registry<SpecialModelFactory> SPECIAL_MODEL_FACTORY = createConstantBoundRegistry(Registries.SPECIAL_MODEL_FACTORY, 16);
    public static final Registry<SpecialModelReader> SPECIAL_MODEL_READER = createConstantBoundRegistry(Registries.SPECIAL_MODEL_READER, 16);
    public static final Registry<RangeDispatchPropertyFactory> RANGE_DISPATCH_PROPERTY_FACTORY = createConstantBoundRegistry(Registries.RANGE_DISPATCH_PROPERTY_FACTORY, 16);
    public static final Registry<RangeDispatchPropertyReader> RANGE_DISPATCH_PROPERTY_READER = createConstantBoundRegistry(Registries.RANGE_DISPATCH_PROPERTY_READER, 16);
    public static final Registry<ConditionPropertyFactory> CONDITION_PROPERTY_FACTORY = createConstantBoundRegistry(Registries.CONDITION_PROPERTY_FACTORY, 16);
    public static final Registry<ConditionPropertyReader> CONDITION_PROPERTY_READER = createConstantBoundRegistry(Registries.CONDITION_PROPERTY_READER, 16);
    public static final Registry<SelectPropertyFactory> SELECT_PROPERTY_FACTORY = createConstantBoundRegistry(Registries.SELECT_PROPERTY_FACTORY, 16);
    public static final Registry<SelectPropertyReader> SELECT_PROPERTY_READER = createConstantBoundRegistry(Registries.SELECT_PROPERTY_READER, 16);
    public static final Registry<RecipeSerializer<?, ? extends Recipe<?>>> RECIPE_SERIALIZER = createConstantBoundRegistry(Registries.RECIPE_FACTORY, 16);
    public static final Registry<ApplyBonusCountFunction.FormulaFactory> FORMULA_FACTORY = createConstantBoundRegistry(Registries.FORMULA_FACTORY, 16);
    public static final Registry<ConditionFactory<PathContext>> PATH_MATCHER_FACTORY = createConstantBoundRegistry(Registries.PATH_MATCHER_FACTORY, 16);
    public static final Registry<ResolutionFactory> RESOLUTION_FACTORY = createConstantBoundRegistry(Registries.RESOLUTION_FACTORY, 16);
    public static final Registry<CustomSmithingTransformRecipe.ItemDataProcessor.ProcessorFactory> SMITHING_RESULT_PROCESSOR_FACTORY = createConstantBoundRegistry(Registries.SMITHING_RESULT_PROCESSOR_FACTORY, 16);
    public static final Registry<ResourcePackHostFactory> RESOURCE_PACK_HOST_FACTORY = createConstantBoundRegistry(Registries.RESOURCE_PACK_HOST_FACTORY, 16);
    public static final Registry<FunctionFactory<Context>> EVENT_FUNCTION_FACTORY = createConstantBoundRegistry(Registries.EVENT_FUNCTION_FACTORY, 128);
    public static final Registry<ConditionFactory<Context>> EVENT_CONDITION_FACTORY = createConstantBoundRegistry(Registries.EVENT_CONDITION_FACTORY, 128);
    public static final Registry<PlayerSelectorFactory<?>> PLAYER_SELECTOR_FACTORY = createConstantBoundRegistry(Registries.PLAYER_SELECTOR_FACTORY, 16);
    public static final Registry<EquipmentFactory> EQUIPMENT_FACTORY = createConstantBoundRegistry(Registries.EQUIPMENT_FACTORY, 8);
    public static final Registry<SlotDisplay.Type<?>> SLOT_DISPLAY_TYPE = createConstantBoundRegistry(Registries.SLOT_DISPLAY_TYPE, 16);
    public static final Registry<RecipeDisplay.Type<?>> RECIPE_DISPLAY_TYPE = createConstantBoundRegistry(Registries.RECIPE_DISPLAY_TYPE, 16);
    public static final Registry<LegacyRecipe.Type<?>> LEGACY_RECIPE_TYPE = createConstantBoundRegistry(Registries.LEGACY_RECIPE_TYPE, 16);
    public static final Registry<PostProcessor.Type<?>> RECIPE_POST_PROCESSOR_TYPE = createConstantBoundRegistry(Registries.RECIPE_POST_PROCESSOR_TYPE, 16);
    public static final Registry<ItemUpdaterType<?>> ITEM_UPDATER_TYPE = createConstantBoundRegistry(Registries.ITEM_UPDATER_TYPE, 16);
    public static final Registry<NetworkCodec<FriendlyByteBuf, ? extends ModPacket>> MOD_PACKET = createConstantBoundRegistry(Registries.MOD_PACKET, 16);
    public static final Registry<BlockEntityType<?>> BLOCK_ENTITY_TYPE = createConstantBoundRegistry(Registries.BLOCK_ENTITY_TYPE, 64);
    public static final Registry<BlockEntityElementConfigFactory<?>> BLOCK_ENTITY_ELEMENT_TYPE = createConstantBoundRegistry(Registries.BLOCK_ENTITY_ELEMENT_TYPE, 16);
    public static final Registry<CraftRemainderFactory<?>> CRAFT_REMAINDER_FACTORY = createConstantBoundRegistry(Registries.CRAFT_REMAINDER_FACTORY, 16);
    public static final Registry<FurnitureElementConfigFactory<?>> FURNITURE_ELEMENT_TYPE = createConstantBoundRegistry(Registries.FURNITURE_ELEMENT_TYPE, 16);
    public static final Registry<FurnitureHitBoxConfigFactory<?>> FURNITURE_HITBOX_TYPE = createConstantBoundRegistry(Registries.FURNITURE_HITBOX_TYPE, 16);
    public static final Registry<FurnitureBehaviorType<?>> FURNITURE_BEHAVIOR_TYPE = createConstantBoundRegistry(Registries.FURNITURE_BEHAVIOR_TYPE, 32);

    private static <T> Registry<T> createConstantBoundRegistry(ResourceKey<? extends Registry<T>> key, int expectedSize) {
        return new ConstantBoundRegistry<>(key, expectedSize);
    }

    private static <T> Registry<T> createDynamicBoundRegistry(ResourceKey<? extends Registry<T>> key, int expectedSize) {
        return new DynamicBoundRegistry<>(key, expectedSize);
    }
}
