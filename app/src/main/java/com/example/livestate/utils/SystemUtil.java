package com.example.livestate.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;


import com.example.livestate.model.LanguageModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SystemUtil {
    private static Locale myLocale;

    // Lưu ngôn ngwux đã cài đặt
    public static void saveLocal(Context context, String lang){
            setPreLanguage(context, lang);
    }

    //load lại nooon ngữ đã lưu vaf thay đổi
    public static void setLocale(Context context) {
        String language = getPreLanguage(context);
        if (!language.equals("")) {
            changeLang(language, context);
        }
    }

    // method phục vụ cho việc thay đổi ngôn ngữ
    public static void changeLang(String lang, Context context){
        if (lang.equalsIgnoreCase(""))
            return;
        myLocale = new Locale(lang);
        saveLocal(context,lang);
        Locale.setDefault(myLocale);
        Configuration config = new Configuration();
        config.locale = myLocale;
        context.getResources().updateConfiguration(config, context.getResources().getDisplayMetrics());
    }


    public static String getPreLanguage(Context mContext) {
        SharedPreferences preferences = mContext.getSharedPreferences("data", Context.MODE_PRIVATE);
        return preferences.getString("KEY_LANGUAGE", "");
    }

    public static void setPreLanguage(Context context, String language) {
        if (language == null || language.equals("")) {
            return;
        } else {
            SharedPreferences preferences = context.getSharedPreferences("data", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("KEY_LANGUAGE", language);
            editor.apply();
        }
    }


    public static List<LanguageModel> listLanguage() {
        List<LanguageModel> list = new ArrayList<>();
        list.add(new LanguageModel("English", "en"));
        list.add(new LanguageModel("Hindi", "hi"));
        list.add(new LanguageModel("Spanish", "es"));
        list.add(new LanguageModel("Portuguese", "pt"));
        list.add(new LanguageModel("Vietnamese", "vi"));
        list.add(new LanguageModel("Japanese", "ja"));
        list.add(new LanguageModel("German", "de"));
        list.add(new LanguageModel("French", "fr"));
        list.add(new LanguageModel("Indonesian", "in"));

        return list;
    }
}
