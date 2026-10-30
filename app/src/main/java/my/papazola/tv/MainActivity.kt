package my.papazola.tv

import android.app.Activity
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Color
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors

/** Native TV launcher. Forecast-based scenery, not real-time rain detection. */
class MainActivity : Activity() {
    private data class TvApp(val packageName: String, val title: String, val intent: Intent)
    private val prefs by lazy { getSharedPreferences("launcher", MODE_PRIVATE) }
    private val handler = Handler(Looper.getMainLooper())
    private val worker = Executors.newSingleThreadExecutor()
    private val malaysia = TimeZone.getTimeZone("Asia/Kuala_Lumpur")
    private lateinit var wallpaper: ImageView
    private lateinit var effects: WeatherEffects
    private lateinit var clock: TextView
    private lateinit var date: TextView
    private lateinit var prayers: LinearLayout
    private val prayerValues = mutableListOf<TextView>()
    private lateinit var prayerStatus: TextView
    private lateinit var weather: TextView
    private lateinit var dock: LinearLayout
    private var homeFocus: View? = null
    private var apps: List<TvApp> = emptyList()
    private var resumed = false
    private var fetching = false
    private var motion = true
    private var activeTheme = ""
    private var lastDay = ""
    private var lastAttempt = 0L
    private var networkFailed = false
    private var drawer: AlertDialog? = null
    private val cyan = Color.rgb(78, 220, 255)
    private val navy = Color.rgb(5, 17, 34)
    private val scale by lazy { minOf(resources.configuration.screenWidthDp / 960f, resources.configuration.screenHeightDp / 540f).coerceAtLeast(.6f) }
    private fun dp(value: Int) = (value * resources.displayMetrics.density * scale).toInt()
    private fun today() = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { timeZone = malaysia }.format(Date())
    private fun hour() = Calendar.getInstance(malaysia).get(Calendar.HOUR_OF_DAY)
    private fun fmt(pattern: String) = SimpleDateFormat(pattern, Locale("ms", "MY")).apply { timeZone = malaysia }.format(Date())
    private val tick = object : Runnable {
        override fun run() {
            if (!resumed) return
            clock.text = fmt("HH:mm")
            date.text = fmt("EEEE, d MMMM yyyy").uppercase(Locale("ms", "MY"))
            val day = today()
            if (day != lastDay) { lastDay = day; refreshData(true) }
            updateWidgets()
            handler.postDelayed(this, 30000L)
        }
    }
    private val refresh = object : Runnable {
        override fun run() { if (resumed) { refreshData(false); handler.postDelayed(this, 30 * 60 * 1000L) } }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        motion = prefs.getBoolean("motion", true)
        buildHome()
    }
    override fun onResume() {
        super.onResume(); resumed = true
        apps = discoverApps(); renderDock()
        tick.run(); handler.removeCallbacks(refresh); refresh.run()
        startEffects()
    }
    override fun onPause() {
        resumed = false; handler.removeCallbacks(tick); handler.removeCallbacks(refresh)
        effects.running = false; super.onPause()
    }
    override fun onDestroy() { worker.shutdownNow(); drawer?.dismiss(); super.onDestroy() }
    private fun text(value: String, size: Float, bold: Boolean = false) = TextView(this).apply {
        text = value; textSize = size * scale; setTextColor(Color.WHITE)
        if (bold) typeface = Typeface.create("sans-serif", Typeface.BOLD)
        setShadowLayer(dp(3).toFloat(), 0f, 1f, Color.BLACK)
    }
    private fun canAnimate() = resumed && motion && Settings.Global.getFloat(contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) > 0f
    private fun surface(focused: Boolean = false) = GradientDrawable().apply {
        setColor(if (focused) Color.rgb(9, 47, 65) else Color.argb(205, 6, 19, 36))
        cornerRadius = dp(9).toFloat(); setStroke(dp(if (focused) 2 else 1), if (focused) cyan else Color.argb(65, 190, 222, 240))
    }
    private fun focusStyle(v: View) {
        v.isFocusable = true; v.isFocusableInTouchMode = true; v.background = surface()
        v.setOnFocusChangeListener { _, focused -> v.background = surface(focused) }
    }
    private fun button(label: String, action: () -> Unit) = Button(this).apply {
        text = label; textSize = 13f * scale; isAllCaps = false; setTextColor(Color.WHITE)
        setPadding(dp(8), 0, dp(8), 0); minHeight = 0; minimumHeight = 0; minWidth = 0; minimumWidth = 0
        focusStyle(this); setOnClickListener { action() }
    }
    private class Scene(context: android.content.Context) : FrameLayout(context) {
        private val bounds = mutableMapOf<View, RectF>()
        fun place(view: View, l: Float, t: Float, r: Float, b: Float) { bounds[view] = RectF(l,t,r,b); addView(view) }
        override fun onMeasure(wSpec: Int, hSpec: Int) {
            val w = MeasureSpec.getSize(wSpec); val h = MeasureSpec.getSize(hSpec); setMeasuredDimension(w,h)
            bounds.forEach { (v,r) -> v.measure(MeasureSpec.makeMeasureSpec(((r.right-r.left)*w).toInt(),MeasureSpec.EXACTLY),MeasureSpec.makeMeasureSpec(((r.bottom-r.top)*h).toInt(),MeasureSpec.EXACTLY)) }
        }
        override fun onLayout(changed: Boolean,l: Int,t: Int,r: Int,b: Int) {
            bounds.forEach { (v,box) -> v.layout((box.left*width).toInt(),(box.top*height).toInt(),(box.right*width).toInt(),(box.bottom*height).toInt()) }
        }
    }
    private fun buildHome() {
        val root = Scene(this).apply { setBackgroundColor(navy) }
        wallpaper = ImageView(this).apply { scaleType = ImageView.ScaleType.CENTER_CROP; importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO }
        root.place(wallpaper,0f,0f,1f,1f)
        effects = WeatherEffects(this)
        root.place(View(this).apply { background = GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(Color.argb(230,3,13,31),Color.argb(150,3,13,31),Color.TRANSPARENT,Color.argb(230,3,13,31))) },0f,0f,1f,1f)
        root.place(effects,0f,0f,1f,1f)
        root.place(text("FayyadhOS",27f,true),.04f,.035f,.38f,.10f)
        root.place(text("A QUIETER KIND OF SMART.",10f),.042f,.10f,.39f,.13f)
        val controls = LinearLayout(this).apply { gravity = Gravity.END }
        controls.addView(button("Tetapan TV") { openSettings() },LinearLayout.LayoutParams(0,-1,1f).apply { marginEnd=dp(8) })
        controls.addView(button("Pilihan") { showOptions() },LinearLayout.LayoutParams(0,-1,1f))
        root.place(controls,.73f,.045f,.96f,.11f)
        clock = text("--:--",54f); root.place(clock,.04f,.145f,.24f,.265f)
        date = text("",11f,true); root.place(date,.043f,.27f,.29f,.305f)
        prayers = LinearLayout(this).apply { orientation=LinearLayout.VERTICAL }
        prayers.addView(text("WAKTU SOLAT",11f,true))
        val prayerRow=LinearLayout(this)
        listOf("SUBUH","ZOHOR","ASAR","MAGHRIB","ISYAK").forEach { label ->
            val column=LinearLayout(this).apply { orientation=LinearLayout.VERTICAL;setPadding(0,dp(5),0,dp(4)) }
            column.addView(text(label,10f,true))
            val value=text("--:--",17f,true);prayerValues.add(value);column.addView(value)
            prayerRow.addView(column,LinearLayout.LayoutParams(0,-2,1f))
        }
        prayers.addView(prayerRow)
        prayerStatus=text("KINARUT · JAKIM SBH07",10f);prayers.addView(prayerStatus)
        root.place(prayers,.29f,.158f,.73f,.31f)
        weather = text("",12f).apply { setLineSpacing(dp(3).toFloat(),1f) }
        root.place(weather,.77f,.15f,.965f,.345f)
        val story = LinearLayout(this).apply { orientation=LinearLayout.VERTICAL }
        story.addView(text("DUNIA",23f,true))
        story.addView(text("AYAH\nFAYYADH",33f,true).apply { setTextColor(Color.rgb(178,229,248)); setLineSpacing(0f,.94f) })
        story.addView(text("Pengembaraan bermula di sini.",13f).apply { setPadding(0,dp(13),0,0) })
        root.place(story,.043f,.36f,.34f,.72f)
        dock = LinearLayout(this).apply { gravity=Gravity.CENTER; clipChildren=false; clipToPadding=false }
        root.place(dock,.07f,.81f,.93f,.955f)
        root.place(text("PREVIEW 0.2 • ILUSTRASI AI TIDAK RASMI • CUACA IALAH RAMALAN",9f).apply { gravity=Gravity.CENTER },.14f,.967f,.86f,.995f)
        setContentView(root); applyTheme("default")
    }
    @Suppress("DEPRECATION")
    private fun discoverApps(): List<TvApp> = AppOrder.sort(listOf(Intent.CATEGORY_LEANBACK_LAUNCHER,Intent.CATEGORY_LAUNCHER)
        .flatMap { packageManager.queryIntentActivities(Intent(Intent.ACTION_MAIN).addCategory(it),0) }
        .filter { it.activityInfo.packageName != packageName && it.activityInfo.enabled }
        .distinctBy { it.activityInfo.packageName }.mapNotNull {
            val pkg=it.activityInfo.packageName
            (packageManager.getLeanbackLaunchIntentForPackage(pkg) ?: packageManager.getLaunchIntentForPackage(pkg))?.let { launch -> TvApp(pkg,it.loadLabel(packageManager).toString(),launch) }
        }) { it.title }
    private fun favourites() = prefs.getStringSet("favourites",emptySet())?.toSet() ?: emptySet()
    private fun renderDock() {
        val focused = currentFocus?.tag as? String
        dock.removeAllViews()
        val fav=favourites()
        val preferred=listOf("com.google.android.youtube.tv","com.netflix.ninja","com.android.vending")
        val chosen=apps.sortedWith(compareBy<TvApp> { if(it.packageName in fav) 0 else 1 }.thenBy { val i=preferred.indexOf(it.packageName); if(i<0) 100 else i }.thenBy { it.title.lowercase(Locale.ROOT) }).take(5)
        chosen.forEach { app ->
            val card=LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; gravity=Gravity.CENTER; tag=app.packageName; contentDescription=app.title; setPadding(dp(4),dp(7),dp(4),dp(4)) }
            val icon=ImageView(this).apply { try { setImageDrawable(packageManager.getApplicationIcon(app.packageName)) } catch (_:android.content.pm.PackageManager.NameNotFoundException) { setImageResource(android.R.drawable.sym_def_app_icon) }; importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO }
            card.addView(icon,LinearLayout.LayoutParams(dp(37),dp(37)))
            card.addView(text(app.title,11f).apply { gravity=Gravity.CENTER; maxLines=1; ellipsize=android.text.TextUtils.TruncateAt.END; setPadding(0,dp(5),0,0) })
            focusStyle(card); card.setOnClickListener { launchApp(app) }; card.setOnLongClickListener { favouriteMenu(app);true }
            card.setOnKeyListener { _,code,event -> if(code==android.view.KeyEvent.KEYCODE_MENU && event.action==android.view.KeyEvent.ACTION_UP) { favouriteMenu(app);true } else false }
            dock.addView(card,LinearLayout.LayoutParams(0,-1,1f).apply { marginEnd=dp(8) })
        }
        val all=button("▦\nSemua app") { showDrawer(false) }.apply { tag="all" }
        dock.addView(all,LinearLayout.LayoutParams(0,-1,1f).apply { marginEnd=dp(8) })
        dock.addView(button("★\nKegemaran") { showDrawer(true) }.apply { tag="favs" },LinearLayout.LayoutParams(0,-1,1f))
        homeFocus=all
        (focused?.let { dock.findViewWithTag<View>(it) } ?: all).requestFocus()
    }
    private fun showDrawer(onlyFavourites: Boolean) {
        drawer?.dismiss()
        val content=LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; setPadding(dp(12),dp(4),dp(12),dp(4)) }
        val tabs=LinearLayout(this)
        tabs.addView(button("Semua app") { showDrawer(false) },LinearLayout.LayoutParams(0,dp(42),1f))
        tabs.addView(button("Kegemaran") { showDrawer(true) },LinearLayout.LayoutParams(0,dp(42),1f).apply { marginStart=dp(8) })
        content.addView(tabs)
        val visible=if(onlyFavourites) apps.filter { it.packageName in favourites() } else apps
        val scroller=ScrollView(this)
        val rows=LinearLayout(this).apply { orientation=LinearLayout.VERTICAL }
        if(visible.isEmpty()) rows.addView(text(if(onlyFavourites) "Belum ada kegemaran. Tahan OK pada app untuk menambah." else "Tiada aplikasi ditemui. Semak Tetapan TV.",14f).apply { setPadding(0,dp(16),0,dp(16)) })
        visible.chunked(3).forEach { group ->
            val row=LinearLayout(this)
            group.forEach { app ->
                val b=button(app.title) { launchApp(app) }; b.setOnLongClickListener { favouriteMenu(app);true }
                row.addView(b,LinearLayout.LayoutParams(0,dp(48),1f).apply { marginEnd=dp(6);topMargin=dp(6) })
            }
            rows.addView(row)
        }
        scroller.addView(rows); content.addView(scroller,LinearLayout.LayoutParams(-1,dp(190)))
        drawer=AlertDialog.Builder(this).setTitle(if(onlyFavourites) "Kegemaran" else "Semua aplikasi").setView(content).setNegativeButton("Kembali",null).create()
        drawer?.setOnShowListener { tabs.getChildAt(if(onlyFavourites)1 else 0).requestFocus() };drawer?.show()
    }
    private fun favouriteMenu(app: TvApp) {
        val adding=app.packageName !in favourites()
        showTvMenu(app.title,listOf(if(adding)"Tambah kegemaran" else "Buang kegemaran","Buka aplikasi")) { which ->
            if(which==0) { prefs.edit().putStringSet("favourites",AppOrder.toggle(favourites(),app.packageName)).apply();renderDock() } else launchApp(app)
        }
    }
    private fun launchApp(app: TvApp) = safeStart(Intent(app.intent).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    private fun safeStart(intent: Intent) {
        try { startActivity(intent) } catch(_:ActivityNotFoundException) { Toast.makeText(this,"Aplikasi tidak tersedia.",Toast.LENGTH_LONG).show() } catch(_:SecurityException) { Toast.makeText(this,"Peranti menyekat tindakan ini.",Toast.LENGTH_LONG).show() }
    }
    private fun openSettings()=safeStart(Intent(Settings.ACTION_SETTINGS))
    private fun showOptions() {
        showTvMenu("Pilihan launcher",listOf("Efek: ${if(motion)"ON" else "OFF"}","Wallpaper / cuaca","Segarkan data","Pilih Home launcher","Tentang preview")) { which ->
            when(which) {
                0 -> { motion=!motion;prefs.edit().putBoolean("motion",motion).apply();startEffects() }
                1 -> showTvMenu("Wallpaper — Auto ikut ramalan",listOf("Auto ikut cuaca","Cerah — taman permainan","Hujan — payung","Ribut — mi di tingkap","Malam — bintang","Asal — Kinabalu")) { setWeatherMode(listOf("auto","sunny","rain","storm","night","default")[it]) }
                2 -> refreshData(true)
                3 -> { val i=Intent(Settings.ACTION_HOME_SETTINGS);if(i.resolveActivity(packageManager)!=null)safeStart(i) else openSettings() }
                4 -> AlertDialog.Builder(this).setTitle("Dunia Ayah Fayyadh — 0.2 preview")
                    .setMessage("Kinarut, Sabah. Solat: JAKIM SBH07. Cuaca: ramalan METMalaysia daerah Papar melalui data.gov.my (CC BY 4.0). Bukan suhu atau hujan masa nyata. Lokasi tetap, tiada GPS.\n\nIlustrasi AI tidak rasmi, tiada video atau animasi watak. Efek ringan boleh dimatikan. Semak hak sebelum mengedarkan. Preview ditandatangani debug, belum disahkan pada TV sebenar.")
                    .setPositiveButton("OK",null).show()
            }
        }
    }
    private fun showTvMenu(title: String,labels: List<String>,action:(Int)->Unit) {
        lateinit var dialog: AlertDialog
        val content=LinearLayout(this).apply { orientation=LinearLayout.VERTICAL;setPadding(dp(14),dp(6),dp(14),dp(6)) }
        labels.forEachIndexed { i,label -> content.addView(button(label) { dialog.dismiss();action(i) },LinearLayout.LayoutParams(-1,dp(42)).apply { bottomMargin=dp(5) }) }
        dialog=AlertDialog.Builder(this).setTitle(title).setView(content).setNegativeButton("Tutup",null).create()
        dialog.setOnShowListener { content.getChildAt(0).requestFocus() }; dialog.show()
    }
    fun setWeatherMode(value: String) { prefs.edit().putString("weather_mode",WeatherRules.validMode(value)).apply(); updateWidgets() }
    private fun startEffects() { effects.running=canAnimate() }
    private fun applyTheme(theme: String) {
        if(theme!=activeTheme) {
            activeTheme=theme
            wallpaper.setImageResource(when(theme) { "sunny" -> R.drawable.weather_sunny;"rain" -> R.drawable.weather_rain;"storm" -> R.drawable.weather_storm;else -> R.drawable.weather_default })
            wallpaper.setColorFilter(if(theme=="night") Color.argb(95,9,20,62) else Color.TRANSPARENT,android.graphics.PorterDuff.Mode.SRC_ATOP)
            wallpaper.contentDescription="Wallpaper $theme"
        }
        effects.mode=theme;startEffects()
    }
    private fun cache(key:String):JSONObject? = try { prefs.getString(key,null)?.let { JSONObject(it) } } catch(_:org.json.JSONException) { null }
    private fun updateWidgets() {
        val day=today();val h=hour()
        val prayer=cache("prayer_cache")?.takeIf { it.optString("day")==day && it.optString("zone")=="SBH07" }
        val fields=listOf("fajr","dhuhr","asr","maghrib","isha")
        fields.forEachIndexed { i,key -> prayerValues[i].text=prayer?.optJSONObject("times")?.optString(key)?.take(5) ?: "--:--" }
        prayerStatus.text="KINARUT · JAKIM SBH07${if(prayer==null)" · belum tersedia" else if(networkFailed)" · cache hari ini" else ""}"
        val cached=cache("weather_cache")
        val row=cached?.optJSONObject("row")?.takeIf { it.optString("date")==day && it.optJSONObject("location")?.optString("location_id")=="Ds548" && System.currentTimeMillis()-(cached.optLong("fetched")) in 0..86400000L }
        val forecast=row?.optString(WeatherRules.period(h))?.takeIf { it.isNotBlank() }
        val temp=if(row!=null) "${row.optInt("min_temp")}–${row.optInt("max_temp")}°C" else "--°C"
        val mode=WeatherRules.validMode(prefs.getString("weather_mode","auto") ?: "auto")
        val selected=if(mode!="auto")mode else if(forecast!=null)WeatherRules.theme(forecast,h) else "default"
        applyTheme(selected)
        val status=if(row==null)"Data belum tersedia" else "${if(networkFailed)"Cache" else "Dikemas kini"} ${SimpleDateFormat("HH:mm",Locale.US).apply { timeZone=malaysia }.format(Date(cached!!.optLong("fetched")))} MYT"
        weather.text="Kinarut, Sabah\n$temp · Ramalan\n${forecast ?: "Menunggu data cuaca"}\nDaerah Papar · METMalaysia\n$status${if(mode!="auto")"\nWallpaper manual: $mode" else ""}"
    }
    private fun json(url:String):String {
        val c=URL(url).openConnection() as HttpURLConnection
        try {
            c.connectTimeout=12000;c.readTimeout=12000;c.setRequestProperty("User-Agent","DuniaAyahFayyadh/0.2")
            c.instanceFollowRedirects=false
            if(c.responseCode!=200)throw java.io.IOException("HTTP ${c.responseCode}")
            return c.inputStream.bufferedReader().use { it.readText() }.also { if(it.length>1000000)throw java.io.IOException("Oversize response") }
        } finally { c.disconnect() }
    }
    private fun refreshData(force:Boolean) {
        if(fetching || worker.isShutdown || (!force && System.currentTimeMillis()-lastAttempt<25*60*1000L))return
        fetching=true;lastAttempt=System.currentTimeMillis()
        val requestedDay=today()
        worker.execute {
            var failed=false
            try {
                val p=JSONObject(json("https://www.e-solat.gov.my/index.php?r=esolatApi/TakwimSolat&period=today&zone=SBH07"))
                val times=p.getJSONArray("prayerTime").getJSONObject(0)
                val prayerDate=SimpleDateFormat("dd-MMM-yyyy",Locale.US).apply { timeZone=malaysia;isLenient=false }.parse(times.getString("date"))
                val parsedDay=SimpleDateFormat("yyyy-MM-dd",Locale.US).apply { timeZone=malaysia }.format(prayerDate!!)
                require(p.getString("status")=="OK!" && p.getString("zone")=="SBH07" && p.getString("serverTime").startsWith(requestedDay) && parsedDay==requestedDay)
                listOf("fajr","dhuhr","asr","maghrib","isha").forEach { require(Regex("(?:[01][0-9]|2[0-3]):[0-5][0-9]:[0-5][0-9]").matches(times.getString(it))) }
                prefs.edit().putString("prayer_cache",JSONObject().put("day",requestedDay).put("zone","SBH07").put("times",times).toString()).apply()
            } catch(e:Exception) { failed=true;android.util.Log.w("WeatherData","JAKIM unavailable: ${e.javaClass.simpleName}: ${e.message}") }
            try {
                val a=JSONArray(json("https://api.data.gov.my/weather/forecast?filter=Ds548%40location__location_id&date_start=$requestedDay%40date&date_end=$requestedDay%40date&limit=7"))
                val row=(0 until a.length()).map { a.getJSONObject(it) }.first { it.getString("date")==requestedDay && it.getJSONObject("location").getString("location_id")=="Ds548" }
                require(row.has("min_temp") && row.has("max_temp") && row.getInt("min_temp")<=row.getInt("max_temp"))
                listOf("morning_forecast","afternoon_forecast","night_forecast").forEach { require(row.getString(it).isNotBlank()) }
                prefs.edit().putString("weather_cache",JSONObject().put("row",row).put("fetched",System.currentTimeMillis()).toString()).apply()
            } catch(e:Exception) { failed=true;android.util.Log.w("WeatherData","MET forecast unavailable: ${e.javaClass.simpleName}: ${e.message}") }
            handler.post { fetching=false;networkFailed=failed;if(!isDestroyed)updateWidgets() }
        }
    }
    @Deprecated("Legacy back handling for launcher API 23+")
    override fun onBackPressed() { homeFocus?.requestFocus() }
}
