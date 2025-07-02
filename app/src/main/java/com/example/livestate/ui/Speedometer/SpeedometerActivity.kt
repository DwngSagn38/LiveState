package com.example.livestate.ui.Speedometer

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import com.example.livestate.base.BaseActivity
import com.example.livestate.databinding.ActivitySpeedometerBinding
import com.google.android.gms.location.*

class SpeedometerActivity : BaseActivity<ActivitySpeedometerBinding>() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    // Xin quyền runtime
    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startLocationUpdates()
        } else {
            Toast.makeText(this, "Vui lòng cấp quyền vị trí để sử dụng tính năng đo tốc độ.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun setViewBinding(): ActivitySpeedometerBinding {
        return ActivitySpeedometerBinding.inflate(layoutInflater)
    }

    override fun initView() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Kiểm tra quyền
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            locationPermissionRequest.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            startLocationUpdates()
        }
    }

    override fun viewListener() {
        // Thêm listener nếu có nút điều khiển
    }

    override fun dataObservable() {
        // Dùng để observe LiveData nếu cần
    }

    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.create().apply {
            interval = 2000
            fastestInterval = 1000
            priority = Priority.PRIORITY_HIGH_ACCURACY
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                super.onLocationResult(result)
                for (location in result.locations) {
                    val speedMps = location.speed     // đơn vị: m/s
                    val speedKmh = speedMps * 3.6     // đổi sang km/h

                    // Cập nhật giao diện đồng hồ tốc độ
                    binding.speedView.speedTo(speedKmh.toFloat())
                }
            }
        }

        // Chạy cập nhật vị trí
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                mainLooper
            )
        }
    }

    override fun onPause() {
        super.onPause()
        if (::fusedLocationClient.isInitialized && ::locationCallback.isInitialized) {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }
}
