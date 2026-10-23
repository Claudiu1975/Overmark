package com.overmark.app;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

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
}
