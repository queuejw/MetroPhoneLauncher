package ru.dimon6018.metrolauncher

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.os.Build
import ru.dimon6018.metrolauncher.content.data.Prefs
import ru.dimon6018.metrolauncher.helpers.bsod.BsodDetector

class Application : Application() {
    override fun onCreate() {
        BsodDetector.setContext(applicationContext)
        Thread.setDefaultUncaughtExceptionHandler(BsodDetector())
        super.onCreate()
        appContext = applicationContext
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        var appContext: Context? = null
            private set
        const val VERSION_CODE: Int = BuildConfig.VERSION_CODE
        const val VERSION_NAME: String = BuildConfig.VERSION_NAME
        const val BUILD_TYPE: String = BuildConfig.BUILD_TYPE
        val ANDROID_VERSION: Int = Build.VERSION.SDK_INT
        val MODEL: String = Build.MODEL
        val BUILD: String = Build.DISPLAY
        val PRODUCT: String = Build.PRODUCT
        val BRAND: String = Build.BRAND
        val DEVICE: String = Build.DEVICE
        val HARDWARE: String = Build.HARDWARE
        val TIME: Long = Build.TIME
        val TAG: String = ""

        var isUpdateDownloading = false
        private var accentColors = intArrayOf(
                R.color.tile_lime, R.color.tile_green, R.color.tile_emerald, R.color.tile_cyan,
                R.color.tile_teal, R.color.tile_cobalt, R.color.tile_indigo, R.color.tile_violet,
                R.color.tile_pink, R.color.tile_magenta, R.color.tile_crimson, R.color.tile_red,
                R.color.tile_orange, R.color.tile_amber, R.color.tile_yellow, R.color.tile_brown,
                R.color.tile_olive, R.color.tile_steel, R.color.tile_mauve, R.color.tile_taupe
        )
        private var accentNames = arrayOf(
                "lime", "green", "emerald", "cyan", "teal", "cobalt", "indigo", "violet",
                "pink", "magenta", "crimson", "red", "orange", "amber", "yellow", "brown",
                "olive", "steel", "mauve", "taupe"
        )
        val accentColorFromPrefs: Int
            get() {
                val selectedColor = Prefs(appContext!!).accentColor
                return if (selectedColor >= 0 && selectedColor < accentColors.size) {
                    appContext!!.getColor(accentColors[selectedColor])
                } else {
                    // Default to cobalt if the selected color is out of bounds
                    appContext!!.getColor(R.color.tile_cobalt)
                }
            }
        fun getTileColorFromPrefs(tileColor: Int): Int {
            // 0 - use accent color
            // 1 - lime
            // 2 - green
            // 3 - emerald
            // 4 - cyan
            // 5 - teal
            // 6 - cobalt
            // 7 - indigo
            // 8 - violet
            // 9 - pink
            // 10 - magenta
            // 11 - crimson
            // 12 - red
            // 13 - orange
            // 14 - amber
            // 15 - yellow
            // 16 - brown
            // 17 - olive
            // 18 - steel
            // 19 - mauve
            // 20 - taupe
            return if (tileColor >= 0 && tileColor < accentColors.size) {
                appContext!!.getColor(accentColors[tileColor])
            } else {
                // Default to cobalt if the selected color is out of bounds
                appContext!!.getColor(R.color.tile_cobalt)
            }
        }

        val launcherAccentTheme: Int
            get() {
                val themeStyles = intArrayOf(
                        R.style.AppTheme_Lime, R.style.AppTheme_Green, R.style.AppTheme_Emerald,
                        R.style.AppTheme_Cyan, R.style.AppTheme_Teal, R.style.AppTheme_Cobalt,
                        R.style.AppTheme_Indigo, R.style.AppTheme_Violet, R.style.AppTheme_Pink,
                        R.style.AppTheme_Magenta, R.style.AppTheme_Crimson, R.style.AppTheme_Red,
                        R.style.AppTheme_Orange, R.style.AppTheme_Amber, R.style.AppTheme_Yellow,
                        R.style.AppTheme_Brown, R.style.AppTheme_Olive, R.style.AppTheme_Steel,
                        R.style.AppTheme_Mauve, R.style.AppTheme_Taupe
                )
                val selectedColor = Prefs(appContext!!).accentColor
                return if (selectedColor >= 0 && selectedColor < themeStyles.size) {
                    themeStyles[selectedColor]
                } else {
                    // Default to cobalt theme if the selected color is out of bounds
                    R.style.AppTheme_Cobalt
                }
            }
        val accentName: String
            get() {
                val selectedColor = Prefs(appContext!!).accentColor
                return if (selectedColor >= 0 && selectedColor < accentNames.size) {
                    accentNames[selectedColor]
                } else {
                    // Default to "unknown" if the selected color is out of bounds
                    "unknown"
                }
            }

        fun getTileColorName(color: Int): String {
            return if (color >= 0 && color < accentNames.size) {
                accentNames[color]
            } else {
                // Default to "unknown" if the selected color is out of bounds
                "unknown"
            }
        }
    }
}
