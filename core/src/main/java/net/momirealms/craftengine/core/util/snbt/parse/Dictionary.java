package net.momirealms.craftengine.core.util.snbt.parse;

import javax.annotation.Nullable;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

@SuppressWarnings("unchecked")
public class Dictionary<S> {
    private final Map<Atom<?>, Entry<S, ?>> terms = new IdentityHashMap<>();

    public <T> NamedRule<S, T> put(Atom<T> name, Rule<S, T> entry) {
        Entry<S, T> holder = (Entry<S, T>)this.terms.computeIfAbsent(name, Entry::new);
        if (holder.value != null) {
            throw new IllegalArgumentException("Trying to override rule: " + name);
        }
        holder.value = entry;
        return holder;
    }

    public <T> NamedRule<S, T> putComplex(Atom<T> name, Term<S> term, Rule.RuleAction<S, T> action) {
        return this.put(name, Rule.fromTerm(term, action));
    }

    public <T> NamedRule<S, T> put(Atom<T> name, Term<S> term, Rule.SimpleRuleAction<S, T> action) {
        return this.put(name, Rule.fromTerm(term, action));
    }

    public void checkAllBound() {
        List<? extends Atom<?>> unboundNames = this.terms.entrySet().stream()
                .filter(entry -> entry.getValue() == null)
                .map(Map.Entry::getKey)
                .toList();
        if (!unboundNames.isEmpty()) {
            throw new IllegalStateException("Unbound names: " + unboundNames);
        }
    }

    public <T> NamedRule<S, T> forward(Atom<T> name) {
        return this.getOrCreateEntry(name);
    }

    private <T> Entry<S, T> getOrCreateEntry(Atom<T> name) {
        return (Entry<S, T>)this.terms.computeIfAbsent(name, Entry::new);
    }

    public <T> Term<S> named(Atom<T> name) {
        return new Reference<>(this.getOrCreateEntry(name), name);
    }

    public <T> Term<S> namedWithAlias(Atom<T> nameToParse, Atom<T> nameToStore) {
        return new Reference<>(this.getOrCreateEntry(nameToParse), nameToStore);
    }

    static class Entry<S, T> implements NamedRule<S, T>, Supplier<String> {
        private final Atom<T> name;
        @Nullable
        Rule<S, T> value;

        private Entry(Atom<T> name) {
            this.name = name;
        }

        @Override
        public Atom<T> name() {
            return this.name;
        }

        @Override
        public Rule<S, T> value() {
            return Objects.requireNonNull(this.value, this);
        }

        @Override
        public String get() {
            return "Unbound rule " + this.name;
        }
    }

    record Reference<S, T>(Entry<S, T> ruleToParse, Atom<T> nameToStore) implements Term<S> {
        @Override
        public boolean parse(ParseState<S> state, Scope scope, Control control) {
            T result = state.parse(this.ruleToParse);
            if (result == null) {
                return false;
            }
            scope.put(this.nameToStore, result);
            return true;
        }
    }
}
