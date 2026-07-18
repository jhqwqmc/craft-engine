package net.momirealms.craftengine.core.attribute.formula;

import net.momirealms.craftengine.core.attribute.DamageFormula;
import net.momirealms.craftengine.core.util.Key;

import java.util.Map;

public final class VictimToFormula {
    private final DamageFormula defaultFormula;
    private final Map<Key, DamageFormula> formulas;

    public VictimToFormula(DamageFormula defaultFormula, Map<Key, DamageFormula> formulas) {
        this.defaultFormula = defaultFormula;
        this.formulas = formulas;
    }

    public DamageFormula getFormula(Key key) {
        return this.formulas.getOrDefault(key, this.defaultFormula);
    }
}
