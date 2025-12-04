package net.momirealms.craftengine.core.plugin.locale;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.translation.Translator;
import net.momirealms.craftengine.core.plugin.Manageable;
import net.momirealms.craftengine.core.plugin.config.ConfigParser;
import net.momirealms.craftengine.core.plugin.text.minimessage.IndexedArgumentTag;
import net.momirealms.craftengine.core.util.AdventureHelper;
import org.incendo.cloud.suggestion.Suggestion;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public interface TranslationManager extends Manageable {
    Set<String> ALL_LANG = Set.of(
            "af_za", "ar_sa", "ast_es", "az_az", "ba_ru", "bar", "be_by", "be_latn",
            "bg_bg", "br_fr", "brb", "bs_ba", "ca_es", "cs_cz", "cy_gb", "da_dk",
            "de_at", "de_ch", "de_de", "el_gr", "en_au", "en_ca", "en_gb", "en_nz",
            "en_pt", "en_ud", "en_us", "enp", "enws", "eo_uy", "es_ar", "es_cl",
            "es_ec", "es_es", "es_mx", "es_uy", "es_ve", "esan", "et_ee", "eu_es",
            "fa_ir", "fi_fi", "fil_ph", "fo_fo", "fr_ca", "fr_fr", "fra_de", "fur_it",
            "fy_nl", "ga_ie", "gd_gb", "gl_es", "haw_us", "he_il", "hi_in", "hn_no",
            "hr_hr", "hu_hu", "hy_am", "id_id", "ig_ng", "io_en", "is_is", "isv",
            "it_it", "ja_jp", "jbo_en", "ka_ge", "kk_kz", "kn_in", "ko_kr", "ksh",
            "kw_gb", "ky_kg", "la_la", "lb_lu", "li_li", "lmo", "lo_la", "lol_us", "lt_lt",
            "lv_lv", "lzh", "mk_mk", "mn_mn", "ms_my", "mt_mt", "nah", "nds_de",
            "nl_be", "nl_nl", "nn_no", "no_no", "oc_fr", "ovd", "pl_pl", "pls",
            "pt_br", "pt_pt", "qya_aa", "ro_ro", "rpr", "ru_ru", "ry_ua", "sah_sah",
            "se_no", "sk_sk", "sl_si", "so_so", "sq_al", "sr_cs", "sr_sp", "sv_se",
            "sxu", "szl", "ta_in", "th_th", "tl_ph", "tlh_aa", "tok", "tr_tr",
            "tt_ru", "tzo_mx", "uk_ua", "val_es", "vec_it", "vi_vn", "vp_vl", "yi_de",
            "yo_ng", "zh_cn", "zh_hk", "zh_tw", "zlm_arab"
    );
    List<Suggestion> ALL_LANG_SUGGESTIONS = ALL_LANG.stream().map(Suggestion::suggestion).toList();
    Map<String, List<String>> LOCALE_2_COUNTRIES = ALL_LANG.stream()
            .map(lang -> lang.split("_"))
            .filter(split -> split.length >= 2)
            .collect(Collectors.groupingBy(
                    split -> split[0],
                    Collectors.mapping(split -> split[1], Collectors.toUnmodifiableList())
            ));

    static TranslationManager instance() {
        return TranslationManagerImpl.instance;
    }

    ConfigParser[] parsers();

    default String miniMessageTranslation(String key) {
        return miniMessageTranslation(key, null);
    }

    String miniMessageTranslation(String key, @Nullable Locale locale);

    default Component render(Component component) {
        return render(component, null);
    }

    Component render(Component component, @Nullable Locale locale);

    static @Nullable Locale parseLocale(@Nullable String locale) {
        return locale == null || locale.isEmpty() ? null : Translator.parseLocale(locale);
    }

    static String formatLocale(Locale locale) {
        String language = locale.getLanguage().toLowerCase(Locale.ROOT);
        String country = locale.getCountry().toLowerCase(Locale.ROOT);
        if (country.isEmpty()) {
            return language;
        } else {
            return language + "_" + country;
        }
    }

    default String translateLog(String id, String... arguments) {
        String translation = miniMessageTranslation(id);
        if (translation == null) {
            return id;
        }
        Component deserialize = AdventureHelper.customMiniMessage().deserialize(translation, new IndexedArgumentTag(Arrays.stream(arguments).map(Component::text).toList()));
        return AdventureHelper.plainTextContent(deserialize);
    }

    Set<String> translationKeys();

    void log(String id, String... args);

    Map<String, LangData> clientLangData();

    void addClientTranslation(String langId, Map<String, String> translations);
}
