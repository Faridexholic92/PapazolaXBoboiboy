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
                i.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_RIGHT);i.waitForIdleSync()
                scenario.onActivity { a -> assertTrue((a.currentFocus as? Button)?.text?.contains("Kegemaran")==true) }
                i.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_CENTER);i.waitForIdleSync();screenshot("favourites-empty.png")
                i.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK);i.waitForIdleSync()
                scenario.onActivity { a -> descendants(a.window.decorView).filterIsInstance<Button>().first { it.text=="Pilihan" }.performClick() }
                i.waitForIdleSync();screenshot("options-preview.png")
                i.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_CENTER);i.waitForIdleSync()
                scenario.onActivity { a -> assertFalse(a.getSharedPreferences("launcher",0).getBoolean("motion",true)) }
                screenshot("motion-off.png")
                for(mode in listOf("sunny","rain","storm","night")) {
                    scenario.onActivity { a -> a.setWeatherMode(mode);assertEquals(mode,a.getSharedPreferences("launcher",0).getString("weather_mode","")) }
                    screenshot("weather-$mode.png")
                }
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
