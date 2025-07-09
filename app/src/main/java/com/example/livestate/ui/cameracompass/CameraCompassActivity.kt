package com.example.livestate.ui.cameracompass

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.example.livestate.R
import com.example.livestate.base.BaseActivity
import com.example.livestate.base.BaseActivity2
import com.example.livestate.databinding.ActivityCameraCompassBinding
import com.example.livestate.entity.MapUtil
import com.example.livestate.sharePreferent.SharePrefKotlin
import com.example.livestate.utils.MapHelper
import com.example.livestate.widget.gone
import com.example.livestate.widget.invisible
import com.example.livestate.widget.tap
import com.example.livestate.widget.visible

import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import org.maplibre.android.maps.MapLibreMap
import java.io.IOException
import java.util.Locale
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

class CameraCompassActivity : BaseActivity2<ActivityCameraCompassBinding>(), SensorEventListener {
    private lateinit var locationCallback: LocationCallback

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var sensorManager: SensorManager
    private var currentAzimuth: Float = 0f
    private lateinit var targetLocation: LatLng
    private var currentLatLng: LatLng? = null
    private var previousMagneticFieldStrength = 0.0
    private val ALPHA = 0.1
    private var lastMagneticFieldStrength: Double = 0.0
    private val movementThreshold: Double = 1.0
    private var locationDialog: AlertDialog? = null
    private val axisXBuffer = FloatArray(10)
    private val axisYBuffer = FloatArray(10)
    private var index = 0
    private lateinit var mapLibreMap: MapLibreMap
    private var checkStatus = 0
    private var currentLocation: Location? = null



