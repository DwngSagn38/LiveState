package com.example.livestate.entity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Looper
import android.provider.Settings
import android.view.LayoutInflater
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.content.ContextCompat.startActivity
import com.example.livestate.R
import com.example.livestate.utils.SystemUtil
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.model.LatLng
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

object MapUtil {

    fun calculateBearing(current: LatLng, target: LatLng): Double {
        val lat1 = Math.toRadians(current.latitude)
        val lat2 = Math.toRadians(target.latitude)
        val deltaLon = Math.toRadians(target.longitude - current.longitude)
        val y = sin(deltaLon) * cos(lat2)
        val x = cos(lat1) * sin(lat2) - sin(lat1) * cos(lat2) * cos(deltaLon)
        val bearingRadians = atan2(y, x)
        var bearingDegrees = Math.toDegrees(bearingRadians)
        bearingDegrees = (bearingDegrees + 360) % 360
        return bearingDegrees
    }

    fun isLocationEnabled(context: Context): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    fun showLocationEnableDialog(context: Context): AlertDialog {
        SystemUtil.setLocale(context)

        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_location_enable, null)
        val dialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        val buttonOn = dialogView.findViewById<Button>(R.id.button_on)
        val buttonCancel = dialogView.findViewById<Button>(R.id.button_cancel)

        buttonOn.setOnClickListener {
            context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            dialog.dismiss()
        }

        buttonCancel.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
        return dialog
    }




    fun getDirectionString(currentDirection: Float): String {
        return when {
            currentDirection >= 337.5 || currentDirection < 22.5 -> "N"
            currentDirection >= 22.5 && currentDirection < 67.5 -> "NE"
            currentDirection >= 67.5 && currentDirection < 112.5 -> "E"
            currentDirection >= 112.5 && currentDirection < 157.5 -> "SE"
            currentDirection >= 157.5 && currentDirection < 202.5 -> "S"
            currentDirection >= 202.5 && currentDirection < 247.5 -> "SW"
            currentDirection >= 247.5 && currentDirection < 292.5 -> "W"
            currentDirection >= 292.5 && currentDirection < 337.5 -> "NW"
            else -> "N"
        }
    }
}