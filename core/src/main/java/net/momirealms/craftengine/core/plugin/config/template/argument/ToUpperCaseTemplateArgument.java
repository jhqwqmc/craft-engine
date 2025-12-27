package net.momirealms.craftengine.core.plugin.config.template.argument;

import net.momirealms.craftengine.core.plugin.locale.LocalizedResourceConfigException;
import net.momirealms.craftengine.core.plugin.locale.TranslationManager;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;

import java.util.Locale;
import java.util.Map;

public final class ToUpperCaseTemplateArgument implements TemplateArgument {
    public static final Key ID = Key.of("craftengine:to_upper_case");
    public static final TemplateArgumentFactory FACTORY = new Factory();
    private final String result;

    private ToUpperCaseTemplateArgument(String result) {
        this.result = result;
    }

    public static ToUpperCaseTemplateArgument toUpperCase(String result) {
        return new ToUpperCaseTemplateArgument(result.toUpperCase(Locale.ROOT));
    }

    public String result() {
        return this.result;
    }

    @Override
    public Object get(Map<String, TemplateArgument> arguments) {
        return this.result;
    }

    private static class Factory implements TemplateArgumentFactory {

        @Override
        public TemplateArgument create(Map<String, Object> arguments) {
            String text = ResourceConfigUtils.requireNonEmptyStringOrThrow(arguments.get("value"), "warning.config.template.argument.to_upper_case.missing_value");
            String localeName = arguments.containsKey("locale") ? arguments.get("locale").toString() : null;
            Locale locale = localeName != null ? TranslationManager.parseLocale(localeName) : Locale.ROOT;
            if (locale == null) {
                throw new LocalizedResourceConfigException("warning.config.template.argument.to_upper_case.invalid_locale", localeName);
            }
            return new ToUpperCaseTemplateArgument(text.toUpperCase(locale));
        }
    }
}
