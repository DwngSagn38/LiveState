package com.example.livestate.ui.nearby_places

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.livestate.R
import com.example.livestate.base.BaseActivity2
import com.example.livestate.data.DataApp
import com.example.livestate.databinding.ActivityNearbyPlacesDetailBinding
import com.example.livestate.model.NearbyPlace
import com.example.livestate.model.PlacesModel
import com.example.livestate.ui.route_map.RouteMapActivity
import com.example.livestate.utils.MapHelper
import com.example.livestate.widget.gone
import com.example.livestate.widget.invisible
import com.example.livestate.widget.tap
import com.example.livestate.widget.visible
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import org.maplibre.android.annotations.IconFactory
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.style.layers.FillLayer
import org.maplibre.android.style.layers.PropertyFactory.fillColor
import org.maplibre.android.style.layers.PropertyFactory.fillOpacity
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection
import org.maplibre.geojson.Point
import org.maplibre.geojson.Polygon

class NearbyPlacesDetailActivity : BaseActivity2<ActivityNearbyPlacesDetailBinding>() {
    private var currentLocation: Location? = null
    private lateinit var mapLibreMap: MapLibreMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var myLocation: String
    private var isLoading = false
    private lateinit var currentPlace: PlacesModel


    override fun setViewBinding(): ActivityNearbyPlacesDetailBinding {
        MapHelper.initMapLibre(applicationContext)
        return ActivityNearbyPlacesDetailBinding.inflate(layoutInflater)
    }

    override fun initView(savedInstanceState: Bundle?) {
        val id = intent.getIntExtra("idPlace", -1)
        if (id != -1){
            currentPlace = DataApp.getListPlaces(this)[id]
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        binding.mapView.onCreate(savedInstanceState)
        binding.mapView.getMapAsync { map ->
            mapLibreMap = map
            MapHelper.loadMapWithOSM(map) {
                requestUserLocation()
            }
        }
        binding.imgBack.tap { finish() }
    }

    override fun viewListener() {

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
                moveLocation(location.latitude, location.longitude,1)

                location?.let {
                    val lat = it.latitude
                    val lng = it.longitude
                    MapHelper.reverseGeocode(lat, lng) { placeName ->
                        runOnUiThread {
                            val cleanedText = cleanUnnamedRoad(placeName) ?: "$lat,$lng"
                            myLocation = cleanedText
                            drawCircleWithRadius(LatLng(lat, lng), 2500.0)
                        }
                    }
                }

                MapHelper.searchNearbyPlace(
                    location.latitude,
                    location.longitude,
                    keyword = currentPlace.name,
                    radius = 2500
                ) { nearbyPlaces  ->
                    runOnUiThread {
                        binding.progressBar.visible()
                    }

                    runOnUiThread {

                        if (nearbyPlaces.isEmpty()) {
                            Log.d("Nearby", "No nearby places found")
                            binding.tvEmpty.visible()
                            binding.lottieNotFound.visible()
                            binding.rcvPlace.invisible()
                        } else {
                            Log.d("Nearby", "List : ${nearbyPlaces}")
                            binding.tvEmpty.invisible()
                            binding.lottieNotFound.invisible()
                            binding.rcvPlace.layoutManager = LinearLayoutManager(this)
                            binding.rcvPlace.adapter = PlaceAdapter(this, nearbyPlaces,
                                onCalling = {phone ->
                                    if (phone.isNotEmpty()) {
                                        val intent = Intent(Intent.ACTION_DIAL).apply {
                                            data = Uri.parse("tel:$phone")
                                        }
                                        startActivity(intent)
                                    } else {
                                        Toast.makeText(this, getString(R.string.dont_have_number_phone), Toast.LENGTH_SHORT).show()
                                    }
                                },
                                onDuration = {
                                    val intent = Intent(this,RouteMapActivity::class.java)
                                    intent.putExtra("location", it)
                                    startActivity(intent)
                                },
                                onItemClick = {
                                    moveLocation(it.lat,it.lng,2)
                                }
                            )

                            addMarkersToMap(nearbyPlaces)
                        }
                        binding.progressBar.gone()
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

    private fun addMarkersToMap(points: List<NearbyPlace>) {
        points.forEach { point ->
            val icon = IconFactory.getInstance(this)
                .fromResource(currentPlace.icon)
            val markerOptions = MarkerOptions()
                .position(LatLng(point.lat, point.lng))
                .icon(icon)
            mapLibreMap.addMarker(markerOptions)
        }
    }

    private fun drawCircleWithRadius(center: LatLng, radiusMeters: Double) {
        val circleFeature = Feature.fromGeometry(
            createCirclePolygon(center.longitude, center.latitude, radiusMeters)
        )

        val source = GeoJsonSource("circle-source", FeatureCollection.fromFeature(circleFeature))
        mapLibreMap.style?.removeLayer("circle-layer")
        mapLibreMap.style?.removeSource("circle-source")

        mapLibreMap.style?.addSource(source)

        val fillLayer = FillLayer("circle-layer", "circle-source")
            .withProperties(
                fillColor("#228BE6"),
                fillOpacity(0.25f)
            )

        mapLibreMap.style?.addLayerBelow(fillLayer, "water") // Add below labels or any visible layer
    }

    private fun createCirclePolygon(
        centerLng: Double,
        centerLat: Double,
        radiusMeters: Double,
        points: Int = 64
    ): Polygon {
        val coords = mutableListOf<Point>()
        val earthRadius = 6371000.0
        val lat = Math.toRadians(centerLat)
        val lng = Math.toRadians(centerLng)
        val d = radiusMeters / earthRadius

        for (i in 0..points) {
            val theta = 2.0 * Math.PI * i.toDouble() / points
            val latRadians = Math.asin(
                Math.sin(lat) * Math.cos(d) +
                        Math.cos(lat) * Math.sin(d) * Math.cos(theta)
            )
            val lngRadians = lng + Math.atan2(
                Math.sin(theta) * Math.sin(d) * Math.cos(lat),
                Math.cos(d) - Math.sin(lat) * Math.sin(latRadians)
            )
            coords.add(Point.fromLngLat(Math.toDegrees(lngRadians), Math.toDegrees(latRadians)))
        }
        coords.add(coords[0]) // Close the polygon
        isLoading = true
        return Polygon.fromLngLats(listOf(coords))
    }


    private fun moveLocation(lat: Double, lng: Double, type : Int){
        if (type == 1){
            MapHelper.showUserLocation(
                mapLibreMap,
                lat,
                lng,
                BitmapFactory.decodeResource(resources, R.drawable.ic_location)
            )
        }else{
            MapHelper.moveLocation(
                mapLibreMap,
                lat,
                lng
            )
        }
    }
}