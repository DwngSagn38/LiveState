package com.example.livestate.model
data class WeatherDay(
    val date: String,
    val iconResId: Int, // dùng Int thay vì String URL
    val temperatureText: String,
    val weatherType: String
)
