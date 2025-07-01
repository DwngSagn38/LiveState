package com.example.livestate.ui.world_clock

import com.example.livestate.model.RestCountry
import retrofit2.http.GET

interface CountryService {
    @GET("all?fields=name,flags,timezones")
    suspend fun getAllCountries(): List<RestCountry>
}
