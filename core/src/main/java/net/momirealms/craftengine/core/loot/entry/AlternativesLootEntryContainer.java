package net.momirealms.craftengine.core.loot.entry;

import net.momirealms.craftengine.core.loot.LootContext;
import net.momirealms.craftengine.core.plugin.context.CommonConditions;
import net.momirealms.craftengine.core.plugin.context.Condition;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;

import java.util.List;
import java.util.Map;

public final class AlternativesLootEntryContainer<T> extends AbstractCompositeLootEntryContainer<T> {
    public static final Key ID = Key.from("craftengine:alternatives");
    public static final LootEntryContainerFactory<?> FACTORY = new Factory<>();

    private AlternativesLootEntryContainer(List<Condition<LootContext>> conditions, List<LootEntryContainer<T>> children) {
        super(conditions, children);
    }

    @Override
    protected LootEntryContainer<T> compose(List<? extends LootEntryContainer<T>> children) {
        return switch (children.size()) {
            case 0 -> LootEntryContainer.alwaysFalse();
            case 1 -> children.get(0);
            case 2 -> children.get(0).or(children.get(1));
            default -> (context, choiceConsumer) -> {
                for (LootEntryContainer<T> child : children) {
                    if (child.expand(context, choiceConsumer)) {
                        return true;
                    }
                }
                return false;
            };
        };
    }

    private static class Factory<A> implements LootEntryContainerFactory<A> {

        @Override
        public LootEntryContainer<A> create(Map<String, Object> arguments) {
            List<LootEntryContainer<A>> containers = ResourceConfigUtils.parseConfigAsList(ResourceConfigUtils.get(arguments, "children", "terms", "branches"), LootEntryContainers::fromMap);
            List<Condition<LootContext>> conditions = ResourceConfigUtils.parseConfigAsList(arguments.get("conditions"), CommonConditions::fromMap);
            return new AlternativesLootEntryContainer<>(conditions, containers);
        }
    }
}
