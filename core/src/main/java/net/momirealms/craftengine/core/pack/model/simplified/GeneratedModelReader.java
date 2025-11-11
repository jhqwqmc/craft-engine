package net.momirealms.craftengine.core.pack.model.simplified;

import net.momirealms.craftengine.core.util.Key;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GeneratedModelReader implements SimplifiedModelReader {
    public static final GeneratedModelReader INSTANCE = new GeneratedModelReader();

    @Override
    public Map<String, Object> convert(List<String> textures, List<String> optionalModelPaths, Key id) {
        boolean autoModelPath = optionalModelPaths.size() != 1;
        Map<String, String> texturesProperty;
        switch (textures.size()) {
            case 1 -> texturesProperty = Map.of("layer0", textures.getFirst());
            case 2 -> texturesProperty = Map.of(
                    "layer0", textures.get(0),
                    "layer1", textures.get(1)
            );
            default -> {
                texturesProperty = new HashMap<>();
                for (int i = 0; i < textures.size(); i++) {
                    texturesProperty.put("layer" + i, textures.get(i));
                }
            }
        }
        return Map.of(
                "type", "model",
                "path", autoModelPath ? id.namespace() + ":item/" + id.value() : optionalModelPaths.getFirst(),
                "generation", Map.of(
                        "parent", "item/generated",
                        "textures", texturesProperty
                )
        );
    }
}
