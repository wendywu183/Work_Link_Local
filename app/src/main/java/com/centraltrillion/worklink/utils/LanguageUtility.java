package com.centraltrillion.worklink.utils;


import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import java.util.Locale;

public class LanguageUtility {

    private static Locale locale;
    private static String KEY_LANGUAGE = "Language";
    private static String APP_LOCALE = "APPLocale";


    public static void changeLang(Context context, String language) {
        if (language.equalsIgnoreCase(""))
            return;
        if (language.equals("TW")) {
            locale = new Locale("zh", "TW");
        } else if (language.equals("JP")) {
            locale = new Locale("ja", "JP");
        } else if (language.equals("EN")) {
            locale = new Locale("en", "US");
        } else if (language.equals("CN")) {
            locale = new Locale("zh", "CN");
        } else if (language.equals("KR")) {
            locale = new Locale("ko", "KR");
        }
        //locale = new Locale(language);
        saveLocale(context, language);
        Locale.setDefault(locale);
        android.content.res.Configuration config = new android.content.res.Configuration();
        config.locale = locale;
        context.getApplicationContext().getResources().updateConfiguration(config, context.getApplicationContext().getResources().getDisplayMetrics());
    }

    public static void saveLocale(Context context, String language) {
        SharedPreferences prefs = context.getSharedPreferences(APP_LOCALE, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_LANGUAGE, language);
        editor.commit();
    }

    public static void loadLocale(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(APP_LOCALE, Activity.MODE_PRIVATE);
        String language = prefs.getString(KEY_LANGUAGE, "");
        changeLang(context, language);
    }

    public static String readLanguage(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(APP_LOCALE, Activity.MODE_PRIVATE);
        String language = prefs.getString(KEY_LANGUAGE, "");
        return language;
    }
}

