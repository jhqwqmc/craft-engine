package net.momirealms.craftengine.proxy.minecraft.world.item.enchantment;

import net.momirealms.craftengine.proxy.minecraft.core.HolderProxy;
import net.momirealms.sparrow.reflection.proxy.ASMProxyFactory;
import net.momirealms.sparrow.reflection.proxy.annotation.MethodInvoker;
import net.momirealms.sparrow.reflection.proxy.annotation.ReflectionProxy;
import net.momirealms.sparrow.reflection.proxy.annotation.Type;

@ReflectionProxy(name = "net.minecraft.world.item.enchantment.Enchantment")
public interface EnchantmentProxy {
    EnchantmentProxy INSTANCE = ASMProxyFactory.create(EnchantmentProxy.class);

    @MethodInvoker(name = "getFullname", activeIf = "max_version=1.20.6")
    Object getFullname$legacy(Object target, int level);

    @MethodInvoker(name = "getFullname", isStatic = true, activeIf = "min_version=1.21")
    Object getFullname(@Type(clazz = HolderProxy.class) Object enchantment, int level);
}
