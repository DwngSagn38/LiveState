package com.example.livestate.ui.weather_activity

import ForecastResponse
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.livestate.R
import com.example.livestate.base.BaseActivity
import com.example.livestate.databinding.ActivityWeatherActivityBinding
import com.example.livestate.model.*
import com.example.livestate.service.GeoCodingService2
import com.example.livestate.service.OpenMeteoService

import com.example.livestate.service.WeatherAPIService
import com.example.myapplication.model.GeoCodingService
import com.example.myapplication.model.GeoLocation
import com.example.myapplication.model.WeatherResponse
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import okhttp3.OkHttpClient
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class WeatherActivity : BaseActivity<ActivityWeatherActivityBinding>() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private fun createHttpClientWithUserAgent(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .header("User-Agent", "LiveStateWeatherApp/1.0 (livestate@example.com)")
                    .build()
                chain.proceed(request)
            }
            .build()
    }

    private val openMeteoService = Retrofit.Builder()
        .baseUrl("https://api.open-meteo.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(OpenMeteoService::class.java)

    private val weatherAPIService = Retrofit.Builder()
        .baseUrl("https://api.weatherapi.com/v1/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(WeatherAPIService::class.java)

    private val geoService: GeoCodingService by lazy {
        Retrofit.Builder()
            .baseUrl("https://nominatim.openstreetmap.org/")
            .client(createHttpClientWithUserAgent())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GeoCodingService::class.java)
    }

    private val geoService2: GeoCodingService2 by lazy {
        Retrofit.Builder()
            .baseUrl("https://nominatim.openstreetmap.org/")
            .client(createHttpClientWithUserAgent())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GeoCodingService2::class.java)
    }


    private val weatherApiKey = "49dd2b59043244258de75124252506"

    override fun setViewBinding() = ActivityWeatherActivityBinding.inflate(layoutInflater)

    override fun initView() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        showLoading() // 👉 Hiển thị loading khi vừa vào màn hình
        getCityFromCurrentLocation()
    }

    override fun viewListener() {
        binding.btnSearch.setOnClickListener {
            val city = binding.etCity.text.toString().trim()
            if (city.isNotEmpty()) {
                showLoading()
//                binding.weatherResultLayout.removeAllViews()
                fetchCoordinates(city)
            } else {
                Toast.makeText(this, "Vui lòng nhập tên thành phố", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun dataObservable() {}

    private fun fetchCoordinates(city: String) {
        geoService.getLocationByCityName(city).enqueue(object : Callback<List<GeoLocation>> {
            override fun onResponse(
                call: Call<List<GeoLocation>>,
                response: Response<List<GeoLocation>>
            ) {
                val location = response.body()?.firstOrNull()
                if (location != null) {
                    fetchOpenMeteoWeather(
                        location.lat.toDouble(),
                        location.lon.toDouble(),
                        location.display_name
                    )
                } else {
                    showError("❌ Không tìm thấy thành phố")
                }
            }

            override fun onFailure(call: Call<List<GeoLocation>>, t: Throwable) {
                showError("⚠️ Lỗi tìm tọa độ: ${t.message}")
            }
        })
    }
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1002 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getCityFromCurrentLocation()
        }
    }

    private fun getCityFromCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1002
            )
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val lat = location.latitude
                val lon = location.longitude

                geoService2.getCityNameFromLocation(lat, lon)
                    .enqueue(object : Callback<GeoLocation> {
                        override fun onResponse(call: Call<GeoLocation>, response: Response<GeoLocation>) {
                            if (response.isSuccessful) {
                                val geoLocation = response.body()
                                val displayName = geoLocation?.display_name
                                val cityName = displayName?.split(",")?.firstOrNull()?.trim()

                                if (!cityName.isNullOrEmpty()) {
                                    binding.etCity.setText(cityName)
                                    fetchCoordinates(cityName)
                                } else {
                                    showError("❌ Không tìm được tên thành phố từ vị trí GPS. Hãy thử lại.")
                                }
                            } else {
                                showError("❌ Lỗi khi truy cập dịch vụ định vị. Mã lỗi: ${response.code()}")
                            }

                    }
                        override fun onFailure(call: Call<GeoLocation>, t: Throwable) {
                            showError("⚠️ Lỗi khi lấy tên thành phố: ${t.message}")
                            hideLoading()
                        }
                    })

            } else {
                showError("❌ Không lấy được vị trí hiện tại")
                hideLoading()
            }
        }
    }



    private fun fetchOpenMeteoWeather(lat: Double, lon: Double, displayName: String) {
        openMeteoService.getWeather(lat, lon).enqueue(object : Callback<WeatherResponse> {
            override fun onResponse(
                call: Call<WeatherResponse>,
                response: Response<WeatherResponse>
            ) {
                val weather = response.body()
                val temp = weather?.current_weather?.temperature
                val code = weather?.current_weather?.weathercode
                    ?: return showError("❌ Không có dữ liệu thời tiết")
                val time = weather.current_weather.time

                val isDay = weather.current_weather.is_day == 1

                val header = TextView(this@WeatherActivity).apply {
                    text =
                        "📍 $displayName\n🌡 $temp°C - ${if (isDay) "Ban ngày" else "Ban đêm"}\n🕒 $time\n⛅ Mã thời tiết: $code"
                    textSize = 18f
                    setPadding(0, 16, 0, 16)
                }
//                binding.weatherResultLayout.addView(header)
                val hourly = weather.hourly
                val index =
                    findClosestTimeIndex(weather.current_weather.time, hourly?.time ?: emptyList())

                val feelsLike = hourly?.apparent_temperature?.getOrNull(index)
                val humidity = hourly?.relativehumidity_2m?.getOrNull(index)
                val pressure = hourly?.pressure_msl?.getOrNull(index)
                val cloud = hourly?.cloudcover?.getOrNull(index)
                val windGust = hourly?.windgusts_10m?.getOrNull(index)
                val precipitation = hourly?.precipitation?.getOrNull(index)
                val isRaining = precipitation != null && precipitation > 0

//                val detailView = TextView(this@WeatherActivityActivity).apply {
//                    text = buildString {
//                        appendLine("🌡 Nhiệt độ hiện tại: $temp°C")
//                        if (feelsLike != null) appendLine("🥵 Cảm giác như: $feelsLike°C")
//                        appendLine("💨 Gió: ${weather.current_weather.windspeed} km/h")
//                        if (windGust != null) appendLine("🌬 Gió giật: $windGust km/h")
//                        if (humidity != null) appendLine("💧 Độ ẩm: $humidity%")
//                        if (pressure != null) appendLine("📈 Áp suất: $pressure hPa")
//                        if (cloud != null) appendLine("☁️ Mây che phủ: $cloud%")
//                        appendLine("🌧 Mưa: ${if (isRaining) "Có ☔" else "Không"}")
//                        appendLine("📷 Trạng thái: ${getWeatherIcon(code)} (code $code)")
//                    }
//                    textSize = 16f
//                    setPadding(0, 0, 0, 16)
//                }
//                binding.weatherResultLayout.addView(detailView)
                val windSpeedMs = weather.current_weather.windspeed / 3.6
                val windGustMs = windGust?.div(3.6)
                binding.tvLocation.text = displayName
                binding.tvTempurator.text = "$temp°C"
                binding.tvWind.text = String.format("%.1f m/s", windSpeedMs)
                binding.tvHumidity.text = "${humidity ?: "--"}%"
                binding.tvPressure.text = "${pressure ?: "--"} hPa"
                binding.tvWindGusts.text =
                    if (windGustMs != null) String.format("%.1f m/s", windGustMs) else "--"
                binding.tvCloudiness.text = "${cloud ?: "--"}%"
                binding.tvVisibility.text = if (isRaining) "☔ Có mưa" else "Không"
                fetchWeatherApiDetails(displayName.split(",")[0])
            }

            override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                showError("⚠️ Lỗi thời tiết: ${t.message}")
            }
        })
    }

    private fun getWeatherIcon(code: Int): String {
        return when (code) {
            in 0..1 -> "☀️"
            2 -> "⛅"
            3 -> "☁️"
            in 45..48 -> "🌫"
            in 51..67 -> "🌦"
            in 80..82 -> "🌧"
            in 95..99 -> "⛈"
            else -> "❓"
        }
    }
    private fun showLoading() {
        binding.loadingOverlay.visibility = View.VISIBLE
    }

    private fun hideLoading() {
        binding.loadingOverlay.visibility = View.GONE
    }

    private fun findClosestTimeIndex(targetTime: String, timeList: List<String>): Int {
        val targetHour = targetTime.substring(0, 13)
        return timeList.indexOfFirst { it.startsWith(targetHour) }.takeIf { it >= 0 } ?: 0
    }

    private fun fetchWeatherApiDetails(city: String) {
        weatherAPIService.getForecast(weatherApiKey, city)
            .enqueue(object : Callback<ForecastResponse> {
                @RequiresApi(Build.VERSION_CODES.O)
                override fun onResponse(
                    call: Call<ForecastResponse>,
                    response: Response<ForecastResponse>
                ) {
                    val forecast = response.body() ?: return showError("❌ Không lấy được dữ liệu")
                    val today = forecast.forecast.forecastday.firstOrNull()?.let { day ->
                        WeatherDay(
                            date = getShortWeekday(day.date),
                            iconResId = getDrawableIconRes(day.day.condition.text),
                            temperatureText = String.format(
                                "%.2f°C",
                                (day.day.mintemp_c + day.day.maxtemp_c) / 2
                            ),
                            weatherType = day.day.condition.text
                        )
                    }
                    if (today != null) {
                        binding.tvWeatherToday.text = today.weatherType.toString()
                        binding.ivWeatherToday.setImageResource(today.iconResId)
                    }
                    val forecastDays = forecast.forecast.forecastday
                    if (forecastDays.size > 1) {
                        val dayList = forecastDays.subList(1, forecastDays.size).map { day ->
                            WeatherDay(
                                date = getShortWeekday(day.date),
                                iconResId = getDrawableIconRes(day.day.condition.text),
                                temperatureText = String.format(
                                    "%.1f°C",
                                    (day.day.mintemp_c + day.day.maxtemp_c) / 2
                                ),
                                weatherType = simplifyWeatherCondition(day.day.condition.text)
                            )
                        }
                        val adapter = WeatherDayAdapter(dayList)
                        binding.rcvNextDay.adapter = adapter
                        hideLoading()
                    }

                    binding.rcvNextDay.layoutManager = LinearLayoutManager(
                        this@WeatherActivity,
                        LinearLayoutManager.HORIZONTAL,
                        false
                    )
                }

                override fun onFailure(call: Call<ForecastResponse>, t: Throwable) {
                    showError("⚠️ Lỗi dữ liệu: ${t.message}")
                    hideLoading()
                }
            })

    }

    fun getDrawableIconRes(condition: String): Int {
        val lower = condition.lowercase()

        return when {
            "sun" in lower || "clear" in lower -> R.drawable.ic_sunny
            ("partly" in lower || "patchy" in lower) && "cloud" in lower -> R.drawable.ic_sunny_clound
            "sunny" in lower && "wind" in lower -> R.drawable.ic_sunny_wind
            ("rain" in lower || "drizzle" in lower) && "sun" in lower -> R.drawable.ic_sunny_rain
            "cloud" in lower || "overcast" in lower -> R.drawable.ic_clound
            "thunder" in lower || ("storm" in lower && "heavy" in lower) -> R.drawable.ic_rain_thunder
            "storm" in lower || "thunderstorm" in lower -> R.drawable.ic_rain_thunder_min
            "snow" in lower || "blizzard" in lower -> R.drawable.ic_snow
            "rain" in lower -> R.drawable.ic_sunny_rain
            else -> R.drawable.ic_clound
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getShortWeekday(dateStr: String): String {
        val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val date = LocalDate.parse(dateStr, inputFormatter)

        val formatter = DateTimeFormatter.ofPattern("EEE", Locale.ENGLISH)
        return date.format(formatter)
    }

    fun simplifyWeatherCondition(condition: String): String {
        val lower = condition.lowercase()

        return when {
            "sun" in lower -> "Sunny"
            "clear" in lower -> "Sunny"
            "cloud" in lower -> if ("partly" in lower || "patchy" in lower) "Cloudy" else "Cloudy"
            "overcast" in lower -> "Cloudy"
            "rain" in lower || "drizzle" in lower -> "Rain"
            "thunder" in lower || "storm" in lower -> "Thunderstorm"
            "snow" in lower || "blizzard" in lower -> "Snow"
            "fog" in lower || "mist" in lower || "haze" in lower -> "Fog"
            else -> "Unknown"
        }
    }


    private fun showError(message: String) {
        val errorView = TextView(this).apply {
            text = message
            textSize = 16f
            setPadding(0, 16, 0, 16)
        }
//        binding.weatherResultLayout.addView(errorView)
    }
}
