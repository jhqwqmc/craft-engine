package net.momirealms.craftengine.core.util.snbt.parse.grammar;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import it.unimi.dsi.fastutil.chars.CharList;
import net.momirealms.craftengine.core.util.snbt.parse.*;

import java.util.stream.Collectors;

public interface StringReaderTerms {
    DynamicCommandExceptionType LITERAL_INCORRECT = new LocalizedDynamicCommandExceptionType(
            expected -> new LocalizedMessage("warning.config.type.snbt.parser.incorrect", String.valueOf(expected))
    );

    static Term<StringReader> character(final char value) {
        return new TerminalCharacters(CharList.of(value)) {
            @Override
            protected boolean isAccepted(char v) {
                return value == v;
            }
        };
    }

    static Term<StringReader> characters(final char v1, final char v2) {
        return new TerminalCharacters(CharList.of(v1, v2)) {
            @Override
            protected boolean isAccepted(char v) {
                return v == v1 || v == v2;
            }
        };
    }

    static StringReader createReader(String contents, int cursor) {
        StringReader reader = new StringReader(contents);
        reader.setCursor(cursor);
        return reader;
    }

    abstract class TerminalCharacters implements Term<StringReader> {
        private final DelayedException<CommandSyntaxException> error;
        private final SuggestionSupplier<StringReader> suggestions;

        public TerminalCharacters(CharList values) {
            String joinedValues = values.intStream().mapToObj(Character::toString).collect(Collectors.joining("|"));
            this.error = DelayedException.create(LITERAL_INCORRECT, joinedValues);
            this.suggestions = s -> values.intStream().mapToObj(Character::toString);
        }

        @Override
        public boolean parse(ParseState<StringReader> state, Scope scope, Control control) {
            state.input().skipWhitespace();
            int cursor = state.mark();
            if (state.input().canRead() && this.isAccepted(state.input().read())) {
                return true;
            }
            state.errorCollector().store(cursor, this.suggestions, this.error);
            return false;
        }

        protected abstract boolean isAccepted(char value);
    }

}
