package my.papazola.tv
import org.junit.Assert.*
import org.junit.Test
class WeatherRulesTest {
    @Test fun forecastPriorityAndNegation() {
        assertEquals("sunny",WeatherRules.theme("Tiada hujan",10))
        assertEquals("rain",WeatherRules.theme("Hujan di satu dua tempat",10))
        assertEquals("storm",WeatherRules.theme("Ribut petir",21))
        assertEquals("night",WeatherRules.theme("Tiada hujan",21))
        assertEquals("default",WeatherRules.theme("Berawan",10))
        assertEquals("rain",WeatherRules.theme("Hujan",23))
    }
    @Test fun malaysiaPeriodBoundaries() {
        assertEquals("night_forecast",WeatherRules.period(5))
        assertEquals("morning_forecast",WeatherRules.period(6))
        assertEquals("morning_forecast",WeatherRules.period(11))
        assertEquals("afternoon_forecast",WeatherRules.period(12))
        assertEquals("afternoon_forecast",WeatherRules.period(18))
        assertEquals("night_forecast",WeatherRules.period(19))
        assertEquals("auto",WeatherRules.validMode("invalid"))
    }
}
