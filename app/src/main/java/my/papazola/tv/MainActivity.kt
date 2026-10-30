package my.papazola.tv

import android.animation.ValueAnimator
import android.app.Activity
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Offline, remote-first preview. No account, network or broad package permissions. */
class MainActivity : Activity() {
    private data class TvApp(val packageName: String, val title: String, val intent: Intent)
    private val prefs by lazy { getSharedPreferences("launcher", MODE_PRIVATE) }
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var clock: TextView
    private lateinit var date: TextView
    private lateinit var grid: LinearLayout
    private lateinit var heading: TextView
    private lateinit var background: View
    private lateinit var allTab: Button
    private lateinit var favouritesTab: Button
    private var showFavourites = false
    private var motion = true
    private var pulse: ValueAnimator? = null
    private var apps: List<TvApp> = emptyList()
    private val yellow = Color.rgb(255, 211, 78)
    private val ink = Color.rgb(9, 44, 80)
    private val tick = object : Runnable {
        override fun run() {
            val now = Date()
            clock.text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(now)
            date.text = SimpleDateFormat("EEE, d MMM yyyy", Locale.getDefault()).format(now)
            handler.postDelayed(this, 1000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showFavourites = savedInstanceState?.getBoolean("favourites") ?: false
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN or
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        motion = prefs.getBoolean("motion", true)
        buildHome()
    }

    override fun onResume() {
        super.onResume()
        apps = discoverApps()
        renderApps()
        handler.removeCallbacks(tick)
        tick.run()
        startAmbient()
    }

    override fun onPause() {
        handler.removeCallbacks(tick)
        pulse?.cancel()
        super.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean("favourites", showFavourites)
        super.onSaveInstanceState(outState)
    }

    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()
    private fun text(value: String, size: Float, color: Int = Color.WHITE, bold: Boolean = false) =
        TextView(this).apply {
            text = value; textSize = size; setTextColor(color)
            if (bold) typeface = Typeface.create("sans-serif", Typeface.BOLD)
        }
    private fun shape(color: Int, border: Int = Color.TRANSPARENT) = GradientDrawable().apply {
        setColor(color); cornerRadius = dp(16).toFloat(); setStroke(dp(3), border)
    }
    private fun canAnimate() = motion && Settings.Global.getFloat(
        contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) > 0f

    private fun buildHome() {
        val root = FrameLayout(this).apply { setBackgroundColor(ink) }
        background = View(this).apply {
            background = GradientDrawable(GradientDrawable.Orientation.TL_BR,
                intArrayOf(Color.rgb(15, 112, 174), Color.rgb(14, 63, 111), Color.rgb(13, 44, 78)))
        }
        root.addView(background, FrameLayout.LayoutParams(-1, -1))
        val scroll = ScrollView(this).apply {
            clipToPadding = false; isFillViewport = true
            isVerticalScrollBarEnabled = false
        }
        val body = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(40), dp(24), dp(40), dp(28))
            clipChildren = false; clipToPadding = false
        }
        val top = LinearLayout(this).apply { gravity = Gravity.CENTER_VERTICAL }
        val brand = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        brand.addView(text("PAPAZOLA × BOBOIBOY", 26f, yellow, true))
        brand.addView(text("STESEN KUASA  /  TV LAUNCHER", 13f))
        top.addView(brand, LinearLayout.LayoutParams(0, -2, 1f))
        val timeBox = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; gravity = Gravity.END }
        clock = text("", 30f, Color.WHITE, true)
        date = text("", 13f)
        timeBox.addView(clock); timeBox.addView(date); top.addView(timeBox)
        body.addView(top)

