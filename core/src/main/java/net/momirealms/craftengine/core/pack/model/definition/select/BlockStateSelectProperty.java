package net.momirealms.craftengine.core.pack.model.definition.select;

import com.google.gson.JsonObject;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;

import java.util.Map;

public final class BlockStateSelectProperty implements SelectProperty {
    public static final SelectPropertyFactory<BlockStateSelectProperty> FACTORY = new Factory();
    public static final SelectPropertyReader<BlockStateSelectProperty> READER = new Reader();
    private final String blockStateProperty;

    public BlockStateSelectProperty(String blockStateProperty) {
        this.blockStateProperty = blockStateProperty;
    }

    public String blockStateProperty() {
        return this.blockStateProperty;
    }

    @Override
    public void accept(JsonObject jsonObject) {
        jsonObject.addProperty("property", "block_state");
        jsonObject.addProperty("block_state_property", this.blockStateProperty);
    }

    private static class Factory implements SelectPropertyFactory<BlockStateSelectProperty> {
        @Override
        public BlockStateSelectProperty create(Map<String, Object> arguments) {
            String property = ResourceConfigUtils.requireNonEmptyStringOrThrow(arguments.get("block-state-property"), "warning.config.item.model.select.block_state.missing_property");
            return new BlockStateSelectProperty(property);
        }
    }

    private static class Reader implements SelectPropertyReader<BlockStateSelectProperty> {
        @Override
        public BlockStateSelectProperty read(JsonObject json) {
            String property = json.get("block_state_property").getAsString();
            return new BlockStateSelectProperty(property);
        }
    }
}
