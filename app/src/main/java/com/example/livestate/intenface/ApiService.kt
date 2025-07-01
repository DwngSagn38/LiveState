package com.example.livestate.intenface

import com.example.livestate.entity.CurrencyResponse
import retrofit2.http.GET

interface ApiService {
    @GET("latest")
    suspend fun getCurrencyRates(): CurrencyResponse
}
