package ru.dimon6018.metrolauncher.content.data

import android.content.Context
import android.content.SharedPreferences

class Prefs(context: Context) {

    val prefs: SharedPreferences

    private var prefsChanged = false

    private val fileName = "prefs"
    private val prefsArray = arrayOf(
        "accentColor", "crashLogs", "updateState", //0 1 2
        "updateLevel", "updateLevel", "autoUpdate", //3 4 5
        "updMessage", "versionCode", "versionCode", //6 7 8
        "updateNotification", "feedback", "launcherState", //9 10 11
        "useLightTheme", "navBarColor", "isMoreTilesEnabled", //12 13 14
        "wallpaperUsing", "iconPack", "pinNewAppsToStart", //15 16 17
        "allAppsSettingsBtnEnabled", "alphabetEnabled", "isTilesTransparent", //18 19 20
        "allAppsWallpaperBackground", "allAppsWallpaperBackground", "tilesTransparency", //21 22 23
        "searchBarEnabled", "bottomBarIcon", "isStartScreenBlocked", //24 25 26
        "transitionAnim", "tileAnim", "allAppsAnim", //27 28 29
        "allAppsAnim", "liveTileAnim", "maxResultsSearchBar", //30 31 32
        "allAppsEnabled", "tilesScreenAnim", "alphabetAnim", //33 34 35
        "autoStdwnAnimations", "bsod_output_enabled" //36 37
    )
    init {
        prefs = context.getSharedPreferences(fileName, 0)
    }

    fun setWallpaper(bool: Boolean) {
        prefs.edit().putBoolean(prefsArray[15], bool).apply()
    }

    val isWallpaperUsed: Boolean
        get() = prefs.getBoolean(prefsArray[15], false)

    fun setTransparentTiles(bool: Boolean) {
        prefs.edit().putBoolean(prefsArray[20], bool).apply()
    }
    val isTilesTransparent: Boolean
        get() =  prefs.getBoolean(prefsArray[20], false)

    fun setMoreTilesPref(bool: Boolean) {
        prefs.edit().putBoolean(prefsArray[14], bool).apply()
    }
    val isMoreTilesEnabled: Boolean
        get() =  prefs.getBoolean(prefsArray[14], false)

    fun useLightTheme(bool: Boolean) {
        prefs.edit().putBoolean(prefsArray[12], bool).apply()
    }
    fun setMaxCrashLogs(int: Int) {
        prefs.edit().putInt(prefsArray[1], int).apply()
    }
    val maxCrashLogs: Int
        get() = prefs.getInt(prefsArray[1], 1)

    val isLightThemeUsed: Boolean
        get() = prefs.getBoolean(prefsArray[12], false)

    fun setFeedback(bool: Boolean) {
        prefs.edit().putBoolean(prefsArray[10], bool).apply()
    }
    val isFeedbackEnabled: Boolean
        get() = prefs.getBoolean(prefsArray[10], true)

    fun setUpdateState(int: Int) {
        // 0 - nothing
        // 1 - checking
        // 2 - downloading
        // 3 - up-to-date
        // 4 - ready to install
        // 5 - failed
        // 6 - ready to download
        // 7 - ready to download (beta ver)
        prefs.edit().putInt(prefsArray[2], int).apply()
    }
    val updateState: Int
        get() =  prefs.getInt(prefsArray[2], 0)

    fun setUpdateProgressLevel(int: Int) {
        prefs.edit().putInt(prefsArray[3], int).apply()
    }
    val updateProgressLevel: Int
        get() =  prefs.getInt(prefsArray[3], 0)

    fun setAutoUpdate(bool: Boolean) {
        prefs.edit().putBoolean(prefsArray[5], bool).apply()
    }
    val isAutoUpdateEnabled: Boolean
        get() = prefs.getBoolean(prefsArray[5], false)

    fun setUpdateNotification(bool: Boolean) {
        prefs.edit().putBoolean(prefsArray[9], bool).apply()
    }
    val isUpdateNotificationEnabled: Boolean
        get() =  prefs.getBoolean(prefsArray[9], false)
    // is only used to save update data
    fun setVersionCode(int: Int) {
        prefs.edit().putInt(prefsArray[7], int).apply()
    }
    // is only used to save update data
    fun setUpdateMessage(value: String) {
        prefs.edit().putString(prefsArray[6], value).apply()
    }
    // is only used to save update data
    val updateMessage: String
        get() = prefs.getString(prefsArray[6], "")!!

    // is only used to save update data
    val versionCode: Int
        get() = prefs.getInt(prefsArray[7], 0)

