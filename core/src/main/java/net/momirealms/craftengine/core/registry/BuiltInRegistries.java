package net.momirealms.craftengine.core.registry;

import net.momirealms.craftengine.core.block.CustomBlock;
import net.momirealms.craftengine.core.block.behavior.BlockBehaviorType;
import net.momirealms.craftengine.core.block.entity.BlockEntity;
import net.momirealms.craftengine.core.block.entity.BlockEntityType;
import net.momirealms.craftengine.core.block.entity.render.element.BlockEntityElement;
import net.momirealms.craftengine.core.block.entity.render.element.BlockEntityElementConfigType;
import net.momirealms.craftengine.core.block.properties.PropertyType;
import net.momirealms.craftengine.core.entity.furniture.behavior.FurnitureBehavior;
import net.momirealms.craftengine.core.entity.furniture.behavior.FurnitureBehaviorType;
import net.momirealms.craftengine.core.entity.furniture.element.FurnitureElement;
import net.momirealms.craftengine.core.entity.furniture.element.FurnitureElementConfigType;
import net.momirealms.craftengine.core.entity.furniture.hitbox.FurnitureHitBox;
import net.momirealms.craftengine.core.entity.furniture.hitbox.FurnitureHitboxConfigType;
import net.momirealms.craftengine.core.item.behavior.ItemBehaviorType;
import net.momirealms.craftengine.core.item.equipment.Equipment;
import net.momirealms.craftengine.core.item.equipment.EquipmentType;
import net.momirealms.craftengine.core.item.processor.ItemProcessorType;
import net.momirealms.craftengine.core.item.recipe.CustomSmithingTransformRecipe;
import net.momirealms.craftengine.core.item.recipe.Recipe;
import net.momirealms.craftengine.core.item.recipe.RecipeSerializer;
import net.momirealms.craftengine.core.item.recipe.network.legacy.LegacyRecipe;
import net.momirealms.craftengine.core.item.recipe.network.modern.display.RecipeDisplay;
import net.momirealms.craftengine.core.item.recipe.network.modern.display.slot.SlotDisplay;
import net.momirealms.craftengine.core.item.recipe.remainder.CraftRemainderType;
import net.momirealms.craftengine.core.item.recipe.result.PostProcessorType;
import net.momirealms.craftengine.core.item.updater.ItemUpdaterType;
import net.momirealms.craftengine.core.loot.entry.LootEntryContainerType;
import net.momirealms.craftengine.core.loot.function.LootFunctionType;
import net.momirealms.craftengine.core.loot.function.formula.FormulaType;
import net.momirealms.craftengine.core.pack.conflict.matcher.PathMatcherType;
import net.momirealms.craftengine.core.pack.conflict.resolution.ResolutionType;
import net.momirealms.craftengine.core.pack.host.ResourcePackHostType;
import net.momirealms.craftengine.core.pack.model.definition.ItemModelType;
import net.momirealms.craftengine.core.pack.model.definition.condition.ConditionPropertyType;
import net.momirealms.craftengine.core.pack.model.definition.rangedisptach.RangeDispatchPropertyType;
import net.momirealms.craftengine.core.pack.model.definition.select.SelectPropertyType;
import net.momirealms.craftengine.core.pack.model.definition.special.SpecialModelType;
import net.momirealms.craftengine.core.pack.model.definition.tint.TintType;
import net.momirealms.craftengine.core.plugin.config.template.argument.TemplateArgumentType;
import net.momirealms.craftengine.core.plugin.context.ConditionType;
import net.momirealms.craftengine.core.plugin.context.Context;
import net.momirealms.craftengine.core.plugin.context.FunctionType;
import net.momirealms.craftengine.core.plugin.context.number.NumberProviderType;
import net.momirealms.craftengine.core.plugin.context.selector.PlayerSelectorType;
import net.momirealms.craftengine.core.plugin.network.ModPacket;
import net.momirealms.craftengine.core.plugin.network.codec.NetworkCodec;
import net.momirealms.craftengine.core.util.FriendlyByteBuf;
import net.momirealms.craftengine.core.util.ResourceKey;

