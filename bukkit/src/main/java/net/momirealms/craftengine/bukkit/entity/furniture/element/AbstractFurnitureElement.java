package net.momirealms.craftengine.bukkit.entity.furniture.element;

import net.momirealms.craftengine.core.entity.furniture.element.ConditionalFurnitureElement;
import net.momirealms.craftengine.core.plugin.context.PlayerContext;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

public abstract class AbstractFurnitureElement implements ConditionalFurnitureElement {
    protected final Predicate<PlayerContext> predicate;
    protected final boolean hasCondition;

    public AbstractFurnitureElement(Predicate<PlayerContext> predicate, boolean hasCondition) {
        this.predicate = predicate;
        this.hasCondition = hasCondition;
    }

    @Override
    public @NotNull Predicate<PlayerContext> viewCondition() {
        return this.predicate;
    }

    @Override
    public boolean hasCondition() {
        return this.hasCondition;
    }
}
