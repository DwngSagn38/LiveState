package com.example.livestate.model

data class RestCountry(
    val name: Name,
    val flags: Flags,
    val timezones: List<String>
)

data class Name(
    val common: String
)

data class Flags(
    val png: String
)
