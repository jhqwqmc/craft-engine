package net.momirealms.craftengine.core.item.processor;

import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.item.ItemBuildContext;
import net.momirealms.craftengine.core.item.ItemProcessorFactory;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.compatibility.ItemSource;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.LazyReference;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class ExternalSourceProcessor<I> implements ItemProcessor<I> {
    public static final ItemProcessorFactory<?> FACTORY = new Factory<>();
    private static final ThreadLocal<Set<Dependency>> BUILD_STACK = ThreadLocal.withInitial(LinkedHashSet::new);
    private final String id;
    private final LazyReference<ItemSource<I>> provider;

    public ExternalSourceProcessor(String id, LazyReference<ItemSource<I>> provider) {
        this.id = id;
        this.provider = provider;
    }

    public String id() {
        return id;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Item<I> apply(Item<I> item, ItemBuildContext context) {
        ItemSource<I> provider = this.provider.get();
        if (provider == null) return item;

        Dependency dependency = new Dependency(provider.plugin(), this.id);
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
            I another = provider.build(this.id, context);
            if (another == null) {
                CraftEngine.instance().logger().warn("'" + this.id + "' could not be found in " + provider.plugin());
                return item;
            }
            Item<I> anotherWrapped = (Item<I>) CraftEngine.instance().itemManager().wrap(another);
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

    private static class Factory<I> implements ItemProcessorFactory<I> {

        @SuppressWarnings("unchecked")
        @Override
        public ItemProcessor<I> create(Object arg) {
            Map<String, Object> data = ResourceConfigUtils.getAsMap(arg, "external");
            String plugin = ResourceConfigUtils.requireNonEmptyStringOrThrow(ResourceConfigUtils.get(data, "plugin", "source"), "warning.config.item.data.external.missing_source");
            String id = ResourceConfigUtils.requireNonEmptyStringOrThrow(data.get("id"), "warning.config.item.data.external.missing_id");
            return new ExternalSourceProcessor<>(id, LazyReference.lazyReference(() -> {
                ItemSource<?> itemSource = CraftEngine.instance().compatibilityManager().getItemSource(plugin.toLowerCase(Locale.ENGLISH));
                if (itemSource == null) {
                    CraftEngine.instance().logger().warn("Item source '" + plugin + "' not found for item '" + id + "'");
                }
                return (ItemSource<I>) itemSource;
            }));
        }
    }

    private record Dependency(String source, String id) {

        public @NotNull String asString() {
            return this.source + "[id=" + this.id + "]";
        }
    }
}
