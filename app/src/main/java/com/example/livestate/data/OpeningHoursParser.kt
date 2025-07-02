package com.example.livestate.data

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalTime

object OpeningHoursParser {
    @RequiresApi(Build.VERSION_CODES.O)
    fun isOpenNow(openingHours: String): Boolean {
        try {
            val now = LocalTime.now()
            val regex = Regex("""(\d{2}):(\d{2})-(\d{2}):(\d{2})""")
            val match = regex.find(openingHours)
            if (match != null) {
                val (startH, startM, endH, endM) = match.destructured
                val start = LocalTime.of(startH.toInt(), startM.toInt())
                val end = LocalTime.of(endH.toInt(), endM.toInt())
                return now.isAfter(start) && now.isBefore(end)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

}
