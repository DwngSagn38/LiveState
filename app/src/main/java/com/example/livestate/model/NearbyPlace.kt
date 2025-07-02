package com.example.livestate.model

data class NearbyPlace(
    val name: String?,
    val address: String?,
    val isOpen: Boolean?,
    val lat: Double,
    val lng: Double,
    val phone : String? = null,
    val openingHours: String? = null
)

