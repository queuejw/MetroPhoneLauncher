package ru.dimon6018.metrolauncher.content.settings

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.WindowCompat
import com.google.android.material.card.MaterialCardView
//import leakcanary.LeakCanary
import ru.dimon6018.metrolauncher.Application
import ru.dimon6018.metrolauncher.Application.Companion.PREFS
import ru.dimon6018.metrolauncher.R
import ru.dimon6018.metrolauncher.content.settings.activities.AboutSettingsActivity
import ru.dimon6018.metrolauncher.content.settings.activities.AllAppsSettingsActivity
import ru.dimon6018.metrolauncher.content.settings.activities.ExperimentsSettingsActivity
import ru.dimon6018.metrolauncher.content.settings.activities.FeedbackSettingsActivity
import ru.dimon6018.metrolauncher.content.settings.activities.IconSettingsActivity
import ru.dimon6018.metrolauncher.content.settings.activities.NavBarSettingsActivity
import ru.dimon6018.metrolauncher.content.settings.activities.ThemeSettingsActivity
import ru.dimon6018.metrolauncher.content.settings.activities.TileSettingsActivity
import ru.dimon6018.metrolauncher.content.settings.activities.UpdateActivity
import ru.dimon6018.metrolauncher.content.settings.activities.WeatherSettingsActivity
import ru.dimon6018.metrolauncher.helpers.WPDialog
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.accentName
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.applyWindowInsets
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.launcherAccentTheme


class SettingsActivity : AppCompatActivity() {

    private var themeSub: TextView? = null
    private var navSub: TextView? = null
    private var iconsSub: TextView? = null

    private var isDialogEnabled = true

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(launcherAccentTheme())
        setAppTheme()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.launcher_settings_main)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val cord: CoordinatorLayout = findViewById(R.id.coordinator)

        themeSub = findViewById(R.id.theme_sub)
        iconsSub = findViewById(R.id.icons_sub)
        navSub = findViewById(R.id.navbar_sub)

        applyWindowInsets(cord)
    }
    override fun onStart() {
        super.onStart()
        val themeBtn = findViewById<MaterialCardView>(R.id.themeSetting)
        themeBtn.setOnClickListener { startActivity(Intent(this@SettingsActivity, ThemeSettingsActivity::class.java)) }
        val allAppsBtn = findViewById<MaterialCardView>(R.id.allAppsSetting)
        allAppsBtn.setOnClickListener { startActivity(Intent(this@SettingsActivity, AllAppsSettingsActivity::class.java)) }
        val tilesBtn = findViewById<MaterialCardView>(R.id.tilesSetting)
        tilesBtn.setOnClickListener { startActivity(Intent(this@SettingsActivity, TileSettingsActivity::class.java)) }
        val aboutBtn = findViewById<MaterialCardView>(R.id.aboutSetting)
        aboutBtn.setOnClickListener { startActivity(Intent(this@SettingsActivity, AboutSettingsActivity::class.java)) }
        val feedbackBtn = findViewById<MaterialCardView>(R.id.feedbackSetting)
        feedbackBtn.setOnClickListener { startActivity(Intent(this@SettingsActivity, FeedbackSettingsActivity::class.java)) }
        val updateBtn = findViewById<MaterialCardView>(R.id.updatesSetting)
        updateBtn.setOnClickListener { startActivity(Intent(this@SettingsActivity, UpdateActivity::class.java)) }
        val navBarBtm = findViewById<MaterialCardView>(R.id.navbarSetting)
        navBarBtm.setOnClickListener { startActivity(Intent(this@SettingsActivity, NavBarSettingsActivity::class.java)) }
        val weatherBtm = findViewById<MaterialCardView>(R.id.weatherSetting)
        weatherBtm.setOnClickListener { startActivity(Intent(this@SettingsActivity, WeatherSettingsActivity::class.java)) }
        val iconBtn = findViewById<MaterialCardView>(R.id.iconsSetting)
        iconBtn.setOnClickListener { startActivity(Intent(this@SettingsActivity, IconSettingsActivity::class.java)) }
        val expBtn = findViewById<MaterialCardView>(R.id.expSetting)
        expBtn.setOnClickListener { startActivity(Intent(this@SettingsActivity, ExperimentsSettingsActivity::class.java)) }
        val leaks = findViewById<MaterialCardView>(R.id.leaks)
       // leaks.setOnClickListener { startActivity(LeakCanary.newLeakDisplayActivityIntent()) }
    }
    private fun setAppTheme() {
        if (PREFS!!.isLightThemeUsed) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                (application as Application).setNightMode()
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                (application as Application).setNightMode()
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)

            }
        }
    }
    private fun isHomeApp(): Boolean {
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_HOME)
        val res = packageManager.resolveActivity(intent, 0)
        return res!!.activityInfo != null && (packageName
                == res.activityInfo.packageName)
    }
    override fun onResume() {
        super.onResume()
        themeSub?.text = accentName(this)
        navSub?.text = when (PREFS!!.navBarColor) {
            0 -> getString(R.string.always_dark)
            1 -> getString(R.string.always_light)
            2 -> getString(R.string.matches_accent_color)
            3 -> getString(R.string.hide_navbar)
            4 -> getString(R.string.auto)
            else -> getString(R.string.navigation_bar_2)
        }
        try {
            iconsSub?.text =
                if (PREFS!!.iconPackPackage == "null") getString(R.string.iconPackNotSelectedSub) else packageManager.getApplicationLabel(
                    packageManager.getApplicationInfo(PREFS!!.iconPackPackage!!, 0)
                )
        } catch (e: Exception) {
            iconsSub?.text = getString(R.string.iconPackNotSelectedSub)
        }
        if(!isHomeApp() && isDialogEnabled) {
            isDialogEnabled = false
            WPDialog(this).setTopDialog(false)
                .setTitle(getString(R.string.tip))
                .setMessage(getString(R.string.setAsDefaultLauncher))
                .setNegativeButton(getString(R.string.no), null)
                .setPositiveButton(getString(R.string.yes)) {
                    startActivity(Intent(Settings.ACTION_HOME_SETTINGS))
                }.show()
        }
    }
}
