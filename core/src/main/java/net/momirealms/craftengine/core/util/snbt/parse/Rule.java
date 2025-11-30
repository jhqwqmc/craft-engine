package net.momirealms.craftengine.core.util.snbt.parse;

import javax.annotation.Nullable;

public interface Rule<S, T> {
    @Nullable
    T parse(ParseState<S> state);

    static <S, T> Rule<S, T> fromTerm(Term<S> child, RuleAction<S, T> action) {
        return new WrappedTerm<>(action, child);
    }

    static <S, T> Rule<S, T> fromTerm(Term<S> child, SimpleRuleAction<S, T> action) {
        return new WrappedTerm<>(action, child);
    }

    @FunctionalInterface
    interface RuleAction<S, T> {
        @Nullable
        T run(ParseState<S> state);
    }

    @FunctionalInterface
    interface SimpleRuleAction<S, T> extends RuleAction<S, T> {
        T run(Scope ruleScope);

        @Override
        default T run(ParseState<S> state) {
            return this.run(state.scope());
        }
    }

    record WrappedTerm<S, T>(RuleAction<S, T> action, Term<S> child) implements Rule<S, T> {
        @Nullable
        @Override
        public T parse(ParseState<S> state) {
            Scope scope = state.scope();
            scope.pushFrame();

            try {
                if (!this.child.parse(state, scope, Control.UNBOUND)) {
                    return null;
                }

                return this.action.run(state);
            } finally {
                scope.popFrame();
            }
        }
    }
}
