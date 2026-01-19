package net.momirealms.craftengine.core.pack.model.definition;

import com.google.gson.JsonObject;
import net.momirealms.craftengine.core.plugin.locale.LocalizedResourceConfigException;
import net.momirealms.craftengine.core.registry.BuiltInRegistries;
import net.momirealms.craftengine.core.registry.Registries;
import net.momirealms.craftengine.core.registry.WritableRegistry;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.MiscUtils;
import net.momirealms.craftengine.core.util.ResourceKey;

import java.util.List;
import java.util.Map;

public final class ItemModels {
    public static final ItemModelType<EmptyItemModel> EMPTY = register(Key.of("empty"), EmptyItemModel.FACTORY, EmptyItemModel.READER);
    public static final ItemModelType<BaseItemModel> MODEL = register(Key.of("model"), BaseItemModel.FACTORY, BaseItemModel.READER);
    public static final ItemModelType<CompositeItemModel> COMPOSITE = register(Key.of("composite"), CompositeItemModel.FACTORY, CompositeItemModel.READER);
    public static final ItemModelType<ConditionItemModel> CONDITION = register(Key.of("condition"), ConditionItemModel.FACTORY, ConditionItemModel.READER);
    public static final ItemModelType<RangeDispatchItemModel> RANGE_DISPATCH = register(Key.of("range_dispatch"), RangeDispatchItemModel.FACTORY, RangeDispatchItemModel.READER);
    public static final ItemModelType<SelectItemModel> SELECT = register(Key.of("select"), SelectItemModel.FACTORY, SelectItemModel.READER);
    public static final ItemModelType<SpecialItemModel> SPECIAL = register(Key.of("special"), SpecialItemModel.FACTORY, SpecialItemModel.READER);
    public static final ItemModelType<BundleSelectedItemModel> BUNDLE_SELECTED_ITEM = register(Key.of("bundle/selected_item"), BundleSelectedItemModel.FACTORY, BundleSelectedItemModel.READER);

    private ItemModels() {}

    public static <T extends ItemModel> ItemModelType<T> register(Key key, ItemModelFactory<T> factory, ItemModelReader<T> reader) {
        ItemModelType<T> type = new ItemModelType<>(key, factory, reader);
        ((WritableRegistry<ItemModelType<? extends ItemModel>>) BuiltInRegistries.ITEM_MODEL_TYPE)
                .register(ResourceKey.create(Registries.ITEM_MODEL_TYPE.location(), key), type);
        return type;
    }

    public static ItemModel fromObj(Object object) {
        if (object == null) return null;
        if (object instanceof Map<?,?> map) {
            return fromMap(MiscUtils.castToMap(map, false));
        } else {
            return new BaseItemModel(object.toString(), List.of(), null);
        }
    }

    public static ItemModel fromMap(Map<String, Object> map) {
        String type = map.getOrDefault("type", "minecraft:model").toString();
        Key key = Key.withDefaultNamespace(type, "minecraft");
        ItemModelType<? extends ItemModel> itemModelType = BuiltInRegistries.ITEM_MODEL_TYPE.getValue(key);
        if (itemModelType == null) {
            throw new LocalizedResourceConfigException("warning.config.item.model.invalid_type", type);
        }
        return itemModelType.factory().create(map);
    }

    public static ItemModel fromJson(JsonObject json) {
        String type = json.get("type").getAsString();
        Key key = Key.withDefaultNamespace(type, "minecraft");
        ItemModelType<? extends ItemModel> itemModelType = BuiltInRegistries.ITEM_MODEL_TYPE.getValue(key);
        if (itemModelType == null) {
            throw new IllegalArgumentException("Invalid item model type: " + key);
        }
        return itemModelType.reader().read(json);
    }
}
