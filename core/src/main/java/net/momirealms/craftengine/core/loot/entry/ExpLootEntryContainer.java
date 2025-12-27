package net.momirealms.craftengine.core.loot.entry;

import net.momirealms.craftengine.core.loot.LootContext;
import net.momirealms.craftengine.core.plugin.context.CommonConditions;
import net.momirealms.craftengine.core.plugin.context.Condition;
import net.momirealms.craftengine.core.plugin.context.number.NumberProvider;
import net.momirealms.craftengine.core.plugin.context.number.NumberProviders;
import net.momirealms.craftengine.core.plugin.context.parameter.DirectContextParameters;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public final class ExpLootEntryContainer<T> extends AbstractLootEntryContainer<T> {
    public static final Key ID = Key.from("craftengine:exp");
    public static final Factory<?> FACTORY = new Factory<>();
    private final NumberProvider value;

    private ExpLootEntryContainer(NumberProvider value, List<Condition<LootContext>> conditions) {
        super(conditions);
        this.value = value;
    }

    @Override
    public boolean expand(LootContext context, Consumer<LootEntry<T>> choiceConsumer) {
        if (super.test(context)) {
            context.getOptionalParameter(DirectContextParameters.POSITION)
                    .ifPresent(it -> it.world().dropExp(it, value.getInt(context)));
            return true;
        } else {
            return false;
        }
    }

    public static class Factory<A> implements LootEntryContainerFactory<A> {

        @Override
        public LootEntryContainer<A> create(Map<String, Object> arguments) {
            Object value = ResourceConfigUtils.requireNonNullOrThrow(arguments.get("count"), "warning.config.loot_table.entry.exp.missing_count");
            List<Condition<LootContext>> conditions = ResourceConfigUtils.parseConfigAsList(arguments.get("conditions"), CommonConditions::fromMap);
            return new ExpLootEntryContainer<>(NumberProviders.fromObject(value), conditions);
        }
    }
}
