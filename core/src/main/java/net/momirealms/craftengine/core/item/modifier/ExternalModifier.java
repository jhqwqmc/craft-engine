package net.momirealms.craftengine.core.item.modifier;

import cn.gtemc.itembridge.api.Provider;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.item.ItemBuildContext;
import net.momirealms.craftengine.core.item.ItemDataModifierFactory;
import net.momirealms.craftengine.core.item.ItemManager;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.locale.LocalizedResourceConfigException;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class ExternalModifier<I> implements ItemDataModifier<I> {
    public static final Factory<?> FACTORY = new Factory<>();
    private static final ThreadLocal<Set<Dependency>> BUILD_STACK = ThreadLocal.withInitial(LinkedHashSet::new);
    private final String id;
    private final Provider<I> provider;

    public ExternalModifier(String id, Provider<I> provider) {
        this.id = id;
        this.provider = provider;
    }

    public String id() {
        return id;
    }

    public Provider<I> source() {
        return provider;
    }

    @Override
    public Key type() {
        return ItemDataModifiers.EXTERNAL;
    }

    @Override
    public Item<I> apply(Item<I> item, ItemBuildContext context) {
        Dependency dependency = new Dependency(provider.plugin(), id);
        Set<Dependency> buildStack = BUILD_STACK.get();

        if (buildStack.contains(dependency)) {
            StringJoiner dependencyChain = new StringJoiner(" -> ");
            buildStack.forEach(element -> dependencyChain.add(element.asString()));
            dependencyChain.add(dependency.asString());
            CraftEngine.instance().logger().warn(
                    "Failed to build '" + this.id + "' from plugin '" + provider.plugin() + "' due to dependency loop: " + dependencyChain
            );
            return item;
        }

        buildStack.add(dependency);
        try {
            ItemManager<I> itemManager = CraftEngine.instance().itemManager();
            Optional<I> another = itemManager.buildPlatformItem(this.provider, this.id, context);
            if (another.isEmpty()) {
                CraftEngine.instance().logger().warn("'" + this.id + "' could not be found in " + provider.plugin());
                return item;
            }
            Item<I> anotherWrapped = itemManager.wrap(another.get());
            item.merge(anotherWrapped);
            return item;
        } catch (Throwable e) {
            CraftEngine.instance().logger().warn("Failed to build item '" + this.id + "' from plugin '" + provider.plugin() + "'", e);
            return item;
        } finally {
            buildStack.remove(dependency);
            BUILD_STACK.remove();
        }
    }

    public static class Factory<I> implements ItemDataModifierFactory<I> {

        @Override
        public ItemDataModifier<I> create(Object arg) {
            Map<String, Object> data = ResourceConfigUtils.getAsMap(arg, "external");
            String plugin = ResourceConfigUtils.requireNonEmptyStringOrThrow(ResourceConfigUtils.get(data, "plugin", "source"), "warning.config.item.data.external.missing_source");
            String id = ResourceConfigUtils.requireNonEmptyStringOrThrow(data.get("id"), "warning.config.item.data.external.missing_id");
            ItemManager<I> itemManager = CraftEngine.instance().itemManager();
            Provider<I> provider = itemManager.itemBridgeProvider().provider(plugin.toLowerCase(Locale.ENGLISH)).orElseThrow(
                    () -> new LocalizedResourceConfigException("warning.config.item.data.external.invalid_source", plugin)
            );
            return new ExternalModifier<>(id, provider);
        }
    }

    private record Dependency(String source, String id) {

        public @NotNull String asString() {
            return source + "[id=" + id + "]";
        }
    }
}
