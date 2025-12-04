package net.momirealms.craftengine.core.util.snbt.parse.grammar;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.momirealms.craftengine.core.util.snbt.parse.DelayedException;
import net.momirealms.craftengine.core.util.snbt.parse.ParseState;
import net.momirealms.craftengine.core.util.snbt.parse.Rule;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class GreedyPatternParseRule implements Rule<StringReader, String> {
    private final Pattern pattern;
    private final DelayedException<CommandSyntaxException> error;

    public GreedyPatternParseRule(Pattern pattern, DelayedException<CommandSyntaxException> error) {
        this.pattern = pattern;
        this.error = error;
    }

    @Override
    public String parse(ParseState<StringReader> state) {
        StringReader input = state.input();
        String fullString = input.getString();
        Matcher matcher = this.pattern.matcher(fullString).region(input.getCursor(), fullString.length());
        if (!matcher.lookingAt()) {
            state.errorCollector().store(state.mark(), this.error);
            return null;
        }
        input.setCursor(matcher.end());
        return matcher.group(0);
    }
}
