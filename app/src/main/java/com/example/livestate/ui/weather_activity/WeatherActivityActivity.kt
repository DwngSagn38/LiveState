package com.example.livestate.ui.weather_activity

import ForecastResponse
import android.os.Bundle
import android.widget.*
import com.bumptech.glide.Glide
import com.example.livestate.base.BaseActivity
import com.example.livestate.databinding.ActivityWeatherActivityBinding
import com.example.livestate.model.*
import com.example.livestate.service.OpenMeteoService

import com.example.livestate.service.WeatherAPIService
import com.example.myapplication.model.GeoCodingService
import com.example.myapplication.model.GeoLocation
import com.example.myapplication.model.WeatherResponse
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory

class WeatherActivityActivity : BaseActivity<ActivityWeatherActivityBinding>() {

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

    private val geoService = Retrofit.Builder()
        .baseUrl("https://nominatim.openstreetmap.org/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(GeoCodingService::class.java)

    private val weatherApiKey = "49dd2b59043244258de75124252506"

    override fun setViewBinding() = ActivityWeatherActivityBinding.inflate(layoutInflater)

    override fun initView() {}

    override fun viewListener() {
        binding.btnSearch.setOnClickListener {
            val city = binding.etCity.text.toString().trim()
            if (city.isNotEmpty()) {
                binding.weatherResultLayout.removeAllViews()
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

    private fun fetchOpenMeteoWeather(lat: Double, lon: Double, displayName: String) {
        openMeteoService.getWeather(lat, lon).enqueue(object : Callback<WeatherResponse> {
            override fun onResponse(
                call: Call<WeatherResponse>,
                response: Response<WeatherResponse>
            ) {
                val weather = response.body()
                val code = weather?.current_weather?.weathercode
                    ?: return showError("❌ Không có dữ liệu thời tiết")
                val time = weather.current_weather.time
                val temp = weather.current_weather.temperature
                val isDay = weather.current_weather.is_day == 1

                val header = TextView(this@WeatherActivityActivity).apply {
                    text =
                        "📍 $displayName\n🌡 $temp°C - ${if (isDay) "Ban ngày" else "Ban đêm"}\n🕒 $time\n⛅ Mã thời tiết: $code"
                    textSize = 18f
                    setPadding(0, 16, 0, 16)
                }
                binding.weatherResultLayout.addView(header)
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

                val detailView = TextView(this@WeatherActivityActivity).apply {
                    text = buildString {
                        appendLine("🌡 Nhiệt độ hiện tại: $temp°C")
                        if (feelsLike != null) appendLine("🥵 Cảm giác như: $feelsLike°C")
                        appendLine("💨 Gió: ${weather.current_weather.windspeed} km/h")
                        if (windGust != null) appendLine("🌬 Gió giật: $windGust km/h")
                        if (humidity != null) appendLine("💧 Độ ẩm: $humidity%")
                        if (pressure != null) appendLine("📈 Áp suất: $pressure hPa")
                        if (cloud != null) appendLine("☁️ Mây che phủ: $cloud%")
                        appendLine("🌧 Mưa: ${if (isRaining) "Có ☔" else "Không"}")
                        appendLine("📷 Trạng thái: ${getWeatherIcon(code)} (code $code)")
                    }
                    textSize = 16f
                    setPadding(0, 0, 0, 16)
                }
                binding.weatherResultLayout.addView(detailView)

                // Gọi thêm API WeatherAPI để lấy ảnh + mô tả
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

    private fun findClosestTimeIndex(targetTime: String, timeList: List<String>): Int {
        val targetHour = targetTime.substring(0, 13)
        return timeList.indexOfFirst { it.startsWith(targetHour) }.takeIf { it >= 0 } ?: 0
    }

    private fun fetchWeatherApiDetails(city: String) {
        weatherAPIService.getForecast(weatherApiKey, city)
            .enqueue(object : Callback<ForecastResponse> {
                override fun onResponse(
                    call: Call<ForecastResponse>,
                    response: Response<ForecastResponse>
                ) {
                    val forecast =
                        response.body() ?: return showError("❌ Không lấy được dữ liệu mô tả")

                    for (day in forecast.forecast.forecastday) {
                        val row = LinearLayout(this@WeatherActivityActivity).apply {
                            orientation = LinearLayout.HORIZONTAL
                            setPadding(0, 12, 0, 12)
                        }

                        val iconView = ImageView(this@WeatherActivityActivity).apply {
                            Glide.with(this@WeatherActivityActivity)
                                .load("https:${day.day.condition.icon}")
                                .into(this)
                            layoutParams = LinearLayout.LayoutParams(120, 120)
                        }

                        val info = TextView(this@WeatherActivityActivity).apply {
                            text = """
        🗓 ${day.date}
        🌡 Nhiệt độ: ${day.day.mintemp_c}°C - ${day.day.maxtemp_c}°C
        🌤 ${day.day.condition.text}
        🌧 Mưa: ${day.day.daily_chance_of_rain}%
        ❄️ Tuyết: ${day.day.daily_chance_of_snow}%
    """.trimIndent()
                            textSize = 16f
                            setPadding(16, 0, 0, 0)
                        }


                        row.addView(iconView)
                        row.addView(info)
                        binding.weatherResultLayout.addView(row)
                    }
                }

                override fun onFailure(call: Call<ForecastResponse>, t: Throwable) {
                    showError("⚠️ Lỗi dữ liệu chi tiết: ${t.message}")
                }
            })
    }

    private fun showError(message: String) {
        val errorView = TextView(this).apply {
            text = message
            textSize = 16f
            setPadding(0, 16, 0, 16)
        }
        binding.weatherResultLayout.addView(errorView)
    }
}
