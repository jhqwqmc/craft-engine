package net.momirealms.craftengine.core.item.component.predicate;

import net.momirealms.craftengine.core.plugin.config.ConfigSection;
import net.momirealms.craftengine.core.plugin.config.KnownResourceException;
import net.momirealms.craftengine.core.registry.BuiltInRegistries;
import net.momirealms.craftengine.core.registry.Registries;
import net.momirealms.craftengine.core.registry.WritableRegistry;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.ResourceKey;

public class DataComponentPredicates {
    public static final DataComponentPredicateType<AllOfDataComponentPredicate> ALL_OF = register(Key.ce("all_of"), AllOfDataComponentPredicate.FACTORY);

    protected DataComponentPredicates() {}

    public static <T extends DataComponentPredicate> DataComponentPredicateType<T> register(Key key, DataComponentPredicateFactory<T> factory) {
        DataComponentPredicateType<T> type = new DataComponentPredicateType<>(key, factory);
        ((WritableRegistry<DataComponentPredicateType<? extends DataComponentPredicate>>) BuiltInRegistries.DATA_COMPONENT_PREDICATE_TYPE)
                .register(ResourceKey.create(Registries.DATA_COMPONENT_PREDICATE_TYPE.location(), key), type);
        return type;
    }

    public static DataComponentPredicate fromConfig(ConfigSection section) {
        String type = section.getNonEmptyString("type");
        Key key = Key.ce(type);
        DataComponentPredicateType<? extends DataComponentPredicate> behaviorType = BuiltInRegistries.DATA_COMPONENT_PREDICATE_TYPE.getValue(key);
        if (behaviorType == null) {
            throw new KnownResourceException("resource.item.data_component_predicate.unknown_type", section.assemblePath("type"), key.asString());
        }
        return behaviorType.factory().create(section);
    }
}