    private val gpsStatusReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == LocationManager.PROVIDERS_CHANGED_ACTION) {
                if (targetLocation.latitude == 0.0 && targetLocation.longitude == 0.0) {
                    binding.dot.visibility = View.GONE
//                    Toast.makeText(context, "Tọa độ là 0.0, ẩn dot", Toast.LENGTH_SHORT).show()
                    binding.tvAngle.text="0°"
                    return
                }

                if (MapUtil.isLocationEnabled(context!!)) {
                    Toast.makeText(context, R.string.gps_on, Toast.LENGTH_SHORT).show()
                    locationDialog?.dismiss()
                    locationDialog = null
                    registerSensors()
                    checkShare(targetLocation)
                } else {
                    Toast.makeText(context, R.string.gps_off, Toast.LENGTH_SHORT).show()
                    binding.dot.visibility = View.GONE
                    if (locationDialog == null) {
                        locationDialog = MapUtil.showLocationEnableDialog(context)
                    }
                }
            }
        }
    }

    override fun setViewBinding(): ActivityCameraCompassBinding {
        MapHelper.initMapLibre(applicationContext)
        return ActivityCameraCompassBinding.inflate(layoutInflater)
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding.mapView.visible()
        binding.previewView.visible()
        binding.ivBack.tap { finish() }
        val compassId = SharePrefKotlin.getSelectedCompassId(this)
        binding.imgClock.setBackgroundResource(R.drawable.img_clock6)
        Handler(Looper.getMainLooper()).post {
            startCamera()
        }
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        getCurrentLocation { currentLocation ->
            checkShare(currentLocation) // dùng luôn currentLocation làm target
        }
        binding.mapView.onCreate(savedInstanceState)
        binding.mapView.getMapAsync { map ->
            mapLibreMap = map
            MapHelper.loadMapWithOSM(map) {
                requestUserLocation()
            }
        }
    }



    fun checkShare(targetLocation: LatLng) {
        if (targetLocation.latitude == 0.0 && targetLocation.longitude == 0.0) {
            binding.dot.visibility = View.GONE
            binding.tvAngle.text="0°"
            return
        }
        getCurrentLocation { currentLocation ->
            Log.d("map1", "Vị trí hiện tại: ${currentLocation.latitude}, ${currentLocation.longitude}")
            Log.d("map1", "Tọa độ mục tiêu: ${targetLocation.latitude}, ${targetLocation.longitude}")
            val bearingDegrees = MapUtil.calculateBearing(currentLocation, targetLocation)
            Log.d("dotbug", "Góc độ hướng: $bearingDegrees")
            updateDotPosition(bearingDegrees)
            updateCompass()
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

                        }
                    }
                }
            } else {
            }
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.previewView.surfaceProvider)
            }
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview)
            } catch (exc: Exception) {
                Log.e("CameraCompassActivity", "Failed to bind camera", exc)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    @SuppressLint("MissingPermission")
    fun getCurrentLocation(callback: (LatLng) -> Unit) {
        val locationRequest = LocationRequest.create().apply {
            interval = 5000
            fastestInterval = 1000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                val location = locationResult.lastLocation
                location?.let {
                    currentLatLng = LatLng(it.latitude, it.longitude)

                    SharePrefKotlin.saveCoordinatesToSharedPreferences(
                        this@CameraCompassActivity,
                        currentLatLng!!.latitude,
                        currentLatLng!!.longitude
                    )
                    targetLocation = currentLatLng!!
                    fetchAddressFromLatLng(currentLatLng!!)
                    callback(currentLatLng!!)
                } ?: run {
                    Toast.makeText(this@CameraCompassActivity, R.string.location_not_available, Toast.LENGTH_SHORT).show()
                }
            }
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    }



    fun updateDotPosition(bearingDegrees: Double) {
        Log.d("DotRotation", "Dot is rotating to: $bearingDegrees degrees")
        val angleRadians = Math.toRadians(bearingDegrees)
        val imgClockWidth = binding.imgClock.width
        val imgClockHeight = binding.imgClock.height
        val dotSize = binding.dot.width
        val marginInDp = 20f
        val scale = resources.displayMetrics.density
        val margin = (marginInDp * scale + 0.5f).toInt()
        val radius = (imgClockWidth / 2) + margin
        val centerX = binding.imgClock.left + (imgClockWidth / 2)
        val centerY = binding.imgClock.top + (imgClockHeight / 2)
        val dotX = centerX + (radius * sin(angleRadians))
        val dotY = centerY - (radius * cos(angleRadians))
        binding.dot.x = (dotX - (dotSize / 2)).toFloat().coerceIn(
            0f,
            (binding.layout2.width - dotSize).toFloat()
        )
        binding.dot.y = (dotY - (dotSize / 2)).toFloat().coerceIn(
            0f,
            (binding.layout2.height - dotSize).toFloat()
        )
        updateAngle(bearingDegrees)
    }



    private fun updateAngle(bearingDegrees: Double) {
        var angleDifference = (bearingDegrees - currentAzimuth + 360) % 360
        if (angleDifference > 180) {
            angleDifference -= 360
        }
        val directionString = MapUtil.getDirectionString(angleDifference.toFloat())
        val displayAngle = if (angleDifference < 1 && angleDifference > -1) {
            "0°"
        } else {
            if (angleDifference < 0) {
                "${-angleDifference.toInt()}°$directionString"
            } else {
                "${angleDifference.toInt()}°$directionString"
            }
        }
        binding.tvAngle.text = displayAngle
    }


    @SuppressLint("SetTextI18n")
    private fun updateCompass() {
        binding.layout2.rotation = -currentAzimuth
        val currentDirection = (currentAzimuth + 360) % 360
        val directionString = MapUtil.getDirectionString(currentDirection)
        binding.coordinates.text = "${currentDirection.toInt()}° $directionString"
        binding.tvTrueHeading.text = "${currentDirection.toInt()}° $directionString"
        binding.tvAngle.text = "-${currentDirection.toInt()}° $directionString"
    }

    override fun onResume() {
        super.onResume()
        val filter = IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION)
        filter.addAction(Intent.ACTION_PROVIDER_CHANGED)
        registerReceiver(gpsStatusReceiver, filter)

        if (!MapUtil.isLocationEnabled(this)) {
            MapUtil.showLocationEnableDialog(this)
        } else {
            registerSensors()
            targetLocation = SharePrefKotlin.getSavedCoordinates(this)
            Log.d("map1", "Updated targetLocation: $targetLocation")
            checkShare(targetLocation)
        }
    }
    private fun registerSensors() {
        sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)?.let { sensor ->
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI)
        }
        sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)?.let { sensor ->
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI)
        }
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.let { sensor ->
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI)
        }
    }


    override fun onPause() {
        super.onPause()
        if (::fusedLocationClient.isInitialized && ::locationCallback.isInitialized) {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
        sensorManager.unregisterListener(this)
    }
    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        when (event.sensor.type) {
            Sensor.TYPE_ROTATION_VECTOR -> {
                val rotationMatrix = FloatArray(9)
                val orientationValues = FloatArray(3)
                SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                SensorManager.getOrientation(rotationMatrix, orientationValues)
                currentAzimuth = Math.toDegrees(orientationValues[0].toDouble()).toFloat()

                updateCompass()
                currentLatLng?.let { latLng ->
                    val bearingDegrees = MapUtil.calculateBearing(latLng, targetLocation)
                    updateDotPosition(bearingDegrees)
                }
            }

            Sensor.TYPE_MAGNETIC_FIELD -> {
                val magneticFieldStrength = Math.sqrt(
                    (event.values[0] * event.values[0] + event.values[1] * event.values[1] + event.values[2] * event.values[2]).toDouble()
                )
                previousMagneticFieldStrength = ALPHA * magneticFieldStrength + (1 - ALPHA) * previousMagneticFieldStrength
                if (Math.abs(previousMagneticFieldStrength - lastMagneticFieldStrength) > movementThreshold) {
                    binding.tvMagneticField.text = "%.1fµT".format(previousMagneticFieldStrength)
                    lastMagneticFieldStrength = previousMagneticFieldStrength
                }
            }

            Sensor.TYPE_ACCELEROMETER -> {
                val axisX = event.values[0]
                val axisY = event.values[1]
                axisXBuffer[index] = axisX
                axisYBuffer[index] = axisY
                index = (index + 1) % axisXBuffer.size
                val averageX = axisXBuffer.average()
                val averageY = axisYBuffer.average()

                binding.tvAxis.text = "X %.1f\nY %.1f".format(averageX, averageY)
            }
        }
    }
    private fun fetchAddressFromLatLng(latLng: LatLng) {
        val geocoder = Geocoder(this, Locale.getDefault())
        try {
            val addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0].getAddressLine(0)
                binding.tvLocation.text = address
            } else {
                binding.tvLocation.text = getString(R.string.address_not_found)
            }
        } catch (e: IOException) {
            e.printStackTrace()
            binding.tvLocation.text = getString(R.string.error_fetching_address)
        }
    }


    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    override fun viewListener() {
        binding.ivBack.setOnClickListener {
            finish()
        }
        binding.iv1.setOnClickListener {
            if (checkStatus == 1) {
                checkStatus = 0
                binding.mapView.gone()
                binding.previewView.gone()
                binding.tvNameActivity.setTextColor(R.color.black)
                binding.iv1.setImageResource(R.drawable.light)
            } else {
                checkStatus = 1
                binding.mapView.visible()
                binding.previewView.gone()
                binding.iv1.setImageResource(R.drawable.ic_x)
                binding.tvNameActivity.setTextColor(R.color.white)
                binding.iv2.setImageResource(R.drawable.ic_camera)

            }
        }
        binding.iv2.setOnClickListener {
            if (checkStatus == 2) {
                checkStatus = 0
                binding.mapView.gone()
                binding.previewView.gone()
                binding.tvNameActivity.setTextColor(R.color.black)
                binding.iv2.setImageResource(R.drawable.ic_camera)
            } else {
                checkStatus = 2
                binding.mapView.gone()
                binding.previewView.visible()
                binding.iv2.setImageResource(R.drawable.ic_x)
                binding.tvNameActivity.setTextColor(R.color.white)
                binding.iv1.setImageResource(R.drawable.light)
            }
        }
    }


    override fun dataObservable() {}




}
