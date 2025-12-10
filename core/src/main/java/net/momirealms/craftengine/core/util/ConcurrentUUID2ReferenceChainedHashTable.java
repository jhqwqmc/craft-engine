/**
 * This implementation references the ConcurrentUtil implementation by Tuinity,
 * available at: https://github.com/Tuinity/ConcurrentUtil
 * <p>
 * This work is licensed under the GNU General Public License v3.0 (GPLv3)
 */
package net.momirealms.craftengine.core.util;

import ca.spottedleaf.concurrentutil.util.*;

import java.lang.invoke.VarHandle;
import java.util.*;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Concurrent hashtable implementation supporting mapping UUID values onto non-null Object
 * values with support for multiple writer and multiple reader threads.
 */
@SuppressWarnings("unchecked")
public class ConcurrentUUID2ReferenceChainedHashTable<V> implements Iterable<ConcurrentUUID2ReferenceChainedHashTable.TableEntry<V>> {
    protected static final int DEFAULT_CAPACITY = 16;
    protected static final float DEFAULT_LOAD_FACTOR = 0.75f;
    protected static final int MAXIMUM_CAPACITY = Integer.MIN_VALUE >>> 1;

    protected final LongAdder size = new LongAdder();
    protected final float loadFactor;

    protected volatile TableEntry<V>[] table;

    protected static final int THRESHOLD_NO_RESIZE = -1;
    protected static final int THRESHOLD_RESIZING  = -2;
    protected volatile int threshold;
    protected static final VarHandle THRESHOLD_HANDLE = ConcurrentUtil.getVarHandle(ConcurrentUUID2ReferenceChainedHashTable.class, "threshold", int.class);

    protected final int getThresholdAcquire() {
        return (int)THRESHOLD_HANDLE.getAcquire(this);
    }

    protected final int getThresholdVolatile() {
        return (int)THRESHOLD_HANDLE.getVolatile(this);
    }

    protected final void setThresholdPlain(final int threshold) {
        THRESHOLD_HANDLE.set(this, threshold);
    }

    protected final void setThresholdRelease(final int threshold) {
        THRESHOLD_HANDLE.setRelease(this, threshold);
    }

    protected final void setThresholdVolatile(final int threshold) {
        THRESHOLD_HANDLE.setVolatile(this, threshold);
    }

    protected final int compareExchangeThresholdVolatile(final int expect, final int update) {
        return (int)THRESHOLD_HANDLE.compareAndExchange(this, expect, update);
    }

    protected Values<V> values;
    protected EntrySet<V> entrySet;

    public ConcurrentUUID2ReferenceChainedHashTable() {
        this(DEFAULT_CAPACITY, DEFAULT_LOAD_FACTOR);
    }

    protected static int getTargetThreshold(final int capacity, final float loadFactor) {
        final double ret = (double)capacity * (double)loadFactor;
        if (Double.isInfinite(ret) || ret >= ((double)Integer.MAX_VALUE)) {
            return THRESHOLD_NO_RESIZE;
        }

        return (int)Math.ceil(ret);
    }

