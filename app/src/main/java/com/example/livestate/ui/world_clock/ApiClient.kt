package com.example.livestate.ui.world_clock

import retrofit2.Retrofit


import retrofit2.converter.gson.GsonConverterFactory


object ApiClient {
    private const val BASE_URL = "https://restcountries.com/v3.1/"

    val countryService: CountryService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CountryService::class.java)
    }
}
