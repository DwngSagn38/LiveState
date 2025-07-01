package com.example.livestate.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "calculator_table")  // Tên bảng trong cơ sở dữ liệu
data class CalculatorEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    var content: String,
    var result: String,
    var type: Int,
    var isDeleteIconVisible: Int = 0,
    var currentInputDisplay: String
)
