import androidx.annotation.Keep

@Keep
data class ForecastResponse(
    val location: Location,
    val forecast: Forecast
)
@Keep
data class Location(
    val name: String,
    val country: String
)
@Keep
data class Forecast(
    val forecastday: List<ForecastDay>
)
@Keep
data class ForecastDay(
    val date: String,
    val day: Day
)
@Keep
data class Day(
    val condition: Condition,
    val daily_chance_of_rain: Int,
    val daily_chance_of_snow: Int,
    val maxtemp_c: Float,    // ✅ Thêm dòng này
    val mintemp_c: Float     // ✅ Thêm dòng này
)
@Keep
data class Condition(
    val text: String,
    val icon: String
)
