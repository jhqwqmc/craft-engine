package net.momirealms.craftengine.core.item.processor;

import com.google.common.collect.ImmutableMap;
import net.momirealms.craftengine.core.item.*;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.MiscUtils;
import net.momirealms.craftengine.core.util.VersionHelper;
import net.momirealms.sparrow.nbt.CompoundTag;
import net.momirealms.sparrow.nbt.Tag;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class HideTooltipProcessor<I> implements ItemProcessor<I> {
    public static final ItemProcessorFactory<?> FACTORY = new Factory<>();
    public static final Map<Key, Integer> TO_LEGACY;
    public static final List<Key> COMPONENTS = List.of(
            DataComponentKeys.UNBREAKABLE,
            DataComponentKeys.ENCHANTMENTS,
            DataComponentKeys.STORED_ENCHANTMENTS,
            DataComponentKeys.CAN_PLACE_ON,
            DataComponentKeys.CAN_BREAK,
            DataComponentKeys.ATTRIBUTE_MODIFIERS,
            DataComponentKeys.DYED_COLOR,
            DataComponentKeys.TRIM,
            DataComponentKeys.JUKEBOX_PLAYABLE
    );
    static {
        ImmutableMap.Builder<Key, Integer> builder = ImmutableMap.builder();
        builder.put(DataComponentKeys.ENCHANTMENTS, 1);
        builder.put(DataComponentKeys.ATTRIBUTE_MODIFIERS, 2);
        builder.put(DataComponentKeys.UNBREAKABLE, 4);
        builder.put(DataComponentKeys.CAN_BREAK, 8);
        builder.put(DataComponentKeys.CAN_PLACE_ON, 16);
        builder.put(DataComponentKeys.STORED_ENCHANTMENTS, 32);
        builder.put(DataComponentKeys.POTION_CONTENTS, 32);
        builder.put(DataComponentKeys.WRITTEN_BOOK_CONTENT, 32);
        builder.put(DataComponentKeys.FIREWORKS, 32);
        builder.put(DataComponentKeys.FIREWORK_EXPLOSION, 32);
        builder.put(DataComponentKeys.BUNDLE_CONTENTS, 32);
        builder.put(DataComponentKeys.MAP_ID, 32);
        builder.put(DataComponentKeys.MAP_COLOR, 32);
        builder.put(DataComponentKeys.MAP_DECORATIONS, 32);
        builder.put(DataComponentKeys.DYED_COLOR, 64);
        builder.put(DataComponentKeys.TRIM, 128);
        TO_LEGACY = builder.build();
    }

    private final List<Key> components;
    private final Applier<I> applier;

    public HideTooltipProcessor(List<Key> components) {
        this.components = components;
        if (VersionHelper.isOrAbove1_21_5()) {
            this.applier = new ModernApplier<>(components);
        } else if (VersionHelper.isOrAbove1_20_5()) {
            if (components.isEmpty()) {
                this.applier = new DummyApplier<>();
            } else if (components.size() == 1) {
                if (COMPONENTS.contains(components.getFirst())) {
                    this.applier = new SemiModernApplier<>(components.getFirst());
                } else {
                    this.applier = new DummyApplier<>();
                }
            } else {
                List<Applier<I>> appliers = new ArrayList<>();
                for (Key key : components) {
                    if (!COMPONENTS.contains(key)) continue;
                    appliers.add(new SemiModernApplier<>(key));
                }
                if (appliers.isEmpty()) {
                    this.applier = new DummyApplier<>();
                } else if (appliers.size() == 1) {
                    this.applier = appliers.getFirst();
                } else {
                    this.applier = new CompoundApplier<>(appliers);
                }
            }
        } else {
            this.applier = new LegacyApplier<>(components);
        }
    }

    public List<Key> components() {
        return this.components;
    }

    @Override
    public Item<I> apply(Item<I> item, ItemBuildContext context) {
        this.applier.apply(item);
        return item;
    }

    @Override
    public Item<I> prepareNetworkItem(Item<I> item, ItemBuildContext context, CompoundTag networkData) {
        if (VersionHelper.isOrAbove1_21_5()) {
            Tag previous = item.getSparrowNBTComponent(DataComponentKeys.TOOLTIP_DISPLAY);
            if (previous != null) {
                networkData.put(DataComponentKeys.TOOLTIP_DISPLAY.asString(), NetworkItemHandler.pack(NetworkItemHandler.Operation.ADD, previous));
            } else {
                networkData.put(DataComponentKeys.TOOLTIP_DISPLAY.asString(), NetworkItemHandler.pack(NetworkItemHandler.Operation.REMOVE));
            }
        } else if (VersionHelper.isOrAbove1_20_5()) {
            for (Key component : this.components) {
                Tag previous = item.getSparrowNBTComponent(component);
                if (previous != null) {
                    networkData.put(component.asString(), NetworkItemHandler.pack(NetworkItemHandler.Operation.ADD, previous));
                } else {
                    networkData.put(component.asString(), NetworkItemHandler.pack(NetworkItemHandler.Operation.REMOVE));
                }
            }
        } else {
            Tag previous = item.getTag("HideFlags");
            if (previous != null) {
                networkData.put("HideFlags", NetworkItemHandler.pack(NetworkItemHandler.Operation.ADD, previous));
            } else {
                networkData.put("HideFlags", NetworkItemHandler.pack(NetworkItemHandler.Operation.REMOVE));
            }
        }
        return item;
    }

    public interface Applier<I> {

        void apply(Item<I> item);
    }

    public static class DummyApplier<T> implements Applier<T> {

        @Override
        public void apply(Item<T> item) {
        }
    }

    public static class SemiModernApplier<I> implements Applier<I> {
        private final Key component;

        public SemiModernApplier(Key component) {
            this.component = component;
        }

        @Override
        public void apply(Item<I> item) {
            Tag previous = item.getSparrowNBTComponent(this.component);
            if (previous instanceof CompoundTag compoundTag) {
                compoundTag.putBoolean("show_in_tooltip", false);
                item.setNBTComponent(this.component, compoundTag);
            }
        }
    }

    public record CompoundApplier<I>(List<Applier<I>> appliers) implements Applier<I> {

        @Override
        public void apply(Item<I> item) {
            for (Applier<I> applier : appliers) {
                applier.apply(item);
            }
        }
    }

    public static class LegacyApplier<W> implements Applier<W> {
        private final int legacyValue;

        public LegacyApplier(List<Key> components) {
            int i = 0;
            for (Key key : components) {
                Integer flag = TO_LEGACY.get(key);
                if (flag != null) {
                    i += flag;
                }
            }
            this.legacyValue = i;
        }

        public int legacyValue() {
            return legacyValue;
        }

        @Override
        public void apply(Item<W> item) {
            Integer previousFlags = (Integer) item.getJavaTag("HideFlags");
            if (previousFlags != null) {
                item.setTag(this.legacyValue | previousFlags, "HideFlags");
            } else {
                item.setTag(this.legacyValue, "HideFlags");
            }
        }
    }

    public static class ModernApplier<W> implements Applier<W> {
        private final List<String> components;

        public ModernApplier(List<Key> components) {
            this.components = components.stream().map(Key::toString).collect(Collectors.toList());
        }

        public List<String> components() {
            return components;
        }

        @Override
        public void apply(Item<W> item) {
            Map<String, Object> data = MiscUtils.castToMap(item.getJavaComponent(DataComponentKeys.TOOLTIP_DISPLAY), true);
            if (data == null) {
                item.setJavaComponent(DataComponentKeys.TOOLTIP_DISPLAY, Map.of("hidden_components", this.components));
            } else {
                if (data.get("hidden_components") instanceof List<?> list) {
                    List<String> hiddenComponents = list.stream().map(Object::toString).toList();
                    List<String> mergedComponents = Stream.concat(
                            hiddenComponents.stream(),
                            this.components.stream()
                    ).distinct().toList();
                    Map<String, Object> newData = new HashMap<>(data);
                    newData.put("hidden_components", mergedComponents);
                    item.setJavaComponent(DataComponentKeys.TOOLTIP_DISPLAY, newData);
                } else {
                    Map<String, Object> newData = new HashMap<>(data);
                    newData.put("hidden_components", this.components);
                    item.setJavaComponent(DataComponentKeys.TOOLTIP_DISPLAY, newData);
                }
            }
        }
    }

    private static class Factory<I> implements ItemProcessorFactory<I> {

        @Override
        public ItemProcessor<I> create(Object arg) {
            List<Key> components = MiscUtils.getAsStringList(arg).stream().map(Key::of).toList();
            return new HideTooltipProcessor<>(components);
        }
    }
}
