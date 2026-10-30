package my.papazola.tv

import android.graphics.Bitmap
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class LauncherSmokeTest {
    private fun descendants(view: View): List<View> = listOf(view) +
        if (view is ViewGroup) (0 until view.childCount).flatMap { descendants(view.getChildAt(it)) } else emptyList()

    @Test fun homeTabsMotionAndRemote() {
        val instrument = InstrumentationRegistry.getInstrumentation()
        instrument.targetContext.getSharedPreferences("launcher", 0).edit().clear().commit()
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val nodes = descendants(activity.window.decorView)
                assertTrue(nodes.filterIsInstance<TextView>().any { it.text.toString() == "MASA UNTUK BERAKSI!" })
                assertTrue(activity.currentFocus is Button)
                nodes.filterIsInstance<Button>().first { it.text.contains("Kegemaran") }.performClick()
                assertTrue(descendants(activity.window.decorView).filterIsInstance<TextView>().any { it.text.startsWith("Kegemaran  /") })
            }
            instrument.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK)
            instrument.waitForIdleSync()
            scenario.onActivity { activity ->
                assertTrue(descendants(activity.window.decorView).filterIsInstance<TextView>().any { it.text.startsWith("Semua aplikasi  /") })
                assertTrue(activity.currentFocus is Button)
                descendants(activity.window.decorView).filterIsInstance<Button>().first { it.text == "Pilihan" }.performClick()
            }
            instrument.waitForIdleSync()
            instrument.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_CENTER)
            instrument.waitForIdleSync()
            scenario.onActivity { activity -> assertFalse(activity.getSharedPreferences("launcher", 0).getBoolean("motion", true)) }
            val bitmap = instrument.uiAutomation.takeScreenshot()
            assertNotNull(bitmap)
            val destination = File(instrument.targetContext.getExternalFilesDir(null), "home-preview.png")
            destination.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        }
    }
}
