package net.momirealms.craftengine.core.pack.model.definition.condition;

import com.google.gson.JsonObject;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;

import java.util.Map;

public final class HasComponentConditionProperty implements ConditionProperty {
    public static final ConditionPropertyFactory FACTORY = new Factory();
    public static final ConditionPropertyReader READER = new Reader();
    private final String component;
    private final boolean ignoreDefault;

    public HasComponentConditionProperty(String component, boolean ignoreDefault) {
        this.component = component;
        this.ignoreDefault = ignoreDefault;
    }

    public String component() {
        return this.component;
    }

    public boolean ignoreDefault() {
        return this.ignoreDefault;
    }

    @Override
    public void accept(JsonObject jsonObject) {
        jsonObject.addProperty("property", "has_component");
        jsonObject.addProperty("component", this.component);
        if (this.ignoreDefault) {
            jsonObject.addProperty("ignore_default", true);
        }
    }

    private static class Factory implements ConditionPropertyFactory {
        @Override
        public ConditionProperty create(Map<String, Object> arguments) {
            boolean ignoreDefault = ResourceConfigUtils.getAsBoolean(arguments.getOrDefault("ignore-default", false), "ignore-default");
            String componentObj = ResourceConfigUtils.requireNonEmptyStringOrThrow(arguments.get("component"), "warning.config.item.model.condition.has_component.missing_component");
            return new HasComponentConditionProperty(componentObj, ignoreDefault);
        }
    }

    private static class Reader implements ConditionPropertyReader {
        @Override
        public ConditionProperty read(JsonObject json) {
            String component = json.get("component").getAsString();
            boolean ignoreDefault = json.has("ignore_default") && json.get("ignore_default").getAsBoolean();
            return new HasComponentConditionProperty(component, ignoreDefault);
        }
    }
}
