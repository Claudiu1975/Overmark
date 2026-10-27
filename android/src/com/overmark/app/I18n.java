package com.overmark.app;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

public final class I18n {
    public static String lang = "en";
    private static JSONObject messages = new JSONObject();
    private static JSONArray languages = new JSONArray();

    private I18n() {}

    public static void load(Context context) {
        if (languages.length() > 0) return;
        try {
            InputStream in = context.getAssets().open("messages.json");
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buf = new byte[4096];
            int n;
            while ((n = in.read(buf)) >= 0) out.write(buf, 0, n);
            in.close();
            JSONObject root = new JSONObject(new String(out.toByteArray(), StandardCharsets.UTF_8));
            messages = root.getJSONObject("messages");
            languages = root.getJSONArray("languages");
        } catch (Exception ignored) {
            messages = new JSONObject();
            languages = new JSONArray();
        }
    }

    public static JSONArray languages() {
        return languages;
    }

    public static String t(String key) {
        try {
            JSONObject bag = messages.optJSONObject(lang);
            if (bag != null && bag.has(key)) return bag.getString(key);
            JSONObject en = messages.optJSONObject("en");
            if (en != null && en.has(key)) return en.getString(key);
        } catch (Exception ignored) {
            /* fall through */
        }
        return key;
    }

    public static boolean rtl() {
        return "ar".equals(lang) || "fa".equals(lang) || "he".equals(lang) || "ur".equals(lang);
    }

    /** Device language on first launch. Not written until the user picks a language. */
    public static String matchDevice(Locale locale) {
        if (locale == null) return "en";
        String language = locale.getLanguage() == null ? "" : locale.getLanguage().toLowerCase(Locale.US);
        String country = locale.getCountry() == null ? "" : locale.getCountry().toUpperCase(Locale.US);
        String script = "";
        try {
            if (locale.getScript() != null) script = locale.getScript();
        } catch (Exception ignored) {
            script = "";
        }
        if ("zh".equals(language)) {
            if ("Hant".equalsIgnoreCase(script) || "TW".equals(country) || "HK".equals(country) || "MO".equals(country)) {
                return has("zh-Hant") ? "zh-Hant" : "zh";
            }
            return has("zh") ? "zh" : "en";
        }
        if ("nb".equals(language) || "nn".equals(language) || "no".equals(language)) {
            return has("nb") ? "nb" : "en";
        }
        if ("fil".equals(language) || "tl".equals(language)) {
            return has("fil") ? "fil" : "en";
        }
        if (has(language)) return language;
        return "en";
    }

    private static boolean has(String code) {
        for (int i = 0; i < languages.length(); i++) {
            JSONObject item = languages.optJSONObject(i);
            if (item != null && code.equals(item.optString("code"))) return true;
        }
        return false;
    }
}
