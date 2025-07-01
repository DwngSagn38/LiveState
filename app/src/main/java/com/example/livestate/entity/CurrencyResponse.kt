package com.example.livestate.entity
data class CurrencyResponse(
    val rates: Map<String, Double>,
    val base: String,
    val timestamp: Long
)
