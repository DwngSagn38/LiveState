package com.example.livestate.model

import androidx.room.PrimaryKey


data class Currency(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val contentDisplay: String,
    val content: String,
    var rate: Double,
    var isSelected: Boolean,
) {}