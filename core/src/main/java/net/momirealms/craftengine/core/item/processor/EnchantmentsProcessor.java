package net.momirealms.craftengine.core.item.processor;

import net.momirealms.craftengine.core.item.*;
import net.momirealms.craftengine.core.item.data.Enchantment;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class EnchantmentsProcessor implements SimpleNetworkItemProcessor {
    public static final ItemProcessorFactory<EnchantmentsProcessor> FACTORY = new Factory();
    private static final Object[] STORED_ENCHANTMENTS = new Object[] {"StoredEnchantments"};
    private static final Object[] ENCHANTMENTS = new Object[] {"Enchantments"};
    private final List<Enchantment> enchantments;
    private final boolean merge;

    public EnchantmentsProcessor(List<Enchantment> enchantments, boolean merge) {
        this.enchantments = enchantments;
        this.merge = merge;
    }

    public boolean merge() {
        return merge;
    }

    public List<Enchantment> enchantments() {
        return enchantments;
    }

    @Override
    public <I> Item<I> apply(Item<I> item, ItemBuildContext context) {
        if (item.vanillaId().equals(ItemKeys.ENCHANTED_BOOK)) {
            if (this.merge) {
                Optional<List<Enchantment>> previousEnchantments = item.storedEnchantments();
                if (previousEnchantments.isPresent()) {
                    return item.setStoredEnchantments(Stream.concat(previousEnchantments.get().stream(), this.enchantments.stream())
                            .collect(Collectors.toMap(
                                    Enchantment::id,
                                    enchantment -> enchantment,
                                    (existing, replacement) ->
                                            existing.level() > replacement.level() ? existing : replacement
                            ))
                            .values()
                            .stream()
                            .toList());
                }
            }
            return item.setStoredEnchantments(this.enchantments);
        } else {
            if (this.merge) {
                Optional<List<Enchantment>> previousEnchantments = item.enchantments();
                if (previousEnchantments.isPresent()) {
                    return item.setEnchantments(Stream.concat(previousEnchantments.get().stream(), this.enchantments.stream())
                            .collect(Collectors.toMap(
                                    Enchantment::id,
                                    enchantment -> enchantment,
                                    (existing, replacement) ->
                                            existing.level() > replacement.level() ? existing : replacement
                            ))
                            .values()
                            .stream()
                            .toList());
                }
            }
            return item.setEnchantments(this.enchantments);
        }
    }

    @Override
    public <I> Key componentType(Item<I> item, ItemBuildContext context) {
        return item.vanillaId().equals(ItemKeys.ENCHANTED_BOOK) ? DataComponentKeys.STORED_ENCHANTMENTS : DataComponentKeys.ENCHANTMENTS;
    }

    @Override
    public <I> Object[] nbtPath(Item<I> item, ItemBuildContext context) {
        return item.vanillaId().equals(ItemKeys.ENCHANTED_BOOK) ? STORED_ENCHANTMENTS : ENCHANTMENTS;
    }

    @Override
    public <I> String nbtPathString(Item<I> item, ItemBuildContext context) {
        return item.vanillaId().equals(ItemKeys.ENCHANTED_BOOK) ? "StoredEnchantments" : "Enchantments";
    }

    private static class Factory implements ItemProcessorFactory<EnchantmentsProcessor> {

        @Override
        public EnchantmentsProcessor create(Object arg) {
            Map<String, Object> enchantData = ResourceConfigUtils.getAsMap(arg, "enchantments");
            List<Enchantment> enchantments = new ArrayList<>();
            boolean merge = false;
            if (enchantData.containsKey("enchantments")) {
                merge = ResourceConfigUtils.getAsBoolean(enchantData.get("merge"), "merge");
                enchantData = ResourceConfigUtils.getAsMap(enchantData.get("enchantments"), "enchantments");
            }
            for (Map.Entry<String, Object> e : enchantData.entrySet()) {
                if (e.getValue() instanceof Number number) {
                    enchantments.add(new Enchantment(Key.of(e.getKey()), number.intValue()));
                }
            }
            return new EnchantmentsProcessor(enchantments, merge);
        }
    }
}
