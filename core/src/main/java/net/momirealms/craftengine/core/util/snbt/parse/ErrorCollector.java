package net.momirealms.craftengine.core.util.snbt.parse;

import net.momirealms.craftengine.core.util.MiscUtils;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unchecked")
public interface ErrorCollector<S> {
    void store(int cursor, SuggestionSupplier<S> suggestions, Object reason);

    default void store(int cursor, Object reason) {
        this.store(cursor, SuggestionSupplier.empty(), reason);
    }

    void finish(int finalCursor);

    class LongestOnly<S> implements ErrorCollector<S> {
        private MutableErrorEntry<S>[] entries = new MutableErrorEntry[16];
        private int nextErrorEntry;
        private int lastCursor = -1;

        private void discardErrorsFromShorterParse(int cursor) {
            if (cursor > this.lastCursor) {
                this.lastCursor = cursor;
                this.nextErrorEntry = 0;
            }
        }

        @Override
        public void finish(int finalCursor) {
            this.discardErrorsFromShorterParse(finalCursor);
        }

        @Override
        public void store(int cursor, SuggestionSupplier<S> suggestions, Object reason) {
            this.discardErrorsFromShorterParse(cursor);
            if (cursor == this.lastCursor) {
                this.addErrorEntry(suggestions, reason);
            }
        }

        private void addErrorEntry(SuggestionSupplier<S> suggestions, Object reason) {
            int currentSize = this.entries.length;
            if (this.nextErrorEntry >= currentSize) {
                int newSize = MiscUtils.growByHalf(currentSize, this.nextErrorEntry + 1);
                MutableErrorEntry<S>[] newEntries = new MutableErrorEntry[newSize];
                System.arraycopy(this.entries, 0, newEntries, 0, currentSize);
                this.entries = newEntries;
            }

            int entryIndex = this.nextErrorEntry++;
            MutableErrorEntry<S> entry = this.entries[entryIndex];
            if (entry == null) {
                entry = new MutableErrorEntry<>();
                this.entries[entryIndex] = entry;
            }

            entry.suggestions = suggestions;
            entry.reason = reason;
        }

        public List<ErrorEntry<S>> entries() {
            int errorCount = this.nextErrorEntry;
            if (errorCount == 0) {
                return List.of();
            }
            List<ErrorEntry<S>> result = new ArrayList<>(errorCount);

            for (int i = 0; i < errorCount; i++) {
                MutableErrorEntry<S> mutableErrorEntry = this.entries[i];
                result.add(new ErrorEntry<>(this.lastCursor, mutableErrorEntry.suggestions, mutableErrorEntry.reason));
            }

            return result;
        }

        public int cursor() {
            return this.lastCursor;
        }

        static class MutableErrorEntry<S> {
            SuggestionSupplier<S> suggestions = SuggestionSupplier.empty();
            Object reason = "empty";
        }
    }

    class Nop<S> implements ErrorCollector<S> {
        @Override
        public void store(int cursor, SuggestionSupplier<S> suggestions, Object reason) {
        }

        @Override
        public void finish(int finalCursor) {
        }
    }
}
