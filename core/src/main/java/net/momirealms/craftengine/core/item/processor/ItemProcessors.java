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

    public static final ItemProcessorType<?> ITEM_MODEL = register(ItemModelProcessor.ID, ItemModelProcessor.FACTORY, VersionHelper.isOrAbove1_21_2());
    public static final ItemProcessorType<?> ARGUMENTS = register(ArgumentsProcessor.ID, ArgumentsProcessor.FACTORY);
    public static final ItemProcessorType<?> OVERWRITABLE_ITEM_MODEL = register(OverwritableItemModelProcessor.ID, OverwritableItemModelProcessor.FACTORY, VersionHelper.isOrAbove1_21_2());
    public static final ItemProcessorType<?> ID = register(IdProcessor.ID, IdProcessor.FACTORY);
    public static final ItemProcessorType<?> HIDE_TOOLTIP = register(HideTooltipProcessor.ID, HideTooltipProcessor.FACTORY);
    public static final ItemProcessorType<?> FOOD = register(FoodProcessor.ID, FoodProcessor.FACTORY, VersionHelper.isOrAbove1_20_5());
    public static final ItemProcessorType<?> EXTERNAL = register(ExternalSourceProcessor.ID, ExternalSourceProcessor.FACTORY);
    public static final ItemProcessorType<?> EQUIPPABLE = register(EquippableProcessor.ID, EquippableProcessor.FACTORY, VersionHelper.isOrAbove1_21_2());
    public static final ItemProcessorType<?> EQUIPPABLE_ASSET_ID = register(EquippableAssetIdProcessor.ID, EquippableAssetIdProcessor.FACTORY, VersionHelper.isOrAbove1_21_2());
    public static final ItemProcessorType<?> ENCHANTMENTS = register(EnchantmentsProcessor.ID, EnchantmentsProcessor.FACTORY);
    public static final ItemProcessorType<?> ENCHANTMENT = register(Key.of("craftengine:enchantment"), EnchantmentsProcessor.FACTORY);
    public static final ItemProcessorType<?> DYED_COLOR = register(DyedColorProcessor.ID, DyedColorProcessor.FACTORY);
    public static final ItemProcessorType<?> DISPLAY_NAME = register(Key.of("craftengine:display_name"), ItemNameProcessor.FACTORY);
    public static final ItemProcessorType<?> ITEM_NAME = register(ItemNameProcessor.ID, ItemNameProcessor.FACTORY);
    public static final ItemProcessorType<?> CUSTOM_NAME = register(CustomNameProcessor.ID, CustomNameProcessor.FACTORY);
    public static final ItemProcessorType<?> CUSTOM_MODEL_DATA = register(CustomModelDataProcessor.ID, CustomModelDataProcessor.FACTORY);
    public static final ItemProcessorType<?> OVERWRITABLE_CUSTOM_MODEL_DATA = register(OverwritableCustomModelDataProcessor.ID, OverwritableCustomModelDataProcessor.FACTORY);
    public static final ItemProcessorType<?> COMPONENTS = register(ComponentsProcessor.ID, ComponentsProcessor.FACTORY, VersionHelper.isOrAbove1_20_5());
    public static final ItemProcessorType<?> COMPONENT = register(Key.of("craftengine:component"), ComponentsProcessor.FACTORY, VersionHelper.isOrAbove1_20_5());
    public static final ItemProcessorType<?> ATTRIBUTE_MODIFIERS = register(AttributeModifiersProcessor.ID, AttributeModifiersProcessor.FACTORY);
    public static final ItemProcessorType<?> ATTRIBUTES = register(Key.of("craftengine:attributes"), AttributeModifiersProcessor.FACTORY);
    public static final ItemProcessorType<?> PDC = register(PDCProcessor.ID, PDCProcessor.FACTORY);
    public static final ItemProcessorType<?> OVERWRITABLE_ITEM_NAME = register(OverwritableItemNameProcessor.ID, OverwritableItemNameProcessor.FACTORY);
    public static final ItemProcessorType<?> JUKEBOX_PLAYABLE = register(JukeboxSongProcessor.ID, JukeboxSongProcessor.FACTORY, VersionHelper.isOrAbove1_21());
    public static final ItemProcessorType<?> REMOVE_COMPONENTS = register(RemoveComponentProcessor.ID, RemoveComponentProcessor.FACTORY, VersionHelper.isOrAbove1_20_5());
    public static final ItemProcessorType<?> REMOVE_COMPONENT = register(Key.of("craftengine:remove_component"), RemoveComponentProcessor.FACTORY, VersionHelper.isOrAbove1_20_5());
    public static final ItemProcessorType<?> TAGS = register(TagsProcessor.ID, TagsProcessor.FACTORY);
    public static final ItemProcessorType<?> NBT = register(Key.of("craftengine:nbt"), TagsProcessor.FACTORY);
    public static final ItemProcessorType<?> TOOLTIP_STYLE = register(TooltipStyleProcessor.ID, TooltipStyleProcessor.FACTORY, VersionHelper.isOrAbove1_21_2());
    public static final ItemProcessorType<?> TRIM = register(TrimProcessor.ID, TrimProcessor.FACTORY);
    public static final ItemProcessorType<?> LORE = register(LoreProcessor.ID, LoreProcessor.FACTORY);
    public static final ItemProcessorType<?> UNBREAKABLE = register(UnbreakableProcessor.ID, UnbreakableProcessor.FACTORY);
    public static final ItemProcessorType<?> DYNAMIC_LORE = register(DynamicLoreProcessor.ID, DynamicLoreProcessor.FACTORY);
    public static final ItemProcessorType<?> OVERWRITABLE_LORE = register(OverwritableLoreProcessor.ID, OverwritableLoreProcessor.FACTORY);
    public static final ItemProcessorType<?> MAX_DAMAGE = register(MaxDamageProcessor.ID, MaxDamageProcessor.FACTORY, VersionHelper.isOrAbove1_20_5());
    public static final ItemProcessorType<?> BLOCK_STATE = register(BlockStateProcessor.ID, BlockStateProcessor.FACTORY);
    public static final ItemProcessorType<?> CONDITIONAL = register(ConditionalProcessor.ID, ConditionalProcessor.FACTORY, VersionHelper.PREMIUM);
    public static final ItemProcessorType<?> CONDITION = register(Key.of("craftengine:condition"), ConditionalProcessor.FACTORY, VersionHelper.PREMIUM);

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
