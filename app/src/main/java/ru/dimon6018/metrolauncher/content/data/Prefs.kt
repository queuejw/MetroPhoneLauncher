package ru.dimon6018.metrolauncher.content.data

import android.content.Context
import android.content.SharedPreferences

class Prefs(context: Context) {
    private val prefs: SharedPreferences

    init {
        prefs = context.getSharedPreferences(FILE_NAME, 0)
    }

    fun setLauncherCustomBackgrdAvailability(bool: Boolean) {
        prefs.edit().putBoolean(IS_LAUNCHER_USING_CUSTOM_BACKGRD, bool).apply()
    }

    val isCustomBackgroundUsed: Boolean
        get() = prefs.getBoolean(IS_LAUNCHER_USING_CUSTOM_BACKGRD, false)

    fun setMoreTilesPref(bool: Boolean) {
        prefs.edit().putBoolean(MORE_TILES, bool).apply()
    }

    val isMoreTilesEnabled: Boolean
        get() = prefs.getBoolean(MORE_TILES, false)

    fun setCustomBackgrdPath(path: String?) {
        prefs.edit().putString(LAUNCHER_CUSTOM_BACKGRD, path).apply()
    }

    val backgroundPath: String?
        get() = prefs.getString(LAUNCHER_CUSTOM_BACKGRD, "")

    fun useLightTheme(bool: Boolean) {
        prefs.edit().putBoolean(LAUNCHER_LIGHT_THEME, bool).apply()
    }

    val isLightThemeUsed: Boolean
        get() = prefs.getBoolean(LAUNCHER_LIGHT_THEME, false)
    var accentColor: Int
        get() = prefs.getInt(ACCENT_COLOR, 5)
        set(color) {
            // 0 - lime
            // 1 - green
            // 2 - emerald
            // 3 - cyan
            // 4 - teal
            // 5 - cobalt
            // 6 - indigo
            // 7 - violet
            // 8 - pink
            // 9 - magenta
            // 10 - crimson
            // 11 - red
            // 12 - orange
            // 13 - amber
            // 14 - yellow
            // 15 - brown
            // 16 - olive
            // 17 - steel
            // 18 - mauve
            // 19 - taupe
            isAccentChanged = true
            prefs.edit()
                    .putInt(ACCENT_COLOR, color)
                    .apply()
        }

    fun reset() {
        prefs.all.clear()
    }

    val editor: SharedPreferences.Editor
        get() = prefs.edit()

    val pref: SharedPreferences
        get() = prefs

    companion object {
        const val FILE_NAME = "Prefs"
        const val ACCENT_COLOR = "accentColor"
        const val LAUNCHER_LIGHT_THEME = "useLightTheme"
        const val LAUNCHER_CUSTOM_BACKGRD = "useCustomBackground"
        const val MORE_TILES = "isMoreTilesEnabled"
        const val IS_LAUNCHER_USING_CUSTOM_BACKGRD = "isCustomBackgrdUsing"
        var isAccentChanged = false
    }
}
