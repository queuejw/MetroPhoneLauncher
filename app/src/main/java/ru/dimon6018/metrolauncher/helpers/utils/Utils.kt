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
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
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
import ru.dimon6018.metrolauncher.content.data.tile.Tile
import ru.dimon6018.metrolauncher.content.data.tile.TileDao
import ru.dimon6018.metrolauncher.content.settings.activities.UpdateActivity
import ru.dimon6018.metrolauncher.helpers.receivers.PackageChangesReceiver
import java.io.File
import java.util.Calendar
import java.util.Locale
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
                val systemBarInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                view.updatePadding(
                    bottom = systemBarInsets.bottom,
                    left = systemBarInsets.left,
                    right = systemBarInsets.right,
                    top = systemBarInsets.top
                )
                insets
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
            R.style.AppTheme_Mauve, R.style.AppTheme_Taupe, R.style.AppTheme_Dynamic
        )

        fun accentColorFromPrefs(context: Context): Int {
            val selectedColor = Prefs(context).accentColor
            if (selectedColor == 20) {
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
            val selectedColor = PREFS.accentColor
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

        fun launcherBackgroundColor(theme: Resources.Theme): Int {
            val typedValue = TypedValue()
            theme.resolveAttribute(
                android.R.attr.colorBackground,
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

        fun launcherOnSurfaceColor(theme: Resources.Theme): Int {
            val typedValue = TypedValue()
            theme.resolveAttribute(
                com.google.android.material.R.attr.colorOnSurface,
                typedValue,
                true
            )
            return typedValue.data
        }

        fun accentName(context: Context): String {
            val selectedColor = PREFS.accentColor
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

        fun setUpApps(pManager: PackageManager, context: Context): MutableList<App> {
            val appList = ArrayList<App>()
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
                appList.add(item)
                pos += 1
            }
            appList.sortBy { it.appLabel }
            return appList
        }

        fun saveError(e: String, db: BSOD) {
            CoroutineScope(Dispatchers.IO).launch {
                if (PREFS.isFeedbackEnabled) {
                    Log.e("BSOD", e)
                    val entity = BSODEntity()
                    entity.date = Calendar.getInstance().time.toString()
                    entity.log = e
                    val dao = db.getDao()
                    val pos: Int
                    when (PREFS.maxCrashLogs) {
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
            val int = if (!genBigTiles) Random.nextInt(0, 2) else Random.nextInt(0, 3)
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
                return powerManager?.isInteractive == true
            } else {
                return false
            }
        }

        fun isDevMode(context: Context): Boolean {
            return Settings.Secure.getInt(
                context.contentResolver,
                Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0
            ) != 0
        }

        @SuppressLint("ClickableViewAccessibility")
        fun setViewInteractAnimation(view: View) {
            view.setOnTouchListener { _, event ->
                val centerX = view.width / 2f
                val centerY = view.height / 2f
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        val rotationX = (event.y - centerY) / centerY * 5
                        val rotationY = (centerX - event.x) / centerX * 5
                        ObjectAnimator.ofFloat(view, "rotationX", -rotationX).setDuration(200)
                            .start()
                        ObjectAnimator.ofFloat(view, "rotationY", -rotationY).setDuration(200)
                            .start()
                    }

                    MotionEvent.ACTION_UP -> {
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
        suspend fun generatePlaceholder(call: TileDao, value: Int) {
            val size = value
            val startFrom = call.getTilesList().size
            val end = startFrom + size
            for (i in startFrom..end) {
                val placeholder = Tile(
                    i, (i + 1).toLong(), -1, -1,
                    isSelected = false,
                    tileSize = "small",
                    tileLabel = "",
                    tilePackage = ""
                )
                call.addTile(placeholder)
            }
        }
        fun checkStoragePermissions(context: Context): Boolean {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                Environment.isExternalStorageManager()
            } else {
                val write =
                    ContextCompat.checkSelfPermission(
                        context,
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                val read =
                    ContextCompat.checkSelfPermission(
                        context,
                        android.Manifest.permission.READ_EXTERNAL_STORAGE
                    )
                read == PackageManager.PERMISSION_GRANTED && write == PackageManager.PERMISSION_GRANTED
            }
        }

        fun getCustomFont(): Typeface? {
            val path = PREFS.customFontPath
            if (!PREFS.customFontInstalled || path == null) return null
            return path.let {
                val fontFile = File(it)
                if (fontFile.exists()) {
                    val typeface = Typeface.createFromFile(fontFile)
                    typeface
                } else {
                    PREFS.customFontPath = null
                    PREFS.customFontInstalled = false
                    PREFS.customFontName = null
                    null
                }
            }
        }

        fun getCustomLightFont(): Typeface? {
            val path = PREFS.customLightFontPath
            if (!PREFS.customFontInstalled || path == null) return null
            return path.let {
                val fontFile = File(it)
                if (fontFile.exists()) {
                    val typeface = Typeface.createFromFile(fontFile)
                    typeface
                } else {
                    PREFS.customLightFontPath = null
                    PREFS.customLightFontName = null
                    null
                }
            }
        }

        fun getCustomBoldFont(): Typeface? {
            val path = PREFS.customBoldFontPath
            if (!PREFS.customFontInstalled || path == null) return null
            return path.let {
                val fontFile = File(it)
                if (fontFile.exists()) {
                    val typeface = Typeface.createFromFile(fontFile)
                    typeface
                } else {
                    PREFS.customBoldFontPath = null
                    PREFS.customBoldFontName = null
                    null
                }
            }
        }

        fun getDefaultLocale(): Locale {
            return Locale.getDefault()
        }
    }

    class MarginItemDecoration(private val spaceSize: Int) : ItemDecoration() {
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

    class BottomOffsetDecoration(private val bottomOffset: Int) : ItemDecoration() {
        override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
        ) {
            if (parent.getChildAdapterPosition(view) == parent.adapter!!.itemCount - 1) {
                outRect.bottom = bottomOffset
            }
        }
    }
}