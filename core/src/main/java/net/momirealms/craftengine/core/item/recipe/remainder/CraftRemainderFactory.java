package net.momirealms.craftengine.core.item.recipe.remainder;

import java.util.Map;

public interface CraftRemainderFactory<T extends CraftRemainder> {

    T create(Map<String, Object> args);
}
