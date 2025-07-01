package com.example.livestate.service

import ForecastResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherAPIService {
    @GET("forecast.json")
    fun getForecast(
        @Query("key") key: String,
        @Query("q") city: String,
        @Query("days") days: Int = 6
    ): Call<ForecastResponse>
}
