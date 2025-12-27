package net.momirealms.craftengine.core.plugin.context.number;

import com.ezylang.evalex.EvaluationException;
import com.ezylang.evalex.Expression;
import com.ezylang.evalex.parser.ParseException;
import net.kyori.adventure.text.Component;
import net.momirealms.craftengine.core.plugin.context.Context;
import net.momirealms.craftengine.core.util.AdventureHelper;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;

import java.util.Map;

public record ExpressionNumberProvider(String expression) implements NumberProvider {
    public static final Key ID = Key.of("craftengine:expression");
    public static final NumberProviderFactory FACTORY = new Factory();

    @Override
    public float getFloat(Context context) {
        Component resultComponent = AdventureHelper.customMiniMessage().deserialize(this.expression, context.tagResolvers());
        String resultString = AdventureHelper.plainTextContent(resultComponent);
        Expression expression = new Expression(resultString);
        try {
            return expression.evaluate().getNumberValue().floatValue();
        } catch (EvaluationException | ParseException e) {
            throw new RuntimeException("Invalid expression: " + this.expression + " -> " + resultString + " -> Cannot parse", e);
        }
    }

    @Override
    public double getDouble(Context context) {
        Component resultComponent = AdventureHelper.customMiniMessage().deserialize(this.expression, context.tagResolvers());
        String resultString = AdventureHelper.plainTextContent(resultComponent);
        Expression expression = new Expression(resultString);
        try {
            return expression.evaluate().getNumberValue().doubleValue();
        } catch (EvaluationException | ParseException e) {
            throw new RuntimeException("Invalid expression: " + this.expression + " -> " + resultString + " -> Cannot parse", e);
        }
    }

    private static class Factory implements NumberProviderFactory {

        @Override
        public NumberProvider create(Map<String, Object> arguments) {
            String value = ResourceConfigUtils.requireNonEmptyStringOrThrow(arguments.get("expression"), "warning.config.number.expression.missing_expression");
            return new ExpressionNumberProvider(value);
        }
    }
}
