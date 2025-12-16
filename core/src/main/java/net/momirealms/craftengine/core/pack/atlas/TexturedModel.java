package net.momirealms.craftengine.core.pack.atlas;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.momirealms.craftengine.core.util.Key;

import java.util.HashMap;
import java.util.Map;

public class TexturedModel {
    public static final TexturedModel EMPTY = new TexturedModel(Map.of());
    public static final TexturedModel BUILTIN = new TexturedModel(Map.of());
    public final Map<String, Key> textures;

    private TexturedModel(Map<String, Key> textures) {
        this.textures = textures;
    }

    public TexturedModel(JsonObject model) {
        this.textures = new HashMap<>();
        if (model.has("textures")) {
            JsonObject textures = model.get("textures").getAsJsonObject();
            for (Map.Entry<String, JsonElement> entry : textures.entrySet()) {
                String value = entry.getValue().getAsString();
                // fixme 处理欠妥
                if (value.isEmpty() || value.charAt(0) == '#') continue;
                this.textures.put(entry.getKey(), Key.of(value));
            }
        }
    }

    // 合并父模型的贴图
    public void addParent(TexturedModel parent) {
        if (parent == null || parent == EMPTY) return;
        for (Map.Entry<String, Key> texture : parent.textures.entrySet()) {
            if (!this.textures.containsKey(texture.getKey())) {
                this.textures.put(texture.getKey(), texture.getValue());
            }
        }
    }
}
