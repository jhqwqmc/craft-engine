package net.momirealms.craftengine.core.plugin.config.template.argument;

import net.momirealms.craftengine.core.plugin.locale.LocalizedResourceConfigException;
import net.momirealms.craftengine.core.plugin.locale.TranslationManager;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;

import java.util.Locale;
import java.util.Map;

public final class ToLowerCaseTemplateArgument implements TemplateArgument {
    public static final Key ID = Key.of("craftengine:to_lower_case");
    public static final TemplateArgumentFactory FACTORY = new Factory();
    private final String result;

    private ToLowerCaseTemplateArgument(String result) {
        this.result = result;
    }

    public static ToLowerCaseTemplateArgument toLowerCase(String result) {
        return new ToLowerCaseTemplateArgument(result.toLowerCase(Locale.ROOT));
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
