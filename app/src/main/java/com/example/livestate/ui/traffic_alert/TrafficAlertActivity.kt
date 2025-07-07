package com.example.livestate.ui.traffic_alert

import android.content.Context
import android.graphics.BitmapFactory
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.livestate.R
import com.example.livestate.base.BaseActivity2
import com.example.livestate.databinding.ActivityTrafficAlertBinding
import com.example.livestate.utils.MapHelper
import com.example.livestate.widget.invisible
import com.example.livestate.widget.tap
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.maps.MapLibreMap

class TrafficAlertActivity : BaseActivity2<ActivityTrafficAlertBinding>() {
    private var currentLocation: Location? = null
    private lateinit var mapLibreMap: MapLibreMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var myLocation: String
    private var isLoading = false
    private var isLightStyle = false

    override fun setViewBinding(): ActivityTrafficAlertBinding {
        MapHelper.initMapLibre(applicationContext)
        return ActivityTrafficAlertBinding.inflate(layoutInflater)
    }

    override fun initView(savedInstanceState: Bundle?) {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        binding.mapView.onCreate(savedInstanceState)
        binding.mapView.getMapAsync { map ->
            mapLibreMap = map
            MapHelper.loadMapWithOSMTraffic(this@TrafficAlertActivity,isLightStyle,map) {
                requestUserLocation()
            }
        }
    }

    override fun viewListener() {
        binding.imgBack.tap { finish() }
        binding.imgMyLocation.tap { moveLocation(currentLocation!!.latitude,currentLocation!!.longitude) }
        binding.imgLight.tap {
            isLightStyle = !isLightStyle
            MapHelper.loadMapWithOSMTraffic(this@TrafficAlertActivity,isLightStyle,mapLibreMap){}
            if(isLightStyle){
                binding.imgLight.setImageResource(R.drawable.ic_moon)
            }else{
                binding.imgLight.setImageResource(R.drawable.ic_light)
            }
        }

        binding.imgAddZoom.setOnClickListener {
            val currentZoom = mapLibreMap.cameraPosition.zoom
            if (currentZoom < mapLibreMap.maxZoomLevel) {
                mapLibreMap.animateCamera(CameraUpdateFactory.zoomTo(currentZoom + 1))
            }
        }

        binding.imgMinusZoom.setOnClickListener {
            val currentZoom = mapLibreMap.cameraPosition.zoom
            if (currentZoom > mapLibreMap.minZoomLevel) {
                mapLibreMap.animateCamera(CameraUpdateFactory.zoomTo(currentZoom - 1))
            }
        }
    }

    override fun dataObservable() {

    }
    private fun requestUserLocation() {
        if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 1001)
            return
        }

        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        if (!isGpsEnabled) {
            Toast.makeText(this, getString(R.string.turn_on_gps), Toast.LENGTH_SHORT).show()
            return
        }

        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000).build()
        fusedLocationClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY, null
        ).addOnSuccessListener { location ->
            if (location != null) {
                currentLocation = location
                moveLocation(location.latitude,location.longitude)

                location?.let {
                    val lat = it.latitude
                    val lng = it.longitude
                    MapHelper.reverseGeocode(lat, lng) { placeName ->
                        runOnUiThread {
                            val cleanedText = cleanUnnamedRoad(placeName) ?: "$lat,$lng"
                            myLocation = cleanedText
                            binding.progressBar.invisible()
                            isLoading = true
                        }
                    }
                }

            } else {
                // Có thể yêu cầu cập nhật vị trí hoặc báo lỗi
            }
        }
    }

    private fun cleanUnnamedRoad(address: String?): String? {
        if (address == null) return null
        val parts = address.split(",").map { it.trim() }
        return if (parts.isNotEmpty() && parts[0].equals("unnamed road", ignoreCase = true)) {
            parts.drop(1).joinToString(", ")
        } else {
            address
        }
    }

    private fun moveLocation(lat: Double, lng: Double){
        MapHelper.showUserLocation(
            mapLibreMap,
            lat,
            lng,
            BitmapFactory.decodeResource(resources, R.drawable.ic_location)
        )
    }

}