        val hero = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = resources.getDrawable(R.drawable.hero_art, theme)
            setPadding(dp(24), dp(16), dp(24), dp(16))
        }
        hero.addView(text("MASA UNTUK BERAKSI!", 26f, ink, true))
        hero.addView(text("Dunia hiburan kau, satu remote sahaja.", 17f, ink))
        hero.addView(text("PETIR  /  API  /  ANGIN  /  DAUN", 12f, ink, true).apply { setPadding(0, dp(10), 0, 0) })
        body.addView(hero, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(22); bottomMargin = dp(16) })

        val nav = LinearLayout(this).apply { clipChildren = false; clipToPadding = false }
        allTab = navButton("Semua app") { showFavourites = false; renderApps() }
        favouritesTab = navButton("Kegemaran") { showFavourites = true; renderApps() }
        nav.addView(allTab); nav.addView(favouritesTab)
        nav.addView(navButton("Tetapan TV") { openSettings() })
        nav.addView(navButton("Pilihan") { showOptions() })
        body.addView(nav)
        heading = text("", 18f, Color.WHITE, true).apply { setPadding(0, dp(12), 0, dp(8)) }
        body.addView(heading)
        grid = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL; clipChildren = false; clipToPadding = false
        }
        body.addView(grid)
        body.addView(text("OK: buka  •  Tahan OK / Menu: kegemaran  •  Back: semua app", 13f).apply {
            setPadding(0, dp(12), 0, 0)
        })
        body.addView(text("PREVIEW 0.1  •  Ilustrasi AI tidak rasmi • Bukan aplikasi rasmi", 11f).apply {
            setPadding(0, dp(6), 0, 0)
        })
        scroll.addView(body)
        root.addView(scroll)
        setContentView(root)
        allTab.requestFocus()
    }

    private fun navButton(label: String, action: () -> Unit): Button = Button(this).apply {
        text = label; textSize = 14f; isAllCaps = false
        setPadding(dp(14), dp(6), dp(14), dp(6))
        layoutParams = LinearLayout.LayoutParams(-2, dp(48)).apply { marginEnd = dp(10) }
        setOnClickListener { action() }
        styleFocus(this, Color.rgb(19, 61, 99))
    }

    private fun styleFocus(view: View, base: Int) {
        view.isFocusable = true; view.isFocusableInTouchMode = true
        fun update(focused: Boolean) {
            view.background = shape(if (focused) yellow else base,
                if (focused) Color.WHITE else Color.TRANSPARENT)
            if (view is TextView) view.setTextColor(if (focused) ink else Color.WHITE)
            val scale = if (focused) 1.035f else 1f
            view.animate().cancel()
            if (canAnimate()) view.animate().scaleX(scale).scaleY(scale)
                .setDuration(150).setInterpolator(DecelerateInterpolator()).start()
            else { view.scaleX = 1f; view.scaleY = 1f }
            view.elevation = if (focused) dp(4).toFloat() else 0f
        }
        update(false)
        view.setOnFocusChangeListener { _, focused -> update(focused) }
    }

    @Suppress("DEPRECATION")
    private fun discoverApps(): List<TvApp> {
        val pm = packageManager
        val entries = listOf(Intent.CATEGORY_LEANBACK_LAUNCHER, Intent.CATEGORY_LAUNCHER)
            .flatMap { category -> pm.queryIntentActivities(Intent(Intent.ACTION_MAIN).addCategory(category), 0) }
            .filter { it.activityInfo.packageName != packageName && it.activityInfo.enabled }
            .distinctBy { it.activityInfo.packageName }
            .mapNotNull { info ->
                val pkg = info.activityInfo.packageName
                val intent = pm.getLeanbackLaunchIntentForPackage(pkg) ?: pm.getLaunchIntentForPackage(pkg)
                intent?.let { TvApp(pkg, info.loadLabel(pm).toString(), it) }
            }
        return AppOrder.sort(entries) { it.title }
    }

    private fun favourites(): Set<String> = prefs.getStringSet("favourites", emptySet())?.toSet() ?: emptySet()

    private fun renderApps() {
        val focusedPackage = currentFocus?.tag as? String
        val favourites = favourites()
        val visible = if (showFavourites) apps.filter { it.packageName in favourites } else apps
        heading.text = "${if (showFavourites) "Kegemaran" else "Semua aplikasi"}  /  ${visible.size}"
        allTab.text = if (!showFavourites) "● Semua app" else "Semua app"
        favouritesTab.text = if (showFavourites) "● Kegemaran" else "Kegemaran"
        grid.removeAllViews()
        if (visible.isEmpty()) {
            grid.addView(text(if (showFavourites) "Belum ada kegemaran. Pilih Semua app, kemudian tahan OK pada app."
                else "Tiada aplikasi ditemui. Buka Tetapan TV untuk semak aplikasi peranti.", 17f).apply {
                setPadding(dp(12), dp(20), dp(12), dp(20))
            })
            if (focusedPackage != null) allTab.requestFocus()
            return
        }
        val columns = if (resources.configuration.screenWidthDp >= 800) 5 else 4
        val colors = intArrayOf(Color.rgb(21, 75, 116), Color.rgb(92, 49, 113), Color.rgb(22, 86, 79), Color.rgb(116, 49, 46))
        var restored = false
        visible.chunked(columns).forEachIndexed { rowIndex, chunk ->
            val row = LinearLayout(this).apply { clipChildren = false; clipToPadding = false }
            grid.addView(row)
            chunk.forEachIndexed { index, app ->
                val card = LinearLayout(this).apply {
                    orientation = LinearLayout.VERTICAL; gravity = Gravity.CENTER
                    setPadding(dp(8), dp(10), dp(8), dp(10))
                    tag = app.packageName
                    contentDescription = "${app.title}${if (app.packageName in favourites) ", kegemaran" else ""}"
                }
                val icon = ImageView(this).apply {
                    try { setImageDrawable(packageManager.getApplicationIcon(app.packageName)) }
                    catch (_: android.content.pm.PackageManager.NameNotFoundException) { setImageResource(android.R.drawable.sym_def_app_icon) }
                    importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
                }
                card.addView(icon, LinearLayout.LayoutParams(dp(42), dp(42)))
                card.addView(text("${if (app.packageName in favourites) "★ " else ""}${app.title}", 14f, Color.WHITE, true).apply {
                    gravity = Gravity.CENTER; maxLines = 2
                    ellipsize = android.text.TextUtils.TruncateAt.END
                    setPadding(0, dp(8), 0, 0)
                }, LinearLayout.LayoutParams(-1, -2))
                styleFocus(card, colors[(rowIndex + index) % colors.size])
                // Card labels stay white on a dark base; focused cards use a dark blue surface.
                card.setOnFocusChangeListener { _, focused ->
                    card.background = shape(if (focused) ink else colors[(rowIndex + index) % colors.size],
                        if (focused) yellow else Color.TRANSPARENT)
                    card.animate().cancel()
                    val scale = if (focused && canAnimate()) 1.035f else 1f
                    card.animate().scaleX(scale).scaleY(scale).setDuration(if (canAnimate()) 150L else 0L).start()
                }
                card.setOnClickListener { launchApp(app) }
                card.setOnLongClickListener { favouriteMenu(app); true }
                card.setOnKeyListener { _, code, event ->
                    if (code == KeyEvent.KEYCODE_MENU && event.action == KeyEvent.ACTION_UP) {
                        favouriteMenu(app); true
                    } else false
                }
                row.addView(card, LinearLayout.LayoutParams(0, dp(112), 1f).apply {
                    marginEnd = dp(10); bottomMargin = dp(10)
                })
                if (app.packageName == focusedPackage) { card.requestFocus(); restored = true }
            }
            repeat(columns - chunk.size) { row.addView(Space(this), LinearLayout.LayoutParams(0, 1, 1f).apply { marginEnd = dp(10) }) }
        }
        if (focusedPackage != null && !restored) allTab.requestFocus()
    }

    private fun favouriteMenu(app: TvApp) {
        val adding = app.packageName !in favourites()
        AlertDialog.Builder(this).setTitle(app.title)
            .setItems(arrayOf(if (adding) "Tambah kegemaran" else "Buang kegemaran", "Buka aplikasi")) { _, which ->
                if (which == 0) {
                    prefs.edit().putStringSet("favourites", AppOrder.toggle(favourites(), app.packageName)).apply()
                    renderApps()
                } else launchApp(app)
            }.setNegativeButton("Batal", null).show().also { focusFirstDialogItem(it) }
    }

    private fun launchApp(app: TvApp) = safeStart(Intent(app.intent).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    private fun safeStart(intent: Intent) {
        try { startActivity(intent) }
        catch (_: ActivityNotFoundException) { Toast.makeText(this, "Aplikasi tidak tersedia.", Toast.LENGTH_LONG).show() }
        catch (_: SecurityException) { Toast.makeText(this, "Peranti menyekat tindakan ini.", Toast.LENGTH_LONG).show() }
    }
    private fun openSettings() = safeStart(Intent(Settings.ACTION_SETTINGS))

    private fun showOptions() {
        AlertDialog.Builder(this).setTitle("Pilihan launcher")
            .setItems(arrayOf("Animasi: ${if (motion) "ON" else "OFF"}", "Pilih Home launcher", "Tentang preview")) { _, which ->
                when (which) {
                    0 -> {
                        motion = !motion
                        prefs.edit().putBoolean("motion", motion).apply()
                        startAmbient()
                        Toast.makeText(this, if (motion) "Animasi dihidupkan" else "Animasi dimatikan", Toast.LENGTH_SHORT).show()
                    }
                    1 -> {
                        val home = Intent(Settings.ACTION_HOME_SETTINGS)
                        if (home.resolveActivity(packageManager) != null) safeStart(home)
                        else { openSettings(); Toast.makeText(this, "Pilih Default apps > Home jika peranti menyokongnya.", Toast.LENGTH_LONG).show() }
                    }
                    2 -> AlertDialog.Builder(this).setTitle("Papazola × Boboiboy TV")
                        .setMessage("Preview 0.1.0 — belum disahkan stabil.\n\nIlustrasi AI berinspirasikan watak, bukan aset atau aplikasi rasmi. Tiada iklan, akaun atau sambungan internet.\n\nTekan Home untuk kembali. Sesetengah TV tidak membenarkan launcher pihak ketiga menjadi Home.")
                        .setPositiveButton("OK", null).show()
                }
            }.setNegativeButton("Tutup", null).show().also { focusFirstDialogItem(it) }
    }

    private fun focusFirstDialogItem(dialog: AlertDialog) {
        dialog.listView?.apply {
            isFocusableInTouchMode = true
            requestFocus()
            setSelection(0)
        }
    }

    private fun startAmbient() {
        pulse?.cancel()
        background.alpha = 1f
        if (!canAnimate()) return
        pulse = ValueAnimator.ofFloat(0.68f, 1f).apply {
            duration = 7000L; repeatMode = ValueAnimator.REVERSE; repeatCount = ValueAnimator.INFINITE
            addUpdateListener { background.alpha = it.animatedValue as Float }
            start()
        }
    }

    @Deprecated("Legacy back handling for API 23+ launcher")
    override fun onBackPressed() {
        if (showFavourites) { showFavourites = false; renderApps() }
        allTab.requestFocus()
    }
}
