package net.momirealms.craftengine.core.attribute;

import com.ezylang.evalex.EvaluationException;
import com.ezylang.evalex.Expression;
import com.ezylang.evalex.parser.ParseException;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DamageFormula {
    public static final String ATTACKER_PREFIX = "attacker_";
    public static final String VICTIM_PREFIX = "victim_";
    private final String rawExpression;
    private final Expression expression;
    private final List<VariableBinding> bindings;

    public DamageFormula(String rawExpression, Expression expression, List<VariableBinding> bindings) {
        this.rawExpression = rawExpression;
        this.expression = expression;
        this.bindings = bindings;
    }

    public double getValue(DamageEvent event) {
        synchronized (this.expression) {
            for (VariableBinding binding : this.bindings) {
                this.expression.with(binding.name(), binding.resolve(event));
            }
            try {
                return this.expression.evaluate().getNumberValue().doubleValue();
            } catch (EvaluationException | ParseException e) {
                throw new RuntimeException("Failed to evaluate damage formula: " + this.rawExpression, e);
            }
        }
    }

    public record VariableBinding(String name, @Nullable AttributeSide side, @Nullable Attribute attribute, int fieldKind) {
        static final int KIND_ATTRIBUTE = 0;
        static final int FIELD_DAMAGE = 1;
        static final int FIELD_IS_CRITICAL = 2;

        static VariableBinding field(String name, int fieldKind) {
            return new VariableBinding(name, null, null, fieldKind);
        }

        static VariableBinding attribute(String name, AttributeSide side, Attribute attribute) {
            return new VariableBinding(name, side, attribute, KIND_ATTRIBUTE);
        }

        double resolve(DamageEvent event) {
            return switch (this.fieldKind) {
                case FIELD_DAMAGE -> event.damage();
                case FIELD_IS_CRITICAL -> event.source().isCritical() ? 1d : 0d;
                default -> event.getAttributeValue(this.side, this.attribute);
            };
        }
    }
}
