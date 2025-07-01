package com.example.livestate.ui.main

import android.content.pm.PackageManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.example.livestate.base.BaseActivity
import com.example.livestate.databinding.ActivityMainBinding
import com.example.livestate.ui.earth3d.TheEarthActivity
import com.example.livestate.ui.my_location.MyLocationActivity
import com.example.livestate.ui.nearby_places.NearbyPlacesActivity
import com.example.livestate.ui.nearby_places.NearbyPlacesDetailActivity
import com.example.livestate.ui.route_map.RouteMapActivity
import com.example.livestate.ui.setting.SettingActivity
import com.example.livestate.ui.weather_activity.WeatherActivityActivity
import com.example.livestate.widget.tap

class MainActivity : BaseActivity<ActivityMainBinding>() {
    override fun setViewBinding(): ActivityMainBinding {
        return ActivityMainBinding.inflate(layoutInflater)
    }

    override fun initView() {

    }

    override fun viewListener() {

        binding.apply {
            imgSetting.tap { showActivity(SettingActivity::class.java) }
            llWeather.tap { showActivity(WeatherActivityActivity::class.java) }
            llEarth3D.tap { checkLocationPermissionThenNavigate(TheEarthActivity::class.java) }
            llRouterMap.tap { checkLocationPermissionThenNavigate(RouteMapActivity::class.java) }
            llMyLocation.tap { checkLocationPermissionThenNavigate(MyLocationActivity::class.java) }
            llNearbyPlaces.tap { checkLocationPermissionThenNavigate(NearbyPlacesActivity::class.java) }
        }
    }

    override fun dataObservable() {
    }

    private fun checkLocationPermissionThenNavigate(targetActivity: Class<*>) {
        if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            showActivity(targetActivity)
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                1001
            )
            pendingActivityClass = targetActivity
        }
    }

    private var pendingActivityClass: Class<*>? = null

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1001 && grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            pendingActivityClass?.let {
                showActivity(it)
            }
            pendingActivityClass = null
        } else {
            Toast.makeText(this, "Bạn cần cấp quyền vị trí để tiếp tục", Toast.LENGTH_SHORT).show()
        }
    }

}