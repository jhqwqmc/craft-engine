package net.momirealms.craftengine.core.block.properties;

import net.momirealms.craftengine.core.block.properties.type.*;
import net.momirealms.craftengine.core.plugin.locale.LocalizedResourceConfigException;
import net.momirealms.craftengine.core.registry.BuiltInRegistries;
import net.momirealms.craftengine.core.registry.Registries;
import net.momirealms.craftengine.core.registry.WritableRegistry;
import net.momirealms.craftengine.core.util.*;

import java.util.Map;

public final class Properties {
    public static final PropertyType<?> BOOLEAN = register(BooleanProperty.ID, BooleanProperty.FACTORY);
    public static final PropertyType<?> INT = register(IntegerProperty.ID, IntegerProperty.FACTORY);
    public static final PropertyType<?> STRING = register(StringProperty.ID, StringProperty.FACTORY);
    public static final PropertyType<?> AXIS = register(Key.of("craftengine:axis"), EnumProperty.factory(Direction.Axis.class));
    public static final PropertyType<?> HORIZONTAL_DIRECTION = register(Key.of("craftengine:horizontal_direction"), EnumProperty.factory(HorizontalDirection.class));
    public static final PropertyType<?> FOUR_DIRECTION = register(Key.of("craftengine:4-direction"), EnumProperty.factory(HorizontalDirection.class));
    public static final PropertyType<?> DIRECTION = register(Key.of("craftengine:direction"), EnumProperty.factory(Direction.class));
    public static final PropertyType<?> SIX_DIRECTION = register(Key.of("craftengine:6-direction"), EnumProperty.factory(Direction.class));
    public static final PropertyType<?> SINGLE_BLOCK_HALF = register(Key.of("craftengine:single_block_half"), EnumProperty.factory(SingleBlockHalf.class));
    public static final PropertyType<?> DOUBLE_BLOCK_HALF = register(Key.of("craftengine:double_block_half"), EnumProperty.factory(DoubleBlockHalf.class));
    public static final PropertyType<?> HINGE = register(Key.of("craftengine:hinge"), EnumProperty.factory(DoorHinge.class));
    public static final PropertyType<?> STAIRS_SHAPE = register(Key.of("craftengine:stairs_shape"), EnumProperty.factory(StairsShape.class));
    public static final PropertyType<?> SLAB_TYPE = register(Key.of("craftengine:slab_type"), EnumProperty.factory(SlabType.class));
    public static final PropertyType<?> SOFA_SHAPE = register(Key.of("craftengine:sofa_shape"), EnumProperty.factory(SofaShape.class));
    public static final PropertyType<?> ANCHOR_TYPE = register(Key.of("craftengine:anchor_type"), EnumProperty.factory(AnchorType.class));

    private Properties() {}

    public static <T extends Comparable<T>> PropertyType<T> register(Key key, PropertyFactory<T> factory) {
        PropertyType<T> type = new PropertyType<>(key, factory);
        ((WritableRegistry<PropertyType<?>>) BuiltInRegistries.PROPERTY_TYPE)
                .register(ResourceKey.create(Registries.PROPERTY_TYPE.location(), key), type);
        return type;
    }

    @SuppressWarnings("unchecked")
    public static <T extends Comparable<T>> Property<T> fromMap(String name, Map<String, Object> map) {
        String type = ResourceConfigUtils.requireNonEmptyStringOrThrow(map.get("type"), "warning.config.block.state.property.missing_type");
        Key key = Key.withDefaultNamespace(type, Key.DEFAULT_NAMESPACE);
        PropertyType<T> propertyType = (PropertyType<T>) BuiltInRegistries.PROPERTY_TYPE.getValue(key);
        if (propertyType == null) {
            throw new LocalizedResourceConfigException("warning.config.block.state.property.invalid_type", key.toString(), name);
        }
        return propertyType.factory().create(name, map);
    }
}
