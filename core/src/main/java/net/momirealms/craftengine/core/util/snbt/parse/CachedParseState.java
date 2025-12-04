package net.momirealms.craftengine.core.util.snbt.parse;

import net.momirealms.craftengine.core.util.MiscUtils;

import javax.annotation.Nullable;

@SuppressWarnings("unchecked")
public abstract class CachedParseState<S> implements ParseState<S> {
    private PositionCache[] positionCache = new PositionCache[256];
    private final ErrorCollector<S> errorCollector;
    private final Scope scope = new Scope();
    private SimpleControl[] controlCache = new SimpleControl[16];
    private int nextControlToReturn;
    private final Silent silent = new Silent();
    public static final Object JAVA_NULL_VALUE_MARKER = new Object() {
        @Override
        public String toString() {
            return "null";
        }
    };

    protected CachedParseState(ErrorCollector<S> errorCollector) {
        this.errorCollector = errorCollector;
    }

    @Override
    public Scope scope() {
        return this.scope;
    }

    @Override
    public ErrorCollector<S> errorCollector() {
        return this.errorCollector;
    }

    @Nullable
    @Override
    public <T> T parse(NamedRule<S, T> rule) {
        int markBeforeParse = this.mark();
        PositionCache positionCache = this.getCacheForPosition(markBeforeParse);
        int entryIndex = positionCache.findKeyIndex(rule.name());
        if (entryIndex != -1) {
            CacheEntry<T> value = positionCache.getValue(entryIndex);
            if (value != null) {
                if (value == CachedParseState.CacheEntry.NEGATIVE) {
                    return null;
                }
                this.restore(value.markAfterParse);
                return value.value;
            }
        } else {
            entryIndex = positionCache.allocateNewEntry(rule.name());
        }

        T result = rule.value().parse(this);
        CacheEntry<T> entry;
        if (result == null) {
            entry = CacheEntry.negativeEntry();
        } else {
            int markAfterParse = this.mark();
            entry = new CacheEntry<>(result, markAfterParse);
        }

        positionCache.setValue(entryIndex, entry);
        return result;
    }

    private PositionCache getCacheForPosition(int index) {
        int currentSize = this.positionCache.length;
        if (index >= currentSize) {
            int newSize = MiscUtils.growByHalf(currentSize, index + 1);
            PositionCache[] newCache = new PositionCache[newSize];
            System.arraycopy(this.positionCache, 0, newCache, 0, currentSize);
            this.positionCache = newCache;
        }

        PositionCache result = this.positionCache[index];
        if (result == null) {
            result = new PositionCache();
            this.positionCache[index] = result;
        }

        return result;
    }

    @Override
    public Control acquireControl() {
        int currentSize = this.controlCache.length;
        if (this.nextControlToReturn >= currentSize) {
            int newSize = MiscUtils.growByHalf(currentSize, this.nextControlToReturn + 1);
            SimpleControl[] newControlCache = new SimpleControl[newSize];
            System.arraycopy(this.controlCache, 0, newControlCache, 0, currentSize);
            this.controlCache = newControlCache;
        }

        int controlIndex = this.nextControlToReturn++;
        SimpleControl entry = this.controlCache[controlIndex];
        if (entry == null) {
            entry = new SimpleControl();
            this.controlCache[controlIndex] = entry;
        } else {
            entry.reset();
        }

        return entry;
    }

    @Override
    public void releaseControl() {
        this.nextControlToReturn--;
    }

    @Override
    public ParseState<S> silent() {
        return this.silent;
    }

    record CacheEntry<T>(@Nullable T value, int markAfterParse) {
        public static final CacheEntry<?> NEGATIVE = new CacheEntry<>(null, -1);

        public static <T> CacheEntry<T> negativeEntry() {
            return (CacheEntry<T>) NEGATIVE;
        }
    }

    static class PositionCache {
        public static final int ENTRY_STRIDE = 2;
        private static final int NOT_FOUND = -1;
        private Object[] atomCache = new Object[16];
        private int nextKey;

        public int findKeyIndex(Atom<?> key) {
            for (int i = 0; i < this.nextKey; i += ENTRY_STRIDE) {
                if (this.atomCache[i] == key) {
                    return i;
                }
            }

            return NOT_FOUND;
        }

        public int allocateNewEntry(Atom<?> key) {
            int newKeyIndex = this.nextKey;
            this.nextKey += ENTRY_STRIDE;
            int newValueIndex = newKeyIndex + 1;
            int currentSize = this.atomCache.length;
            if (newValueIndex >= currentSize) {
                int newSize = MiscUtils.growByHalf(currentSize, newValueIndex + 1);
                Object[] newCache = new Object[newSize];
                System.arraycopy(this.atomCache, 0, newCache, 0, currentSize);
                this.atomCache = newCache;
            }

            this.atomCache[newKeyIndex] = key;
            return newKeyIndex;
        }

        @Nullable
        public <T> CacheEntry<T> getValue(int keyIndex) {
            return (CacheEntry<T>) this.atomCache[keyIndex + 1];
        }

        public void setValue(int keyIndex, CacheEntry<?> entry) {
            this.atomCache[keyIndex + 1] = entry;
        }
    }

    class Silent implements ParseState<S> {
        private final ErrorCollector<S> silentCollector = new ErrorCollector.Nop<>();

        @Override
        public ErrorCollector<S> errorCollector() {
            return this.silentCollector;
        }

        @Override
        public Scope scope() {
            return CachedParseState.this.scope();
        }

        @Nullable
        @Override
        public <T> T parse(NamedRule<S, T> rule) {
            return CachedParseState.this.parse(rule);
        }

        @Override
        public S input() {
            return CachedParseState.this.input();
        }

        @Override
        public int mark() {
            return CachedParseState.this.mark();
        }

        @Override
        public void restore(int mark) {
            CachedParseState.this.restore(mark);
        }

        @Override
        public Control acquireControl() {
            return CachedParseState.this.acquireControl();
        }

        @Override
        public void releaseControl() {
            CachedParseState.this.releaseControl();
        }

        @Override
        public ParseState<S> silent() {
            return this;
        }
    }

    static class SimpleControl implements Control {
        private boolean hasCut;

        @Override
        public void cut() {
            this.hasCut = true;
        }

        @Override
        public boolean hasCut() {
            return this.hasCut;
        }

        public void reset() {
            this.hasCut = false;
        }
    }
}
