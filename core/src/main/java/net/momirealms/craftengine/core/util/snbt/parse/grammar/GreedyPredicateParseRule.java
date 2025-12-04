package net.momirealms.craftengine.core.util.snbt.parse.grammar;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.momirealms.craftengine.core.util.snbt.parse.DelayedException;
import net.momirealms.craftengine.core.util.snbt.parse.ParseState;
import net.momirealms.craftengine.core.util.snbt.parse.Rule;

import javax.annotation.Nullable;

public abstract class GreedyPredicateParseRule implements Rule<StringReader, String> {
    private final int minSize;
    private final int maxSize;
    private final DelayedException<CommandSyntaxException> error;

    public GreedyPredicateParseRule(int minSize, DelayedException<CommandSyntaxException> error) {
        this(minSize, Integer.MAX_VALUE, error);
    }

    public GreedyPredicateParseRule(int minSize, int maxSize, DelayedException<CommandSyntaxException> error) {
        this.minSize = minSize;
        this.maxSize = maxSize;
        this.error = error;
    }

    @Nullable
    @Override
    public String parse(ParseState<StringReader> state) {
        StringReader input = state.input();
        String fullString = input.getString();
        int start = input.getCursor();
        int pos = start;

        while (pos < fullString.length() && this.isAccepted(fullString.charAt(pos)) && pos - start < this.maxSize) {
            pos++;
        }

        int length = pos - start;
        if (length < this.minSize) {
            state.errorCollector().store(state.mark(), this.error);
            return null;
        }
        input.setCursor(pos);
        return fullString.substring(start, pos);
    }

    protected abstract boolean isAccepted(char c);
}
