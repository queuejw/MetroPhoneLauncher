package ru.dimon6018.metrolauncher

import android.app.Application
import android.app.UiModeManager
import android.os.Build
import com.google.android.material.color.DynamicColors
//import ru.dimon6018.metrolauncher.content.data.ExperimentPrefs
import ru.dimon6018.metrolauncher.content.data.Prefs
import ru.dimon6018.metrolauncher.helpers.bsod.BsodDetector

class Application : Application() {

    override fun onCreate() {
        if(applicationContext == null) {
            super.onCreate()
            return
        }
        BsodDetector.setContext(applicationContext)
        Thread.setDefaultUncaughtExceptionHandler(BsodDetector())
        PREFS = Prefs(applicationContext)
        //EXP_PREFS = ExperimentPrefs(applicationContext)
        setNightMode()
        if(PREFS!!.accentColor == 21 && DynamicColors.isDynamicColorAvailable()) {
            DynamicColors.applyToActivitiesIfAvailable(this)
        }
        super.onCreate()
    }
    fun setNightMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val uiMan: UiModeManager = (applicationContext.getSystemService(UI_MODE_SERVICE) as UiModeManager)
            if(PREFS!!.isLightThemeUsed) {
                uiMan.setApplicationNightMode(UiModeManager.MODE_NIGHT_NO)
            } else {
                uiMan.setApplicationNightMode(UiModeManager.MODE_NIGHT_YES)
            }
        }
    }
    companion object {
        var PREFS: Prefs? = null
        //var EXP_PREFS: ExperimentPrefs? = null

        var isUpdateDownloading = false
        var isAppOpened = false
        var isStartMenuOpened = false
    }
}
