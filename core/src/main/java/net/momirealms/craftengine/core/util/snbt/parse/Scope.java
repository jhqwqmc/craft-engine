package net.momirealms.craftengine.core.util.snbt.parse;

import com.google.common.annotations.VisibleForTesting;
import net.momirealms.craftengine.core.util.MiscUtils;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unchecked")
public final class Scope {
    private static final int NOT_FOUND = -1;
    private static final Object FRAME_START_MARKER = new Object() {
        @Override
        public String toString() {
            return "frame";
        }
    };
    private static final int ENTRY_STRIDE = 2;
    private Object[] stack = new Object[128];
    private int topEntryKeyIndex = 0;
    private int topMarkerKeyIndex = 0;
    private int depth;

    public Scope() {
        this.stack[0] = FRAME_START_MARKER;
        this.stack[1] = null;
    }

    private int valueIndex(Atom<?> atom) {
        for (int i = this.topEntryKeyIndex; i > this.topMarkerKeyIndex; i -= ENTRY_STRIDE) {
            Object key = this.stack[i];

            assert key instanceof Atom;

            if (key == atom) {
                return i + 1;
            }
        }

        return NOT_FOUND;
    }

    public int valueIndexForAny(Atom<?>... atoms) {
        for (int i = this.topEntryKeyIndex; i > this.topMarkerKeyIndex; i -= ENTRY_STRIDE) {
            Object key = this.stack[i];

            assert key instanceof Atom;

            for (Atom<?> atom : atoms) {
                if (atom == key) {
                    return i + 1;
                }
            }
        }

        return NOT_FOUND;
    }

    private void ensureCapacity(int additionalEntryCount) {
        int currentSize = this.stack.length;
        int currentLastValueIndex = this.topEntryKeyIndex + 1;
        int newLastValueIndex = currentLastValueIndex + additionalEntryCount * 2;
        if (newLastValueIndex >= currentSize) {
            int newSize = MiscUtils.growByHalf(currentSize, newLastValueIndex + 1);
            Object[] newStack = new Object[newSize];
            System.arraycopy(this.stack, 0, newStack, 0, currentSize);
            this.stack = newStack;
        }

        assert this.validateStructure();
    }

    private void setupNewFrame() {
        this.topEntryKeyIndex += ENTRY_STRIDE;
        this.stack[this.topEntryKeyIndex] = FRAME_START_MARKER;
        this.stack[this.topEntryKeyIndex + 1] = this.topMarkerKeyIndex;
        this.topMarkerKeyIndex = this.topEntryKeyIndex;
    }

    public void pushFrame() {
        this.ensureCapacity(1);
        this.setupNewFrame();

        assert this.validateStructure();
    }

    private int getPreviousMarkerIndex(int markerKeyIndex) {
        return (Integer) this.stack[markerKeyIndex + 1];
    }

    public void popFrame() {
        assert this.topMarkerKeyIndex != 0;

        this.topEntryKeyIndex = this.topMarkerKeyIndex - ENTRY_STRIDE;
        this.topMarkerKeyIndex = this.getPreviousMarkerIndex(this.topMarkerKeyIndex);

        assert this.validateStructure();
    }

    public void splitFrame() {
        int currentFrameMarkerIndex = this.topMarkerKeyIndex;
        int nonMarkerEntriesInFrame = (this.topEntryKeyIndex - this.topMarkerKeyIndex) / ENTRY_STRIDE;
        this.ensureCapacity(nonMarkerEntriesInFrame + 1);
        this.setupNewFrame();
        int sourceCursor = currentFrameMarkerIndex + ENTRY_STRIDE;
        int targetCursor = this.topEntryKeyIndex;

        for (int i = 0; i < nonMarkerEntriesInFrame; i++) {
            targetCursor += ENTRY_STRIDE;
            Object key = this.stack[sourceCursor];

            assert key != null;

            this.stack[targetCursor] = key;
            this.stack[targetCursor + 1] = null;
            sourceCursor += ENTRY_STRIDE;
        }

        this.topEntryKeyIndex = targetCursor;

        assert this.validateStructure();
    }

    public void clearFrameValues() {
        for (int i = this.topEntryKeyIndex; i > this.topMarkerKeyIndex; i -= ENTRY_STRIDE) {
            assert this.stack[i] instanceof Atom;

            this.stack[i + 1] = null;
        }

        assert this.validateStructure();
    }

    public void mergeFrame() {
        int previousMarkerIndex = this.getPreviousMarkerIndex(this.topMarkerKeyIndex);
        int previousFrameCursor = previousMarkerIndex;
        int currentFrameCursor = this.topMarkerKeyIndex;

        while (currentFrameCursor < this.topEntryKeyIndex) {
            previousFrameCursor += ENTRY_STRIDE;
            currentFrameCursor += ENTRY_STRIDE;
            Object newKey = this.stack[currentFrameCursor];

            assert newKey instanceof Atom;

            Object newValue = this.stack[currentFrameCursor + 1];
            Object oldKey = this.stack[previousFrameCursor];
            if (oldKey != newKey) {
                this.stack[previousFrameCursor] = newKey;
                this.stack[previousFrameCursor + 1] = newValue;
            } else if (newValue != null) {
                this.stack[previousFrameCursor + 1] = newValue;
            }
        }

        this.topEntryKeyIndex = previousFrameCursor;
        this.topMarkerKeyIndex = previousMarkerIndex;

        assert this.validateStructure();
    }

