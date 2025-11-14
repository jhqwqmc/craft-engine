package net.momirealms.craftengine.bukkit.plugin.injector;

import net.momirealms.craftengine.bukkit.nms.FastNMS;
import net.momirealms.craftengine.bukkit.plugin.reflection.minecraft.CoreReflections;
import net.momirealms.craftengine.bukkit.plugin.reflection.minecraft.MBuiltInRegistries;
import net.momirealms.craftengine.bukkit.util.KeyUtils;
import net.momirealms.craftengine.core.util.Key;

import java.util.Set;

public final class BlockStateProviderInjector {

    public static void init() throws ReflectiveOperationException {
        Object registry = MBuiltInRegistries.BLOCKSTATE_PROVIDER_TYPE;
        CoreReflections.field$MappedRegistry$frozen.set(registry, false);

        Object rl1 = KeyUtils.toResourceLocation(Key.of("craftengine:simple_state_provider"));
        Object type1 = FastNMS.INSTANCE.getCraftEngineCustomSimpleStateProviderType();
        Object holder1 = CoreReflections.method$Registry$registerForHolder.invoke(null, registry, rl1, type1);
        CoreReflections.method$Holder$Reference$bindValue.invoke(holder1, type1);
        CoreReflections.field$Holder$Reference$tags.set(holder1, Set.of());

        CoreReflections.field$MappedRegistry$frozen.set(registry, true);
    }
}
