package com.example.livestate.ui.route_map

import android.content.Context
import android.location.Location
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.graphics.Typeface
import android.location.Geocoder
import android.location.LocationManager
import android.os.Bundle
import android.text.Editable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextWatcher
import android.text.style.StyleSpan
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.location.LocationManagerCompat.getCurrentLocation
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.livestate.R
import com.example.livestate.base.BaseActivity2
import com.example.livestate.databinding.ActivityRouteMapBinding
import com.example.livestate.utils.MapHelper
import com.example.livestate.widget.invisible
import com.example.livestate.widget.tap
import com.example.livestate.widget.visible
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.geojson.Point
import kotlin.coroutines.CoroutineContext

class RouteMapActivity : BaseActivity2<ActivityRouteMapBinding>(), CoroutineScope {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var currentLocation: Location? = null
    private lateinit var mapLibreMap: MapLibreMap
    private lateinit var startAdapter: ArrayAdapter<String>
    private lateinit var endAdapter: ArrayAdapter<String>
    private var isSelectingStart = false
    private var isLoading = false
    private var adress :String = ""

    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job


    override fun setViewBinding(): ActivityRouteMapBinding {
        MapHelper.initMapLibre(applicationContext)
        return ActivityRouteMapBinding.inflate(layoutInflater)
    }

