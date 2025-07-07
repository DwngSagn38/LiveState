package com.example.livestate.sharePreferent

import android.content.Context
import android.content.Context.MODE_PRIVATE
import androidx.core.content.ContentProviderCompat.requireContext
import com.google.android.gms.maps.model.LatLng


object SharePrefKotlin {
    fun saveSelectedCompassId(context: Context, id: Int) {
        val sharedPreferences =
            context.getSharedPreferences("CompassPreferences", Context.MODE_PRIVATE)
        sharedPreferences.edit().putInt("selected_compass_id", id).apply()
    }

    fun saveSelectedBackgrId(context: Context, id: Int) {
        val sharedPreferences =
            context.getSharedPreferences("CompassPreferences", Context.MODE_PRIVATE)
        sharedPreferences.edit().putInt("selected_backgr_id", id).apply()
    }

    fun saveFlashState(context: Context, state: Boolean) {
        val sharedPreferences =
            context.getSharedPreferences("CompassPreferences", Context.MODE_PRIVATE)
        sharedPreferences.edit().putBoolean("flash_state", state).apply()
    }

    fun getSelectedCompassId(context: Context): Int {
        val sharedPreferences =
            context.getSharedPreferences("CompassPreferences", Context.MODE_PRIVATE)
        return sharedPreferences.getInt("selected_compass_id", 1)
    }

    fun getSelectedBackgrId(context: Context): Int {
        val sharedPreferences =
            context.getSharedPreferences("CompassPreferences", Context.MODE_PRIVATE)
        return sharedPreferences.getInt("selected_backgr_id", 1)
    }

    fun getSelectedFlashState(context: Context): Boolean {
        val sharedPreferences =
            context.getSharedPreferences("CompassPreferences", Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean("flash_state", false)
    }

    fun getCheckbg(context: Context): Boolean {
        val sharedPreferences =
            context.getSharedPreferences("CompassPreferences", Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean("checkbg", false)
    }

    fun saveCheckbg(context: Context, state: Boolean) {
        val sharedPreferences =
            context.getSharedPreferences("CompassPreferences", Context.MODE_PRIVATE)
        sharedPreferences.edit().putBoolean("checkbg", state).apply()
    }


    fun saveCoordinatesToSharedPreferences(context: Context, lat: Double, lon: Double) {
        val sharedPref = context.getSharedPreferences("MySharedPref", MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putString("latitude", lat.toString())
        editor.putString("longitude", lon.toString())
        editor.apply()
    }

    fun getSavedCoordinates(context: Context): LatLng {
        val sharedPrefs = context.getSharedPreferences("MySharedPref", Context.MODE_PRIVATE)
        val lat = sharedPrefs.getString("latitude", null)
        val lon = sharedPrefs.getString("longitude", null)
        return if (lat != null && lon != null) {
            LatLng(lat.toDouble(), lon.toDouble())
        } else {
            LatLng(0.0, 0.0)
        }
    }

}
