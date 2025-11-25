package net.momirealms.craftengine.core.plugin.config.template.argument;

import net.momirealms.craftengine.core.plugin.locale.LocalizedResourceConfigException;
import net.momirealms.craftengine.core.plugin.locale.TranslationManager;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;

import java.util.Locale;
import java.util.Map;

public class ToLowerCaseTemplateArgument implements TemplateArgument {
    public static final Factory FACTORY = new Factory();
    private final String result;

    private ToLowerCaseTemplateArgument(String result) {
        this.result = result;
    }

    @Override
    public Key type() {
        return TemplateArguments.TO_LOWER_CASE;
    }

    @Override
    public Object get(Map<String, TemplateArgument> arguments) {
        return this.result;
    }

    public static class Factory implements TemplateArgumentFactory {

        @Override
        public TemplateArgument create(Map<String, Object> arguments) {
            String text = ResourceConfigUtils.requireNonEmptyStringOrThrow(arguments.get("value"), "warning.config.template.argument.to_lower_case.missing_value");
            String localeName = arguments.containsKey("locale") ? arguments.get("locale").toString() : null;
            Locale locale = localeName != null ? TranslationManager.parseLocale(localeName) : Locale.ROOT;
            if (locale == null) {
                throw new LocalizedResourceConfigException("warning.config.template.argument.to_lower_case.invalid_locale", localeName);
            }
            return new ToLowerCaseTemplateArgument(text.toLowerCase(locale));
        }
    }
}
