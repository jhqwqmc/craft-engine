package net.momirealms.craftengine.bukkit.util;

import net.momirealms.craftengine.bukkit.plugin.reflection.minecraft.CoreReflections;
import net.momirealms.craftengine.core.item.data.Enchantment;
import net.momirealms.craftengine.core.util.Key;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class EnchantmentUtils {

    private EnchantmentUtils() {}

    @SuppressWarnings("unchecked")
    public static List<Enchantment> toList(Object itemEnchantments) throws ReflectiveOperationException {
        if (itemEnchantments == null) return List.of();
        List<Enchantment> enchantmentList = new ArrayList<>();
        Map<Object, Integer> enchantments = (Map<Object, Integer>) CoreReflections.field$ItemEnchantments$enchantments.get(itemEnchantments);
        for (Map.Entry<Object, Integer> entry : enchantments.entrySet()) {
            Object holder = entry.getKey();
            String name = (String) CoreReflections.method$Holder$getRegisteredName.invoke(holder);
            int level = entry.getValue();
            enchantmentList.add(new Enchantment(Key.of(name), level));
        }
        return enchantmentList;
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Integer> toMap(Object itemEnchantments) throws ReflectiveOperationException {
        if (itemEnchantments == null) return Map.of();
        Map<String, Integer> map = new HashMap<>();
        Map<Object, Integer> enchantments = (Map<Object, Integer>) CoreReflections.field$ItemEnchantments$enchantments.get(itemEnchantments);
        for (Map.Entry<Object, Integer> entry : enchantments.entrySet()) {
            Object holder = entry.getKey();
            String name = (String) CoreReflections.method$Holder$getRegisteredName.invoke(holder);
            int level = entry.getValue();
            map.put(name, level);
        }
        return map;
    }
}
