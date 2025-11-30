package net.momirealms.craftengine.core.util.snbt.parse;

import javax.annotation.Nullable;
import java.util.Optional;

public interface ParseState<S> {
    Scope scope();

    ErrorCollector<S> errorCollector();

    default <T> Optional<T> parseTopRule(NamedRule<S, T> rule) {
        T result = this.parse(rule);
        if (result != null) {
            this.errorCollector().finish(this.mark());
        }

        if (!this.scope().hasOnlySingleFrame()) {
            throw new IllegalStateException("Malformed scope: " + this.scope());
        }
        return Optional.ofNullable(result);
    }

    @Nullable
    <T> T parse(NamedRule<S, T> rule);

    S input();

    int mark();

    void restore(int mark);

    Control acquireControl();

    void releaseControl();

    ParseState<S> silent();
}
