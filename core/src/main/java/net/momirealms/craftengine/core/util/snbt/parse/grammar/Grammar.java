package net.momirealms.craftengine.core.util.snbt.parse.grammar;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.momirealms.craftengine.core.util.snbt.parse.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public record Grammar<T>(Dictionary<StringReader> rules, NamedRule<StringReader, T> top) {
    public Grammar {
        rules.checkAllBound();
    }

    public Optional<T> parse(ParseState<StringReader> state) {
        return state.parseTopRule(this.top);
    }

    public T parse(StringReader reader) throws CommandSyntaxException {
        ErrorCollector.LongestOnly<StringReader> errorCollector = new ErrorCollector.LongestOnly<>();
        StringReaderParserState stringReaderParserState = new StringReaderParserState(errorCollector, reader);
        Optional<T> optionalResult = this.parse(stringReaderParserState);
        if (optionalResult.isPresent()) {
            T result = optionalResult.get();
            if (CachedParseState.JAVA_NULL_VALUE_MARKER.equals(result)) {
                result = null;
            }
            return result;
        }
        List<ErrorEntry<StringReader>> errorEntries = errorCollector.entries();
        List<Exception> exceptions = errorEntries.stream().<Exception>mapMulti((entry, output) -> {
            if (entry.reason() instanceof DelayedException<?> delayedException) {
                output.accept(delayedException.create(reader.getString(), entry.cursor()));
            } else if (entry.reason() instanceof Exception exception1) {
                output.accept(exception1);
            }
        }).toList();

        for (Exception exception : exceptions) {
            if (exception instanceof CommandSyntaxException cse) {
                throw cse;
            }
        }

        if (exceptions.size() == 1 && exceptions.getFirst() instanceof RuntimeException re) {
            throw re;
        }
        throw new IllegalStateException("Failed to parse: " + errorEntries.stream().map(ErrorEntry::toString).collect(Collectors.joining(", ")));
    }
}
