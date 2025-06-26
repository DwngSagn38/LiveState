package com.example.myapplication.model

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query




interface GeoCodingService {
    @GET("search?format=json")
    fun getLocationByCityName(@Query("q") cityName: String): Call<List<GeoLocation>>
}

data class GeoLocation(
    val lat: String,
    val lon: String,
    val display_name: String
)
