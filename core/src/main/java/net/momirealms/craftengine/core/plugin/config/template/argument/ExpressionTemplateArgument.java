package net.momirealms.craftengine.core.plugin.config.template.argument;

import com.ezylang.evalex.Expression;
import com.ezylang.evalex.data.EvaluationValue;
import net.momirealms.craftengine.core.plugin.config.template.ArgumentString;
import net.momirealms.craftengine.core.util.Key;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public final class ExpressionTemplateArgument implements TemplateArgument {
    public static final Key ID = Key.of("craftengine:expression");
    public static final TemplateArgumentFactory FACTORY = new Factory();
    private final ArgumentString expression;
    private final ValueType valueType;

    private ExpressionTemplateArgument(String expression, ValueType valueType) {
        this.expression = ArgumentString.preParse(expression);
        this.valueType = valueType;
    }

    @Override
    public Object get(Map<String, TemplateArgument> arguments) {
        String expression = Optional.ofNullable(this.expression.get(arguments)).map(String::valueOf).orElse(null);
        if (expression == null) return null;
        try {
            return this.valueType.formatter().apply(new Expression(expression).evaluate());
        } catch (Exception e) {
            throw new RuntimeException("Failed to process expression argument: " + this.expression, e);
        }
    }

    protected enum ValueType {
        INT(e -> e.getNumberValue().intValue()),
        LONG(e -> e.getNumberValue().longValue()),
        SHORT(e -> e.getNumberValue().shortValue()),
        DOUBLE(e -> e.getNumberValue().doubleValue()),
        FLOAT(e -> e.getNumberValue().floatValue()),
        BYTE(e -> e.getNumberValue().byteValue()),
        BOOLEAN(EvaluationValue::getBooleanValue),;

        private final Function<EvaluationValue, Object> formatter;

        ValueType(Function<EvaluationValue, Object> formatter) {
            this.formatter = formatter;
        }

        public Function<EvaluationValue, Object> formatter() {
            return this.formatter;
        }
    }

    private static class Factory implements TemplateArgumentFactory {

        @Override
        public TemplateArgument create(Map<String, Object> arguments) {
            return new ExpressionTemplateArgument(
                    arguments.getOrDefault("expression", "").toString(),
                    ValueType.valueOf(arguments.getOrDefault("value-type", "double").toString().toUpperCase(Locale.ROOT))
            );
        }
    }
}
