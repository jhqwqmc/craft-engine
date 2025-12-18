package net.momirealms.craftengine.core.item.modifier;

import cn.gtemc.itembridge.api.Provider;
import cn.gtemc.itembridge.api.context.BuildContext;
import cn.gtemc.itembridge.api.context.ContextKey;
import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.item.ItemBuildContext;
import net.momirealms.craftengine.core.item.ItemDataModifierFactory;
import net.momirealms.craftengine.core.item.ItemManager;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.context.ContextHolder;
import net.momirealms.craftengine.core.plugin.locale.LocalizedResourceConfigException;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;
import net.momirealms.craftengine.core.util.VersionHelper;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Supplier;

public class ExternalModifier<I> implements ItemDataModifier<I> {
    public static final Factory<?> FACTORY = new Factory<>();
    private static final ThreadLocal<Set<Dependency>> BUILD_STACK = ThreadLocal.withInitial(LinkedHashSet::new);
    private final String id;
    private final Provider<I, Object> provider;

    public ExternalModifier(String id, Provider<I, Object> provider) {
        this.id = id;
        this.provider = provider;
    }

    public String id() {
        return id;
    }

    public Provider<I, Object> source() {
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
            Player player = context.player();
            I another = this.provider.buildOrNull(this.id, player == null ? null : player.platformPlayer(), adapt(context));
            if (another == null) {
                CraftEngine.instance().logger().warn("'" + this.id + "' could not be found in " + provider.plugin());
                return item;
            }
            Item<I> anotherWrapped = itemManager.wrap(another);
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

    private static BuildContext adapt(ItemBuildContext context) {
        if (!VersionHelper.IS_RUNNING_IN_DEV) return BuildContext.empty(); // 先不在生产环境启用
        ContextHolder contexts = context.contexts();
        if (contexts.isEmpty()) {
            return BuildContext.empty();
        }
        BuildContext.Builder builder = BuildContext.builder();
        for (Map.Entry<net.momirealms.craftengine.core.plugin.context.ContextKey<?>, Supplier<Object>> entry : contexts.params().entrySet()) {
            Object value = entry.getValue().get();
            if (value == null) {
                continue;
            }
            Class<?> type = value.getClass(); // fixme 这个获取办法并不正确，net.momirealms.craftengine.core.plugin.context.ContextKey 应该在创建的时候记录是什么类型
            @SuppressWarnings("unchecked")
            ContextKey<Object> contextKey = (ContextKey<Object>) ContextKey.of(type, entry.getKey().node());
            with(builder, contextKey, entry.getValue());
        }
        return builder.build();
    }

    private static <T> void with(BuildContext.Builder builder, ContextKey<T> key, Supplier<T> value) {
        builder.with(key, value);
    }

    public static class Factory<I> implements ItemDataModifierFactory<I> {

        @Override
        public ItemDataModifier<I> create(Object arg) {
            Map<String, Object> data = ResourceConfigUtils.getAsMap(arg, "external");
            String plugin = ResourceConfigUtils.requireNonEmptyStringOrThrow(ResourceConfigUtils.get(data, "plugin", "source"), "warning.config.item.data.external.missing_source");
            String id = ResourceConfigUtils.requireNonEmptyStringOrThrow(data.get("id"), "warning.config.item.data.external.missing_id");
            ItemManager<I> itemManager = CraftEngine.instance().itemManager();
            Provider<I, Object> provider = itemManager.itemBridgeProvider().provider(plugin.toLowerCase(Locale.ENGLISH)).orElseThrow(
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
