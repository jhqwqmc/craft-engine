package net.momirealms.craftengine.core.plugin.context.function;

import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.Platform;
import net.momirealms.craftengine.core.plugin.context.*;
import net.momirealms.craftengine.core.plugin.context.parameter.DirectContextParameters;
import net.momirealms.craftengine.core.plugin.context.selector.PlayerSelector;
import net.momirealms.craftengine.core.plugin.context.selector.PlayerSelectors;
import net.momirealms.craftengine.core.plugin.context.text.TextProvider;
import net.momirealms.craftengine.core.plugin.context.text.TextProviders;
import net.momirealms.craftengine.core.util.MiscUtils;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class CommandFunction<CTX extends Context> extends AbstractConditionalFunction<CTX> {
    private final List<TextProvider> command;
    private final PlayerSelector<CTX> selector;
    private final boolean asPlayer;
    private final boolean asEvent;
    private final boolean asOp;

    public CommandFunction(List<Condition<CTX>> predicates, @Nullable PlayerSelector<CTX> selector, List<TextProvider> command,
                           boolean asPlayer, boolean asEvent, boolean asOp) {
        super(predicates);
        this.command = command;
        this.selector = selector;
        this.asPlayer = asPlayer;
        this.asEvent = asEvent;
        this.asOp = asOp;
    }

    @Override
    public void runInternal(CTX ctx) {
        if (this.asPlayer || this.asOp) {
            if (this.selector == null) {
                ctx.getOptionalParameter(DirectContextParameters.PLAYER)
                        .ifPresent(player -> executeCommands(
                                ctx, this.asEvent ? player::performCommandAsEvent : command1 -> player.performCommand(command1, this.asOp)
                        ));
            } else {
                for (Player viewer : this.selector.get(ctx)) {
                    RelationalContext relationalContext = ViewerContext.of(ctx, PlayerOptionalContext.of(viewer));
                    executeCommands(relationalContext, this.asEvent ? viewer::performCommandAsEvent : command1 -> viewer.performCommand(command1, this.asOp));
                }
            }
        } else {
            Platform platform = CraftEngine.instance().platform();
            for (TextProvider c : this.command) {
                platform.dispatchCommand(c.get(ctx));
            }
        }
    }

    private void executeCommands(Context ctx, Consumer<String> executor) {
        for (TextProvider c : this.command) {
            executor.accept(c.get(ctx));
        }
    }

    public static <CTX extends Context> FunctionFactory<CTX> factory(java.util.function.Function<Map<String, Object>, Condition<CTX>> factory) {
        return new Factory<>(factory);
    }

    private static class Factory<CTX extends Context> extends AbstractFactory<CTX> {

        public Factory(java.util.function.Function<Map<String, Object>, Condition<CTX>> factory) {
            super(factory);
        }

        @Override
        public Function<CTX> create(Map<String, Object> arguments) {
            Object command = ResourceConfigUtils.requireNonNullOrThrow(ResourceConfigUtils.get(arguments, "command", "commands"), "warning.config.function.command.missing_command");
            List<TextProvider> commands = MiscUtils.getAsStringList(command).stream().map(TextProviders::fromString).toList();
            boolean asPlayer = ResourceConfigUtils.getAsBoolean(arguments.getOrDefault("as-player", false), "as-player");
            boolean asEvent = ResourceConfigUtils.getAsBoolean(arguments.getOrDefault("as-event", false), "as-event");
            boolean asOp = ResourceConfigUtils.getAsBoolean(arguments.getOrDefault("as-op", false), "as-op");
            return new CommandFunction<>(getPredicates(arguments), PlayerSelectors.fromObject(arguments.get("target"), conditionFactory()), commands, asPlayer, asEvent, asOp);
        }
    }
}
