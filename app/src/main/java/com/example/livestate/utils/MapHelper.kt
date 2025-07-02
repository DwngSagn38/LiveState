package com.example.livestate.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.util.Log
import com.example.livestate.R
import com.example.livestate.data.OpeningHoursParser
import com.example.livestate.model.NearbyPlace
import com.example.livestate.utils.helper.Contants
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okio.IOException
import org.json.JSONObject
import org.maplibre.android.MapLibre
import org.maplibre.android.WellKnownTileServer
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.Style
import org.maplibre.android.style.layers.LineLayer
import org.maplibre.android.style.layers.PropertyFactory.*
import org.maplibre.android.style.layers.RasterLayer
import org.maplibre.android.style.layers.SymbolLayer
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.android.style.sources.RasterSource
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection
import org.maplibre.geojson.LineString
import org.maplibre.geojson.Point
import java.util.Locale
import java.util.concurrent.TimeUnit

object MapHelper {

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .build()

    fun initMapLibre(context: Context) {
        MapLibre.getInstance(context, null, WellKnownTileServer.MapLibre)
    }

    fun loadMap(
        map: MapLibreMap,
        onMapReadyDone: (() -> Unit)? = null,

    ) {
        val styleBuilder = Style.Builder().fromUri(Contants.MAP_STYLE_TRAFFIC)

        map.setStyle(styleBuilder) { style ->
            onMapReadyDone?.invoke()
        }
    }

    fun loadMapWithOSM(map: MapLibreMap, onStyleLoaded: () -> Unit) {
        val styleJson = """
    {
      "version": 8,
      "sources": {
        "osm": {
          "type": "raster",
          "tiles": ["https://a.tile.openstreetmap.org/{z}/{x}/{y}.png"],
          "tileSize": 256,
          "attribution": "© OpenStreetMap contributors"
        }
      },
      "layers": [
        {
          "id": "osm-layer",
          "type": "raster",
          "source": "osm",
          "minzoom": 0,
          "maxzoom": 19
        }
      ]
    }
    """.trimIndent()

        map.setStyle(
            Style.Builder().fromJson(styleJson)
        ) {
            onStyleLoaded()
        }
    }




    fun showUserLocation(map: MapLibreMap, lat: Double, lng: Double, userLocationIcon: Bitmap) {
        Log.d("ShowUserLocation", "Showing location at: $lat, $lng")

        map.style?.apply {
            if (getImage("user-location-icon") == null) {
                addImage("user-location-icon", userLocationIcon)
            }
            removeLayer("user-location-layer")
            removeSource("user-location-source")

            addSource(GeoJsonSource("user-location-source", Feature.fromGeometry(Point.fromLngLat(lng, lat))))
            addLayer(
                SymbolLayer("user-location-layer", "user-location-source").withProperties(
                    iconImage("user-location-icon"),
                    iconSize(0.04f),
                    iconAllowOverlap(true),
                    iconIgnorePlacement(true)
                )
            )
        }

        map.animateCamera(
            org.maplibre.android.camera.CameraUpdateFactory.newLatLngZoom(
                org.maplibre.android.geometry.LatLng(lat, lng),
                15.0
            ), 1000
        )
    }

    fun moveLocation(map: MapLibreMap, lat: Double, lng: Double){
        map.animateCamera(
            org.maplibre.android.camera.CameraUpdateFactory.newLatLngZoom(
                org.maplibre.android.geometry.LatLng(lat, lng),
                15.0
            ), 1000
        )
    }

