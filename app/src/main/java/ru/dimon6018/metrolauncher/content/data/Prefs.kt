package ru.dimon6018.metrolauncher.content.data

import android.content.Context
import android.content.SharedPreferences

class Prefs(context: Context) {

    val prefs: SharedPreferences

    private var prefsChanged = false

    private val fileName = "Prefs"

    private val accentColorPref = "accentColor"
    private val maxCrashLogsPref = "maxCrashLogs"
    private val updateStatePref = "updateState"
    private val updatePercentagePref = "updateLevel"
    private val autoUpdatePref = "autoUpdateEnabled"
    private val updateMessagePref = "updateMessage"
    private val versionCodePref = "versionCode"
    private val updateNotificationPref = "updateNotificationEnabled"
    private val feedbackPref = "feedbackEnabled"
    private val launcherStatePref = "launcherState"
    private val lightThemePref = "useLightTheme"
    private val navbarColorPref = "navBarColor"
    private val showMoreTilesPref = "isMoreTilesEnabled"
    private val wallpaperPref = "wallpaperUsing"
    private val iconPackPref = "iconPack"
    private val autoPinPref = "autoPinApp"
    private val settingsBtnPref = "allAppsSettingsBtnEnabled"
    private val alphabetPref = "alphabetEnabled"
    private val parallaxTilesPref = "parallaxEnabled"
    private val allAppsBackgroundPref = "allAppsWallpaperBackground"
    private val tilesTransparencyPref = "tilesTransparency"
    private val searchBarPref = "searchBarEnabled"
    private val bottomBarIconPref = "bottomBarIcon"
    private val startBlockPref = "isStartBlocked"
    private val transitionAnimationPref = "transitionAnim"
    private val tilesAnimationsPref = "tileAnim"
    private val allAppsAnimationsPref = "allAppsAnim"
    private val liveTilesAnimPref = "liveTileAnim"
    private val searchBarMaxResultsPref = "maxResultsSearchBar"
    private val allAppsScreenPref = "allAppsEnabled"
    private val startScreenAnimPrefs = "startScreenAnim"
    private val alphabetAnimPrefs = "alphabetAnim"
    private val autoShutdownAnimPref = "autoStdwnAnimations"
    private val bsodOutputPref = "bsodOutputEnabled"

    init {
        prefs = context.getSharedPreferences(fileName, 0)
    }

    fun setWallpaper(bool: Boolean) {
        prefs.edit().putBoolean(wallpaperPref, bool).apply()
    }

    val isWallpaperUsed: Boolean
        get() = prefs.getBoolean(wallpaperPref, false)

    fun setTransparentTiles(bool: Boolean) {
        prefs.edit().putBoolean(parallaxTilesPref, bool).apply()
    }
    val isTilesTransparent: Boolean
        get() =  prefs.getBoolean(parallaxTilesPref, false)

    fun setMoreTilesPref(bool: Boolean) {
        prefs.edit().putBoolean(showMoreTilesPref, bool).apply()
    }

    val isMoreTilesEnabled: Boolean
        get() =  prefs.getBoolean(showMoreTilesPref, false)

    fun useLightTheme(bool: Boolean) {
        prefs.edit().putBoolean(lightThemePref, bool).apply()
    }
    fun setMaxCrashLogs(int: Int) {
        prefs.edit().putInt(maxCrashLogsPref, int).apply()
    }
    val maxCrashLogs: Int
        get() = prefs.getInt(maxCrashLogsPref, 1)
    val isLightThemeUsed: Boolean
        get() = prefs.getBoolean(lightThemePref, false)

    fun setFeedback(bool: Boolean) {
        prefs.edit().putBoolean(feedbackPref, bool).apply()
    }
    val isFeedbackEnabled: Boolean
        get() = prefs.getBoolean(feedbackPref, true)

