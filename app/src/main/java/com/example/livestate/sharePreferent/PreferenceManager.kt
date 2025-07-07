package com.example.livestate.sharePreferent

import android.content.Context
import android.content.SharedPreferences
import android.util.Log


class PreferenceManager(context: Context) {
    private val sharedPref: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREF_NAME = "ghost_detector_prefs"
        private const val KEY_FAVORITE_IDS = "KEY_FAVORITE_IDS"

    }

    fun getFavoriteIds(): MutableSet<String> {
        return sharedPref.getStringSet(KEY_FAVORITE_IDS, emptySet())?.toMutableSet() ?: mutableSetOf()
    }


    fun saveFavoriteId(id: String) {
        val ids = getFavoriteIds()
        ids.add(id)
        sharedPref.edit().putStringSet(KEY_FAVORITE_IDS, ids).apply()
        Log.d("PreferenceManager", "saveFavoriteId: $ids")
    }

    fun removeFavoriteId(id: String) {
        val ids = getFavoriteIds()
        ids.remove(id)
        sharedPref.edit().putStringSet(KEY_FAVORITE_IDS, ids).apply()
        Log.d("PreferenceManager", "removeFavoriteId: $ids")
    }


    fun isFavorite(id: String): Boolean {
        return getFavoriteIds().contains(id)
    }
}