public final class BuiltInRegistries {
    public static final Registry<CustomBlock> BLOCK = createDynamicBoundRegistry(Registries.BLOCK, 512);
    public static final Registry<BlockBehaviorType> BLOCK_BEHAVIOR_TYPE = createConstantBoundRegistry(Registries.BLOCK_BEHAVIOR_TYPE, 64);
    public static final Registry<ItemProcessorType<?>> ITEM_PROCESSOR_TYPE = createConstantBoundRegistry(Registries.ITEM_PROCESSOR_TYPE, 64);
    public static final Registry<ItemBehaviorType> ITEM_BEHAVIOR_TYPE = createConstantBoundRegistry(Registries.ITEM_BEHAVIOR_TYPE, 64);
    public static final Registry<PropertyType<? extends Comparable<?>>> PROPERTY_TYPE = createConstantBoundRegistry(Registries.PROPERTY_TYPE, 16);
    public static final Registry<LootFunctionType<?>> LOOT_FUNCTION_TYPE = createConstantBoundRegistry(Registries.LOOT_FUNCTION_TYPE, 32);
    public static final Registry<LootEntryContainerType<?>> LOOT_ENTRY_CONTAINER_TYPE = createConstantBoundRegistry(Registries.LOOT_ENTRY_CONTAINER_TYPE, 16);
    public static final Registry<NumberProviderType> NUMBER_PROVIDER_TYPE = createConstantBoundRegistry(Registries.NUMBER_PROVIDER_TYPE, 16);
    public static final Registry<TemplateArgumentType> TEMPLATE_ARGUMENT_TYPE = createConstantBoundRegistry(Registries.TEMPLATE_ARGUMENT_TYPE, 16);
    public static final Registry<ItemModelType> ITEM_MODEL_TYPE = createConstantBoundRegistry(Registries.ITEM_MODEL_TYPE, 16);
    public static final Registry<TintType> TINT_TYPE = createConstantBoundRegistry(Registries.TINT_TYPE, 16);
    public static final Registry<SpecialModelType> SPECIAL_MODEL_TYPE = createConstantBoundRegistry(Registries.SPECIAL_MODEL_TYPE, 16);
    public static final Registry<RangeDispatchPropertyType> RANGE_DISPATCH_PROPERTY_TYPE = createConstantBoundRegistry(Registries.RANGE_DISPATCH_PROPERTY_TYPE, 16);
    public static final Registry<ConditionPropertyType> CONDITION_PROPERTY_TYPE = createConstantBoundRegistry(Registries.CONDITION_PROPERTY_TYPE, 16);
    public static final Registry<SelectPropertyType> SELECT_PROPERTY_TYPE = createConstantBoundRegistry(Registries.SELECT_PROPERTY_TYPE, 16);
    public static final Registry<RecipeSerializer<?, ? extends Recipe<?>>> RECIPE_SERIALIZER = createConstantBoundRegistry(Registries.RECIPE_SERIALIZER, 16);
    public static final Registry<FormulaType> FORMULA_TYPE = createConstantBoundRegistry(Registries.FORMULA_TYPE, 16);
    public static final Registry<PathMatcherType> PATH_MATCHER_TYPE = createConstantBoundRegistry(Registries.PATH_MATCHER_TYPE, 16);
    public static final Registry<ResolutionType> RESOLUTION_TYPE = createConstantBoundRegistry(Registries.RESOLUTION_TYPE, 16);
    public static final Registry<CustomSmithingTransformRecipe.ItemDataProcessor.Type> SMITHING_RESULT_PROCESSOR_TYPE = createConstantBoundRegistry(Registries.SMITHING_RESULT_PROCESSOR_TYPE, 16);
    public static final Registry<ResourcePackHostType> RESOURCE_PACK_HOST_TYPE = createConstantBoundRegistry(Registries.RESOURCE_PACK_HOST_TYPE, 16);
    public static final Registry<FunctionType<? extends Context>> COMMON_FUNCTION_TYPE = createConstantBoundRegistry(Registries.COMMON_FUNCTION_TYPE, 128);
    public static final Registry<ConditionType<? extends Context>> COMMON_CONDITION_TYPE = createConstantBoundRegistry(Registries.COMMON_CONDITION_TYPE, 128);
    public static final Registry<PlayerSelectorType<? extends Context>> PLAYER_SELECTOR_TYPE = createConstantBoundRegistry(Registries.PLAYER_SELECTOR_TYPE, 16);
    public static final Registry<EquipmentType<? extends Equipment>> EQUIPMENT_TYPE = createConstantBoundRegistry(Registries.EQUIPMENT_TYPE, 8);
    public static final Registry<SlotDisplay.Type<?>> SLOT_DISPLAY_TYPE = createConstantBoundRegistry(Registries.SLOT_DISPLAY_TYPE, 16);
    public static final Registry<RecipeDisplay.Type<?>> RECIPE_DISPLAY_TYPE = createConstantBoundRegistry(Registries.RECIPE_DISPLAY_TYPE, 16);
    public static final Registry<LegacyRecipe.Type<?>> LEGACY_RECIPE_TYPE = createConstantBoundRegistry(Registries.LEGACY_RECIPE_TYPE, 16);
    public static final Registry<PostProcessorType<?>> RECIPE_POST_PROCESSOR_TYPE = createConstantBoundRegistry(Registries.RECIPE_POST_PROCESSOR_TYPE, 16);
    public static final Registry<ItemUpdaterType<?>> ITEM_UPDATER_TYPE = createConstantBoundRegistry(Registries.ITEM_UPDATER_TYPE, 16);
    public static final Registry<NetworkCodec<FriendlyByteBuf, ? extends ModPacket>> MOD_PACKET = createConstantBoundRegistry(Registries.MOD_PACKET, 16);
    public static final Registry<BlockEntityType<? extends BlockEntity>> BLOCK_ENTITY_TYPE = createConstantBoundRegistry(Registries.BLOCK_ENTITY_TYPE, 64);
    public static final Registry<BlockEntityElementConfigType<? extends BlockEntityElement>> BLOCK_ENTITY_ELEMENT_TYPE = createConstantBoundRegistry(Registries.BLOCK_ENTITY_ELEMENT_TYPE, 16);
    public static final Registry<CraftRemainderType<?>> CRAFT_REMAINDER_TYPE = createConstantBoundRegistry(Registries.CRAFT_REMAINDER_TYPE, 16);
    public static final Registry<FurnitureElementConfigType<? extends FurnitureElement>> FURNITURE_ELEMENT_TYPE = createConstantBoundRegistry(Registries.FURNITURE_ELEMENT_TYPE, 16);
    public static final Registry<FurnitureHitboxConfigType<? extends FurnitureHitBox>> FURNITURE_HITBOX_TYPE = createConstantBoundRegistry(Registries.FURNITURE_HITBOX_TYPE, 16);
    public static final Registry<FurnitureBehaviorType<? extends FurnitureBehavior>> FURNITURE_BEHAVIOR_TYPE = createConstantBoundRegistry(Registries.FURNITURE_BEHAVIOR_TYPE, 32);

    private BuiltInRegistries() {}

    private static <T> Registry<T> createConstantBoundRegistry(ResourceKey<? extends Registry<T>> key, int expectedSize) {
        return new ConstantBoundRegistry<>(key, expectedSize);
    }

    private static <T> Registry<T> createDynamicBoundRegistry(ResourceKey<? extends Registry<T>> key, int expectedSize) {
        return new DynamicBoundRegistry<>(key, expectedSize);
    }
}