    fun setUpdateState(int: Int) {
        // 0 - nothing
        // 1 - checking
        // 2 - downloading
        // 3 - up-to-date
        // 4 - ready to install
        // 5 - failed
        // 6 - ready to download
        // 7 - ready to download (beta ver)
        prefs.edit().putInt(updateStatePref, int).apply()
    }
    val updateState: Int
        get() =  prefs.getInt(updateStatePref, 0)

    fun setUpdateProgressLevel(int: Int) {
        prefs.edit().putInt(updatePercentagePref, int).apply()
    }
    val updateProgressLevel: Int
        get() =  prefs.getInt(updatePercentagePref, 0)

    fun setAutoUpdate(bool: Boolean) {
        prefs.edit().putBoolean(autoUpdatePref, bool).apply()
    }
    val isAutoUpdateEnabled: Boolean
        get() = prefs.getBoolean(autoUpdatePref, false)

    fun setUpdateNotification(bool: Boolean) {
        prefs.edit().putBoolean(updateNotificationPref, bool).apply()
    }
    val isUpdateNotificationEnabled: Boolean
        get() =  prefs.getBoolean(updateNotificationPref, false)
    // is only used to save update data
    fun setVersionCode(int: Int) {
        prefs.edit().putInt(versionCodePref, int).apply()
    }
    // is only used to save update data
    fun setUpdateMessage(value: String) {
        prefs.edit().putString(updateMessagePref, value).apply()
    }
    // is only used to save update data
    val updateMessage: String
        get() = prefs.getString(updateMessagePref, "")!!

    // is only used to save update data
    val versionCode: Int
        get() = prefs.getInt(versionCodePref, 0)

