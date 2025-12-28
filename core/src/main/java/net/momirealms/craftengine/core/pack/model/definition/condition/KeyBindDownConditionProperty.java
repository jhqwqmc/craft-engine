package net.momirealms.craftengine.core.pack.model.definition.condition;

import com.google.gson.JsonObject;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;

import java.util.Map;

public final class KeyBindDownConditionProperty implements ConditionProperty {
    public static final ConditionPropertyFactory<KeyBindDownConditionProperty> FACTORY = new Factory();
    public static final ConditionPropertyReader<KeyBindDownConditionProperty> READER = new Reader();
    private final String keybind;

    public KeyBindDownConditionProperty(String keybind) {
        this.keybind = keybind;
    }

    public String keybind() {
        return this.keybind;
    }

    @Override
    public void accept(JsonObject jsonObject) {
        jsonObject.addProperty("property", "keybind_down");
        jsonObject.addProperty("keybind", this.keybind);
    }

    private static class Factory implements ConditionPropertyFactory<KeyBindDownConditionProperty> {
        @Override
        public KeyBindDownConditionProperty create(Map<String, Object> arguments) {
            String keybindObj = ResourceConfigUtils.requireNonEmptyStringOrThrow(arguments.get("keybind"), "warning.config.item.model.condition.keybind.missing_keybind");
            return new KeyBindDownConditionProperty(keybindObj);
        }
    }

    private static class Reader implements ConditionPropertyReader<KeyBindDownConditionProperty> {
        @Override
        public KeyBindDownConditionProperty read(JsonObject json) {
            String keybind = json.get("keybind").getAsString();
            return new KeyBindDownConditionProperty(keybind);
        }
    }
}
