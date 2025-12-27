package net.momirealms.craftengine.core.item.recipe.result;

import java.util.Map;

public interface PostProcessorFactory<T> {

    PostProcessor<T> create(Map<String, Object> args);
}
