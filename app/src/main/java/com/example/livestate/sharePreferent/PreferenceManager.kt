package com.example.livestate.sharePreferent

import android.content.Context
import android.content.SharedPreferences


class PreferenceManager(context: Context) {
    private val sharedPref: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREF_NAME = "ghost_detector_prefs"
        private const val KEY_CHECK_SOUND = "KEY_CHECK_SOUND"
        private const val KEY_FILTER_INDEX = "KEY_FILTER_INDEX"

    }
    fun saveCheckSound(value: Boolean) {
        sharedPref.edit().putBoolean(KEY_CHECK_SOUND, value).apply()
    }

    fun saveFilter(int: Int) {
        sharedPref.edit().putInt(KEY_FILTER_INDEX, int).apply()
    }

    fun getCheckSound(): Boolean {
        return sharedPref.getBoolean(KEY_CHECK_SOUND, true)
    }
    fun getFilter(): Int {
        return sharedPref.getInt(KEY_FILTER_INDEX, 3)
    }

}

