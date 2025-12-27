package net.momirealms.craftengine.core.item.recipe.result;

import net.momirealms.craftengine.core.plugin.locale.LocalizedResourceConfigException;
import net.momirealms.craftengine.core.registry.BuiltInRegistries;
import net.momirealms.craftengine.core.registry.Registries;
import net.momirealms.craftengine.core.registry.WritableRegistry;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;
import net.momirealms.craftengine.core.util.ResourceKey;

import java.util.Map;

public final class PostProcessors {
    public static final PostProcessorType<?> APPLY_DATA = register(Key.ce("apply_data"), ApplyItemDataPostProcessor.FACTORY);

    private PostProcessors() {}

    @SuppressWarnings("unchecked")
    public static <T> PostProcessor<T> fromMap(Map<String, Object> map) {
        String type = ResourceConfigUtils.requireNonEmptyStringOrThrow(map.get("type"), "warning.config.recipe.result.post_processor.missing_type");
        Key key = Key.withDefaultNamespace(type, Key.DEFAULT_NAMESPACE);
        PostProcessorType<T> processorType = (PostProcessorType<T>) BuiltInRegistries.RECIPE_POST_PROCESSOR_TYPE.getValue(key);
        if (processorType == null) {
            throw new LocalizedResourceConfigException("warning.config.recipe.result.post_processor.invalid_type", type);
        }
        return processorType.factory().create(map);
    }

    public static <T> PostProcessorType<T> register(Key id, PostProcessorFactory<T> factory) {
        PostProcessorType<T> type = new PostProcessorType<>(id, factory);
        ((WritableRegistry<PostProcessorType<?>>) BuiltInRegistries.RECIPE_POST_PROCESSOR_TYPE)
                .register(ResourceKey.create(Registries.RECIPE_POST_PROCESSOR_TYPE.location(), id), type);
        return type;
    }
}
