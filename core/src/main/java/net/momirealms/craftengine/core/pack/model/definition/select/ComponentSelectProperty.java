package net.momirealms.craftengine.core.pack.model.definition.select;

import com.google.gson.JsonObject;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;

import java.util.Map;

public final class ComponentSelectProperty implements SelectProperty {
    public static final SelectPropertyFactory FACTORY = new Factory();
    public static final SelectPropertyReader READER = new Reader();
    private final String component;

    public ComponentSelectProperty(String component) {
        this.component = component;
    }

    public String component() {
        return this.component;
    }

    @Override
    public void accept(JsonObject jsonObject) {
        jsonObject.addProperty("property", "component");
        jsonObject.addProperty("component", this.component);
    }

    private static class Factory implements SelectPropertyFactory {
        @Override
        public SelectProperty create(Map<String, Object> arguments) {
            String component = ResourceConfigUtils.requireNonEmptyStringOrThrow(arguments.get("component"), "warning.config.item.model.select.component.missing_component");
            return new ComponentSelectProperty(component);
        }
    }

    private static class Reader implements SelectPropertyReader {
        @Override
        public SelectProperty read(JsonObject json) {
            String component = json.get("component").getAsString();
            return new ComponentSelectProperty(component);
        }
    }
}
