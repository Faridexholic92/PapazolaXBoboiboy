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
    fun jakimDay(value: String): String? {
        if (!Regex("\\d{2}-[A-Za-z]{3}-\\d{4}").matches(value)) return null
        val months=mapOf("JAN" to "Jan","FEB" to "Feb","MAC" to "Mar","MAR" to "Mar","APR" to "Apr","MEI" to "May","MAY" to "May","JUN" to "Jun","JUL" to "Jul","OGO" to "Aug","AUG" to "Aug","SEP" to "Sep","OKT" to "Oct","OCT" to "Oct","NOV" to "Nov","DIS" to "Dec","DEC" to "Dec")
        val parts=value.split("-");val month=months[parts[1].uppercase(java.util.Locale.ROOT)] ?: return null
        return try {
            val zone=java.util.TimeZone.getTimeZone("Asia/Kuala_Lumpur")
            val parsed=java.text.SimpleDateFormat("dd-MMM-yyyy",java.util.Locale.US).apply { timeZone=zone;isLenient=false }.parse("${parts[0]}-$month-${parts[2]}") ?: return null
            java.text.SimpleDateFormat("yyyy-MM-dd",java.util.Locale.US).apply { timeZone=zone }.format(parsed)
        } catch(_:java.text.ParseException) { null }
    }
    fun validMode(mode: String): String = if (mode in setOf("auto", "sunny", "rain", "storm", "night", "default")) mode else "auto"
}
