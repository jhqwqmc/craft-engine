package net.momirealms.craftengine.core.util.snbt.parse.grammar;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.momirealms.craftengine.core.util.snbt.parse.DelayedException;
import net.momirealms.craftengine.core.util.snbt.parse.ParseState;
import net.momirealms.craftengine.core.util.snbt.parse.Rule;

import javax.annotation.Nullable;

public abstract class NumberRunParseRule implements Rule<StringReader, String> {
    private final DelayedException<CommandSyntaxException> noValueError;
    private final DelayedException<CommandSyntaxException> underscoreNotAllowedError;

    public NumberRunParseRule(DelayedException<CommandSyntaxException> noValueError, DelayedException<CommandSyntaxException> underscoreNotAllowedError) {
        this.noValueError = noValueError;
        this.underscoreNotAllowedError = underscoreNotAllowedError;
    }

    @Nullable
    @Override
    public String parse(ParseState<StringReader> state) {
        StringReader input = state.input();
        input.skipWhitespace();
        String fullString = input.getString();
        int start = input.getCursor();
        int pos = start;

        while (pos < fullString.length() && this.isAccepted(fullString.charAt(pos))) {
            pos++;
        }

        int length = pos - start;
        if (length == 0) {
            state.errorCollector().store(state.mark(), this.noValueError);
            return null;
        } else if (fullString.charAt(start) != '_' && fullString.charAt(pos - 1) != '_') {
            input.setCursor(pos);
            return fullString.substring(start, pos);
        }
        state.errorCollector().store(state.mark(), this.underscoreNotAllowedError);
        return null;
    }

    protected abstract boolean isAccepted(char c);
}
