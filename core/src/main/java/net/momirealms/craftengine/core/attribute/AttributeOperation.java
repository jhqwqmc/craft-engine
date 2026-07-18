package net.momirealms.craftengine.core.attribute;

import com.ezylang.evalex.EvaluationException;
import com.ezylang.evalex.Expression;
import com.ezylang.evalex.parser.ParseException;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.config.KnownResourceException;
import net.momirealms.craftengine.core.util.Key;

public interface AttributeOperation {

    Key id();

    int order();

    double apply(double phaseBase, double current, double amount);

    static AttributeOperation of(Key id, int order, ApplyFunction function) {
        return new AttributeOperation() {
            @Override
            public Key id() {
                return id;
            }

            @Override
            public int order() {
                return order;
            }

            @Override
            public double apply(double phaseBase, double current, double amount) {
                return function.apply(phaseBase, current, amount);
            }

            @Override
            public String toString() {
                return "AttributeOperation{" + id.asString() + "}";
            }
        };
    }

    static AttributeOperation expression(Key id, int order, String rawExpression) {
        Expression expression = new Expression(rawExpression);
        try {
            expression.with("base", 0d).with("current", 0d).with("amount", 0d).evaluate();
        } catch (EvaluationException | ParseException e) {
            throw new KnownResourceException("TODO", id.asString(), rawExpression);
        } catch (ArithmeticException ignored) {
            // 零值探针触发的数学域错误（如除零）不代表表达式非法
        }
        return of(id, order, (base, current, amount) -> {
            synchronized (expression) {
                try {
                    return expression
                            .with("base", base)
                            .with("current", current)
                            .with("amount", amount)
                            .evaluate().getNumberValue().doubleValue();
                } catch (EvaluationException | ParseException | ArithmeticException e) {
                    CraftEngine.instance().logger().warn("Failed to evaluate attribute operation '" + id.asString() + "': " + rawExpression + " (" + e.getMessage() + ")");
                    return current;
                }
            }
        });
    }

    @FunctionalInterface
    interface ApplyFunction {

        double apply(double phaseBase, double current, double amount);
    }
}
