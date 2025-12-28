package net.momirealms.craftengine.core.pack.model.definition.tint;

import com.google.gson.JsonObject;
import net.momirealms.craftengine.core.plugin.locale.LocalizedResourceConfigException;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;

import java.util.Map;

public final class GrassTint implements Tint {
    public static final TintFactory<GrassTint> FACTORY = new Factory();
    public static final TintReader<GrassTint> READER = new Reader();
    private final float temperature;
    private final float downfall;

    public GrassTint(float temperature, float downfall) {
        this.temperature = temperature;
        this.downfall = downfall;
    }

    public float temperature() {
        return this.temperature;
    }

    public float downfall() {
        return this.downfall;
    }

    @Override
    public JsonObject get() {
        JsonObject json = new JsonObject();
        json.addProperty("type", "grass");
        json.addProperty("temperature", this.temperature);
        json.addProperty("downfall", this.downfall);
        return json;
    }

    private static class Factory implements TintFactory<GrassTint> {

        @Override
        public GrassTint create(Map<String, Object> arguments) {
            float temperature = ResourceConfigUtils.getAsFloat(arguments.getOrDefault("temperature", 0), "temperature");
            float downfall = ResourceConfigUtils.getAsFloat(arguments.getOrDefault("downfall", 0), "downfall");
            if (temperature > 1 || temperature < 0) {
                throw new LocalizedResourceConfigException("warning.config.item.model.tint.grass.invalid_temp", String.valueOf(temperature));
            }
            if (downfall > 1 || downfall < 0) {
                throw new LocalizedResourceConfigException("warning.config.item.model.tint.grass.invalid_downfall", String.valueOf(downfall));
            }
            return new GrassTint(temperature, downfall);
        }
    }

    private static class Reader implements TintReader<GrassTint> {
        @Override
        public GrassTint read(JsonObject json) {
            float temperature = json.has("temperature") ? json.get("temperature").getAsFloat() : 0;
            float downfall = json.has("downfall") ? json.get("downfall").getAsFloat() : 0;
            return new GrassTint(temperature, downfall);
        }
    }
}
