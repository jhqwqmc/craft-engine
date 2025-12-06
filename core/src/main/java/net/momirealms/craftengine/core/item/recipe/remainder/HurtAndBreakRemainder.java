package net.momirealms.craftengine.core.item.recipe.remainder;

import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;

import java.util.Map;

public class HurtAndBreakRemainder implements CraftRemainder {
    public static final Factory FACTORY = new Factory();
    private final int amount;

    public HurtAndBreakRemainder(int amount) {
        this.amount = amount;
    }

    @Override
    public <T> Item<T> remainder(Key recipeId, Item<T> item) {
        int damage = item.damage().orElse(0);
        int maxDamage = item.maxDamage();
        damage += amount;
        if (damage >= maxDamage) {
            return item.copyWithCount(0);
        } else {
            item = item.copyWithCount(1);
            item.damage(damage);
            return item;
        }
    }

    public static class Factory implements CraftRemainderFactory<HurtAndBreakRemainder> {

        @Override
        public HurtAndBreakRemainder create(Map<String, Object> args) {
            int damage = ResourceConfigUtils.getAsInt(args.getOrDefault("damage", 1), "damage");
            return new HurtAndBreakRemainder(damage);
        }
    }
}
