package net.momirealms.craftengine.core.plugin.locale;

import net.momirealms.craftengine.core.plugin.CraftEngine;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;

public class LangData {
    private static final Map<String, Function<String, List<String>>> LANG_KEY_PROCESSORS = new HashMap<>();
    public Map<String, String> translations = new LinkedHashMap<>();

    static {
        LANG_KEY_PROCESSORS.put("block_name", (id) -> List.of("block." + id.replace(":", ".")));
    }

    public void processTranslations() {
        Map<String, String> temp = new LinkedHashMap<>(Math.max(10, this.translations.size()));
        for (Map.Entry<String, String> entry : this.translations.entrySet()) {
            String key = entry.getKey();
            String[] split = key.split(":", 2);
            if (split.length == 2) {
                Optional.ofNullable(LANG_KEY_PROCESSORS.get(split[0]))
                        .ifPresentOrElse(processor -> {
                                    for (String result : processor.apply(split[1])) {
                                        temp.put(result, entry.getValue());
                                    }
                                },
                                () -> CraftEngine.instance().logger().warn("Unknown lang type: " + key)
                        );
            } else {
                temp.put(key, entry.getValue());
            }
        }
        this.translations = temp;
    }

    public void addTranslations(Map<String, String> data) {
        this.translations.putAll(data);
    }

    public void addTranslation(String key, String value) {
        this.translations.put(key, value);
    }

    @Nullable
    public String translate(String key) {
        return this.translations.get(key);
    }

    @Override
    public String toString() {
        return "LangData{" + translations + "}";
    }

    public static void merge(Map<String, LangData> target, Map<String, LangData> source) {
        source.forEach((key, value) -> {
            LangData copy = new LangData();
            copy.addTranslations(value.translations);
            target.merge(key, copy, (existing, newData) -> {
                existing.addTranslations(newData.translations);
                return existing;
            });
        });
    }
}
