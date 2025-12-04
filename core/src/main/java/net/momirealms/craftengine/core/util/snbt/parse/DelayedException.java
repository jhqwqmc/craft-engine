package net.momirealms.craftengine.core.util.snbt.parse;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.momirealms.craftengine.core.util.snbt.parse.grammar.StringReaderTerms;

@FunctionalInterface
public interface DelayedException<T extends Exception> {
    T create(String contents, int position);

    static DelayedException<CommandSyntaxException> create(SimpleCommandExceptionType type) {
        return (contents, position) -> type.createWithContext(StringReaderTerms.createReader(contents, position));
    }

    static DelayedException<CommandSyntaxException> create(DynamicCommandExceptionType type, String argument) {
        return (contents, position) -> type.createWithContext(StringReaderTerms.createReader(contents, position), argument);
    }
}
