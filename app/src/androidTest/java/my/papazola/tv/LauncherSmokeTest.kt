package my.papazola.tv

import android.graphics.Bitmap
import android.os.ParcelFileDescriptor
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
    private fun descendants(v:View):List<View> = listOf(v) + if(v is ViewGroup)(0 until v.childCount).flatMap { descendants(v.getChildAt(it)) } else emptyList()
    private fun screenshot(name:String) {
        val i=InstrumentationRegistry.getInstrumentation();i.waitForIdleSync()
        val bitmap=i.uiAutomation.takeScreenshot();assertNotNull(bitmap)
        assertTrue("TV landscape HD",bitmap.width>=1280 && bitmap.width>bitmap.height)
        File(i.targetContext.getExternalFilesDir(null),name).outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG,100,it) }
        listOf("mkdir -p /sdcard/Download/papazola","screencap -p /sdcard/Download/papazola/$name").forEach { cmd -> ParcelFileDescriptor.AutoCloseInputStream(i.uiAutomation.executeShellCommand(cmd)).use { it.readBytes() } }
    }
    @Test fun homeWeatherModesEffectsAndRemote() {
        val i=InstrumentationRegistry.getInstrumentation()
        i.targetContext.getSharedPreferences("launcher",0).edit().clear().commit()
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            try {
                scenario.onActivity { a ->
                    assertTrue(descendants(a.window.decorView).filterIsInstance<TextView>().any { it.text.toString()=="AYAH\nFAYYADH" })
                    assertTrue("Dock initially focused",a.currentFocus is Button)
                    assertTrue(descendants(a.window.decorView).filterIsInstance<TextView>().any { it.text.contains("KINARUT · JAKIM SBH07") })
                }
                screenshot("home-preview.png")
                // Bounded wait; network unavailability remains a valid, honest UI state.
                for(attempt in 0 until 26) {
                    var ready=false
                    scenario.onActivity { a -> ready=descendants(a.window.decorView).filterIsInstance<TextView>().any { it.text.contains("Dikemas kini") } }
                    if(ready)break
                    Thread.sleep(1000)
                }
                screenshot("data-status.png")
                i.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_RIGHT);i.waitForIdleSync()
                scenario.onActivity { a -> assertTrue((a.currentFocus as? Button)?.text?.contains("Kegemaran")==true) }
                i.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_CENTER);i.waitForIdleSync();screenshot("favourites-empty.png")
                i.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK);i.waitForIdleSync()
                scenario.onActivity { a -> descendants(a.window.decorView).filterIsInstance<Button>().first { it.text=="Pilihan" }.performClick() }
                i.waitForIdleSync();screenshot("options-preview.png")
                i.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_CENTER);i.waitForIdleSync()
                scenario.onActivity { a -> assertFalse(a.getSharedPreferences("launcher",0).getBoolean("motion",true)) }
                screenshot("motion-off.png")
                scenario.onActivity { a -> descendants(a.window.decorView).filterIsInstance<Button>().first { it.text=="Pilihan" }.performClick() }
                i.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_CENTER);i.waitForIdleSync()
                scenario.onActivity { a -> assertTrue(a.getSharedPreferences("launcher",0).getBoolean("motion",false)) }
                for(mode in listOf("sunny","rain","storm","night")) {
                    scenario.onActivity { a -> a.setWeatherMode(mode);assertEquals(mode,a.getSharedPreferences("launcher",0).getString("weather_mode",""));assertTrue(descendants(a.window.decorView).filterIsInstance<WeatherEffects>().first().running) }
                    screenshot("weather-$mode.png")
                }
                lateinit var overlay:WeatherEffects
                scenario.onActivity { a -> overlay=descendants(a.window.decorView).filterIsInstance<WeatherEffects>().first() }
                scenario.moveToState(androidx.lifecycle.Lifecycle.State.CREATED)
                i.runOnMainSync { assertFalse("Effects pause offscreen",overlay.running) }
                scenario.moveToState(androidx.lifecycle.Lifecycle.State.RESUMED)
                scenario.onActivity { a -> assertTrue("Effects resume",descendants(a.window.decorView).filterIsInstance<WeatherEffects>().first().running) }
                Thread.sleep(800);screenshot("stars-on.png")
                scenario.onActivity { a -> descendants(a.window.decorView).filterIsInstance<Button>().first { it.text=="Pilihan" }.performClick() }
                i.waitForIdleSync()
                val toggle=i.uiAutomation.rootInActiveWindow.findAccessibilityNodeInfosByText("Efek: ON").first { it.isClickable }
                assertTrue("Turn effects off through the visible options UI",toggle.performAction(android.view.accessibility.AccessibilityNodeInfo.ACTION_CLICK))
                i.waitForIdleSync()
                scenario.onActivity { a -> assertFalse("Toggle persisted before recreate",a.getSharedPreferences("launcher",0).getBoolean("motion",true)) }
                scenario.recreate();i.waitForIdleSync()
                scenario.onActivity { a ->
                    assertEquals("night",a.getSharedPreferences("launcher",0).getString("weather_mode",""))
                    assertFalse(a.getSharedPreferences("launcher",0).getBoolean("motion",true))
                    assertFalse(descendants(a.window.decorView).filterIsInstance<WeatherEffects>().first().running)
                    a.setWeatherMode("auto")
                }
                screenshot("auto-restored.png")
            } catch(t:Throwable) { screenshot("failure-state.png");throw t }
        }
    }
}
