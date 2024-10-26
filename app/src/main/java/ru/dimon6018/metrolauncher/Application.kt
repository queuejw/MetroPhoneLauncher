package ru.dimon6018.metrolauncher

//import ru.dimon6018.metrolauncher.content.data.ExperimentPrefs
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.pm.ActivityInfo
import android.os.Bundle
import com.google.android.material.color.DynamicColors
import ru.dimon6018.metrolauncher.content.data.Prefs
import ru.dimon6018.metrolauncher.helpers.bsod.BsodDetector
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.launcherAccentTheme

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
        if(PREFS.accentColor == 21 && DynamicColors.isDynamicColorAvailable()) {
            DynamicColors.applyToActivitiesIfAvailable(this)
        }
        super.onCreate()
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            @SuppressLint("SourceLockedOrientationActivity")
            override fun onActivityPreCreated(activity: Activity, savedInstanceState: Bundle?) {
                activity.setTheme(launcherAccentTheme())
                when(PREFS.orientation) {
                    "p" -> {
                        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
                    }
                    "l" -> {
                        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                    }
                }
                super.onActivityPreCreated(activity, savedInstanceState)
            }
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
            override fun onActivityStarted(activity: Activity) {}
            override fun onActivityResumed(activity: Activity) {}
            override fun onActivityPaused(activity: Activity) {}
            override fun onActivityStopped(activity: Activity) {}
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
            override fun onActivityDestroyed(activity: Activity) {}
        })
    }
    companion object {
        lateinit var PREFS: Prefs
        //var EXP_PREFS: ExperimentPrefs? = null
        var isUpdateDownloading = false
        var isAppOpened = false
        var isStartMenuOpened = false
    }
}