    protected static int getCapacityFor(final int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Invalid capacity: " + capacity);
        }
        if (capacity >= MAXIMUM_CAPACITY) {
            return MAXIMUM_CAPACITY;
        }
        return IntegerUtil.roundCeilLog2(capacity);
    }

    protected ConcurrentUUID2ReferenceChainedHashTable(final int capacity, final float loadFactor) {
        final int tableSize = getCapacityFor(capacity);

        if (loadFactor <= 0.0 || !Float.isFinite(loadFactor)) {
            throw new IllegalArgumentException("Invalid load factor: " + loadFactor);
        }

        if (tableSize == MAXIMUM_CAPACITY) {
            this.setThresholdPlain(THRESHOLD_NO_RESIZE);
        } else {
            this.setThresholdPlain(getTargetThreshold(tableSize, loadFactor));
        }

        this.loadFactor = loadFactor;
        // noinspection unchecked
        this.table = (TableEntry<V>[])new TableEntry[tableSize];
    }

    public static <V> ConcurrentUUID2ReferenceChainedHashTable<V> createWithCapacity(final int capacity) {
        return createWithCapacity(capacity, DEFAULT_LOAD_FACTOR);
    }

    public static <V> ConcurrentUUID2ReferenceChainedHashTable<V> createWithCapacity(final int capacity, final float loadFactor) {
        return new ConcurrentUUID2ReferenceChainedHashTable<>(capacity, loadFactor);
    }

    public static <V> ConcurrentUUID2ReferenceChainedHashTable<V> createWithExpected(final int expected) {
        return createWithExpected(expected, DEFAULT_LOAD_FACTOR);
    }

    public static <V> ConcurrentUUID2ReferenceChainedHashTable<V> createWithExpected(final int expected, final float loadFactor) {
        final int capacity = (int)Math.ceil((double)expected / (double)loadFactor);
        return createWithCapacity(capacity, loadFactor);
    }

    protected static int getHash(final UUID key) {
        return (int)HashUtil.mix(key.getMostSignificantBits() ^ key.getLeastSignificantBits());
    }

    public final float getLoadFactor() {
        return this.loadFactor;
    }

    protected static <V> TableEntry<V> getAtIndexVolatile(final TableEntry<V>[] table, final int index) {
        //noinspection unchecked
        return (TableEntry<V>)TableEntry.TABLE_ENTRY_ARRAY_HANDLE.getVolatile(table, index);
    }

    protected static <V> void setAtIndexRelease(final TableEntry<V>[] table, final int index, final TableEntry<V> value) {
        TableEntry.TABLE_ENTRY_ARRAY_HANDLE.setRelease(table, index, value);
    }

    protected static <V> void setAtIndexVolatile(final TableEntry<V>[] table, final int index, final TableEntry<V> value) {
        TableEntry.TABLE_ENTRY_ARRAY_HANDLE.setVolatile(table, index, value);
    }

    protected static <V> TableEntry<V> compareAndExchangeAtIndexVolatile(final TableEntry<V>[] table, final int index,
                                                                         final TableEntry<V> expect, final TableEntry<V> update) {
        //noinspection unchecked
        return (TableEntry<V>)TableEntry.TABLE_ENTRY_ARRAY_HANDLE.compareAndExchange(table, index, expect, update);
    }

    /**
     * Returns the possible node associated with the key, or {@code null} if there is no such node.
     */
    protected final TableEntry<V> getNode(final UUID key) {
        final int hash = getHash(key);

        TableEntry<V>[] table = this.table;
        for (;;) {
            TableEntry<V> node = getAtIndexVolatile(table, hash & (table.length - 1));

            if (node == null) {
                return node;
            }

            if (node.resize) {
                // noinspection unchecked
                table = (TableEntry<V>[])node.getValuePlain();
                continue;
            }

            for (; node != null; node = node.getNextVolatile()) {
                if (node.key.equals(key)) {
                    return node;
                }
            }

            return node;
        }
    }

    /**
     * Returns the currently mapped value associated with the specified key, or {@code null} if there is none.
     */
    public V get(final UUID key) {
        final TableEntry<V> node = this.getNode(key);
        return node == null ? null : node.getValueVolatile();
    }

    /**
     * Returns the currently mapped value associated with the specified key, or the specified default value if there is none.
     */
    public V getOrDefault(final UUID key, final V defaultValue) {
        final TableEntry<V> node = this.getNode(key);
        if (node == null) {
            return defaultValue;
        }

        final V ret = node.getValueVolatile();
        if (ret == null) {
            return defaultValue;
        }

        return ret;
    }

    /**
     * Returns whether the specified key is mapped to some value.
     */
    public boolean containsKey(final UUID key) {
        return this.get(key) != null;
    }

    /**
     * Returns whether the specified value has a key mapped to it.
     */
    public boolean containsValue(final V value) {

        final NodeIterator<V> iterator = new NodeIterator<>(this.table);

        TableEntry<V> node;
        while ((node = iterator.findNext()) != null) {
            if (node.getValueAcquire() == value) {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns the number of mappings in this map.
     */
    public int size() {
        final long ret = this.size.sum();

        if (ret < 0L) {
            return 0;
        }
        if (ret > (long)Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }

        return (int)ret;
    }

    /**
     * Returns whether this map has no mappings.
     */
    public boolean isEmpty() {
        return this.size.sum() <= 0L;
    }

    /**
     * Adds count to size and checks threshold for resizing
     */
    protected final void addSize(final long count) {
        this.size.add(count);

        final int threshold = this.getThresholdAcquire();

        if (threshold < 0L) {
            return;
        }

        final long sum = this.size.sum();

        if (sum < (long)threshold) {
            return;
        }

        if (threshold != this.compareExchangeThresholdVolatile(threshold, THRESHOLD_RESIZING)) {
            return;
        }

        this.resize(sum);
    }

    /**
     * Resizes table
     */
    private void resize(final long sum) {
        int capacity;

        final double targetD = ((double)sum / (double)this.loadFactor) + 1.0;
        if (targetD >= (double)MAXIMUM_CAPACITY) {
            capacity = MAXIMUM_CAPACITY;
        } else {
            capacity = (int)Math.ceil(targetD);
            capacity = IntegerUtil.roundCeilLog2(capacity);
            if (capacity > MAXIMUM_CAPACITY) {
                capacity = MAXIMUM_CAPACITY;
            }
        }

        // noinspection unchecked
        final TableEntry<V>[] newTable = new TableEntry[capacity];
        // noinspection unchecked
        final TableEntry<V> resizeNode = new TableEntry<>(null, (V)newTable, true);

        final TableEntry<V>[] oldTable = this.table;

        final int capOldShift = IntegerUtil.floorLog2(oldTable.length);
        final int capDiffShift = IntegerUtil.floorLog2(capacity) - capOldShift;

        if (capDiffShift == 0) {
            throw new IllegalStateException("Resizing to same size");
        }

        // noinspection unchecked
        final TableEntry<V>[] work = new TableEntry[1 << capDiffShift];

        for (int i = 0, len = oldTable.length; i < len; ++i) {
            TableEntry<V> binNode = getAtIndexVolatile(oldTable, i);

            for (;;) {
                if (binNode == null) {
                    if (null == (binNode = compareAndExchangeAtIndexVolatile(oldTable, i, null, resizeNode))) {
                        break;
                    }
                }

                synchronized (binNode) {
                    if (binNode != (binNode = getAtIndexVolatile(oldTable, i))) {
                        continue;
                    }

                    TableEntry<V> next = binNode.getNextPlain();

                    if (next == null) {
                        newTable[getHash(binNode.key) & (capacity - 1)] = binNode;
                    } else {
                        Arrays.fill(work, null);

                        for (TableEntry<V> curr = binNode; curr != null; curr = curr.getNextPlain()) {
                            final int newTableIdx = getHash(curr.key) & (capacity - 1);
                            final int workIdx = newTableIdx >>> capOldShift;

                            final TableEntry<V> replace = new TableEntry<>(curr.key, curr.getValuePlain());

                            final TableEntry<V> workNode = work[workIdx];
                            work[workIdx] = replace;

                            if (workNode == null) {
                                newTable[newTableIdx] = replace;
                                continue;
                            } else {
                                workNode.setNextPlain(replace);
                                continue;
                            }
                        }
                    }

                    setAtIndexRelease(oldTable, i, resizeNode);
                    break;
                }
            }
        }

        final int newThreshold;
        if (capacity == MAXIMUM_CAPACITY) {
            newThreshold = THRESHOLD_NO_RESIZE;
        } else {
            newThreshold = getTargetThreshold(capacity, loadFactor);
        }

        this.table = newTable;
        this.setThresholdVolatile(newThreshold);
    }

    /**
     * Subtracts count from size
     */
    protected final void subSize(final long count) {
        this.size.add(-count);
    }

    /**
     * Atomically updates the value associated with {@code key} to {@code value}, or inserts a new mapping.
     */
    public V put(final UUID key, final V value) {

        final int hash = getHash(key);

        TableEntry<V>[] table = this.table;
        table_loop:
        for (;;) {
            final int index = hash & (table.length - 1);

            TableEntry<V> node = getAtIndexVolatile(table, index);
            node_loop:
            for (;;) {
                if (node == null) {
                    if (null == (node = compareAndExchangeAtIndexVolatile(table, index, null, new TableEntry<>(key, value)))) {
                        this.addSize(1L);
                        return null;
                    }
                }

                if (node.resize) {
                    // noinspection unchecked
                    table = (TableEntry<V>[])node.getValuePlain();
                    continue table_loop;
                }

                synchronized (node) {
                    if (node != (node = getAtIndexVolatile(table, index))) {
                        continue node_loop;
                    }
                    TableEntry<V> prev = null;
                    for (; node != null; prev = node, node = node.getNextPlain()) {
                        if (node.key.equals(key)) {
                            final V ret = node.getValuePlain();
                            node.setValueVolatile(value);
                            return ret;
                        }
                    }

                    prev.setNextRelease(new TableEntry<>(key, value));
                }

                this.addSize(1L);
                return null;
            }
        }
    }

    /**
     * Atomically inserts a new mapping if and only if {@code key} is not currently mapped.
     */
    public V putIfAbsent(final UUID key, final V value) {

        final int hash = getHash(key);

        TableEntry<V>[] table = this.table;
        table_loop:
        for (;;) {
            final int index = hash & (table.length - 1);

            TableEntry<V> node = getAtIndexVolatile(table, index);
            node_loop:
            for (;;) {
                if (node == null) {
                    if (null == (node = compareAndExchangeAtIndexVolatile(table, index, null, new TableEntry<>(key, value)))) {
                        this.addSize(1L);
                        return null;
                    }
                }

                if (node.resize) {
                    // noinspection unchecked
                    table = (TableEntry<V>[])node.getValuePlain();
                    continue table_loop;
                }

                if (node.key.equals(key)) {
                    final V ret = node.getValueVolatile();
                    if (ret != null) {
                        return ret;
                    }
                }

                synchronized (node) {
                    if (node != (node = getAtIndexVolatile(table, index))) {
                        continue node_loop;
                    }
                    TableEntry<V> prev = null;
                    for (; node != null; prev = node, node = node.getNextPlain()) {
                        if (node.key.equals(key)) {
                            return node.getValuePlain();
                        }
                    }

                    prev.setNextRelease(new TableEntry<>(key, value));
                }

                this.addSize(1L);
                return null;
            }
        }
    }

    /**
     * Atomically updates the value associated with {@code key} to {@code value}, or does nothing if not mapped.
     */
    public V replace(final UUID key, final V value) {

        final int hash = getHash(key);

        TableEntry<V>[] table = this.table;
        table_loop:
        for (;;) {
            final int index = hash & (table.length - 1);

            TableEntry<V> node = getAtIndexVolatile(table, index);
            node_loop:
            for (;;) {
                if (node == null) {
                    return null;
                }

                if (node.resize) {
                    // noinspection unchecked
                    table = (TableEntry<V>[])node.getValuePlain();
                    continue table_loop;
                }

                synchronized (node) {
                    if (node != (node = getAtIndexVolatile(table, index))) {
                        continue node_loop;
                    }

                    for (; node != null; node = node.getNextPlain()) {
                        if (node.key.equals(key)) {
                            final V ret = node.getValuePlain();
                            node.setValueVolatile(value);
                            return ret;
                        }
                    }
                }

                return null;
            }
        }
    }

    /**
     * Atomically updates the value if the currently associated value is reference equal to {@code expect}.
     */
    public V replace(final UUID key, final V expect, final V update) {

        final int hash = getHash(key);

        TableEntry<V>[] table = this.table;
        table_loop:
        for (;;) {
            final int index = hash & (table.length - 1);

            TableEntry<V> node = getAtIndexVolatile(table, index);
            node_loop:
            for (;;) {
                if (node == null) {
                    return null;
                }

                if (node.resize) {
                    // noinspection unchecked
                    table = (TableEntry<V>[])node.getValuePlain();
                    continue table_loop;
                }

                synchronized (node) {
                    if (node != (node = getAtIndexVolatile(table, index))) {
                        continue node_loop;
                    }

                    for (; node != null; node = node.getNextPlain()) {
                        if (node.key.equals(key)) {
                            final V ret = node.getValuePlain();

                            if (ret != expect) {
                                return ret;
                            }

                            node.setValueVolatile(update);
                            return ret;
                        }
                    }
                }

                return null;
            }
        }
    }

    /**
     * Atomically removes the mapping for the specified key.
     */
    public V remove(final UUID key) {
        final int hash = getHash(key);

        TableEntry<V>[] table = this.table;
        table_loop:
        for (;;) {
            final int index = hash & (table.length - 1);

            TableEntry<V> node = getAtIndexVolatile(table, index);
            node_loop:
            for (;;) {
                if (node == null) {
                    return null;
                }

                if (node.resize) {
                    // noinspection unchecked
                    table = (TableEntry<V>[])node.getValuePlain();
                    continue table_loop;
                }

                boolean removed = false;
                V ret = null;

                synchronized (node) {
                    if (node != (node = getAtIndexVolatile(table, index))) {
                        continue node_loop;
                    }

                    TableEntry<V> prev = null;

                    for (; node != null; prev = node, node = node.getNextPlain()) {
                        if (node.key.equals(key)) {
                            ret = node.getValuePlain();
                            removed = true;

                            if (prev == null) {
                                setAtIndexRelease(table, index, node.getNextPlain());
                            } else {
                                prev.setNextRelease(node.getNextPlain());
                            }

                            break;
                        }
                    }
                }

                if (removed) {
                    this.subSize(1L);
                }

                return ret;
            }
        }
    }

    /**
     * Atomically removes the mapping if it is mapped to {@code expect}.
     */
    public V remove(final UUID key, final V expect) {
        final int hash = getHash(key);

        TableEntry<V>[] table = this.table;
        table_loop:
        for (;;) {
            final int index = hash & (table.length - 1);

            TableEntry<V> node = getAtIndexVolatile(table, index);
            node_loop:
            for (;;) {
                if (node == null) {
                    return null;
                }

                if (node.resize) {
                    // noinspection unchecked
                    table = (TableEntry<V>[])node.getValuePlain();
                    continue table_loop;
                }

                boolean removed = false;
                V ret = null;

                synchronized (node) {
                    if (node != (node = getAtIndexVolatile(table, index))) {
                        continue node_loop;
                    }

                    TableEntry<V> prev = null;

                    for (; node != null; prev = node, node = node.getNextPlain()) {
                        if (node.key.equals(key)) {
                            ret = node.getValuePlain();
                            if (ret == expect) {
                                removed = true;

                                if (prev == null) {
                                    setAtIndexRelease(table, index, node.getNextPlain());
                                } else {
                                    prev.setNextRelease(node.getNextPlain());
                                }
                            }
                            break;
                        }
                    }
                }

                if (removed) {
                    this.subSize(1L);
                }

                return ret;
            }
        }
    }

    /**
     * Removes at least all entries currently mapped at the beginning of this call.
     */
    public void clear() {
        final NodeIterator<V> nodeIterator = new NodeIterator<>(this.table);

        TableEntry<V> node;
        while ((node = nodeIterator.findNext()) != null) {
            this.remove(node.key);
        }
    }

    /**
     * Returns an iterator over the entries in this map.
     */
    public Iterator<TableEntry<V>> entryIterator() {
        return new EntryIterator<>(this);
    }

    @Override
    public final Iterator<TableEntry<V>> iterator() {
        return this.entryIterator();
    }

    /**
     * Returns an iterator over the keys in this map.
     */
    public Iterator<UUID> keyIterator() {
        return new KeyIterator<>(this);
    }

    /**
     * Returns an iterator over the values in this map.
     */
    public Iterator<V> valueIterator() {
        return new ValueIterator<>(this);
    }

    public Collection<V> values() {
        final Values<V> values = this.values;
        if (values != null) {
            return values;
        }
        return this.values = new Values<>(this);
    }

    public Set<TableEntry<V>> entrySet() {
        final EntrySet<V> entrySet = this.entrySet;
        if (entrySet != null) {
            return entrySet;
        }
        return this.entrySet = new EntrySet<>(this);
    }

    /**
     * See {@link java.util.concurrent.ConcurrentMap#computeIfAbsent(Object, Function)}
     * <p>
     * This function is a "functional methods" as defined by {@link ConcurrentUUID2ReferenceChainedHashTable}.
     * </p>
     */
    public V computeIfAbsent(final UUID key, final Function<UUID, ? extends V> function) {

        final int hash = getHash(key);

        TableEntry<V>[] table = this.table;
        table_loop:
        for (;;) {
            final int index = hash & (table.length - 1);

            TableEntry<V> node = getAtIndexVolatile(table, index);
            node_loop:
            for (;;) {
                V ret = null;
                if (node == null) {
                    final TableEntry<V> insert = new TableEntry<>(key, null);

                    boolean added = false;

                    synchronized (insert) {
                        if (null == (node = compareAndExchangeAtIndexVolatile(table, index, null, insert))) {
                            try {
                                ret = function.apply(key);
                            } catch (final Throwable throwable) {
                                setAtIndexVolatile(table, index, null);
                                ThrowUtil.throwUnchecked(throwable);
                                // unreachable
                                return null;
                            }

                            if (ret == null) {
                                setAtIndexVolatile(table, index, null);
                                return null;
                            } else {
                                // volatile ordering ensured by addSize(), but we need release here
                                // to ensure proper ordering with reads and other writes
                                insert.setValueRelease(ret);
                                added = true;
                            }
                        } // else: node != null, fall through
                    }

                    if (added) {
                        this.addSize(1L);
                        return ret;
                    }
                }

                if (node.resize) {
                    // noinspection unchecked
                    table = (TableEntry<V>[])node.getValuePlain();
                    continue table_loop;
                }

                // optimise ifAbsent calls: check if first node is key before attempting lock acquire
                if (node.key.equals(key)) {
                    ret = node.getValueVolatile();
                    if (ret != null) {
                        return ret;
                    } // else: fall back to lock to read the node
                }

                boolean added = false;

                synchronized (node) {
                    if (node != (node = getAtIndexVolatile(table, index))) {
                        continue node_loop;
                    }
                    // plain reads are fine during synchronised access, as we are the only writer
                    TableEntry<V> prev = null;
                    for (; node != null; prev = node, node = node.getNextPlain()) {
                        if (node.key.equals(key)) {
                            ret = node.getValuePlain();
                            return ret;
                        }
                    }

                    final V computed = function.apply(key);
                    if (computed != null) {
                        // volatile ordering ensured by addSize(), but we need release here
                        // to ensure proper ordering with reads and other writes
                        prev.setNextRelease(new TableEntry<>(key, computed));
                        ret = computed;
                        added = true;
                    }
                }

                if (added) {
                    this.addSize(1L);
                }

                return ret;
            }
        }
    }

    // Iterator implementations (similar to original but with UUID instead of long)
    protected static final class EntryIterator<V> extends BaseIteratorImpl<V, TableEntry<V>> {
        public EntryIterator(final ConcurrentUUID2ReferenceChainedHashTable<V> map) {
            super(map);
        }
        @Override public TableEntry<V> next() { return this.nextNode(); }
        @Override public void forEachRemaining(final Consumer<? super TableEntry<V>> action) {
            while (this.hasNext()) { action.accept(this.next()); }
        }
    }

    protected static final class KeyIterator<V> extends BaseIteratorImpl<V, UUID> {
        public KeyIterator(final ConcurrentUUID2ReferenceChainedHashTable<V> map) { super(map); }
        @Override public UUID next() { return this.nextNode().key; }
        @Override public void forEachRemaining(final Consumer<? super UUID> action) {
            while (this.hasNext()) { action.accept(this.next()); }
        }
    }

    protected static final class ValueIterator<V> extends BaseIteratorImpl<V, V> {
        public ValueIterator(final ConcurrentUUID2ReferenceChainedHashTable<V> map) { super(map); }
        @Override public V next() { return this.nextNode().getValueVolatile(); }
        @Override public void forEachRemaining(final Consumer<? super V> action) {
            while (this.hasNext()) { action.accept(this.next()); }
        }
    }

    protected static abstract class BaseIteratorImpl<V, T> extends NodeIterator<V> implements Iterator<T> {
        protected final ConcurrentUUID2ReferenceChainedHashTable<V> map;
        protected TableEntry<V> lastReturned;
        protected TableEntry<V> nextToReturn;

        protected BaseIteratorImpl(final ConcurrentUUID2ReferenceChainedHashTable<V> map) {
            super(map.table);
            this.map = map;
        }

        @Override public final boolean hasNext() {
            if (this.nextToReturn != null) return true;
            return (this.nextToReturn = this.findNext()) != null;
        }

        protected final TableEntry<V> nextNode() throws NoSuchElementException {
            TableEntry<V> ret = this.nextToReturn;
            if (ret != null) {
                this.lastReturned = ret;
                this.nextToReturn = null;
                return ret;
            }
            ret = this.findNext();
            if (ret != null) {
                this.lastReturned = ret;
                return ret;
            }
            throw new NoSuchElementException();
        }

        @Override public final void remove() {
            final TableEntry<V> lastReturned = this.lastReturned;
            if (lastReturned == null) throw new NoSuchElementException();
            this.lastReturned = null;
            this.map.remove(lastReturned.key);
        }

        @Override public abstract T next() throws NoSuchElementException;
        @Override public abstract void forEachRemaining(final Consumer<? super T> action);
    }

    protected static class NodeIterator<V> {
        // Implementation similar to original but with UUID keys
        protected TableEntry<V>[] currentTable;
        protected ResizeChain<V> resizeChain;
        protected TableEntry<V> last;
        protected int nextBin;
        protected int increment;

        protected NodeIterator(final TableEntry<V>[] baseTable) {
            this.currentTable = baseTable;
            this.increment = 1;
        }

        private TableEntry<V>[] pullResizeChain(final int index) {
            final ResizeChain<V> resizeChain = this.resizeChain;
            if (resizeChain == null) {
                this.currentTable = null;
                return null;
            }

            final ResizeChain<V> prevChain = resizeChain.prev;
            this.resizeChain = prevChain;
            if (prevChain == null) {
                this.currentTable = null;
                return null;
            }

            final TableEntry<V>[] newTable = prevChain.table;
            int newIdx = index & (newTable.length - 1);

            final ResizeChain<V> nextPrevChain = prevChain.prev;
            final int increment = nextPrevChain == null ? 1 : nextPrevChain.table.length;

            newIdx += increment;
            this.increment = increment;
            this.nextBin = newIdx;
            this.currentTable = newTable;

            return newTable;
        }

        private TableEntry<V>[] pushResizeChain(final TableEntry<V>[] table, final TableEntry<V> entry) {
            final ResizeChain<V> chain = this.resizeChain;

            if (chain == null) {
                // noinspection unchecked
                final TableEntry<V>[] nextTable = (TableEntry<V>[])entry.getValuePlain();
                final ResizeChain<V> oldChain = new ResizeChain<>(table, null, null);
                final ResizeChain<V> currChain = new ResizeChain<>(nextTable, oldChain, null);
                oldChain.next = currChain;

                this.increment = table.length;
                this.resizeChain = currChain;
                this.currentTable = nextTable;

                return nextTable;
            } else {
                ResizeChain<V> currChain = chain.next;
                if (currChain == null) {
                    // noinspection unchecked
                    final TableEntry<V>[] ret = (TableEntry<V>[])entry.getValuePlain();
                    currChain = new ResizeChain<>(ret, chain, null);
                    chain.next = currChain;

                    this.increment = table.length;
                    this.resizeChain = currChain;
                    this.currentTable = ret;

                    return ret;
                } else {
                    this.increment = table.length;
                    this.resizeChain = currChain;
                    return this.currentTable = currChain.table;
                }
            }
        }

        protected final TableEntry<V> findNext() {
            for (;;) {
                final TableEntry<V> last = this.last;
                if (last != null) {
                    final TableEntry<V> next = last.getNextVolatile();
                    if (next != null) {
                        this.last = next;
                        if (next.getValuePlain() == null) continue;
                        return next;
                    }
                }

                TableEntry<V>[] table = this.currentTable;
                if (table == null) return null;

                int idx = this.nextBin;
                int increment = this.increment;
                for (;;) {
                    if (idx >= table.length) {
                        table = this.pullResizeChain(idx);
                        idx = this.nextBin;
                        increment = this.increment;
                        if (table != null) continue;
                        this.last = null;
                        return null;
                    }

                    final TableEntry<V> entry = getAtIndexVolatile(table, idx);
                    if (entry == null) {
                        idx += increment;
                        continue;
                    }

                    if (entry.resize) {
                        table = this.pushResizeChain(table, entry);
                        increment = this.increment;
                        continue;
                    }

                    this.last = entry;
                    this.nextBin = idx + increment;
                    if (entry.getValuePlain() != null) return entry;
                    break;
                }
            }
        }

        protected static final class ResizeChain<V> {
            public final TableEntry<V>[] table;
            public final ResizeChain<V> prev;
            public ResizeChain<V> next;

            public ResizeChain(final TableEntry<V>[] table, final ResizeChain<V> prev, final ResizeChain<V> next) {
                this.table = table;
                this.prev = prev;
                this.next = next;
            }
        }
    }

    protected static abstract class BaseCollection<V, E> implements Collection<E> {
        protected final ConcurrentUUID2ReferenceChainedHashTable<V> map;

        protected BaseCollection(final ConcurrentUUID2ReferenceChainedHashTable<V> map) {
            this.map = map;
        }

        @Override public int size() { return this.map.size(); }
        @Override public boolean isEmpty() { return this.map.isEmpty(); }
        @Override public void forEach(final Consumer<? super E> action) { this.iterator().forEachRemaining(action); }

        private List<E> asList() {
            final List<E> ret = new ArrayList<>(this.map.size());
            for (final E element : this) ret.add(element);
            return ret;
        }

        @Override public Object[] toArray() { return this.asList().toArray(); }
        @Override public <T> T[] toArray(final T[] a) { return this.asList().toArray(a); }

        @Override public boolean containsAll(final Collection<?> collection) {
            for (final Object value : collection) {
                if (!this.contains(value)) return false;
            }
            return true;
        }

        @Override public boolean add(final E value) { throw new UnsupportedOperationException(); }
        @Override public boolean remove(final Object value) { throw new UnsupportedOperationException(); }
        @Override public boolean addAll(final Collection<? extends E> collection) { throw new UnsupportedOperationException(); }
        @Override public boolean removeAll(final Collection<?> collection) { throw new UnsupportedOperationException(); }
        @Override public boolean removeIf(final Predicate<? super E> filter) { throw new UnsupportedOperationException(); }
        @Override public boolean retainAll(final Collection<?> collection) { throw new UnsupportedOperationException(); }
        @Override public void clear() { throw new UnsupportedOperationException(); }
    }

    protected static class Values<V> extends BaseCollection<V, V> {
        public Values(final ConcurrentUUID2ReferenceChainedHashTable<V> map) { super(map); }
        @Override public boolean contains(final Object value) { return this.map.containsValue((V)value); }
        @Override public Iterator<V> iterator() { return this.map.valueIterator(); }
    }

    protected static class EntrySet<V> extends BaseCollection<V, TableEntry<V>> implements Set<TableEntry<V>> {
        protected EntrySet(final ConcurrentUUID2ReferenceChainedHashTable<V> map) { super(map); }
        @Override public boolean contains(final Object value) {
            if (!(value instanceof TableEntry<?> entry)) return false;
            final V mapped = this.map.get((UUID)entry.getKey());
            return mapped != null && mapped == value;
        }
        @Override public Iterator<TableEntry<V>> iterator() { return this.map.entryIterator(); }
    }

    public static final class TableEntry<V> {
        private static final VarHandle TABLE_ENTRY_ARRAY_HANDLE = ConcurrentUtil.getArrayHandle(TableEntry[].class);

        private final boolean resize;
        private final UUID key;

        private volatile V value;
        private static final VarHandle VALUE_HANDLE = ConcurrentUtil.getVarHandle(TableEntry.class, "value", Object.class);

        private V getValuePlain() { return (V)VALUE_HANDLE.get(this); }
        private V getValueAcquire() { return (V)VALUE_HANDLE.getAcquire(this); }
        private V getValueVolatile() { return (V)VALUE_HANDLE.getVolatile(this); }
        private void setValuePlain(final V value) { VALUE_HANDLE.set(this, (Object)value); }
        private void setValueRelease(final V value) { VALUE_HANDLE.setRelease(this, (Object)value); }
        private void setValueVolatile(final V value) { VALUE_HANDLE.setVolatile(this, (Object)value); }

        private volatile TableEntry<V> next;
        private static final VarHandle NEXT_HANDLE = ConcurrentUtil.getVarHandle(TableEntry.class, "next", TableEntry.class);

        private TableEntry<V> getNextPlain() { return (TableEntry<V>)NEXT_HANDLE.get(this); }
        private TableEntry<V> getNextVolatile() { return (TableEntry<V>)NEXT_HANDLE.getVolatile(this); }
        private void setNextPlain(final TableEntry<V> next) { NEXT_HANDLE.set(this, next); }
        private void setNextRelease(final TableEntry<V> next) { NEXT_HANDLE.setRelease(this, next); }
        private void setNextVolatile(final TableEntry<V> next) { NEXT_HANDLE.setVolatile(this, next); }

        public TableEntry(final UUID key, final V value) {
            this.resize = false;
            this.key = key;
            this.setValuePlain(value);
        }

        public TableEntry(final UUID key, final V value, final boolean resize) {
            this.resize = resize;
            this.key = key;
            this.setValuePlain(value);
        }

        public UUID getKey() { return this.key; }
        public V getValue() { return this.getValueVolatile(); }
    }
}