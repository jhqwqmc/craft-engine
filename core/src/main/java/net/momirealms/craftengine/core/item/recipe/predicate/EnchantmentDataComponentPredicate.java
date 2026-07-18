package net.momirealms.craftengine.core.item.recipe.predicate;

import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.item.ItemKeys;
import net.momirealms.craftengine.core.item.component.value.Enchantment;
import net.momirealms.craftengine.core.plugin.config.ConfigConstants;
import net.momirealms.craftengine.core.plugin.config.ConfigSection;
import net.momirealms.craftengine.core.util.Key;

import java.util.*;

public final class EnchantmentDataComponentPredicate implements DataComponentPredicate {
    public static final DataComponentPredicateFactory<EnchantmentDataComponentPredicate> FACTORY = new Factory();
    private final Map<Key, Integer> enchantments;

    public EnchantmentDataComponentPredicate(Map<Key, Integer> enchantments) {
        this.enchantments = Map.copyOf(enchantments);
    }

    @Override
    public void apply(Item item) {
        if (this.enchantments.isEmpty()) {
            return;
        }
        boolean isBook = item.vanillaId().equals(ItemKeys.ENCHANTED_BOOK);
        Optional<List<Enchantment>> previous = isBook ? item.storedEnchantments() : item.enchantments();
        Map<Key, Enchantment> merged = new HashMap<>();
        if (previous.isPresent()) {
            for (Enchantment enchantment : previous.get()) {
                merged.put(enchantment.id(), enchantment);
            }
        }
        for (Map.Entry<Key, Integer> entry : this.enchantments.entrySet()) {
            Key id = entry.getKey();
            int required = entry.getValue();
            Enchantment existing = merged.get(id);
            if (existing == null || existing.level() < required) {
                merged.put(id, new Enchantment(id, required));
            }
        }
        List<Enchantment> result = new ArrayList<>(merged.values());
        if (isBook) {
            item.setStoredEnchantments(result);
        } else {
            item.setEnchantments(result);
        }
    }

    @Override
    public boolean test(Item item) {
        for (Map.Entry<Key, Integer> entry : this.enchantments.entrySet()) {
            int level = item.getEnchantment(entry.getKey()).map(Enchantment::level).orElse(0);
            if (level < entry.getValue()) {
                return false;
            }
        }
        return true;
    }

    private static class Factory implements DataComponentPredicateFactory<EnchantmentDataComponentPredicate> {

        @Override
        public EnchantmentDataComponentPredicate create(ConfigSection section) {
            ConfigSection enchantSection = section.containsKey("enchantments")
                    ? section.getNonNullSection("enchantments")
                    : section;
            Map<Key, Integer> enchantments = new LinkedHashMap<>();
            for (String key : enchantSection.keySet()) {
                if (key.equals("type")) {
                    continue;
                }
                int level = enchantSection.getNonNullValue(key, ConfigConstants.ARGUMENT_INT, v -> v.getAsInt(1, 255));
                enchantments.put(Key.of(key), level);
            }
            return new EnchantmentDataComponentPredicate(enchantments);
        }
    }
}
