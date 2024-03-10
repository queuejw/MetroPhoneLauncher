package ru.dimon6018.metrolauncher

import android.app.Application
import android.app.DownloadManager
import android.app.UiModeManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources.Theme
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.Log
import android.util.TypedValue
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.dimon6018.metrolauncher.content.data.App
import ru.dimon6018.metrolauncher.content.data.Prefs
import ru.dimon6018.metrolauncher.content.data.bsod.BSOD
import ru.dimon6018.metrolauncher.content.data.bsod.BSODDao
import ru.dimon6018.metrolauncher.content.data.bsod.BSODEntity
import ru.dimon6018.metrolauncher.content.settings.UpdateActivity
import ru.dimon6018.metrolauncher.helpers.bsod.BsodDetector
import java.util.Calendar

class Application : Application() {

    private var context: Context? = null

    override fun onCreate() {
        context = applicationContext
        BsodDetector.setContext(context)
        Thread.setDefaultUncaughtExceptionHandler(BsodDetector())
        PREFS = Prefs(context!!)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val uiMan: UiModeManager = (context!!.getSystemService(UI_MODE_SERVICE) as UiModeManager)
            if(PREFS!!.isLightThemeUsed) {
                uiMan.setApplicationNightMode(UiModeManager.MODE_NIGHT_NO)
            } else {
                uiMan.setApplicationNightMode(UiModeManager.MODE_NIGHT_YES)
            }
        }
        super.onCreate()
    }
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
        var PREFS: Prefs? = null

        var isUpdateDownloading = false
        private var accentColors = intArrayOf(
                R.color.tile_lime, R.color.tile_green, R.color.tile_emerald, R.color.tile_cyan,
                R.color.tile_teal, R.color.tile_cobalt, R.color.tile_indigo, R.color.tile_violet,
                R.color.tile_pink, R.color.tile_magenta, R.color.tile_crimson, R.color.tile_red,
                R.color.tile_orange, R.color.tile_amber, R.color.tile_yellow, R.color.tile_brown,
                R.color.tile_olive, R.color.tile_steel, R.color.tile_mauve, R.color.tile_taupe
        )
        private var accentNames = arrayOf(
                R.string.color_lime, R.string.color_green, R.string.color_emerald, R.string.color_cyan, R.string.color_teal, R.string.color_cobalt, R.string.color_indigo, R.string.color_violet,
                R.string.color_pink, R.string.color_magenta, R.string.color_crimson, R.string.color_red, R.string.color_orange, R.string.color_amber, R.string.color_yellow, R.string.color_brown,
                R.string.color_olive, R.string.color_steel, R.string.color_mauve, R.string.color_taupe
        )
        private val themeStyles = intArrayOf(
                R.style.AppTheme_Lime, R.style.AppTheme_Green, R.style.AppTheme_Emerald,
                R.style.AppTheme_Cyan, R.style.AppTheme_Teal, R.style.AppTheme_Cobalt,
                R.style.AppTheme_Indigo, R.style.AppTheme_Violet, R.style.AppTheme_Pink,
                R.style.AppTheme_Magenta, R.style.AppTheme_Crimson, R.style.AppTheme_Red,
                R.style.AppTheme_Orange, R.style.AppTheme_Amber, R.style.AppTheme_Yellow,
                R.style.AppTheme_Brown, R.style.AppTheme_Olive, R.style.AppTheme_Steel,
                R.style.AppTheme_Mauve, R.style.AppTheme_Taupe
        )
        fun accentColorFromPrefs(context: Context): Int {
                val selectedColor = Prefs(context).accentColor
                return if (selectedColor >= 0 && selectedColor < accentColors.size) {
                    context.getColor(accentColors[selectedColor])
                } else {
                    // Default to cobalt if the selected color is out of bounds
                    context.getColor(R.color.tile_cobalt)
                }
            }
        fun getTileColorFromPrefs(tileColor: Int, context: Context): Int {
            // 0 - use accent color // 1 - lime // 2 - green // 3 - emerald // 4 - cyan
            // 5 - teal // 6 - cobalt // 7 - indigo // 8 - violet
            // 9 - pink // 10 - magenta // 11 - crimson // 12 - red
            // 13 - orange // 14 - amber // 15 - yellow // 16 - brown
            // 17 - olive // 18 - steel // 19 - mauve // 20 - taupe
            return if (tileColor >= 0 && tileColor < accentColors.size) {
                context.getColor(accentColors[tileColor])
            } else {
                // Default to cobalt if the selected color is out of bounds
                context.getColor(R.color.tile_cobalt)
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
        fun launcherAccentColor(theme: Theme): Int {
            val typedValue = TypedValue()
            theme.resolveAttribute(com.google.android.material.R.attr.colorPrimary, typedValue, true)
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
            val request = DownloadManager.Request(Uri.parse(UpdateActivity.URL_RELEASE))
            request.setDescription(context.getString(R.string.update_notification))
            request.setTitle("MPL")
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "MPL_update.apk")
            val manager = context.getSystemService(DOWNLOAD_SERVICE) as DownloadManager
            val downloadId = manager.enqueue(request)
            isUpdateDownloading = true
            val q = DownloadManager.Query()
            q.setFilterById(downloadId)
        }
        fun setUpApps(pManager: PackageManager, context: Context): MutableList<App> {
            val list = ArrayList<App>()
            val i = Intent(Intent.ACTION_MAIN, null)
            i.addCategory(Intent.CATEGORY_LAUNCHER)
            val allApps = pManager.queryIntentActivities(i, 0)
            for (ri in allApps) {
                if(ri.loadLabel(pManager).toString() == context.getString(R.string.app_name)) {
                    continue
                }
                val app = App()
                app.appLabel = ri.loadLabel(pManager).toString()
                app.appPackage = ri.activityInfo.packageName
                app.isSection = false
                list.add(app)
            }
            return list
        }
        fun saveError(e: String, db: BSOD) {
            CoroutineScope(Dispatchers.IO).launch {
                if (PREFS!!.isFeedbackEnabled) {
                    Log.e("UpdateService", e)
                    val entity = BSODEntity()
                    entity.date = Calendar.getInstance().time.toString()
                    entity.log = e
                    val pos: Int
                    when (PREFS!!.getMaxCrashLogs()) {
                        0 -> {
                            db.clearAllTables()
                            pos = db.getDao().getBsodList().size
                        }

                        1 -> {
                            if (db.getDao().getBsodList().size >= 5) {
                                db.clearAllTables()
                            }
                            pos = db.getDao().getBsodList().size
                        }

                        2 -> {
                            if (db.getDao().getBsodList().size >= 10) {
                                db.clearAllTables()
                            }
                            pos = db.getDao().getBsodList().size
                        }

                        3 -> {
                            pos = db.getDao().getBsodList().size
                        }

                        else -> {
                            pos = db.getDao().getBsodList().size
                        }
                    }
                    entity.pos = pos
                    db.getDao().insertLog(entity)
                }
            }
        }
    }
}
