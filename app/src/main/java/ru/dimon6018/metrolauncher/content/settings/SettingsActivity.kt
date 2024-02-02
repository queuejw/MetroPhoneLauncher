package ru.dimon6018.metrolauncher.content.settings

import android.app.UiModeManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.WindowCompat
import com.google.android.material.card.MaterialCardView
import leakcanary.LeakCanary
import ru.dimon6018.metrolauncher.Application
import ru.dimon6018.metrolauncher.Application.Companion.PREFS
import ru.dimon6018.metrolauncher.Main
import ru.dimon6018.metrolauncher.R
import ru.dimon6018.metrolauncher.content.data.Prefs

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        setAppTheme()
        setTheme(Application.launcherAccentTheme())
        super.onCreate(savedInstanceState)
        setContentView(R.layout.launcher_settings_main)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val coord: CoordinatorLayout = findViewById(R.id.coordinator)
        val themebtn = findViewById<MaterialCardView>(R.id.themeSetting)
        themebtn.setOnClickListener { startActivity(Intent(this@SettingsActivity, ThemeSettingsActivity::class.java)) }
        val aboutbtn = findViewById<MaterialCardView>(R.id.aboutSetting)
        aboutbtn.setOnClickListener { startActivity(Intent(this@SettingsActivity, AboutSettingsActivity::class.java)) }
        val feedbackBtn = findViewById<MaterialCardView>(R.id.feedbackSetting)
        feedbackBtn.setOnClickListener { startActivity(Intent(this@SettingsActivity, FeedbackSettingsActivity::class.java)) }
        val updateBtn = findViewById<MaterialCardView>(R.id.updatesSetting)
        updateBtn.setOnClickListener { startActivity(Intent(this@SettingsActivity, UpdateActivity::class.java)) }
        val navBarBtm = findViewById<MaterialCardView>(R.id.navbarSetting)
        navBarBtm.setOnClickListener { startActivity(Intent(this@SettingsActivity, NavBarSettingsActivity::class.java)) }
        val leaks = findViewById<MaterialCardView>(R.id.leaks)
        leaks.setOnClickListener { startActivity(LeakCanary.newLeakDisplayActivityIntent().addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
        Main.applyWindowInsets(coord)
    }

    override fun onResume() {
        super.onResume()
        val themeSub: TextView = findViewById(R.id.theme_sub)
        themeSub.text = Application.accentName()
        val navbarSub: TextView = findViewById(R.id.navbar_sub)
        navbarSub.text = when(PREFS!!.navBarColor) {
            0 -> getString(R.string.always_dark)
            1 -> getString(R.string.always_light)
            2 -> getString(R.string.matches_accent_color)
            3 -> getString(R.string.hide_navbar)
            4 -> getString(R.string.auto)
            else -> getString(R.string.navigation_bar_2)
        }
    }
    private fun setAppTheme() {
        val uimanager = getSystemService(UI_MODE_SERVICE) as UiModeManager
        if (Prefs(this).isLightThemeUsed) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                uimanager.setApplicationNightMode(UiModeManager.MODE_NIGHT_NO)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                uimanager.setApplicationNightMode(UiModeManager.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
        }
    }
}
