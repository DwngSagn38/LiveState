package com.example.livestate.ui.Speedometer

import android.Manifest
import android.animation.ObjectAnimator
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.example.livestate.base.BaseActivity
import com.example.livestate.databinding.ActivitySpeedometerBinding
import com.google.android.gms.location.*

class SpeedometerActivity : BaseActivity<ActivitySpeedometerBinding>() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private var isTracking = false
    private var totalDistance = 0f
    private var maxSpeed = 0f
    private var speedSum = 0f
    private var speedCount = 0

    private var lastLocation: Location? = null

    override fun setViewBinding(): ActivitySpeedometerBinding {
        return ActivitySpeedometerBinding.inflate(layoutInflater)
    }

    override fun initView() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        requestLocationUpdates()
        updateNeedleRotation(0f)
    }

    override fun viewListener() {
        binding.ivBack.setOnClickListener {
           finish()
        }
        binding.btnStart.setOnClickListener {
            isTracking = true
            resetStats()
            requestLocationUpdates()
        }

        binding.btnStop.setOnClickListener {
            isTracking = false
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }

    override fun dataObservable() {
        // Không có data observe trong scope này, có thể thêm sau
    }

    private fun requestLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1001
            )
            return
        }

        val locationRequest = LocationRequest.create().apply {
            interval = 2000 // 2 giây
            fastestInterval = 1000
            priority = Priority.PRIORITY_HIGH_ACCURACY
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                super.onLocationResult(result)
                for (location in result.locations) {
                    updateSpeed(location)
                }
            }
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            mainLooper
        )
    }

    private var lastAngle = -90f

    private fun updateSpeed(location: Location) {
        if (!isTracking) return

        val speedInKmh = (location.speed * 3.6f).coerceIn(0f, 240f)
        binding.tvCurrentSpeed.text = String.format("%.1f", speedInKmh)

        // Update kim
        updateNeedleRotation(speedInKmh)

        // Max speed
        if (speedInKmh > maxSpeed) maxSpeed = speedInKmh

        // AVG speed
        speedSum += speedInKmh
        speedCount++

        // Distance
        lastLocation?.let { last ->
            val distance = last.distanceTo(location)
            totalDistance += distance
        }
        lastLocation = location

        // Cập nhật giao diện
        binding.tvMaxSpeed.text = "%.1f".format(maxSpeed)
        binding.tvAvgSpeed.text = "%.1f".format(if (speedCount > 0) speedSum / speedCount else 0f)
        binding.tvDistance.text = "%.2f".format(totalDistance / 1000f)
        binding.tvOdometer.text = "%.2f".format(totalDistance / 1000f)
    }




    private fun updateNeedleRotation(speedInKmh: Float) {
        val minAngle = -110f
        val maxAngle = 110f

        // Giới hạn test từ 1 đến 5 km/h
        val minTestSpeed = 0f
        val maxTestSpeed = 24f

        // Giới hạn speed để test dễ hơn
        val testSpeed = speedInKmh.coerceIn(minTestSpeed, maxTestSpeed)

        // Chuyển tốc độ test thành góc quay
        val angle = minAngle + ((testSpeed - minTestSpeed) / (maxTestSpeed - minTestSpeed)) * (maxAngle - minAngle)

        ObjectAnimator.ofFloat(binding.needleContainer, View.ROTATION, lastAngle, angle).apply {
            duration = 300
            interpolator = DecelerateInterpolator()
            start()
        }

        lastAngle = angle
    }


    private fun resetStats() {
        totalDistance = 0f
        maxSpeed = 0f
        speedSum = 0f
        speedCount = 0
        lastLocation = null
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == 1001 && grantResults.isNotEmpty()
            && grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            requestLocationUpdates()
        } else {
            Toast.makeText(this, "Bạn cần cấp quyền GPS để xem tốc độ!", Toast.LENGTH_LONG).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::fusedLocationClient.isInitialized && ::locationCallback.isInitialized) {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }
}
