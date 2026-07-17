package net.momirealms.craftengine.core.util;

import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

public final class Map2<K, V> extends AbstractMap<K, V> {
    private final K k0;
    private final V v0;
    private final K k1;
    private final V v1;

    public Map2(K k0, V v0, K k1, V v1) {
        this.k0 = Objects.requireNonNull(k0);
        this.v0 = Objects.requireNonNull(v0);
        if (k0.equals(Objects.requireNonNull(k1))) {
            throw new IllegalArgumentException("duplicate key: " + k0);
        }
        this.k1 = k1;
        this.v1 = Objects.requireNonNull(v1);
    }

    public static <K, V> Map2<K, V> of(K k0, V v0, K k1, V v1) {
        return new Map2<>(k0, v0, k1, v1);
    }

    private static UnsupportedOperationException uoe() {
        return new UnsupportedOperationException();
    }

    @Override
    public V get(Object o) {
        if (o.equals(this.k0)) {
            return this.v0;
        } else if (o.equals(this.k1)) {
            return this.v1;
        }
        return null;
    }

    @Override
    public boolean containsKey(Object o) {
        return o.equals(this.k0) || o.equals(this.k1);
    }

    @Override
    public boolean containsValue(Object o) {
        return o.equals(this.v0) || o.equals(this.v1); // implicit nullcheck of o
    }

    @Override
    public int size() {
        return 2;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public int hashCode() {
        return (this.k0.hashCode() ^ this.v0.hashCode())
                + (this.k1.hashCode() ^ this.v1.hashCode());
    }

    @Override
    public void forEach(BiConsumer<? super K, ? super V> action) {
        action.accept(this.k0, this.v0);
        action.accept(this.k1, this.v1);
    }

    @Override
    public @NotNull Set<Map.Entry<K, V>> entrySet() {
        return new AbstractSet<>() {
            @Override
            public int size() {
                return 2;
            }

            @Override
            public @NotNull Iterator<Map.Entry<K, V>> iterator() {
                return new Iterator<>() {
                    private int idx = 2;

                    @Override
                    public boolean hasNext() {
                        return this.idx > 0;
                    }

                    @Override
                    public Map.Entry<K, V> next() {
                        if (this.idx == 2) {
                            this.idx = 1;
                            return new SimpleImmutableEntry<>(Map2.this.k0, Map2.this.v0);
                        } else if (this.idx == 1) {
                            this.idx = 0;
                            return new SimpleImmutableEntry<>(Map2.this.k1, Map2.this.v1);
                        }
                        throw new NoSuchElementException();
                    }
                };
            }
        };
    }

    @Override
    public void clear() {
        throw uoe();
    }

    @Override
    public V compute(K key, BiFunction<? super K, ? super V, ? extends V> rf) {
        throw uoe();
    }

    @Override
    public V computeIfAbsent(K key, Function<? super K, ? extends V> mf) {
        throw uoe();
    }

    @Override
    public V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> rf) {
        throw uoe();
    }

    @Override
    public V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> rf) {
        throw uoe();
    }

    @Override
    public V put(K key, V value) {
        throw uoe();
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        throw uoe();
    }

    @Override
    public V putIfAbsent(K key, V value) {
        throw uoe();
    }

    @Override
    public V remove(Object key) {
        throw uoe();
    }

    @Override
    public boolean remove(Object key, Object value) {
        throw uoe();
    }

    @Override
    public V replace(K key, V value) {
        throw uoe();
    }

    @Override
    public boolean replace(K key, V oldValue, V newValue) {
        throw uoe();
    }

    @Override
    public void replaceAll(BiFunction<? super K, ? super V, ? extends V> f) {
        throw uoe();
    }
}