    fun drawRoute(map: MapLibreMap, points: List<Point>) {
        val feature = Feature.fromGeometry(LineString.fromLngLats(points))
        val collection = FeatureCollection.fromFeature(feature)

        map.style?.apply {
            removeLayer("route-layer")
            removeSource("route-source")
            addSource(GeoJsonSource("route-source", collection))
            addLayer(LineLayer("route-layer", "route-source").withProperties(
                lineColor(Color.BLUE),
                lineWidth(5f)
            ))
        }

        val boundsBuilder = org.maplibre.android.geometry.LatLngBounds.Builder()
        points.forEach { point ->
            boundsBuilder.include(org.maplibre.android.geometry.LatLng(point.latitude(), point.longitude()))
        }

        map.animateCamera(
            org.maplibre.android.camera.CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 100),
            1000
        )
    }

    fun requestRoute(start: Point, end: Point, callback: (List<Point>?, distance: Double?, duration: Double?) -> Unit) {
        val apiKey = "5b3ce3597851110001cf624811fa844f262c43b4810c006c4a1c5d90"
        val url = "https://api.openrouteservice.org/v2/directions/driving-car?api_key=$apiKey&start=${start.longitude()},${start.latitude()}&end=${end.longitude()},${end.latitude()}"

        okHttpClient.newCall(Request.Builder().url(url).build()).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(null,null, null)
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string() ?: return callback(null, null, null)
                try {
                    val json = JSONObject(body)
                    val feature = json.getJSONArray("features").getJSONObject(0)
                    val geometry = feature.getJSONObject("geometry")
                    val properties = feature.getJSONObject("properties")
                    val summary = properties.getJSONObject("summary")

                    val distance = summary.getDouble("distance") // meters
                    val duration = summary.getDouble("duration") // seconds

                    val coordinates = geometry.getJSONArray("coordinates")
                    val points = List(coordinates.length()) { i ->
                        val coord = coordinates.getJSONArray(i)
                        Point.fromLngLat(coord.getDouble(0), coord.getDouble(1))
                    }

                    callback(points, distance, duration)
                } catch (e: Exception) {
                    callback(null,null, null)
                }
            }
        })
    }

    fun reverseGeocode(lat: Double, lng: Double, callback: (String?) -> Unit) {
        val apiKey = "d5a1ef00759949b9a899bf58c36aa83d"
        val url = "https://api.opencagedata.com/geocode/v1/json?q=$lat+$lng&key=$apiKey"

        okHttpClient.newCall(Request.Builder().url(url).build()).enqueue(object : Callback {
            override fun onFailure(call: Call, e: java.io.IOException) {
                Log.e("ReverseGeocode", "Failed: ${e.message}")
                callback(null)
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string() ?: return callback(null)
                try {
                    val results = JSONObject(body).getJSONArray("results")
                    if (results.length() > 0) {
                        callback(results.getJSONObject(0).optString("formatted"))
                    } else {
                        callback(null)
                    }
                } catch (e: Exception) {
                    Log.e("ReverseGeocode", "Parse error: ${e.message}")
                    callback(null)
                }
            }
        })
    }

    fun forwardGeocode(placeName: String, callback: (Point?) -> Unit) {
        val apiKey = "d5a1ef00759949b9a899bf58c36aa83d"
        val url = "https://api.opencagedata.com/geocode/v1/json?q=${placeName}&key=$apiKey"

        okHttpClient.newCall(Request.Builder().url(url).build()).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("ForwardGeocode", "Failed: ${e.message}")
                callback(null)
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string() ?: return callback(null)
                try {
                    val results = JSONObject(body).getJSONArray("results")
                    if (results.length() > 0) {
                        val geometry = results.getJSONObject(0).getJSONObject("geometry")
                        val lat = geometry.getDouble("lat")
                        val lng = geometry.getDouble("lng")
                        callback(Point.fromLngLat(lng, lat))
                    } else {
                        callback(null)
                    }
                } catch (e: Exception) {
                    Log.e("ForwardGeocode", "Parse error: ${e.message}")
                    callback(null)
                }
            }
        })
    }

    suspend fun fetchSuggestions(query: String): List<String> = withContext(Dispatchers.IO) {
        val language = Locale.getDefault().language
        val apiKey = "d5a1ef00759949b9a899bf58c36aa83d"
        val url = "https://api.opencagedata.com/geocode/v1/json?q=${query.replace(" ", "+")}&key=$apiKey&language=$language"

        try {
            val request = Request.Builder().url(url).build()
            val response = okHttpClient.newCall(request).execute()
            val body = response.body?.string() ?: return@withContext emptyList()

            val json = JSONObject(body)
            val results = json.getJSONArray("results")

            List(results.length()) { i ->
                results.getJSONObject(i).optString("formatted")
            }.filter { it.isNotEmpty() }

        } catch (e: Exception) {
            Log.e("AutoSuggest", "Error: ${e.message}")
            emptyList()
        }
    }


    fun showStartEndMarkers(
        map: MapLibreMap,
        context: Context,
        start: Point,
        end: Point
    ) {
        map.style?.let { style ->
            // Thêm icon nếu chưa có
            if (style.getImage("start-icon") == null) {
                val startBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.ic_location_start)
                style.addImage("start-icon", startBitmap)
            }
            if (style.getImage("end-icon") == null) {
                val endBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.ic_location_end)
                style.addImage("end-icon", endBitmap)
            }

            // Xóa cũ
            style.removeLayer("start-layer")
            style.removeSource("start-source")
            style.removeLayer("end-layer")
            style.removeSource("end-source")

            // Thêm source và layer cho điểm start
            style.addSource(GeoJsonSource("start-source", Feature.fromGeometry(start)))
            style.addLayer(
                SymbolLayer("start-layer", "start-source").withProperties(
                    iconImage("start-icon"),
                    iconSize(0.05f),
                    iconAllowOverlap(true),
                    iconIgnorePlacement(true)
                )
            )

            // Thêm source và layer cho điểm end
            style.addSource(GeoJsonSource("end-source", Feature.fromGeometry(end)))
            style.addLayer(
                SymbolLayer("end-layer", "end-source").withProperties(
                    iconImage("end-icon"),
                    iconSize(0.05f),
                    iconAllowOverlap(true),
                    iconIgnorePlacement(true)
                )
            )
        }
    }

    fun searchNearbyPlace(
        lat: Double,
        lng: Double,
        keyword: String,
        radius: Int = 1000,
        callback: (List<NearbyPlace>) -> Unit
    ) {

        val amenityMap = mapOf(
            "hospital" to ("amenity" to "hospital"),
            "police station" to ("amenity" to "police_station"),
            "gas station" to ("amenity" to "fuel"),
            "park" to ("leisure" to "park"),
            "supermarket" to ("shop" to "supermarket"),
            "post office" to ("amenity" to "post_office"),
            "hotel" to ("tourism" to "hotel"),
            "library" to ("amenity" to "library"),
            "pharmacy" to ("amenity" to "pharmacy"),
            "coffee" to ("amenity" to "cafe"),
            "veterinary" to ("amenity" to "veterinary"),
            "atm" to ("amenity" to "atm"),
            "stadium" to ("leisure" to "stadium"),
            "school" to ("amenity" to "school"),
            "airport" to ("aeroway" to "aerodrome"),
            "train station" to ("railway" to "station"),
            "bookstore" to ("shop" to "books"),
            "bank" to ("amenity" to "bank"),
            "shopping mall" to ("shop" to "mall"),
            "cinema" to ("amenity" to "cinema"),
            "museum" to ("tourism" to "museum"),
            "temple" to ("amenity" to "place_of_worship"),
            "pizza" to ("amenity" to "restaurant"),
            "spa" to ("leisure" to "spa"),
            "zoo" to ("tourism" to "zoo"),
            "bus station" to ("railway" to "station"),
            "restaurant" to ("amenity" to "restaurant"),
        )


        val tag = amenityMap[keyword.lowercase()]
        if (tag == null) {
            Log.e("MapHelper", "Unknown keyword: $keyword")
            callback(emptyList())
            return
        }

        val (key, value) = tag
        val query = """
        [out:json];
        node(around:$radius,$lat,$lng)["$key"="$value"];
        out;
    """.trimIndent()

        val url = "https://overpass-api.de/api/interpreter"
        val body = RequestBody.create("application/x-www-form-urlencoded".toMediaType(), "data=$query")

        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()

        okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("OverpassAPI", "Failed: ${e.message}")
                callback(emptyList())
            }

            override fun onResponse(call: Call, response: Response) {
                val bodyStr = response.body?.string()
                if (bodyStr == null) {
                    callback(emptyList())
                    return
                }

                try {
                    val json = JSONObject(bodyStr)
                    val elements = json.getJSONArray("elements")

                    val places = mutableListOf<NearbyPlace>()

                    var pending = elements.length()

                    if (pending == 0) {
                        callback(emptyList())
                        return
                    }

                    for (i in 0 until elements.length()) {
                        val el = elements.getJSONObject(i)
                        val latRes = el.getDouble("lat")
                        val lonRes = el.getDouble("lon")
                        val tags = el.optJSONObject("tags")

                        val phone = tags?.optString("contact:phone")?.takeIf { it.isNotBlank() }
                            ?: tags?.optString("phone")?.takeIf { it.isNotBlank() }

                        val name = tags?.optString("name")?.takeIf { it.isNotBlank() }
                        val openingHours = tags?.optString("opening_hours")?.takeIf { it.isNotBlank() }
                        val isOpen = openingHours?.let {
                            try {
                                OpeningHoursParser.isOpenNow(it)
                            } catch (e: Exception) {
                                false
                            }
                        } ?: false

                        // Không có địa chỉ => dùng reverseGeocode
                        reverseGeocode(latRes, lonRes) { reverseAddress ->
                            places.add(NearbyPlace(name, reverseAddress ?: "Unknown location", isOpen, latRes, lonRes, phone, openingHours ))
                            pending--
                            if (pending == 0) callback(places)
                        }
                    }


                } catch (e: Exception) {
                    Log.e("OverpassAPI", "Parse error: ${e.message}")
                    callback(emptyList())
                }
            }
        })
    }


    fun buildAddressFromTags(tags: JSONObject): String? {
        return tags.optString("addr:full").takeIf { it.isNotBlank() }
            ?: listOfNotNull(
                tags.optString("addr:housenumber").takeIf { it.isNotBlank() },
                tags.optString("addr:street").takeIf { it.isNotBlank() },
                tags.optString("addr:suburb").takeIf { it.isNotBlank() },
                tags.optString("addr:district").takeIf { it.isNotBlank() },
                tags.optString("addr:city").takeIf { it.isNotBlank() }
            ).takeIf { it.isNotEmpty() }?.joinToString(", ")
    }

}
