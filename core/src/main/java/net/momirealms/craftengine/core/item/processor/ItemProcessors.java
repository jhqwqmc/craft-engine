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

    public static final ItemProcessorType<?> ITEM_MODEL = register(Key.ce("item_model"), ItemModelProcessor.FACTORY, VersionHelper.isOrAbove1_21_2());
    public static final ItemProcessorType<?> ARGUMENTS = register(Key.ce("arguments"), ArgumentsProcessor.FACTORY);
    public static final ItemProcessorType<?> OVERWRITABLE_ITEM_MODEL = register(Key.ce("overwritable_item_model"), OverwritableItemModelProcessor.FACTORY, VersionHelper.isOrAbove1_21_2());
    public static final ItemProcessorType<?> ID = register(Key.ce("id"), IdProcessor.FACTORY);
    public static final ItemProcessorType<?> HIDE_TOOLTIP = register(Key.ce("hide_tooltip"), HideTooltipProcessor.FACTORY);
    public static final ItemProcessorType<?> FOOD = register(Key.ce("food"), FoodProcessor.FACTORY, VersionHelper.isOrAbove1_20_5());
    public static final ItemProcessorType<?> EXTERNAL = register(Key.ce("external"), ExternalSourceProcessor.FACTORY);
    public static final ItemProcessorType<?> EQUIPPABLE = register(Key.ce("equippable"), EquippableProcessor.FACTORY, VersionHelper.isOrAbove1_21_2());
    public static final ItemProcessorType<?> EQUIPPABLE_ASSET_ID = register(Key.ce("equippable_asset_id"), EquippableAssetIdProcessor.FACTORY, VersionHelper.isOrAbove1_21_2());
    public static final ItemProcessorType<?> ENCHANTMENTS = register(Key.ce("enchantments"), EnchantmentsProcessor.FACTORY);
    public static final ItemProcessorType<?> ENCHANTMENT = register(Key.ce("enchantment"), EnchantmentsProcessor.FACTORY);
    public static final ItemProcessorType<?> DYED_COLOR = register(Key.ce("dyed_color"), DyedColorProcessor.FACTORY);
    public static final ItemProcessorType<?> DISPLAY_NAME = register(Key.ce("display_name"), ItemNameProcessor.FACTORY);
    public static final ItemProcessorType<?> ITEM_NAME = register(Key.ce("item_name"), ItemNameProcessor.FACTORY);
    public static final ItemProcessorType<?> CUSTOM_NAME = register(Key.ce("custom_name"), CustomNameProcessor.FACTORY);
    public static final ItemProcessorType<?> CUSTOM_MODEL_DATA = register(Key.ce("custom_model_data"), CustomModelDataProcessor.FACTORY);
    public static final ItemProcessorType<?> OVERWRITABLE_CUSTOM_MODEL_DATA = register(Key.ce("overwritable_custom_model_data"), OverwritableCustomModelDataProcessor.FACTORY);
    public static final ItemProcessorType<?> COMPONENTS = register(Key.ce("components"), ComponentsProcessor.FACTORY, VersionHelper.isOrAbove1_20_5());
    public static final ItemProcessorType<?> COMPONENT = register(Key.ce("component"), ComponentsProcessor.FACTORY, VersionHelper.isOrAbove1_20_5());
    public static final ItemProcessorType<?> ATTRIBUTE_MODIFIERS = register(Key.ce("attribute_modifiers"), AttributeModifiersProcessor.FACTORY);
    public static final ItemProcessorType<?> ATTRIBUTES = register(Key.ce("attributes"), AttributeModifiersProcessor.FACTORY);
    public static final ItemProcessorType<?> PDC = register(Key.ce("pdc"), PDCProcessor.FACTORY);
    public static final ItemProcessorType<?> OVERWRITABLE_ITEM_NAME = register(Key.ce("overwritable_item_name"), OverwritableItemNameProcessor.FACTORY);
    public static final ItemProcessorType<?> JUKEBOX_PLAYABLE = register(Key.ce("jukebox_playable"), JukeboxSongProcessor.FACTORY, VersionHelper.isOrAbove1_21());
    public static final ItemProcessorType<?> REMOVE_COMPONENTS = register(Key.ce("remove_components"), RemoveComponentProcessor.FACTORY, VersionHelper.isOrAbove1_20_5());
    public static final ItemProcessorType<?> REMOVE_COMPONENT = register(Key.ce("remove_component"), RemoveComponentProcessor.FACTORY, VersionHelper.isOrAbove1_20_5());
    public static final ItemProcessorType<?> TAGS = register(Key.ce("tags"), TagsProcessor.FACTORY);
    public static final ItemProcessorType<?> NBT = register(Key.ce("nbt"), TagsProcessor.FACTORY);
    public static final ItemProcessorType<?> TOOLTIP_STYLE = register(Key.ce("tooltip_style"), TooltipStyleProcessor.FACTORY, VersionHelper.isOrAbove1_21_2());
    public static final ItemProcessorType<?> TRIM = register(Key.ce("trim"), TrimProcessor.FACTORY);
    public static final ItemProcessorType<?> LORE = register(Key.ce("lore"), LoreProcessor.FACTORY);
    public static final ItemProcessorType<?> UNBREAKABLE = register(Key.ce("unbreakable"), UnbreakableProcessor.FACTORY);
    public static final ItemProcessorType<?> DYNAMIC_LORE = register(Key.ce("dynamic_lore"), DynamicLoreProcessor.FACTORY);
    public static final ItemProcessorType<?> OVERWRITABLE_LORE = register(Key.ce("overwritable_lore"), OverwritableLoreProcessor.FACTORY);
    public static final ItemProcessorType<?> MAX_DAMAGE = register(Key.ce("max_damage"), MaxDamageProcessor.FACTORY, VersionHelper.isOrAbove1_20_5());
    public static final ItemProcessorType<?> BLOCK_STATE = register(Key.ce("blockstate"), BlockStateProcessor.FACTORY);
    public static final ItemProcessorType<?> CONDITIONAL = register(Key.ce("conditional"), ConditionalProcessor.FACTORY, VersionHelper.PREMIUM);
    public static final ItemProcessorType<?> CONDITION = register(Key.ce("condition"), ConditionalProcessor.FACTORY, VersionHelper.PREMIUM);

    public static <T> ItemProcessorType<T> register(Key key, ItemProcessorFactory<T> factory) {
        ItemProcessorType<T> type = new ItemProcessorType<>(key, factory);
        ((WritableRegistry<ItemProcessorType<?>>) BuiltInRegistries.ITEM_PROCESSOR_TYPE)
                .register(ResourceKey.create(Registries.ITEM_PROCESSOR_TYPE.location(), key), type);
        return type;
    }

    private static <T> ItemProcessorType<T> register(Key key, ItemProcessorFactory<T> factory, boolean condition) {
        return register(key, condition ? factory : null);
    }

    @SuppressWarnings("unchecked")
    public static <I> void applyDataModifiers(Map<String, Object> dataSection, Consumer<ItemProcessor<I>> callback) {
        ExceptionCollector<LocalizedResourceConfigException> errorCollector = new ExceptionCollector<>();
        if (dataSection != null) {
            for (Map.Entry<String, Object> dataEntry : dataSection.entrySet()) {
                Object value = dataEntry.getValue();
                if (value == null) continue;
                String key = processKey(dataEntry.getKey());
                Optional.ofNullable(BuiltInRegistries.ITEM_PROCESSOR_TYPE.getValue(Key.withDefaultNamespace(key, Key.DEFAULT_NAMESPACE))).ifPresent(processorType -> {
                    try {
                        ItemProcessorFactory<?> factory = processorType.factory();
                        if (factory != null) {
                            callback.accept((ItemProcessor<I>) factory.create(value));
                        }
                    } catch (LocalizedResourceConfigException e) {
                        errorCollector.add(e);
                    }
                });
            }
        }
        errorCollector.throwIfPresent();
    }

    public static String processKey(String key) {
        if (key == null) return null;
        int len = key.length();
        if (len == 0) return key;

        // 提前扫描确定是否需要处理
        boolean hasHash = false;
        boolean hasDash = false;
        int hashPos = -1;

        for (int i = 0; i < len; i++) {
            char c = key.charAt(i);
            if (c == '#') {
                hasHash = true;
                hashPos = i;
                break;
            } else if (c == '-') {
                hasDash = true;
            }
        }

        // 情况1：无需任何处理
        if (!hasHash && !hasDash) {
            return key;
        }

        // 情况2：只有替换，没有截断
        if (!hasHash) {
            char[] chars = key.toCharArray();
            for (int i = 0; i < len; i++) {
                if (chars[i] == '-') {
                    chars[i] = '_';
                }
            }
            return new String(chars);
        }

        // 情况3：需要截断（可能有替换）
        int newLen = hashPos;
        char[] result = new char[newLen];

        // 只需要复制到 hashPos 位置
        for (int i = 0; i < newLen; i++) {
            char c = key.charAt(i);
            result[i] = (c == '-') ? '_' : c;
        }

        return new String(result);
    }

    public static void init() {}
}
