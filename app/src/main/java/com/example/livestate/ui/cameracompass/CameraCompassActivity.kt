package com.example.livestate.ui.cameracompass

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
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
import com.example.livestate.databinding.ActivityCameraCompassBinding
import com.example.livestate.entity.MapUtil
import com.example.livestate.sharePreferent.SharePrefKotlin

import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

class CameraCompassActivity : BaseActivity<ActivityCameraCompassBinding>(), SensorEventListener {

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
        return ActivityCameraCompassBinding.inflate(layoutInflater)
    }

    @SuppressLint("SuspiciousIndentation")
    override fun initView() {
        // Nhẹ - nên giữ
        targetLocation = SharePrefKotlin.getSavedCoordinates(this)
        Log.d("map1", "ta get: $targetLocation")
        val compassId = SharePrefKotlin.getSelectedCompassId(this)
        binding.imgClock.setBackgroundResource(R.drawable.img_clock6)

        // Trì hoãn startCamera để không block main thread ngay lúc start
        Handler(Looper.getMainLooper()).post {
            startCamera()
        }

        // Không vấn đề
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        // Kiểm tra nội dung hàm này
        checkShare(targetLocation)
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

            if (check.checkChooseLocation == 1) {
                binding.dot.visibility = View.VISIBLE
            }
            val bearingDegrees = MapUtil.calculateBearing(currentLocation, targetLocation)
            Log.d("dotbug", "Góc độ hướng: $bearingDegrees")
            updateDotPosition(bearingDegrees)
            updateCompass()
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
        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                val location = locationResult.lastLocation
                location?.let {
                    currentLatLng = LatLng(it.latitude, it.longitude)
                    if (currentLatLng!!.latitude == 0.0 && currentLatLng!!.longitude == 0.0) {
                        binding.dot.visibility = View.GONE
                        binding.tvAngle.text="0°"
                    } else {
                        callback(currentLatLng!!)
                        if (check.checkChooseLocation == 1) {
                            binding.dot.visibility = View.VISIBLE

                        }
                    }
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


    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    override fun viewListener() {
        binding.ivBack.setOnClickListener {
            finish()
        }

    }

    override fun dataObservable() {}




}
