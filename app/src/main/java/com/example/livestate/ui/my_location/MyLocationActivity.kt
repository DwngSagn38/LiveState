package com.example.livestate.ui.my_location

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Typeface
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.StyleSpan
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.livestate.R
import com.example.livestate.base.BaseActivity2
import com.example.livestate.databinding.ActivityMyLocationBinding
import com.example.livestate.databinding.ActivityRouteMapBinding
import com.example.livestate.utils.MapHelper
import com.example.livestate.widget.invisible
import com.example.livestate.widget.tap
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import org.maplibre.android.maps.MapLibreMap

class MyLocationActivity : BaseActivity2<ActivityMyLocationBinding>() {
    private var currentLocation: Location? = null
    private lateinit var mapLibreMap: MapLibreMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var myLocation: String
    private var isLoading = false

    override fun setViewBinding(): ActivityMyLocationBinding {
        MapHelper.initMapLibre(applicationContext)
        return ActivityMyLocationBinding.inflate(layoutInflater)
    }


    override fun initView(savedInstanceState: Bundle?) {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        binding.mapView.onCreate(savedInstanceState)
        binding.mapView.getMapAsync { map ->
            mapLibreMap = map
            MapHelper.loadMapWithOSM(map) {
                requestUserLocation()
            }
        }
    }

    override fun viewListener() {
        binding.imgBack.tap { finish() }

        binding.tvCopy.tap {
            if (isLoading){
                val lat = currentLocation?.latitude
                val lng = currentLocation?.longitude
                if (lat != null && lng != null) {
                    val locationText = "$lat,$lng"
                    val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("Copied Location", locationText)
//                val clip = ClipData.newPlainText("Copied Location", myLocation)
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(this, "${getString(R.string.coped)} $locationText", Toast.LENGTH_SHORT).show()
                } else {
//                Toast.makeText(this, "Không thể sao chép vị trí", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.tvShare.tap {
            if (isLoading){
                val lat = currentLocation?.latitude
                val lng = currentLocation?.longitude
                if (lat != null && lng != null) {
                    val locationUri = "https://www.google.com/maps/search/?api=1&query=$lat,$lng"
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_location))
                        putExtra(Intent.EXTRA_TEXT, "${getString(R.string.my_location)} $locationUri")
                    }
                    startActivity(Intent.createChooser(shareIntent, getString(R.string.share_with)))
                } else {
//                Toast.makeText(this, "Không thể chia sẻ vị trí", Toast.LENGTH_SHORT).show()
                }
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
                MapHelper.showUserLocation(
                    mapLibreMap,
                    location.latitude,
                    location.longitude,
                    BitmapFactory.decodeResource(resources, R.drawable.ic_location)
                )

                location?.let {
                    val lat = it.latitude
                    val lng = it.longitude
                    MapHelper.reverseGeocode(lat, lng) { placeName ->
                        runOnUiThread {
                            val cleanedText = cleanUnnamedRoad(placeName) ?: "$lat,$lng"
                            binding.tvMyLocation.text = cleanedText
                            boldText(binding.tvLatitude, getString(R.string.latitude), lat.toString())
                            boldText(binding.tvLongitude, getString(R.string.longitude), lng.toString())
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

    private fun boldText(view: TextView, text: String, text2: String){
        val boldText = SpannableStringBuilder().apply {
            append(text+ "\n")
            val start = length
            append(text2)
            setSpan(StyleSpan(Typeface.BOLD),0, start, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        }

        view.text = boldText
    }
}