    var accentColor: Int
        get() = prefs.getInt(prefsArray[0], 5)
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
            prefsChanged = true
            prefs.edit()
                .putInt("previous_accent_color", prefs.getInt(prefsArray[0], 5))
                .apply()
            prefs.edit()
                    .putInt(prefsArray[0], color)
                    .apply()
        }
    fun setLauncherState(int: Int) {
        // 0 - OOBE
        // 1 - nothing
        // 2 - OOBE - selecting the type of settings (recommended or customize)
        // 3 - waiting for reset (after recovery)
        prefs.edit().putInt(prefsArray[11], int).apply()
    }
    val launcherState: Int
        get() =  prefs.getInt(prefsArray[11], 0)

    fun setNavBarSetting(int: Int) {
        // 0 - always dark
        // 1 - always light
        // 2 - use accent color
        // 3 - hidden
        // 4 - auto
        prefs.edit().putInt(prefsArray[13], int).apply()
    }
    val navBarColor: Int
        get() = prefs.getInt(prefsArray[13], 4)

    fun reset() {
        prefs.edit().clear().apply()
    }
    fun setIconPack(value: String) {
        prefs.edit().putString(prefsArray[16], value).apply()
    }
    val iconPackPackage: String?
        get() = prefs.getString(prefsArray[16], "null")

    var isPrefsChanged: Boolean
        get() = prefsChanged
        set(value) {
            prefsChanged = value
        }

    fun setPinNewApps(value: Boolean) {
        prefs.edit().putBoolean(prefsArray[17], value).apply()
    }
    val pinNewApps: Boolean
        get() = prefs.getBoolean(prefsArray[17], false)

    fun setAllAppsSettingsBtn(value: Boolean) {
        prefs.edit().putBoolean(prefsArray[18], value).apply()
    }
    val isSettingsBtnEnabled: Boolean
        get() = prefs.getBoolean(prefsArray[18], false)


    fun setAlphabetActive(value: Boolean) {
        prefs.edit().putBoolean(prefsArray[19], value).apply()
    }
    val isAlphabetEnabled: Boolean
        get() = prefs.getBoolean(prefsArray[19], true)

    fun setAllAppsBackground(value: Boolean) {
        prefs.edit().putBoolean(prefsArray[21], value).apply()
    }
    val isAllAppsBackgroundEnabled: Boolean
        get() = prefs.getBoolean(prefsArray[21], false)

    fun setTileTransparency(value: Float) {
        prefs.edit().putFloat(prefsArray[23], value).apply()
    }
    val getTilesTransparency: Float
        get() = prefs.getFloat(prefsArray[23], 1.0f)

    fun setNavBarIcon(icon: Int) {
        // 0 - windows (default)
        // 1 - windows old
        // 2 - android
        prefs.edit().putInt(prefsArray[25], icon).apply()
    }
    val navBarIconValue: Int
        get() = prefs.getInt(prefsArray[25], 0)

    fun setSearchBar(value: Boolean) {
        prefs.edit().putBoolean(prefsArray[24], value).apply()
    }
    val isSearchBarEnabled: Boolean
        get() = prefs.getBoolean(prefsArray[24], false)

    fun blockStartScreen(value: Boolean) {
        prefs.edit().putBoolean(prefsArray[26], value).apply()
    }
    val isStartBlocked: Boolean
        get() = prefs.getBoolean(prefsArray[26], false)

    fun setTransitionAnim(value: Boolean) {
        prefs.edit().putBoolean(prefsArray[27], value).apply()
    }
    val isTransitionAnimEnabled: Boolean
        get() = prefs.getBoolean(prefsArray[27], true)

    fun setTilesAnim(value: Boolean) {
        prefs.edit().putBoolean(prefsArray[28], value).apply()
    }
    val isTilesAnimEnabled: Boolean
        get() = prefs.getBoolean(prefsArray[28], true)

    fun setLiveTilesAnim(value: Boolean) {
       prefs.edit().putBoolean(prefsArray[31], value).apply()
    }
    val isLiveTilesAnimEnabled: Boolean
        get() = prefs.getBoolean(prefsArray[31], true)

    fun setAllAppsAnim(value: Boolean) {
        prefs.edit().putBoolean(prefsArray[30], value).apply()
    }
    val isAAllAppsAnimEnabled: Boolean
        get() = prefs.getBoolean(prefsArray[30], true)

    fun setMaxResultCountSearchBar(size: Int) {
        prefs.edit().putInt(prefsArray[32], size).apply()
    }
    val maxResultsSearchBar: Int
        get() = prefs.getInt(prefsArray[32], 4)

    fun setAllAppsAvailability(value: Boolean) {
        prefs.edit().putBoolean(prefsArray[33], value).apply()
    }
    val isAllAppsEnabled: Boolean
        get() = prefs.getBoolean(prefsArray[33], true)

    fun setAlphabetAnim(value: Boolean) {
        prefs.edit().putBoolean(prefsArray[35], value).apply()
    }
    val isAlphabetAnimEnabled: Boolean
        get() = prefs.getBoolean(prefsArray[35], true)

    fun setTilesScreenAnim(value: Boolean) {
        prefs.edit().putBoolean(prefsArray[34], value).apply()
    }
    val isTilesScreenAnimEnabled: Boolean
        get() = prefs.getBoolean(prefsArray[34], true)

    //disabling animations if developer mode is enabled (to avoid problems)
    fun setAutoShutdownAnim(value: Boolean) {
        prefs.edit().putBoolean(prefsArray[36], value).apply()
    }
    val isAutoShutdownAnimEnabled: Boolean
        get() = prefs.getBoolean(prefsArray[36], true)

    fun setBsodOutput(value: Boolean) {
        prefs.edit().putBoolean(prefsArray[37], value).apply()
    }
    val bsodOutputEnabled: Boolean
        get() = prefs.getBoolean(prefsArray[37], false)

}
