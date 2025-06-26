package com.example.livestate.service


import com.example.myapplication.model.WeatherResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface OpenMeteoService {
    @GET("v1/forecast?current_weather=true&hourly=temperature_2m,apparent_temperature,relativehumidity_2m,pressure_msl,cloudcover,windspeed_10m,windgusts_10m,weathercode&daily=temperature_2m_max,temperature_2m_min,precipitation_sum,uv_index_max,sunrise,sunset&timezone=auto&forecast_days=5")
    fun getWeather(
        @Query("latitude") lat: Double,
        @Query("longitude") lon: Double
    ): Call<WeatherResponse>
}
