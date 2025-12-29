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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public abstract class AbstractCustomItem<I> implements CustomItem<I> {
    protected final boolean isVanillaItem;
    protected final UniqueKey id;
    protected final Key material;
    protected final Key clientBoundMaterial;
    protected final ItemProcessor[] modifiers;
    protected final ItemProcessor[] clientBoundModifiers;
    protected final List<ItemBehavior> behaviors;
    protected final ItemSettings settings;
    protected final Map<EventTrigger, List<Function<Context>>> events;
    protected final ItemUpdateConfig updater;

    @SuppressWarnings("unchecked")
    public AbstractCustomItem(boolean isVanillaItem, UniqueKey id, Key material, Key clientBoundMaterial,
                              List<ItemBehavior> behaviors,
                              List<ItemProcessor> modifiers,
                              List<ItemProcessor> clientBoundModifiers,
                              ItemSettings settings,
                              Map<EventTrigger, List<Function<Context>>> events,
                              ItemUpdateConfig updater) {
        this.isVanillaItem = isVanillaItem;
        this.id = id;
        this.material = material;
        this.clientBoundMaterial = clientBoundMaterial;
        this.events = events;
        // unchecked cast
        this.modifiers = modifiers.toArray(new ItemProcessor[0]);
        // unchecked cast
        this.clientBoundModifiers = clientBoundModifiers.toArray(new ItemProcessor[0]);
        this.behaviors = List.copyOf(behaviors);
        this.settings = settings;
        this.updater = updater;
    }

    @Override
    public void execute(Context context, EventTrigger trigger) {
        for (Function<Context> function : Optional.ofNullable(this.events.get(trigger)).orElse(Collections.emptyList())) {
            function.run(context);
        }
    }

    @Override
    public Optional<ItemUpdateConfig> updater() {
        return Optional.ofNullable(this.updater);
    }

    @Override
    public Key id() {
        return this.id.key();
    }

    @Override
    public UniqueKey uniqueId() {
        return this.id;
    }

    @Override
    public Key material() {
        return this.material;
    }

    @Override
    public Key clientBoundMaterial() {
        return this.clientBoundMaterial;
    }

    @Override
    public ItemProcessor[] dataModifiers() {
        return this.modifiers;
    }

    @Override
    public boolean isVanillaItem() {
        return isVanillaItem;
    }

    @Override
    public boolean hasClientBoundDataModifier() {
        return this.clientBoundModifiers.length != 0;
    }

    @Override
    public ItemProcessor[] clientBoundDataModifiers() {
        return this.clientBoundModifiers;
    }

    @Override
    public ItemSettings settings() {
        return this.settings;
    }

    @Override
    public @NotNull List<ItemBehavior> behaviors() {
        return this.behaviors;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }
}
