package com.example.livestate.service


import retrofit2.http.GET
import retrofit2.http.Path

interface TimezoneService {
    @GET("api/timezone/{timezone}")
    suspend fun getTimeInfo(@Path("timezone") timezone: String): TimezoneResponse
}

data class TimezoneResponse(
    val utc_offset: String,
    val datetime: String
)
