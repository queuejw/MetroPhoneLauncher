package ru.dimon6018.metrolauncher.content.data

import android.content.Context
import android.content.SharedPreferences

class Prefs(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(FILE_NAME, 0)
    private var isPrefsChanged = false

    fun setWallpaper(bool: Boolean) {
        prefs.edit().putBoolean(WALLPAPER_USING, bool).apply()
    }

    val isWallpaperUsed: Boolean
        get() = prefs.getBoolean(WALLPAPER_USING, false)

    fun setTransparentTiles(bool: Boolean) {
        prefs.edit().putBoolean(TRANSPARENT_TILES, bool).apply()
    }
    val isTilesTransparent: Boolean
        get() = prefs.getBoolean(TRANSPARENT_TILES, false)

    fun setMoreTilesPref(bool: Boolean) {
        prefs.edit().putBoolean(MORE_TILES, bool).apply()
    }

    val isMoreTilesEnabled: Boolean
        get() = prefs.getBoolean(MORE_TILES, false)

    fun useLightTheme(bool: Boolean) {
        prefs.edit().putBoolean(LAUNCHER_LIGHT_THEME, bool).apply()
    }
    fun setMaxCrashLogs(int: Int) {
        prefs.edit().putInt(MAX_CRASH_LOGS, int).apply()
    }
    fun getMaxCrashLogs(): Int {
        // 0 - 1
        // 1 - 5
        // 2 - 10
        // 3 - no limit
        return prefs.getInt(MAX_CRASH_LOGS, 1)
    }
    val isLightThemeUsed: Boolean
        get() = prefs.getBoolean(LAUNCHER_LIGHT_THEME, false)
    fun setFeedback(bool: Boolean) {
        prefs.edit().putBoolean(FEEDBACK, bool).apply()
    }
    val isFeedbackEnabled: Boolean
        get() = prefs.getBoolean(FEEDBACK, true)
    fun setUpdateState(int: Int) {
        // 0 - nothing
        // 1 - checking
        // 2 - downloading
        // 3 - up-to-date
        // 4 - ready to install
        // 5 - failed
        // 6 - ready to download
        // 7 - ready to download (beta ver)
        prefs.edit().putInt(UPDATE_STATE, int).apply()
    }
    val updateState: Int
        get() = prefs.getInt(UPDATE_STATE, 0)

    fun setUpdateProgressLevel(int: Int) {
        prefs.edit().putInt(UPDATE_LEVEL, int).apply()
    }
    val updateProgressLevel: Int
        get() = prefs.getInt(UPDATE_LEVEL, 0)

    fun setAutoUpdate(bool: Boolean) {
        prefs.edit().putBoolean(AUTO_UPDATE, bool).apply()
    }
    val isAutoUpdateEnabled: Boolean
        get() = prefs.getBoolean(AUTO_UPDATE, false)
    fun setUpdateNotification(bool: Boolean) {
        prefs.edit().putBoolean(UPDATE_NOTIFICATION, bool).apply()
    }
    val isUpdateNotificationEnabled: Boolean
        get() = prefs.getBoolean(UPDATE_NOTIFICATION, false)

    fun setVersionCode(int: Int) {
        prefs.edit().putInt(VERSION_CODE, int).apply()
    }
    fun setUpdateMessage(value: String) {
        prefs.edit().putString(UPDATE_MESSAGE, value).apply()
    }
    val updateMessage: String
        get() = prefs.getString(UPDATE_MESSAGE, "").toString()
    val versionCode: Int
        get() = prefs.getInt(VERSION_CODE, 0)

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
            // 20 - dynamic colors
            isPrefsChanged = true
            prefs.edit()
                .putInt("previous_accent_color", prefs.getInt(ACCENT_COLOR, 5))
                .apply()
            prefs.edit()
                    .putInt(ACCENT_COLOR, color)
                    .apply()
        }
    fun setLauncherState(int: Int) {
        // 0 - OOBE
        // 1 - nothing
        // 2 - OOBE - selecting the type of settings (recommended or customize)
        // 3 - waiting for reset (after recovery)
        prefs.edit().putInt(STATE, int).apply()
    }
    val launcherState: Int
        get() = prefs.getInt(STATE, 0)

    fun setNavBarSetting(int: Int) {
        // 0 - always dark
        // 1 - always light
        // 2 - use accent color
        // 3 - hidden
        // 4 - auto
        prefs.edit().putInt(NAVBAR_COLOR, int).apply()
    }
    val navBarColor: Int
        get() = prefs.getInt(NAVBAR_COLOR, 4)

    fun reset() {
        prefs.edit().clear().apply()
    }

    fun setIconPack(value: String) {
        prefs.edit().putString(ICON_PACK, value).apply()
    }
    val iconPackPackage: String?
        get() = prefs.getString(ICON_PACK, "null")

    val editor: SharedPreferences.Editor
        get() = prefs.edit()
    val pref: SharedPreferences
        get() = prefs

    fun isPrefsChanged(): Boolean {
        return isPrefsChanged
    }
    fun setPrefsChanged(bool: Boolean) {
        isPrefsChanged = bool
    }
    fun setPinNewApps(value: Boolean) {
        prefs.edit().putBoolean(PIN_NEW_APP, value).apply()
    }
    val pinNewApps: Boolean
        get() = prefs.getBoolean(PIN_NEW_APP, false)

    fun setAllAppsSettingsBtn(value: Boolean) {
        prefs.edit().putBoolean(ALL_APPS_SETTINGS_BTN, value).apply()
    }
    val isSettingsBtnEnabled: Boolean
        get() = prefs.getBoolean(ALL_APPS_SETTINGS_BTN, false)

    fun setAlphabetActive(value: Boolean) {
        prefs.edit().putBoolean(ALPHABET_ENABLED, value).apply()
    }
    val isAlphabetEnabled: Boolean
        get() = prefs.getBoolean(ALPHABET_ENABLED, true)

    fun setAllAppsBackground(value: Boolean) {
        prefs.edit().putBoolean(ALL_APPS_BACKGROUND, value).apply()
    }
    val isAllAppsBackgroundEnabled: Boolean
        get() = prefs.getBoolean(ALL_APPS_BACKGROUND, false)

    fun setTileTransparency(value: Float) {
        prefs.edit().putFloat(TILES_TRANSPARENCY, value).apply()
    }
    val getTilesTransparency: Float
        get() = prefs.getFloat(TILES_TRANSPARENCY, 1.0f)

    fun setNavBarIcon(icon: Int) {
        // 0 - windows (default)
        // 1 - windows old
        // 2 - android
        prefs.edit().putInt(BOTTOM_BAR_MAIN_ICON, icon).apply()
    }
    val navBarIconValue: Int
        get() = prefs.getInt(BOTTOM_BAR_MAIN_ICON, 0)

    fun setSearchBar(value: Boolean) {
        prefs.edit().putBoolean(SEARCH_BAR, value).apply()
    }
    val isSearchBarEnabled: Boolean
        get() = prefs.getBoolean(SEARCH_BAR, false)

    fun blockStartScreen(value: Boolean) {
        prefs.edit().putBoolean(START_BLOCKED, value).apply()
    }
    val isStartBlocked: Boolean
        get() = prefs.getBoolean(START_BLOCKED, false)

    fun setTransitionAnim(value: Boolean) {
        prefs.edit().putBoolean(TRANSITION_ANIMATION, value).apply()
    }
    val isTransitionAnimEnabled: Boolean
        get() = prefs.getBoolean(TRANSITION_ANIMATION, true)

    fun setTilesAnim(value: Boolean) {
        prefs.edit().putBoolean(TILES_ANIMATIONS, value).apply()
    }
    val isTilesAnimEnabled: Boolean
        get() = prefs.getBoolean(TILES_ANIMATIONS, true)

    fun setLiveTilesAnim(value: Boolean) {
       prefs.edit().putBoolean(LIVE_TILES_ANIMATIONS, value).apply()
    }
    val isLiveTilesAnimEnabled: Boolean
        get() = prefs.getBoolean(LIVE_TILES_ANIMATIONS, true)

    fun setAllAppsAnim(value: Boolean) {
        prefs.edit().putBoolean(ALL_APPS_ANIMATIONS, value).apply()
    }
    val isAAllAppsAnimEnabled: Boolean
        get() = prefs.getBoolean(ALL_APPS_ANIMATIONS, true)

    fun setMaxResultCountSearchBar(size: Int) {
        prefs.edit().putInt(BOTTOM_SEARCH_BAR_MAX_RESULTS, size).apply()
    }
    val maxResultsSearchBar: Int
        get() = prefs.getInt(BOTTOM_SEARCH_BAR_MAX_RESULTS, 4)

    companion object {
        const val FILE_NAME = "Prefs"
        const val ACCENT_COLOR = "accentColor"
        const val MAX_CRASH_LOGS = "crashLogs"
        const val UPDATE_STATE = "updateState"
        const val UPDATE_LEVEL = "updateLevel"
        const val AUTO_UPDATE = "autoUpdate"
        const val UPDATE_MESSAGE = "updMessage"
        const val VERSION_CODE = "versionCode"
        const val UPDATE_NOTIFICATION = "updateNotification"
        const val FEEDBACK = "feedback"
        const val STATE = "launcherState"
        const val LAUNCHER_LIGHT_THEME = "useLightTheme"
        const val NAVBAR_COLOR = "navBarColor"
        const val MORE_TILES = "isMoreTilesEnabled"
        const val WALLPAPER_USING = "wallpaperUsing"
        const val ICON_PACK = "iconPack"
        const val PIN_NEW_APP = "pinNewAppsToStart"
        const val ALL_APPS_SETTINGS_BTN = "allAppsSettingsBtnEnabled"
        const val ALPHABET_ENABLED = "alphabetEnabled"
        const val TRANSPARENT_TILES = "isTilesTransparent"
        const val ALL_APPS_BACKGROUND = "allAppsWallpaperBackground"
        const val TILES_TRANSPARENCY = "tilesTransparency"
        const val SEARCH_BAR = "searchBar"
        const val BOTTOM_BAR_MAIN_ICON = "bottomBarIcon"
        const val START_BLOCKED = "isStartScreenBlocked"
        const val TRANSITION_ANIMATION = "transitionAnim"
        const val TILES_ANIMATIONS = "tileAnim"
        const val ALL_APPS_ANIMATIONS = "allAppsAnim"
        const val LIVE_TILES_ANIMATIONS = "liveTileAnim"
        const val BOTTOM_SEARCH_BAR_MAX_RESULTS = "maxResultsSearchBar"
    }
}
