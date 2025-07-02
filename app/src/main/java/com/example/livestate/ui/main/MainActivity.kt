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
import com.example.livestate.ui.Speedometer.SpeedometerActivity
import com.example.livestate.ui.cameracompass.CameraCompassActivity
import com.example.livestate.ui.currency.CurrencyActivity
import com.example.livestate.ui.earth3d.TheEarthActivity
import com.example.livestate.ui.my_location.MyLocationActivity
import com.example.livestate.ui.nearby_places.NearbyPlacesActivity
import com.example.livestate.ui.nearby_places.NearbyPlacesDetailActivity
import com.example.livestate.ui.route_map.RouteMapActivity
import com.example.livestate.ui.cameracompass.CameraCompassActivity
import com.example.livestate.ui.currency.CurrencyActivity
import com.example.livestate.ui.setting.SettingActivity
import com.example.livestate.ui.traffic_alert.TrafficAlertActivity
import com.example.livestate.widget.tap
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

        binding.apply {
            imgSetting.tap { showActivity(SettingActivity::class.java) }
            llWeather.tap { showActivity(WeatherActivity::class.java) }
            llEarth3D.tap { checkLocationPermissionThenNavigate(TheEarthActivity::class.java) }
            llRouterMap.tap { checkLocationPermissionThenNavigate(RouteMapActivity::class.java) }
            llMyLocation.tap { checkLocationPermissionThenNavigate(MyLocationActivity::class.java) }
            llNearbyPlaces.tap { checkLocationPermissionThenNavigate(NearbyPlacesActivity::class.java) }
            llTraffic.tap { checkLocationPermissionThenNavigate(TrafficAlertActivity::class.java) }
            imgSetting.tap {showActivity(SettingActivity::class.java) }
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

        binding.llSpeedometer.tap {
            showActivity(SpeedometerActivity::class.java)
        }

        binding.llCompass.tap {
            if (hasAllPermissions()) {
                showActivity(CameraCompassActivity::class.java)
            } else {
                requestPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.CAMERA,
                    )
                )
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

    private fun checkLocationPermissionThenNavigate(targetActivity: Class<*>) {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            showActivity(targetActivity)
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1001
            )
            pendingActivityClass = targetActivity
        }
    }

    private var pendingActivityClass: Class<*>? = null

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 2001 && grantResults.isNotEmpty()
            && grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            showActivity(WeatherActivity::class.java)
        } else {
            Toast.makeText(
                this,
                getString(R.string.grant_location_permission_to_view_details),
                Toast.LENGTH_SHORT
            ).show()
            if (requestCode == 1001 && grantResults.isNotEmpty() &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED
            ) {
                pendingActivityClass?.let {
                    showActivity(it)
                }
                pendingActivityClass = null
            } else {
                Toast.makeText(
                    this,
                    getString(R.string.grant_location_permission_to_view_details),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }



    override fun dataObservable() {}
}
