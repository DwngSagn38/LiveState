package com.example.livestate.ui.main

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.livestate.R
import com.example.livestate.base.BaseActivity
import com.example.livestate.databinding.ActivityMainBinding
import com.example.livestate.ui.cameracompass.CameraCompassActivity
import com.example.livestate.ui.currency.CurrencyActivity
import com.example.livestate.ui.setting.SettingActivity
import com.example.livestate.ui.weather_activity.WeatherActivity
import com.example.livestate.ui.world_clock.WorldClockActivity
import com.example.livestate.widget.tap

class MainActivity : BaseActivity<ActivityMainBinding>() {

    private lateinit var requestPermissionLauncher: ActivityResultLauncher<Array<String>>

    override fun setViewBinding(): ActivityMainBinding {
        return ActivityMainBinding.inflate(layoutInflater)
    }

    override fun initView() {
        // Khởi tạo launcher xin nhiều quyền
        requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val cameraGranted = permissions[Manifest.permission.CAMERA] == true
            val locationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true

            if (cameraGranted && locationGranted) {
                showActivity(CameraCompassActivity::class.java)
            } else {
                Toast.makeText(
                    this,
                    getString(R.string.grant_both_camera_location),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    override fun viewListener() {
        binding.imgSetting.tap {
            showActivity(SettingActivity::class.java)
        }

        binding.llWeather.tap {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
            ) {
                showActivity(WeatherActivity::class.java)
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    2001
                )
            }
        }

        binding.llWorldClock.tap {
            showActivity(WorldClockActivity::class.java)
        }

        binding.llConverter.tap {
            showActivity(CurrencyActivity::class.java)
        }

        binding.llCompass.tap {
            if (hasAllPermissions()) {
                showActivity(CameraCompassActivity::class.java)
            } else {
                requestPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.CAMERA,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    )
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        // Chỉ xử lý quyền của WeatherActivity (ACCESS_FINE_LOCATION)
        if (requestCode == 2001) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showActivity(WeatherActivity::class.java)
            } else {
                Toast.makeText(
                    this,
                    getString(R.string.grant_location_permission_to_view_details),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun hasAllPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    override fun dataObservable() {}
}
