data class ForecastResponse(
    val location: Location,
    val forecast: Forecast
)

data class Location(
    val name: String,
    val country: String
)

data class Forecast(
    val forecastday: List<ForecastDay>
)

data class ForecastDay(
    val date: String,
    val day: Day
)

data class Day(
    val condition: Condition,
    val daily_chance_of_rain: Int,
    val daily_chance_of_snow: Int,
    val maxtemp_c: Float,    // ✅ Thêm dòng này
    val mintemp_c: Float     // ✅ Thêm dòng này
)

data class Condition(
    val text: String,
    val icon: String
)
