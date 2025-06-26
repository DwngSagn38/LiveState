package com.example.livestate.sharePreferent;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class SharePrefUtils {
    private static final String PREF_NAME = "data";

    private static final String KEY_SELECTED_OGG = "selected_ogg_file";

    private static SharedPreferences mPreferences;

    public static void init(Context context) {
        if (mPreferences == null) {
            mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        }
    }
    public static void saveSelectedOggFile(Context context, String fileName) {
        SharedPreferences pre = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pre.edit();
        editor.putString(KEY_SELECTED_OGG, fileName);
        editor.apply();
    }
    public static String getSelectedOggFile(Context context) {
        SharedPreferences pre = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return pre.getString(KEY_SELECTED_OGG, null);
    }

    public static boolean isRated(Context context) {
        SharedPreferences pre = context.getSharedPreferences("data", Context.MODE_PRIVATE);
        return pre.getBoolean("rated", false);
    }

    public static boolean isGoToMain(Context context) {
        SharedPreferences pre = context.getSharedPreferences("data", Context.MODE_PRIVATE);
        return pre.getBoolean("GoToMain", false);
    }

    public static void forceGoToMain(Context context) {
        SharedPreferences pre = context.getSharedPreferences("data", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pre.edit();
        editor.putBoolean("GoToMain", true);
        editor.commit();
    }

    public static void forceRated(Context context) {
        SharedPreferences pre = context.getSharedPreferences("data", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pre.edit();
        editor.putBoolean("rated", true);
        editor.commit();
    }

    public static boolean isHome(Context context) {
        SharedPreferences pre = context.getSharedPreferences("data", Context.MODE_PRIVATE);
        return pre.getBoolean("home", false);
    }

    public static void forceHome(Context context) {
        SharedPreferences pre = context.getSharedPreferences("data", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pre.edit();
        editor.putBoolean("home", true);
        editor.commit();
    }

    public static int getSplashOpenCount(Context context) {
        SharedPreferences pre = context.getSharedPreferences("data", Context.MODE_PRIVATE);
        return pre.getInt("splash_open", 1);
    }

    public static void incrementSplashOpenCount(Context context) {
        SharedPreferences pre = context.getSharedPreferences("data", Context.MODE_PRIVATE);
        int currentCount = getSplashOpenCount(context);
        if (currentCount <= 10) {
            pre.edit().putInt("splash_open", currentCount + 1).apply();
        }
    }


}
