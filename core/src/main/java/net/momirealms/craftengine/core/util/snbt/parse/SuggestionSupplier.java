package net.momirealms.craftengine.core.util.snbt.parse;

import java.util.stream.Stream;

public interface SuggestionSupplier<S> {
    Stream<String> possibleValues(ParseState<S> state);

    static <S> SuggestionSupplier<S> empty() {
        return state -> Stream.empty();
    }
}
