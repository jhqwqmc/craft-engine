package net.momirealms.craftengine.core.block.properties;

import net.momirealms.craftengine.core.registry.BuiltInRegistries;
import net.momirealms.craftengine.core.registry.Holder;
import net.momirealms.craftengine.core.registry.Registries;
import net.momirealms.craftengine.core.registry.WritableRegistry;
import net.momirealms.craftengine.core.util.*;

import java.util.Map;

public class Properties {
    public static final Key BOOLEAN = Key.of("craftengine:boolean");
    public static final Key INT = Key.of("craftengine:int");
    public static final Key STRING = Key.of("craftengine:string");
    public static final Key AXIS = Key.of("craftengine:axis");
    public static final Key HORIZONTAL_DIRECTION = Key.of("craftengine:4-direction");
    public static final Key DIRECTION = Key.of("craftengine:6-direction");
    public static final Key HORIZONTAL_FACING = Key.of("craftengine:horizontal-facing");
    public static final Key DOUBLE_BLOCK_HALF = Key.of("craftengine:double-block-half");

    static {
        register(BOOLEAN, BooleanProperty.FACTORY);
        register(INT, IntegerProperty.FACTORY);
        register(STRING, StringProperty.FACTORY);
        register(AXIS, new EnumProperty.Factory<>(Direction.Axis.class));
        register(DIRECTION, new EnumProperty.Factory<>(Direction.class));
        register(HORIZONTAL_DIRECTION, new EnumProperty.Factory<>(HorizontalDirection.class));
        register(HORIZONTAL_FACING, new EnumProperty.Factory<>(DoorHingeSide.class));
        register(DOUBLE_BLOCK_HALF, new EnumProperty.Factory<>(DoubleBlockHalf.class));
    }

    public static void register(Key key, PropertyFactory factory) {
        Holder.Reference<PropertyFactory> holder = ((WritableRegistry<PropertyFactory>) BuiltInRegistries.PROPERTY_FACTORY).registerForHolder(new ResourceKey<>(Registries.PROPERTY_FACTORY.location(), key));
        holder.bindValue(factory);
    }

    public static Property<?> fromMap(String name, Map<String, Object> map) {
        String type = (String) map.getOrDefault("type", "empty");
        if (type == null) {
            throw new NullPointerException("behavior type cannot be null");
        }
        Key key = Key.withDefaultNamespace(type, "craftengine");
        PropertyFactory factory = BuiltInRegistries.PROPERTY_FACTORY.getValue(key);
        if (factory == null) {
            throw new IllegalArgumentException("Unknown property type: " + type);
        }
        return factory.create(name, map);
    }
}
