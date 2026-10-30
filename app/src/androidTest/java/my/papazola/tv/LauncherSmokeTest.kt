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

    private fun screenshot(name: String) {
        val instrument = InstrumentationRegistry.getInstrumentation()
        instrument.waitForIdleSync()
        val bitmap = instrument.uiAutomation.takeScreenshot()
        assertNotNull("Screenshot $name must be available", bitmap)
        val destination = File(instrument.targetContext.getExternalFilesDir(null), name)
        destination.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }

    @Test fun homeTabsMotionAndRemote() {
        val instrument = InstrumentationRegistry.getInstrumentation()
        instrument.targetContext.getSharedPreferences("launcher", 0).edit().clear().commit()
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            try {
                scenario.onActivity { activity ->
                    val nodes = descendants(activity.window.decorView)
                    assertTrue("Hero heading exists", nodes.filterIsInstance<TextView>().any { it.text.toString() == "MASA UNTUK BERAKSI!" })
                    assertTrue("A navigation button has initial focus", activity.currentFocus is Button)
                }
                screenshot("home-preview.png")
                instrument.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_RIGHT)
                instrument.waitForIdleSync()
                scenario.onActivity { activity ->
                    assertTrue("D-pad right reaches favourites tab", (activity.currentFocus as? Button)?.text?.contains("Kegemaran") == true)
                }
                instrument.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_CENTER)
                instrument.waitForIdleSync()
                scenario.onActivity { activity ->
                    assertTrue("Favourites tab opens", descendants(activity.window.decorView).filterIsInstance<TextView>().any { it.text.startsWith("Kegemaran  /") })
                }
                screenshot("favourites-empty.png")
                instrument.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK)
                instrument.waitForIdleSync()
                scenario.onActivity { activity ->
                    assertTrue("Back returns to all apps", descendants(activity.window.decorView).filterIsInstance<TextView>().any { it.text.startsWith("Semua aplikasi  /") })
                    assertTrue("Back restores navigation focus", activity.currentFocus is Button)
                    descendants(activity.window.decorView).filterIsInstance<Button>().first { it.text == "Pilihan" }.performClick()
                }
                screenshot("options-preview.png")
                instrument.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_CENTER)
                instrument.waitForIdleSync()
                scenario.onActivity { activity ->
                    assertFalse("First dialog item toggles motion with remote OK", activity.getSharedPreferences("launcher", 0).getBoolean("motion", true))
                }
                screenshot("motion-off.png")
            } catch (failure: Throwable) {
                screenshot("failure-state.png")
                throw failure
            }
        }
    }
}
