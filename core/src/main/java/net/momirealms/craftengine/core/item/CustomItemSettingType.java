package net.momirealms.craftengine.core.item;

import net.momirealms.craftengine.core.item.processor.ItemProcessor;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public record CustomItemSettingType<T>(@NotNull BiConsumer<T, Consumer<ItemProcessor>> dataProcessor, @NotNull BiConsumer<T, Consumer<ItemProcessor>> clientBoundDataProcessor) {

    public static <T> CustomItemSettingType<T> simple() {
        return new CustomItemSettingType<>((a, b) -> {}, (a, b) -> {});
    }

    public static <T> CustomItemSettingType<T> newType(BiConsumer<T, Consumer<ItemProcessor>> dataProcessor, BiConsumer<T, Consumer<ItemProcessor>> clientBoundDataProcessor) {
        return new CustomItemSettingType<>(dataProcessor, clientBoundDataProcessor);
    }
}
