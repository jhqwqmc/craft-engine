package net.momirealms.craftengine.core.util.snbt.parse.grammar;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.momirealms.craftengine.core.util.snbt.parse.DelayedException;
import net.momirealms.craftengine.core.util.snbt.parse.ParseState;
import net.momirealms.craftengine.core.util.snbt.parse.Rule;

import javax.annotation.Nullable;

public class UnquotedStringParseRule implements Rule<StringReader, String> {
    private final int minSize;
    private final DelayedException<CommandSyntaxException> error;

    public UnquotedStringParseRule(int minSize, DelayedException<CommandSyntaxException> error) {
        this.minSize = minSize;
        this.error = error;
    }

    @Nullable
    @Override
    public String parse(ParseState<StringReader> state) {
        state.input().skipWhitespace();
        int cursor = state.mark();
        String value = state.input().readUnquotedString();
        if (value.length() < this.minSize) {
            state.errorCollector().store(cursor, this.error);
            return null;
        }
        return value;
    }
}
