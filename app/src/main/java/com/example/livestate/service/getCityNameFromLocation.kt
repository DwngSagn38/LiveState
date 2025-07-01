package com.example.livestate.service

import com.example.myapplication.model.GeoLocation
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface GeoCodingService2 {
    @GET("reverse?format=json&addressdetails=1")
    fun getCityNameFromLocation(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double
    ): Call<GeoLocation>
}
