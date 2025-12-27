package net.momirealms.craftengine.core.loot.function.formula;

import net.momirealms.craftengine.core.registry.BuiltInRegistries;
import net.momirealms.craftengine.core.registry.Registries;
import net.momirealms.craftengine.core.registry.WritableRegistry;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.ResourceKey;

import java.util.Map;

public final class Formulas {
    public static final FormulaType ORE_DROPS = register(Key.ce("ore_drops"), OreDrops.FACTORY);
    public static final FormulaType CROP_DROPS = register(Key.ce("binomial_with_bonus_count"), CropDrops.FACTORY);

    private Formulas() {}

    public static FormulaType register(Key key, FormulaFactory factory) {
        FormulaType type = new FormulaType(key, factory);
        ((WritableRegistry<FormulaType>) BuiltInRegistries.FORMULA_TYPE)
                .register(ResourceKey.create(Registries.FORMULA_TYPE.location(), key), type);
        return type;
    }

    public static Formula fromMap(Map<String, Object> map) {
        String type = (String) map.get("type");
        if (type == null) {
            throw new NullPointerException("number type cannot be null");
        }
        Key key = Key.withDefaultNamespace(type, Key.DEFAULT_NAMESPACE);
        FormulaType formulaType = BuiltInRegistries.FORMULA_TYPE.getValue(key);
        if (formulaType == null) {
            throw new IllegalArgumentException("Unknown formula type: " + type);
        }
        return formulaType.factory().create(map);
    }
}