    public <T> void put(Atom<T> name, @Nullable T value) {
        int valueIndex = this.valueIndex(name);
        if (valueIndex != NOT_FOUND) {
            this.stack[valueIndex] = value;
        } else {
            this.ensureCapacity(1);
            this.topEntryKeyIndex += ENTRY_STRIDE;
            this.stack[this.topEntryKeyIndex] = name;
            this.stack[this.topEntryKeyIndex + 1] = value;
        }

        assert this.validateStructure();
    }

    @Nullable
    public <T> T get(Atom<T> name) {
        int valueIndex = this.valueIndex(name);
        return (T) (valueIndex != NOT_FOUND ? this.stack[valueIndex] : null);
    }

    public <T> T getOrThrow(Atom<T> name) {
        int valueIndex = this.valueIndex(name);
        if (valueIndex == NOT_FOUND) {
            throw new IllegalArgumentException("No value for atom " + name);
        }
        return (T) this.stack[valueIndex];
    }

    public <T> T getOrDefault(Atom<T> name, T fallback) {
        int valueIndex = this.valueIndex(name);
        return (T) (valueIndex != NOT_FOUND ? this.stack[valueIndex] : fallback);
    }

    @Nullable
    @SafeVarargs
    public final <T> T getAny(Atom<? extends T>... names) {
        int valueIndex = this.valueIndexForAny(names);
        return (T) (valueIndex != NOT_FOUND ? this.stack[valueIndex] : null);
    }

    @SafeVarargs
    public final <T> T getAnyOrThrow(Atom<? extends T>... names) {
        int valueIndex = this.valueIndexForAny(names);
        if (valueIndex == NOT_FOUND) {
            throw new IllegalArgumentException("No value for atoms " + Arrays.toString(names));
        }
        return (T) this.stack[valueIndex];
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        boolean afterFrame = true;

        for (int i = 0; i <= this.topEntryKeyIndex; i += ENTRY_STRIDE) {
            Object key = this.stack[i];
            Object value = this.stack[i + 1];
            if (key == FRAME_START_MARKER) {
                result.append('|');
                afterFrame = true;
            } else {
                if (!afterFrame) {
                    result.append(',');
                }

                afterFrame = false;
                result.append(key).append(':').append(value);
            }
        }

        return result.toString();
    }

    @VisibleForTesting
    public Map<Atom<?>, ?> lastFrame() {
        HashMap<Atom<?>, Object> result = new HashMap<>();

        for (int i = this.topEntryKeyIndex; i > this.topMarkerKeyIndex; i -= ENTRY_STRIDE) {
            Object key = this.stack[i];
            Object value = this.stack[i + 1];
            result.put((Atom<?>) key, value);
        }

        return result;
    }

    public boolean hasOnlySingleFrame() {
        for (int i = this.topEntryKeyIndex; i > 0; i--) {
            if (this.stack[i] == FRAME_START_MARKER) {
                return false;
            }
        }

        if (this.stack[0] != FRAME_START_MARKER) {
            throw new IllegalStateException("Corrupted stack");
        }
        return true;
    }

    private boolean validateStructure() {
        assert this.topMarkerKeyIndex >= 0;

        assert this.topEntryKeyIndex >= this.topMarkerKeyIndex;

        for (int i = 0; i <= this.topEntryKeyIndex; i += ENTRY_STRIDE) {
            Object object = this.stack[i];
            if (object != FRAME_START_MARKER && !(object instanceof Atom)) {
                return false;
            }
        }

        for (int ix = this.topMarkerKeyIndex; ix != 0; ix = this.getPreviousMarkerIndex(ix)) {
            Object object = this.stack[ix];
            if (object != FRAME_START_MARKER) {
                return false;
            }
        }

        return true;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <S> Term<S> increaseDepth() {
        class IncreasingDepthTerm<W> implements Term<W> {
            public static final IncreasingDepthTerm INSTANCE = new IncreasingDepthTerm();

            @Override
            public boolean parse(final ParseState<W> state, final Scope scope, final Control control) {
                if (++scope.depth > 512) {
                    state.errorCollector().store(state.mark(), new IllegalStateException("Too deep"));
                    return false;
                }
                return true;
            }
        }
        return (Term<S>) IncreasingDepthTerm.INSTANCE;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <S> Term<S> decreaseDepth() {
        class DecreasingDepthTerm<W> implements Term<W> {
            public static final DecreasingDepthTerm INSTANCE = new DecreasingDepthTerm();

            @Override
            public boolean parse(final ParseState<W> state, final Scope scope, final Control control) {
                scope.depth--;
                return true;
            }
        }
        return (Term<S>) DecreasingDepthTerm.INSTANCE;
    }
}
