package net.momirealms.craftengine.core.loot.function;

import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.item.data.Enchantment;
import net.momirealms.craftengine.core.loot.LootContext;
import net.momirealms.craftengine.core.loot.function.formula.Formula;
import net.momirealms.craftengine.core.loot.function.formula.Formulas;
import net.momirealms.craftengine.core.plugin.context.CommonConditions;
import net.momirealms.craftengine.core.plugin.context.Condition;
import net.momirealms.craftengine.core.plugin.context.parameter.DirectContextParameters;
import net.momirealms.craftengine.core.plugin.locale.LocalizedResourceConfigException;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.MiscUtils;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class ApplyBonusCountFunction<T> extends AbstractLootConditionalFunction<T> {
    public static final LootFunctionFactory<?> FACTORY = new Factory<>();
    private final Key enchantment;
    private final Formula formula;

    public ApplyBonusCountFunction(List<Condition<LootContext>> predicates, Key enchantment, Formula formula) {
        super(predicates);
        this.enchantment = enchantment;
        this.formula = formula;
    }

    @Override
    protected Item<T> applyInternal(Item<T> item, LootContext context) {
        Optional<Item<?>> itemInHand = context.getOptionalParameter(DirectContextParameters.ITEM_IN_HAND);
        int level = itemInHand.map(value -> value.getEnchantment(this.enchantment).map(Enchantment::level).orElse(0)).orElse(0);
        int newCount = this.formula.apply(item.count(), level);
        item.count(newCount);
        return item;
    }

    private static class Factory<T> implements LootFunctionFactory<T> {

        @Override
        public LootFunction<T> create(Map<String, Object> arguments) {
            String enchantment = ResourceConfigUtils.requireNonEmptyStringOrThrow(arguments.get("enchantment"), "warning.config.loot_table.function.apply_bonus.missing_enchantment");
            Map<String, Object> formulaMap = MiscUtils.castToMap(arguments.get("formula"), true);
            if (formulaMap == null) {
                throw new LocalizedResourceConfigException("warning.config.loot_table.function.apply_bonus.missing_formula");
            }
            List<Condition<LootContext>> conditions = ResourceConfigUtils.parseConfigAsList(arguments.get("conditions"), CommonConditions::fromMap);
            return new ApplyBonusCountFunction<>(conditions, Key.from(enchantment), Formulas.fromMap(formulaMap));
        }
    }
}