    var accentColor: Int
        get() = prefs.getInt(accentColorPref, 5)
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
                .putInt("previous_accent_color", prefs.getInt(accentColorPref, 5))
                .apply()
            prefs.edit()
                    .putInt(accentColorPref, color)
                    .apply()
        }
    fun setLauncherState(int: Int) {
        // 0 - OOBE
        // 1 - nothing
        // 2 - OOBE - selecting the type of settings (recommended or customize)
        // 3 - waiting for reset (after recovery)
        prefs.edit().putInt(launcherStatePref, int).apply()
    }
    val launcherState: Int
        get() =  prefs.getInt(launcherStatePref, 0)

    fun setNavBarSetting(int: Int) {
        // 0 - always dark
        // 1 - always light
        // 2 - use accent color
        // 3 - hidden
        // 4 - auto
        prefs.edit().putInt(navbarColorPref, int).apply()
    }
    val navBarColor: Int
        get() = prefs.getInt(navbarColorPref, 4)

    fun reset() {
        prefs.edit().clear().apply()
    }
    fun setIconPack(value: String) {
        prefs.edit().putString(iconPackPref, value).apply()
    }
    val iconPackPackage: String?
        get() = prefs.getString(iconPackPref, "null")

    var isPrefsChanged: Boolean
        get() = prefsChanged
        set(value) {
            prefsChanged = value
        }
    fun setPinNewApps(value: Boolean) {
        prefs.edit().putBoolean(autoPinPref, value).apply()
    }
    val pinNewApps: Boolean
        get() = prefs.getBoolean(autoPinPref, false)

    fun setAllAppsSettingsBtn(value: Boolean) {
        prefs.edit().putBoolean(settingsBtnPref, value).apply()
    }
    val isSettingsBtnEnabled: Boolean
        get() = prefs.getBoolean(settingsBtnPref, false)


    fun setAlphabetActive(value: Boolean) {
        prefs.edit().putBoolean(alphabetPref, value).apply()
    }
    val isAlphabetEnabled: Boolean
        get() = prefs.getBoolean(alphabetPref, true)

    fun setAllAppsBackground(value: Boolean) {
        prefs.edit().putBoolean(allAppsBackgroundPref, value).apply()
    }
    val isAllAppsBackgroundEnabled: Boolean
        get() = prefs.getBoolean(allAppsBackgroundPref, false)

    fun setTileTransparency(value: Float) {
        prefs.edit().putFloat(tilesTransparencyPref, value).apply()
    }
    val getTilesTransparency: Float
        get() = prefs.getFloat(tilesTransparencyPref, 1.0f)

    fun setNavBarIcon(icon: Int) {
        // 0 - windows (default)
        // 1 - windows old
        // 2 - android
        prefs.edit().putInt(bottomBarIconPref, icon).apply()
    }
    val navBarIconValue: Int
        get() = prefs.getInt(bottomBarIconPref, 0)

    fun setSearchBar(value: Boolean) {
        prefs.edit().putBoolean(searchBarPref, value).apply()
    }
    val isSearchBarEnabled: Boolean
        get() = prefs.getBoolean(searchBarPref, false)

    fun blockStartScreen(value: Boolean) {
        prefs.edit().putBoolean(startBlockPref, value).apply()
    }
    val isStartBlocked: Boolean
        get() = prefs.getBoolean(startBlockPref, false)

    fun setTransitionAnim(value: Boolean) {
        prefs.edit().putBoolean(transitionAnimationPref, value).apply()
    }
    val isTransitionAnimEnabled: Boolean
        get() = prefs.getBoolean(transitionAnimationPref, true)

    fun setTilesAnim(value: Boolean) {
        prefs.edit().putBoolean(tilesAnimationsPref, value).apply()
    }
    val isTilesAnimEnabled: Boolean
        get() = prefs.getBoolean(tilesAnimationsPref, true)

    fun setLiveTilesAnim(value: Boolean) {
       prefs.edit().putBoolean(liveTilesAnimPref, value).apply()
    }
    val isLiveTilesAnimEnabled: Boolean
        get() = prefs.getBoolean(liveTilesAnimPref, true)

    fun setAllAppsAnim(value: Boolean) {
        prefs.edit().putBoolean(allAppsAnimationsPref, value).apply()
    }
    val isAAllAppsAnimEnabled: Boolean
        get() = prefs.getBoolean(allAppsAnimationsPref, true)

    fun setMaxResultCountSearchBar(size: Int) {
        prefs.edit().putInt(searchBarMaxResultsPref, size).apply()
    }
    val maxResultsSearchBar: Int
        get() = prefs.getInt(searchBarMaxResultsPref, 4)

    fun setAllAppsAvailability(value: Boolean) {
        prefs.edit().putBoolean(allAppsScreenPref, value).apply()
    }
    val isAllAppsEnabled: Boolean
        get() = prefs.getBoolean(allAppsScreenPref, true)

    fun setAlphabetAnim(value: Boolean) {
        prefs.edit().putBoolean(alphabetAnimPrefs, value).apply()
    }
    val isAlphabetAnimEnabled: Boolean
        get() = prefs.getBoolean(alphabetAnimPrefs, true)

    fun setTilesScreenAnim(value: Boolean) {
        prefs.edit().putBoolean(startScreenAnimPrefs, value).apply()
    }
    val isTilesScreenAnimEnabled: Boolean
        get() = prefs.getBoolean(startScreenAnimPrefs, true)

    //disabling animations if developer mode is enabled (to avoid problems)
    fun setAutoShutdownAnim(value: Boolean) {
        prefs.edit().putBoolean(autoShutdownAnimPref, value).apply()
    }
    val isAutoShutdownAnimEnabled: Boolean
        get() = prefs.getBoolean(autoShutdownAnimPref, true)

    fun setBsodOutput(value: Boolean) {
        prefs.edit().putBoolean(bsodOutputPref, value).apply()
    }
    val bsodOutputEnabled: Boolean
        get() = prefs.getBoolean(bsodOutputPref, false)

    var iconPackChanged: Boolean
        get() = prefs.getBoolean("iconPackChanged", false)
        set(value) {
            prefs.edit().putBoolean("iconPackChanged", value).apply()
        }

}