    override fun initView(savedInstanceState: Bundle?) {
        adress = intent.getStringExtra("location").orEmpty()
        if (adress.isNotBlank()) {
            binding.etEnd.setText(adress)
        }
        startAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, mutableListOf())
        endAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, mutableListOf())

        binding.etStart.setAdapter(startAdapter)
        binding.etEnd.setAdapter(endAdapter)

        setupAutoSuggest(binding.etStart,startAdapter)
        setupAutoSuggest(binding.etEnd,endAdapter)

        binding.etStart.threshold = 1
        binding.etEnd.threshold = 1


        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        binding.mapView.onCreate(savedInstanceState)
        binding.mapView.getMapAsync { map ->
            mapLibreMap = map
            MapHelper.loadMapWithOSM(map) {
                requestUserLocation()
            }

            mapLibreMap.addOnMapClickListener { point ->
                val lat = point.latitude
                val lng = point.longitude

                MapHelper.reverseGeocode(lat, lng) { placeName ->
                    runOnUiThread {
                        val cleanedText = cleanUnnamedRoad(placeName) ?: "$lat,$lng"
                        if (isSelectingStart) {
                            binding.etStart.setText(cleanedText)
                            binding.etStart.clearFocus()
                        } else {
                            binding.etEnd.setText(cleanedText)
                            binding.etEnd.clearFocus()
                        }
                    }
                }


                true
            }

        }
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
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null
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
                            binding.etStart.setText(placeName ?: "$lat,$lng")
                            binding.progressBar.invisible()
                            if (adress != "" || !adress.isNullOrEmpty()){
                                lookRoad()
                            }
                        }
                    }
                }

            } else {
                // Có thể yêu cầu cập nhật vị trí hoặc báo lỗi
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1001 && grantResults.isNotEmpty() && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            requestUserLocation()
        }
    }

    override fun viewListener() {
        binding.imgBack.tap { finish() }
        binding.btnFindRoute.tap {
            lookRoad()
        }

        binding.etStart.setOnEditorActionListener { _, _, _ ->
            binding.etStart.dismissDropDown()
            hideKeyboard()
            true
        }
        binding.etEnd.setOnEditorActionListener { _, _, _ ->
            binding.etEnd.dismissDropDown()
            hideKeyboard()
            true
        }

        binding.etStart.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                isSelectingStart = true
            }
        }

        binding.etEnd.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                isSelectingStart = false
            }
        }

    }

    private fun parsePoint(text: String): Point? {
        return try {
            val parts = text.split(",").map { it.trim() }
            if (parts.size == 2) {
                Point.fromLngLat(parts[1].toDouble(), parts[0].toDouble())
            } else null
        } catch (e: Exception) {
            null
        }
    }

    override fun dataObservable() {

    }

    private fun lookRoad(){
        binding.progressBar.visible()
        hideKeyboard()
        val startText = binding.etStart.text.toString()
        val endText = binding.etEnd.text.toString()

        val startPointParsed = parsePoint(startText)
        val endPointParsed = parsePoint(endText)

        if (startPointParsed != null) {
            handleEndPointAndDrawRoute(startPointParsed, endText, endPointParsed)
        } else {
            // Nếu không parse được, thử geocode startText
            MapHelper.forwardGeocode(startText) { startPointGeocoded ->
                runOnUiThread {
                    if (startPointGeocoded != null) {
                        handleEndPointAndDrawRoute(startPointGeocoded, endText, endPointParsed)
                    } else if (currentLocation != null) {
                        // fallback cuối cùng
                        val fallbackPoint = Point.fromLngLat(currentLocation!!.longitude, currentLocation!!.latitude)
                        handleEndPointAndDrawRoute(fallbackPoint, endText, endPointParsed)

                    } else {
                        Toast.makeText(this, getString(R.string.unable_to_determine_the_starting), Toast.LENGTH_SHORT).show()
                        binding.progressBar.invisible()

                    }
                }
            }
        }
    }
    private fun requestAndDrawRoute(start: Point, end: Point) {
        MapHelper.requestRoute(start, end) { points,distance, duration->
            runOnUiThread {
                if (points != null) {
                    MapHelper.drawRoute(mapLibreMap, points)
                    MapHelper.showStartEndMarkers(mapLibreMap, this, start, end)


                    val distanceKm = String.format("%.2f", (distance ?: 0.0) / 1000)
                    val durationMin = String.format("%.0f", (duration ?: 0.0) / 60)

                    val distanceText = SpannableStringBuilder().apply {
                        append(getString(R.string.distance))
                        val start = length
                        append(": $distanceKm km")
                        setSpan(StyleSpan(Typeface.BOLD), start, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                    }

                    val durationText = SpannableStringBuilder().apply {
                        append(getString(R.string.distance))
                        val start = length
                        append(": $durationMin min")
                        setSpan(StyleSpan(Typeface.BOLD), start, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                    }

                    binding.tvDistance.text = distanceText
                    binding.tvDuration.text = durationText
                } else {
                    Toast.makeText(this, getString(R.string.route_not_found) , Toast.LENGTH_SHORT).show()
                }

                binding.progressBar.invisible()
            }
        }
    }

    private fun handleEndPointAndDrawRoute(startPoint: Point, endText: String, endPointParsed: Point?) {
        if (endPointParsed != null) {
            requestAndDrawRoute(startPoint, endPointParsed)
        } else {
            MapHelper.forwardGeocode(endText) { endPoint ->
                runOnUiThread {
                    if (endPoint != null) {
                        requestAndDrawRoute(startPoint, endPoint)
                    } else {
                        Toast.makeText(this, getString(R.string.destination_not_found) , Toast.LENGTH_SHORT).show()
                    }
                    binding.progressBar.invisible()

                }
            }
        }
    }

    private fun setupAutoSuggest(autoCompleteTextView: AutoCompleteTextView, adapter: ArrayAdapter<String>) {
        var searchJob: Job? = null

        autoCompleteTextView.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus && autoCompleteTextView.text.toString().isNotEmpty()) {
                autoCompleteTextView.showDropDown()
            }
        }

        autoCompleteTextView.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val query = s?.toString()?.trim() ?: return
                if (query.length < 2) return

                searchJob?.cancel()
                searchJob = lifecycleScope.launch {
                    delay(300) // debounce
                    val suggestions = MapHelper.fetchSuggestions(query)
                    Log.d("RouteMapActivity", "Suggestions: $suggestions")

                    runOnUiThread {
                        adapter.clear()
                        adapter.addAll(suggestions)
                        adapter.notifyDataSetChanged()

                        if (suggestions.isNotEmpty() && autoCompleteTextView.hasFocus()) {
                            autoCompleteTextView.showDropDown()
                        }
                    }
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        val view = currentFocus ?: window.decorView
        imm.hideSoftInputFromWindow(view.windowToken, 0)
        view.clearFocus()
        Log.d("RouteMapActivity", "hideKeyboard called")
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (ev.action == MotionEvent.ACTION_DOWN) {
            currentFocus?.let { v ->
                if (v is AutoCompleteTextView) {
                    val outRect = Rect()
                    v.getGlobalVisibleRect(outRect)
                    if (!outRect.contains(ev.rawX.toInt(), ev.rawY.toInt())) {
                        v.dismissDropDown() // <== QUAN TRỌNG
                        v.clearFocus()
                        hideKeyboard()
                    }
                } else {
                    hideKeyboard() // Cho các view khác (không phải AutoCompleteTextView)
                }
            }
        }
        return super.dispatchTouchEvent(ev)
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

}