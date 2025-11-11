package net.momirealms.craftengine.core.pack.model.simplified;

import net.momirealms.craftengine.core.util.Key;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public interface SimplifiedModelReader {

    @Nullable
    Map<String, Object> convert(List<String> textures, List<String> optionalModelPaths, Key id);
}
