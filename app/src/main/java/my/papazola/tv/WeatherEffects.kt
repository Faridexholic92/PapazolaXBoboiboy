package my.papazola.tv

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.SystemClock
import android.view.View
import kotlin.math.sin
import java.util.Random

/** Transparent native overlay. No moving wallpaper/video, audio or flashing lightning. */
class WeatherEffects(context: Context) : View(context) {
    var mode: String = "default"
    var running = false
        set(value) { field = value; invalidate() }
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val seed = Random(24)
    private val points = List(42) { floatArrayOf(seed.nextFloat(), seed.nextFloat(), seed.nextFloat()) }
    init { importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_NO; isFocusable = false; isClickable = false }
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (!running) return
        val t = (SystemClock.uptimeMillis() % 120000L) / 1000f
        if (mode == "night") {
            points.forEach { p ->
                val x = p[0] * width; val y = (.04f + p[1] * .40f) * height
                paint.color = Color.WHITE
                paint.alpha = (90 + 100 * (.5 + .5 * sin(t * .65 + p[2] * 6.28))).toInt()
                canvas.drawCircle(x, y, (.7f + p[2] * 1.2f) * resources.displayMetrics.density, paint)
            }
        } else if (mode == "rain" || mode == "storm") {
            // In the indoor storm illustration, rain stays outside the window.
            if (mode == "storm") { canvas.save(); canvas.clipRect(width * .71f, 0f, width.toFloat(), height * .62f) }
            paint.color = Color.rgb(183, 218, 241); paint.alpha = 75; paint.strokeWidth = resources.displayMetrics.density * .6f
            points.forEach { p ->
                val x = p[0] * width; val y = ((p[1] + t * (.12f + p[2] * .05f)) % 1f) * height
                canvas.drawLine(x, y, x - width * .003f, y + height * .018f, paint)
            }
            if (mode == "storm") canvas.restore()
        }
        if (mode in setOf("night", "rain", "storm")) postInvalidateDelayed(50L)
    }
}
