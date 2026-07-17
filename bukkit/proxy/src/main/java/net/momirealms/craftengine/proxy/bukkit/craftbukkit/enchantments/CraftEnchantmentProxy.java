package net.momirealms.craftengine.proxy.bukkit.craftbukkit.enchantments;

import net.momirealms.sparrow.reflection.proxy.ASMProxyFactory;
import net.momirealms.sparrow.reflection.proxy.annotation.FieldGetter;
import net.momirealms.sparrow.reflection.proxy.annotation.ReflectionProxy;

@ReflectionProxy(name = "org.bukkit.craftbukkit.enchantments.CraftEnchantment")
public interface CraftEnchantmentProxy {
    CraftEnchantmentProxy INSTANCE = ASMProxyFactory.create(CraftEnchantmentProxy.class);

    @FieldGetter(name = {"handle", "target"})
    Object getHandle(Object target);
}
