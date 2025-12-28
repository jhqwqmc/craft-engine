package net.momirealms.craftengine.core.loot.function.formula;

import java.util.Map;

public interface FormulaFactory<T extends Formula> {

    T create(Map<String, Object> arguments);
}