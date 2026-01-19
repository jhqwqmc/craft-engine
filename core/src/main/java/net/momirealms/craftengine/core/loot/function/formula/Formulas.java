package net.momirealms.craftengine.core.loot.function.formula;

import net.momirealms.craftengine.core.registry.BuiltInRegistries;
import net.momirealms.craftengine.core.registry.Registries;
import net.momirealms.craftengine.core.registry.WritableRegistry;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.ResourceKey;

import java.util.Map;

public final class Formulas {
    public static final FormulaType<OreDrops> ORE_DROPS = register(Key.ce("ore_drops"), OreDrops.FACTORY);
    public static final FormulaType<CropDrops> CROP_DROPS = register(Key.ce("binomial_with_bonus_count"), CropDrops.FACTORY);

    private Formulas() {}

    public static <T extends Formula> FormulaType<T> register(Key key, FormulaFactory<T> factory) {
        FormulaType<T> type = new FormulaType<>(key, factory);
        ((WritableRegistry<FormulaType<? extends Formula>>) BuiltInRegistries.FORMULA_TYPE)
                .register(ResourceKey.create(Registries.FORMULA_TYPE.location(), key), type);
        return type;
    }

    public static Formula fromMap(Map<String, Object> map) {
        String type = (String) map.get("type");
        if (type == null) {
            throw new NullPointerException("number type cannot be null");
        }
        Key key = Key.withDefaultNamespace(type, Key.DEFAULT_NAMESPACE);
        FormulaType<? extends Formula> formulaType = BuiltInRegistries.FORMULA_TYPE.getValue(key);
        if (formulaType == null) {
            throw new IllegalArgumentException("Unknown formula type: " + type);
        }
        return formulaType.factory().create(map);
    }
}