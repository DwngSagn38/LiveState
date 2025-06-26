package com.example.myapplication.model



import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherService {
    @GET("v1/forecast?current_weather=true&hourly=temperature_2m,apparent_temperature,relativehumidity_2m,pressure_msl,cloudcover,windspeed_10m,windgusts_10m,weathercode&daily=temperature_2m_max,temperature_2m_min,precipitation_sum,uv_index_max,sunrise,sunset&timezone=auto&forecast_days=5")
    fun getWeather(
        @Query("latitude") lat: Double,
        @Query("longitude") lon: Double
    ): Call<WeatherResponse>
}
data class WeatherResponse(
    val current_weather: CurrentWeather,
    val hourly: HourlyData?,
    val daily: DailyData?
)


data class CurrentWeather(
    val temperature: Float,
    val windspeed: Float,
    val winddirection: Float,
    val weathercode: Int,
    val is_day: Int,
    val time: String
)
data class HourlyData(
    val time: List<String>?,
    val temperature_2m: List<Float>?,
    val apparent_temperature: List<Float>?,
    val relativehumidity_2m: List<Float>?,
    val pressure_msl: List<Float>?,
    val cloudcover: List<Float>?,
    val windspeed_10m: List<Float>?,
    val windgusts_10m: List<Float>?,
    val precipitation: List<Float>?
)


data class DailyData(
    val time: List<String>,
    val temperature_2m_max: List<Float>,
    val temperature_2m_min: List<Float>,
    val precipitation_sum: List<Float>,
    val uv_index_max: List<Float>,
    val sunrise: List<String>,
    val sunset: List<String>
)
