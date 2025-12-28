package net.momirealms.craftengine.core.item;

import net.momirealms.craftengine.core.item.behavior.ItemBehavior;
import net.momirealms.craftengine.core.item.processor.ItemProcessor;
import net.momirealms.craftengine.core.item.updater.ItemUpdateConfig;
import net.momirealms.craftengine.core.plugin.context.Context;
import net.momirealms.craftengine.core.plugin.context.EventTrigger;
import net.momirealms.craftengine.core.plugin.context.function.Function;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.UniqueKey;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface CustomItem<I> extends BuildableItem<I> {

    /**
     * Since CraftEngine allows users to add certain functionalities to vanilla items, this custom item might actually be a vanilla item.
     * This will be refactored before the 1.0 release, but no changes will be made for now to ensure compatibility.
     */
    boolean isVanillaItem();

    Key id();

    UniqueKey uniqueId();

    default String translationKey() {
        Key id = this.id();
        return "item." + id.namespace() + "." + id.value();
    }

    Key material();

    Key clientBoundMaterial();

    ItemProcessor[] dataModifiers();

    boolean hasClientBoundDataModifier();

    ItemProcessor[] clientBoundDataModifiers();

    ItemSettings settings();

    Optional<ItemUpdateConfig> updater();

    default boolean is(Key tag) {
        return settings().tags().contains(tag);
    }

    void execute(Context context, EventTrigger trigger);

    @NotNull
    List<ItemBehavior> behaviors();

    interface Builder<I> {
        Builder<I> isVanillaItem(boolean isVanillaItem);

        Builder<I> id(UniqueKey id);

        Builder<I> clientBoundMaterial(Key clientBoundMaterialKey);

        Builder<I> material(Key material);

        Builder<I> dataModifier(ItemProcessor modifier);

        Builder<I> dataModifiers(List<ItemProcessor> modifiers);

        Builder<I> clientBoundDataModifier(ItemProcessor modifier);

        Builder<I> clientBoundDataModifiers(List<ItemProcessor> modifiers);

        Builder<I> behavior(ItemBehavior behavior);

        Builder<I> behaviors(List<ItemBehavior> behaviors);

        Builder<I> settings(ItemSettings settings);

        Builder<I> updater(ItemUpdateConfig updater);

        Builder<I> events(Map<EventTrigger, List<Function<Context>>> events);

        CustomItem<I> build();
    }
}
