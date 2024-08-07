package ru.dimon6018.metrolauncher.helpers.utils

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Rect
import android.icu.text.AlphabeticIndex
import android.icu.text.UnicodeSet
import android.icu.util.ULocale
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.dimon6018.metrolauncher.Application
import ru.dimon6018.metrolauncher.Application.Companion.PREFS
import ru.dimon6018.metrolauncher.BuildConfig
import ru.dimon6018.metrolauncher.R
import ru.dimon6018.metrolauncher.content.data.Prefs
import ru.dimon6018.metrolauncher.content.data.app.App
import ru.dimon6018.metrolauncher.content.data.bsod.BSOD
import ru.dimon6018.metrolauncher.content.data.bsod.BSODEntity
import ru.dimon6018.metrolauncher.content.settings.activities.UpdateActivity
import ru.dimon6018.metrolauncher.helpers.receivers.PackageChangesReceiver
import java.util.Calendar
import java.util.Collections
import java.util.Locale
import java.util.regex.Pattern
import kotlin.random.Random


class Utils {
    companion object {

        const val VERSION_CODE: Int = BuildConfig.VERSION_CODE
        const val VERSION_NAME: String = BuildConfig.VERSION_NAME

        val ANDROID_VERSION: Int = Build.VERSION.SDK_INT
        val MODEL: String = Build.MODEL
        val BUILD: String = Build.DISPLAY
        val PRODUCT: String = Build.PRODUCT
        val BRAND: String = Build.BRAND
        val DEVICE: String = Build.DEVICE
        val HARDWARE: String = Build.HARDWARE
        val MANUFACTURER: String = Build.MANUFACTURER
        val TIME: Long = Build.TIME

        fun applyWindowInsets(target: View) {
            ViewCompat.setOnApplyWindowInsetsListener(target) { view, insets ->
                val paddingBottom =
                    insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
                val paddingTop = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
                view.setPadding(0, paddingTop, 0, paddingBottom)
                WindowInsetsCompat.CONSUMED
            }
        }

        private var accentColors = intArrayOf(
            R.color.tile_lime, R.color.tile_green, R.color.tile_emerald, R.color.tile_cyan,
            R.color.tile_teal, R.color.tile_cobalt, R.color.tile_indigo, R.color.tile_violet,
            R.color.tile_pink, R.color.tile_magenta, R.color.tile_crimson, R.color.tile_red,
            R.color.tile_orange, R.color.tile_amber, R.color.tile_yellow, R.color.tile_brown,
            R.color.tile_olive, R.color.tile_steel, R.color.tile_mauve, R.color.tile_taupe
        )
        private var accentNames = arrayOf(
            R.string.color_lime,
            R.string.color_green,
            R.string.color_emerald,
            R.string.color_cyan,
            R.string.color_teal,
            R.string.color_cobalt,
            R.string.color_indigo,
            R.string.color_violet,
            R.string.color_pink,
            R.string.color_magenta,
            R.string.color_crimson,
            R.string.color_red,
            R.string.color_orange,
            R.string.color_amber,
            R.string.color_yellow,
            R.string.color_brown,
            R.string.color_olive,
            R.string.color_steel,
            R.string.color_mauve,
            R.string.color_taupe,
            R.string.color_dynamic
        )
        private val themeStyles = intArrayOf(
            R.style.AppTheme_Lime, R.style.AppTheme_Green, R.style.AppTheme_Emerald,
            R.style.AppTheme_Cyan, R.style.AppTheme_Teal, R.style.AppTheme_Cobalt,
            R.style.AppTheme_Indigo, R.style.AppTheme_Violet, R.style.AppTheme_Pink,
            R.style.AppTheme_Magenta, R.style.AppTheme_Crimson, R.style.AppTheme_Red,
            R.style.AppTheme_Orange, R.style.AppTheme_Amber, R.style.AppTheme_Yellow,
            R.style.AppTheme_Brown, R.style.AppTheme_Olive, R.style.AppTheme_Steel,
            R.style.AppTheme_Mauve, R.style.AppTheme_Taupe,  R.style.AppTheme_Dynamic
        )

        fun accentColorFromPrefs(context: Context): Int {
            val selectedColor = Prefs(context).accentColor
            if(selectedColor == 20) {
                return launcherAccentColor(context.theme)
            }
            return if (selectedColor >= 0 && selectedColor < accentColors.size) {
                ContextCompat.getColor(context, accentColors[selectedColor])
            } else {
                // Default to cobalt if the selected color is out of bounds
                ContextCompat.getColor(context, R.color.tile_cobalt)
            }
        }

        fun getTileColorFromPrefs(tileColor: Int, context: Context): Int {
            // 0 - use accent color // 1 - lime // 2 - green // 3 - emerald // 4 - cyan
            // 5 - teal // 6 - cobalt // 7 - indigo // 8 - violet
            // 9 - pink // 10 - magenta // 11 - crimson // 12 - red
            // 13 - orange // 14 - amber // 15 - yellow // 16 - brown
            // 17 - olive // 18 - steel // 19 - mauve // 20 - taupe
            return if (tileColor >= 0 && tileColor < accentColors.size) {
                ContextCompat.getColor(context, accentColors[tileColor])
            } else {
                // Default to cobalt if the selected color is out of bounds
                ContextCompat.getColor(context, R.color.tile_cobalt)
            }
        }

        fun launcherAccentTheme(): Int {
            val selectedColor = PREFS!!.accentColor
            return if (selectedColor >= 0 && selectedColor < themeStyles.size) {
                themeStyles[selectedColor]
            } else {
                // Default to cobalt theme if the selected color is out of bounds
                R.style.AppTheme_Cobalt
            }
        }
        fun launcherAccentColor(theme: Resources.Theme): Int {
            val typedValue = TypedValue()
            theme.resolveAttribute(
                com.google.android.material.R.attr.colorPrimary,
                typedValue,
                true
            )
            return typedValue.data
        }
        fun launcherSurfaceColor(theme: Resources.Theme): Int {
            val typedValue = TypedValue()
            theme.resolveAttribute(
                com.google.android.material.R.attr.colorSurface,
                typedValue,
                true
            )
            return typedValue.data
        }
        fun accentName(context: Context): String {
            val selectedColor = PREFS!!.accentColor
            return if (selectedColor >= 0 && selectedColor < accentNames.size) {
                context.getString(accentNames[selectedColor])
            } else {
                // Default to "unknown" if the selected color is out of bounds
                "unknown"
            }
        }

        fun getTileColorName(color: Int, context: Context): String {
            return if (color >= 0 && color < accentNames.size) {
                context.getString(accentNames[color])
            } else {
                // Default to "unknown" if the selected color is out of bounds
                "unknown"
            }
        }

        fun downloadUpdate(context: Context) {
            val request = DownloadManager.Request(Uri.parse(UpdateActivity.URL_RELEASE_FILE))
            request.setDescription(context.getString(R.string.update_notification))
            request.setTitle("MPL")
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
            request.setDestinationInExternalPublicDir(
                Environment.DIRECTORY_DOWNLOADS,
                "MPL_update.apk"
            )
            val manager =
                context.getSystemService(android.app.Application.DOWNLOAD_SERVICE) as DownloadManager
            val downloadId = manager.enqueue(request)
            Application.isUpdateDownloading = true
            val q = DownloadManager.Query()
            q.setFilterById(downloadId)
        }

        fun setUpApps(pManager: PackageManager, context: Context): ArrayList<App> {
            val list = ArrayList<App>()
            val i = Intent(Intent.ACTION_MAIN, null).addCategory(Intent.CATEGORY_LAUNCHER)
            val allApps = pManager.queryIntentActivities(i, 0)
            var pos = 0
            for (app in allApps) {
                val item = App()
                item.id = pos
                item.appPackage = app.activityInfo.packageName
                item.appLabel = app.loadLabel(pManager).toString()
                if (item.appPackage == context.packageName && item.appLabel == "Leaks") {
                    continue
                }
                if (item.appPackage == context.packageName && item.appLabel == context.getString(R.string.app_name)) {
                    continue
                }
                item.type = 0
                list.add(item)
                pos += 1
            }
            return list
        }

        fun saveError(e: String, db: BSOD) {
            CoroutineScope(Dispatchers.IO).launch {
                if (PREFS!!.isFeedbackEnabled) {
                    Log.e("BSOD", e)
                    val entity = BSODEntity()
                    entity.date = Calendar.getInstance().time.toString()
                    entity.log = e
                    val dao = db.getDao()
                    val pos: Int
                    when (PREFS!!.maxCrashLogs) {
                        0 -> {
                            db.clearAllTables()
                            pos = 0
                        }
                        1 -> {
                            if (dao.getBsodList().size >= 5) {
                                dao.removeLog(dao.getBsodList().first())
                            }
                            pos = db.getDao().getBsodList().size
                        }
                        2 -> {
                            if (dao.getBsodList().size >= 10) {
                                dao.removeLog(dao.getBsodList().first())
                            }
                            pos = dao.getBsodList().size
                        }
                        else -> {
                            pos = dao.getBsodList().size
                        }
                    }
                    entity.pos = pos
                    db.getDao().insertLog(entity)
                }
            }
        }
        fun generateRandomTileSize(genBigTiles: Boolean): String {
            val int = if(!genBigTiles) Random.nextInt(0, 2) else Random.nextInt(0, 3)
            return when (int) {
                0 -> "small"
                1 -> "medium"
                2 -> "big"
                else -> "medium"
            }
        }
        fun sendCrash(text: String, activity: Activity) {
            val intent = Intent(Intent.ACTION_SENDTO)
            intent.setData(Uri.parse("mailto:dimon6018t@gmail.com"))
            intent.putExtra(Intent.EXTRA_SUBJECT, "MPL Crash report")
            intent.putExtra(Intent.EXTRA_TEXT, text)
            if (intent.resolveActivity(activity.packageManager) != null) {
                activity.startActivity(intent)
            }
        }
        @SuppressLint("InlinedApi", "UnspecifiedRegisterReceiverFlag")
        fun registerPackageReceiver(
            activity: AppCompatActivity,
            packageReceiver: PackageChangesReceiver?
        ) {
            IntentFilter().apply {
                addAction(Intent.ACTION_PACKAGE_ADDED)
                addAction(Intent.ACTION_PACKAGE_REMOVED)
                addAction(Intent.ACTION_PACKAGE_REPLACED)
                addAction(Intent.ACTION_PACKAGE_CHANGED)
                addDataScheme("package")
            }.also {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    activity.registerReceiver(packageReceiver, it, Context.RECEIVER_EXPORTED)
                } else {
                    activity.registerReceiver(packageReceiver, it)
                }
            }
        }
        fun unregisterPackageReceiver(
            activity: AppCompatActivity,
            packageReceiver: PackageChangesReceiver?
        ) {
            try {
                activity.unregisterReceiver(packageReceiver)
            } catch (w: IllegalArgumentException) {
                Log.w("Utils", "unregisterPackageReceiver error: $w")
            }
        }
        fun isScreenOn(context: Context?): Boolean {
            if (context != null) {
                val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager?
                return powerManager?.isInteractive ?: false
            } else {
                return false
            }
        }
        fun sortApps(newApps: MutableList<App>): MutableList<App> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Collections.sort(
                    newApps,
                    Comparator.comparing { app: App -> app.appLabel!![0].lowercase(Locale.getDefault()) })
            } else {
                newApps.sortWith { app1: App, app2: App ->
                    app1.appLabel!![0].lowercase(Locale.getDefault())
                        .compareTo(app2.appLabel!![0].lowercase(Locale.getDefault()))
                }
            }
            return newApps
        }
        fun isDevMode(context: Context): Boolean {
            return Settings.Secure.getInt(context.contentResolver,
                Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0) != 0
        }
        fun setViewInteractAnimation(view: View) {
            view.setOnTouchListener { _, event ->
                val centerX = view.width / 2f
                val centerY = view.height / 2f
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        val rotationX = (event.y - centerY) / centerY * 5
                        val rotationY = (centerX - event.x) / centerX * 5
                        ObjectAnimator.ofFloat(view, "rotationX", -rotationX).setDuration(200).start()
                        ObjectAnimator.ofFloat(view, "rotationY", -rotationY).setDuration(200).start()
                    }
                    MotionEvent.ACTION_UP -> {
                        Handler(Looper.getMainLooper()).postDelayed({
                            if (event.x >= 0 && event.x <= view.width && event.y >= 0 && event.y <= view.height) {
                                view.performClick()
                            }
                        }, 200)
                        ObjectAnimator.ofFloat(view, "rotationX", 0f).setDuration(200).start()
                        ObjectAnimator.ofFloat(view, "rotationY", 0f).setDuration(200).start()
                    }
                    MotionEvent.ACTION_CANCEL -> {
                        ObjectAnimator.ofFloat(view, "rotationX", 0f).setDuration(200).start()
                        ObjectAnimator.ofFloat(view, "rotationY", 0f).setDuration(200).start()
                    }
                }
                false
            }
        }
        fun getUserLanguageRegexCompat(locale: Locale): Regex {
            return when (locale.language) {
                "ru" -> Regex("[а-яА-ЯёЁ]")
                "en" -> Regex("[a-zA-Z]")
                else -> Regex("[a-zA-Z]")
            }
        }
        @RequiresApi(Build.VERSION_CODES.N)
        fun getUserLanguageRegex(locale: Locale): Pattern {
            val uLocale = ULocale.forLocale(locale)
            val lowercaseLetters = UnicodeSet().addAll(getAlphabet(uLocale.language))
            val uppercaseLetters = UnicodeSet(lowercaseLetters).apply {
                for (char in this) {
                    this.add(char.uppercase(Locale.ROOT))
                }
            }
            lowercaseLetters.addAll(uppercaseLetters)
            val regexPattern = "[${lowercaseLetters.toPattern(false)}]"
            return Pattern.compile(regexPattern)
        }
        fun getAlphabetCompat(languageCode: String): List<String>? {
            val alphabets = mapOf(
                "en" to ('A'..'Z').map { it.toString() },
                "ru" to ('А'..'Я').map { it.toString() },
            )
            val alphabet = alphabets[languageCode] ?: alphabets["en"]
            return alphabet
        }
        @RequiresApi(Build.VERSION_CODES.N)
        fun getAlphabet(languageCode: String): List<String> {
            val index = AlphabeticIndex<String>(ULocale(languageCode))
            val alphabet: MutableList<String> = ArrayList()
            for (bucket in index) {
                if (bucket.label.isNotEmpty()) {
                    alphabet.add(bucket.label.lowercase(Locale.ROOT))
                }
            }
            return alphabet
        }
    }
    class MarginItemDecoration(private val spaceSize: Int) : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(
            outRect: Rect, view: View,
            parent: RecyclerView,
            state: RecyclerView.State
        ) {
            with(outRect) {
                top = spaceSize
                left = spaceSize
                right = spaceSize
                bottom = spaceSize
            }
        }
    }
}