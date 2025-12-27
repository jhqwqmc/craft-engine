package net.momirealms.craftengine.core.loot;

import java.util.ArrayList;
import java.util.List;

public final class VanillaLoot {
    private final Type type;
    private final List<LootTable<?>> lootTables;
    private boolean override;

    public VanillaLoot(Type type) {
        this.type = type;
        this.override = false;
        this.lootTables = new ArrayList<>();
    }

    public void addLootTable(LootTable<?> table) {
        this.lootTables.add(table);
    }

    public Type type() {
        return type;
    }

    public List<LootTable<?>> lootTables() {
        return lootTables;
    }

    public void override(boolean override) {
        this.override = override;
    }

    public boolean override() {
        return override;
    }

    public enum Type {
        BLOCK,
        ENTITY
    }
}
