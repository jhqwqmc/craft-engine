package net.momirealms.craftengine.core.item.processor;

import net.momirealms.craftengine.core.item.ItemProcessorFactory;
import net.momirealms.craftengine.core.item.processor.lore.DynamicLoreProcessor;
import net.momirealms.craftengine.core.item.processor.lore.LoreProcessor;
import net.momirealms.craftengine.core.item.processor.lore.OverwritableLoreProcessor;
import net.momirealms.craftengine.core.plugin.locale.LocalizedResourceConfigException;
import net.momirealms.craftengine.core.registry.BuiltInRegistries;
import net.momirealms.craftengine.core.registry.Registries;
import net.momirealms.craftengine.core.registry.WritableRegistry;
import net.momirealms.craftengine.core.util.ExceptionCollector;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.ResourceKey;
import net.momirealms.craftengine.core.util.VersionHelper;

import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public final class ItemProcessors {
    private ItemProcessors() {}

    public static final Key ITEM_MODEL = Key.of("craftengine:item_model");
    public static final Key OVERWRITABLE_ITEM_MODEL = Key.of("craftengine:overwritable_item_model");
    public static final Key ID = Key.of("craftengine:id");
    public static final Key HIDE_TOOLTIP = Key.of("craftengine:hide_tooltip");
    public static final Key FOOD = Key.of("craftengine:food");
    public static final Key EXTERNAL = Key.of("craftengine:external");
    public static final Key EQUIPPABLE = Key.of("craftengine:equippable");
    public static final Key EQUIPPABLE_ASSET_ID = Key.of("craftengine:equippable_asset_id");
    public static final Key ENCHANTMENT = Key.of("craftengine:enchantment");
    public static final Key ENCHANTMENTS = Key.of("craftengine:enchantments");
    public static final Key DYED_COLOR = Key.of("craftengine:dyed_color");
    public static final Key DISPLAY_NAME = Key.of("craftengine:display_name");
    public static final Key CUSTOM_NAME = Key.of("craftengine:custom_name");
    public static final Key CUSTOM_MODEL_DATA = Key.of("craftengine:custom_model_data");
    public static final Key OVERWRITABLE_CUSTOM_MODEL_DATA = Key.of("craftengine:overwritable_custom_model_data");
    public static final Key COMPONENTS = Key.of("craftengine:components");
    public static final Key COMPONENT = Key.of("craftengine:component");
    public static final Key ATTRIBUTE_MODIFIERS = Key.of("craftengine:attribute_modifiers");
    public static final Key ATTRIBUTES = Key.of("craftengine:attributes");
    public static final Key ARGUMENTS = Key.of("craftengine:arguments");
    public static final Key VERSION = Key.of("craftengine:version");
    public static final Key PDC = Key.of("craftengine:pdc");
    public static final Key ITEM_NAME = Key.of("craftengine:item_name");
    public static final Key OVERWRITABLE_ITEM_NAME = Key.of("craftengine:overwritable_item_name");
    public static final Key JUKEBOX_PLAYABLE = Key.of("craftengine:jukebox_playable");
    public static final Key REMOVE_COMPONENTS = Key.of("craftengine:remove_components");
    public static final Key REMOVE_COMPONENT = Key.of("craftengine:remove_component");
    public static final Key TAGS = Key.of("craftengine:tags");
    public static final Key NBT = Key.of("craftengine:nbt");
    public static final Key TOOLTIP_STYLE = Key.of("craftengine:tooltip_style");
    public static final Key TRIM = Key.of("craftengine:trim");
    public static final Key LORE = Key.of("craftengine:lore");
    public static final Key UNBREAKABLE = Key.of("craftengine:unbreakable");
    public static final Key DYNAMIC_LORE = Key.of("craftengine:dynamic_lore");
    public static final Key OVERWRITABLE_LORE = Key.of("craftengine:overwritable_lore");
    public static final Key MAX_DAMAGE = Key.of("craftengine:max_damage");
    public static final Key BLOCK_STATE = Key.of("craftengine:block_state");
    public static final Key CONDITIONAL = Key.of("craftengine:conditional");
    public static final Key CONDITION = Key.of("craftengine:condition");

    public static <T> ItemProcessorType<T> register(Key key, ItemProcessorFactory<T> factory) {
        ItemProcessorType<T> type = new ItemProcessorType<>(key, factory);
        ((WritableRegistry<ItemProcessorFactory<?>>) BuiltInRegistries.ITEM_DATA_MODIFIER_FACTORY)
                .register(ResourceKey.create(Registries.ITEM_DATA_MODIFIER_FACTORY.location(), key), factory);
        return type;
    }

    @SuppressWarnings("unchecked")
    public static <I> void applyDataModifiers(Map<String, Object> dataSection, Consumer<ItemProcessor<I>> callback) {
        ExceptionCollector<LocalizedResourceConfigException> errorCollector = new ExceptionCollector<>();
        if (dataSection != null) {
            for (Map.Entry<String, Object> dataEntry : dataSection.entrySet()) {
                Object value = dataEntry.getValue();
                if (value == null) continue;
                String key = dataEntry.getKey();
                int idIndex = key.indexOf('#');
                if (idIndex != -1)
                    key = key.substring(0, idIndex);
                if (key.contains("-"))
                    key = key.replace("-", "_");
                Optional.ofNullable(BuiltInRegistries.ITEM_DATA_MODIFIER_FACTORY.getValue(Key.withDefaultNamespace(key, Key.DEFAULT_NAMESPACE))).ifPresent(factory -> {
                    try {
                        callback.accept((ItemProcessor<I>) factory.create(value));
                    } catch (LocalizedResourceConfigException e) {
                        errorCollector.add(e);
                    }
                });
            }
        }
        errorCollector.throwIfPresent();
    }

    public static void init() {}

    static {
        register(EXTERNAL, ExternalSourceProcessor.FACTORY);
        register(LORE, LoreProcessor.FACTORY);
        register(DYNAMIC_LORE, DynamicLoreProcessor.FACTORY);
        register(OVERWRITABLE_LORE, OverwritableLoreProcessor.FACTORY);
        register(DYED_COLOR, DyedColorProcessor.FACTORY);
        register(TAGS, TagsProcessor.FACTORY);
        register(NBT, TagsProcessor.FACTORY);
        register(ATTRIBUTE_MODIFIERS, AttributeModifiersProcessor.FACTORY);
        register(ATTRIBUTES, AttributeModifiersProcessor.FACTORY);
        register(CUSTOM_MODEL_DATA, CustomModelDataProcessor.FACTORY);
        register(UNBREAKABLE, UnbreakableProcessor.FACTORY);
        register(ENCHANTMENT, EnchantmentsProcessor.FACTORY);
        register(ENCHANTMENTS, EnchantmentsProcessor.FACTORY);
        register(TRIM, TrimProcessor.FACTORY);
        register(HIDE_TOOLTIP, HideTooltipProcessor.FACTORY);
        register(ARGUMENTS, ArgumentsProcessor.FACTORY);
        register(OVERWRITABLE_ITEM_NAME, OverwritableItemNameProcessor.FACTORY);
        register(PDC, PDCProcessor.FACTORY);
        register(BLOCK_STATE, BlockStateProcessor.FACTORY);
        if (VersionHelper.isOrAbove1_20_5()) {
            register(CUSTOM_NAME, CustomNameProcessor.FACTORY);
            register(ITEM_NAME, ItemNameProcessor.FACTORY);
            register(DISPLAY_NAME, ItemNameProcessor.FACTORY);
            register(COMPONENTS, ComponentsProcessor.FACTORY);
            register(COMPONENT, ComponentsProcessor.FACTORY);
            register(REMOVE_COMPONENTS, RemoveComponentProcessor.FACTORY);
            register(REMOVE_COMPONENT, RemoveComponentProcessor.FACTORY);
            register(FOOD, FoodProcessor.FACTORY);
            register(MAX_DAMAGE, MaxDamageProcessor.FACTORY);
        } else {
            register(CUSTOM_NAME, CustomNameProcessor.FACTORY);
            register(ITEM_NAME, CustomNameProcessor.FACTORY);
            register(DISPLAY_NAME, CustomNameProcessor.FACTORY);
        }
        if (VersionHelper.isOrAbove1_21()) {
            register(JUKEBOX_PLAYABLE, JukeboxSongProcessor.FACTORY);
        }
        if (VersionHelper.isOrAbove1_21_2()) {
            register(TOOLTIP_STYLE, TooltipStyleProcessor.FACTORY);
            register(ITEM_MODEL, ItemModelProcessor.FACTORY);
            register(EQUIPPABLE, EquippableProcessor.FACTORY);
        }
        if (VersionHelper.PREMIUM) {
            register(CONDITIONAL, ConditionalProcessor.FACTORY);
            register(CONDITION, ConditionalProcessor.FACTORY);
        }
    }
}
