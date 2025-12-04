package net.momirealms.craftengine.core.util.snbt;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.DynamicOps;
import net.momirealms.craftengine.core.util.snbt.parse.*;
import net.momirealms.sparrow.nbt.util.UUIDUtil;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class SnbtOperations {
    static final DelayedException<CommandSyntaxException> ERROR_EXPECTED_STRING_UUID = DelayedException.create(
            new LocalizedSimpleCommandExceptionType(new LocalizedMessage("warning.config.type.snbt.parser.expected_string_uuid"))
    );
    static final DelayedException<CommandSyntaxException> ERROR_EXPECTED_NUMBER_OR_BOOLEAN = DelayedException.create(
            new LocalizedSimpleCommandExceptionType(new LocalizedMessage("warning.config.type.snbt.parser.expected_number_or_boolean"))
    );
    public static final String BUILTIN_TRUE = "true";
    public static final String BUILTIN_FALSE = "false";
    public static final String BUILTIN_NULL = "null";
    public static final Map<BuiltinKey, BuiltinOperation> BUILTIN_OPERATIONS = Map.of(
            new BuiltinKey("bool", 1), new BuiltinOperation() {
                @Override
                public <T> T run(DynamicOps<T> ops, List<T> arguments, ParseState<StringReader> state) {
                    Boolean result = convert(ops, arguments.getFirst());
                    if (result == null) {
                        state.errorCollector().store(state.mark(), SnbtOperations.ERROR_EXPECTED_NUMBER_OR_BOOLEAN);
                        return null;
                    }
                    return ops.createBoolean(result);
                }

                @Nullable
                private static <T> Boolean convert(DynamicOps<T> ops, T arg) {
                    Optional<Boolean> asBoolean = ops.getBooleanValue(arg).result();
                    if (asBoolean.isPresent()) {
                        return asBoolean.get();
                    } else {
                        Optional<Number> asNumber = ops.getNumberValue(arg).result();
                        return asNumber.isPresent() ? asNumber.get().doubleValue() != 0.0 : null;
                    }
                }
            }, new BuiltinKey("uuid", 1), new BuiltinOperation() {
                @Override
                public <T> T run(DynamicOps<T> ops, List<T> arguments, ParseState<StringReader> state) {
                    Optional<String> arg = ops.getStringValue(arguments.getFirst()).result();
                    if (arg.isEmpty()) {
                        state.errorCollector().store(state.mark(), SnbtOperations.ERROR_EXPECTED_STRING_UUID);
                        return null;
                    }
                    UUID uuid;
                    try {
                        uuid = UUID.fromString(arg.get());
                    } catch (IllegalArgumentException var7) {
                        state.errorCollector().store(state.mark(), SnbtOperations.ERROR_EXPECTED_STRING_UUID);
                        return null;
                    }

                    return ops.createIntList(IntStream.of(UUIDUtil.uuidToIntArray(uuid)));
                }
            }
    );
    public static final SuggestionSupplier<StringReader> BUILTIN_IDS = new SuggestionSupplier<>() {
        private final Set<String> keys = Stream.concat(
                        Stream.of(BUILTIN_FALSE, BUILTIN_TRUE, BUILTIN_NULL), SnbtOperations.BUILTIN_OPERATIONS.keySet().stream().map(BuiltinKey::id)
                )
                .collect(Collectors.toSet());

        @Override
        public Stream<String> possibleValues(ParseState<StringReader> state) {
            return this.keys.stream();
        }
    };

    public record BuiltinKey(String id, int argCount) {
        @Override
        public @NotNull String toString() {
            return this.id + "/" + this.argCount;
        }
    }

    public interface BuiltinOperation {
        @Nullable
        <T> T run(DynamicOps<T> ops, List<T> arguments, ParseState<StringReader> state);
    }
}
