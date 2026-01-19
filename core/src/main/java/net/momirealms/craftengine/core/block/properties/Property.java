package net.momirealms.craftengine.core.block.properties;

import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.util.Direction;
import net.momirealms.craftengine.core.util.HorizontalDirection;
import net.momirealms.craftengine.core.util.SegmentedAngle;
import net.momirealms.craftengine.core.world.context.BlockPlaceContext;
import net.momirealms.sparrow.nbt.Tag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Function;

@SuppressWarnings("unchecked")
public abstract class Property<T extends Comparable<T>> {
    public static final Map<String, Function<Property<?>, BiFunction<BlockPlaceContext, ImmutableBlockState, ImmutableBlockState>>> HARD_CODED_PLACEMENTS = new HashMap<>();

    static {
        HARD_CODED_PLACEMENTS.put("axis", (property -> {
            Property<Direction.Axis> axisProperty = (Property<Direction.Axis>) property;
            return (context, state) -> state.with(axisProperty, context.getClickedFace().axis());
        }));
        HARD_CODED_PLACEMENTS.put("facing", (property -> {
            if (property.valueClass() == HorizontalDirection.class) {
                Property<HorizontalDirection> directionProperty = (Property<HorizontalDirection>) property;
                return (context, state) -> state.with(directionProperty, context.getHorizontalDirection().opposite().toHorizontalDirection());
            } else if (property.valueClass() == Direction.class) {
                Property<Direction> directionProperty = (Property<Direction>) property;
                return (context, state) -> state.with(directionProperty, context.getNearestLookingDirection().opposite());
            } else {
                return (context, state) -> state;
            }
        }));
        HARD_CODED_PLACEMENTS.put("facing_clockwise", (property -> {
            if (property.valueClass() == HorizontalDirection.class) {
                Property<HorizontalDirection> directionProperty = (Property<HorizontalDirection>) property;
                return (context, state) -> state.with(directionProperty, context.getHorizontalDirection().clockWise().toHorizontalDirection());
            } else {
                return (context, state) -> state;
            }
        }));
        HARD_CODED_PLACEMENTS.put("waterlogged", (property -> {
            Property<Boolean> waterloggedProperty = (Property<Boolean>) property;
            return (context, state) -> state.with(waterloggedProperty, context.isWaterSource());
        }));
        HARD_CODED_PLACEMENTS.put("rotation", property -> {
            if (property.valueClass() == Integer.class) {
                IntegerProperty rotationProperty = (IntegerProperty) property;
                if (rotationProperty.min == 0 && rotationProperty.max > 0) {
                    return (context, state) -> {
                        float rotation = context.getRotation();
                        SegmentedAngle segmentedAngle = new SegmentedAngle(rotationProperty.max + 1);
                        return state.with(rotationProperty, segmentedAngle.fromDegrees(rotation + 180));
                    };
                }
            }
            return (context, state) -> state;
        });
    }

    private final Class<T> clazz;
    private final String name;
    @Nullable
    private Integer hashCode;

    private static final AtomicInteger ID_GENERATOR = new AtomicInteger();
    private final int id;
    private final T defaultValue;
    private T[] byId;
    private int defaultValueIndex;

    protected Property(String name, Class<T> clazz, T defaultValue) {
        this.clazz = clazz;
        this.name = name;
        this.id = ID_GENERATOR.getAndIncrement();
        this.defaultValue = defaultValue;
    }

    public T defaultValue() {
        return defaultValue;
    }

    public int defaultValueIndex() {
        return defaultValueIndex;
    }

    public int id() {
        return this.id;
    }

    public T byId(int id) {
        final T[] byId = this.byId;
        return id < 0 || id >= byId.length ? null : this.byId[id];
    }

    protected void setById(T[] byId) {
        if (this.byId != null) {
            throw new IllegalStateException();
        }
        this.byId = byId;
        this.defaultValueIndex = indexOf(defaultValue);
    }

    public Value<T> value(T value) {
        return new Value<>(this, value);
    }

    public abstract int idFor(T value);

    public abstract String valueName(T value);

    public abstract List<T> possibleValues();

    public abstract Optional<T> optional(String valueName);

    public abstract Optional<Tag> createOptionalTag(String valueName);

    public abstract Tag pack(T value);

    public int indexOf(Tag tag) {
        return indexOf(unpack(tag));
    }

    @Nullable
    public abstract T valueByName(String name);

    public abstract int indexOf(T value);

    public abstract T unpack(Tag tag);

    public String name() {
        return this.name;
    }

    public Class<T> valueClass() {
        return this.clazz;
    }

//    @Override
//    public boolean equals(Object object) {
//        return this == object;
//    }

    @Override
    public final int hashCode() {
        if (this.hashCode == null) {
            this.hashCode = this.generateHashCode();
        }
        return this.hashCode;
    }

    protected int generateHashCode() {
        return 31 * this.clazz.hashCode() + this.name.hashCode();
    }

    public record Value<T extends Comparable<T>>(Property<T> property, T value) {
        public Value(Property<T> property, T value) {
            if (!property.possibleValues().contains(value)) {
                throw new IllegalArgumentException("Value " + value + " does not belong to property " + property);
            } else {
                this.property = property;
                this.value = value;
            }
        }

        @Override
        public @NotNull String toString() {
            return this.property.name + "=" + this.property.valueName(this.value);
        }
    }

    public static BiFunction<BlockPlaceContext, ImmutableBlockState, ImmutableBlockState> createStateForPlacement(String id, Property<?> property) {
        return Optional.ofNullable(HARD_CODED_PLACEMENTS.get(id))
                .map(it -> it.apply(property))
                .orElse(((context, state) -> ImmutableBlockState.with(state, property, property.defaultValue())));
    }

    @SuppressWarnings("unchecked")
    public static <T extends Comparable<T>> String formatValue(Property<T> property, Comparable<?> value) {
        return property.valueName((T) value);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{clazz=" + this.clazz + ", name='" + this.name + "', values=" + this.possibleValues() + '}';
    }
}
