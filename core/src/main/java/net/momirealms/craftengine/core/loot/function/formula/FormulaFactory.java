package net.momirealms.craftengine.core.loot.function.formula;

import java.util.Map;

public interface FormulaFactory {

    Formula create(Map<String, Object> arguments);
}