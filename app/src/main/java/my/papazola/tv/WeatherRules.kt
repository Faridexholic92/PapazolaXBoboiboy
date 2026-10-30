package my.papazola.tv

/** Pure rules: forecasts are not observations. All hours supplied in Malaysia time. */
object WeatherRules {
    fun period(hour: Int): String = when (hour) { in 6..11 -> "morning_forecast"; in 12..18 -> "afternoon_forecast"; else -> "night_forecast" }
    fun theme(forecast: String, hour: Int): String {
        val s = forecast.lowercase(java.util.Locale.ROOT)
        if (s.contains("ribut") || s.contains("petir") || s.contains("thunder")) return "storm"
        if (!s.contains("tiada hujan") && (s.contains("hujan") || s.contains("rain"))) return "rain"
        if (hour < 6 || hour >= 19) return "night"
        if (s.contains("cerah") || s.contains("tiada hujan") || s.contains("sunny")) return "sunny"
        return "default"
    }
    fun validMode(mode: String): String = if (mode in setOf("auto", "sunny", "rain", "storm", "night", "default")) mode else "auto"
}
