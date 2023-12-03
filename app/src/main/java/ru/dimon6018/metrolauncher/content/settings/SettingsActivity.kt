package ru.dimon6018.metrolauncher.content.settings

import android.app.UiModeManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.card.MaterialCardView
import ru.dimon6018.metrolauncher.Application
import ru.dimon6018.metrolauncher.R
import ru.dimon6018.metrolauncher.content.data.Prefs

class SettingsActivity : AppCompatActivity() {

    private var coord: CoordinatorLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        setAppTheme()
        setTheme(Application.getLauncherAccentTheme())
        super.onCreate(savedInstanceState)
        setContentView(R.layout.launcher_settings_main)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        coord = findViewById(R.id.coordinator)
        val themebtn = findViewById<MaterialCardView>(R.id.themeSetting)
        themebtn.setOnClickListener { startActivity(Intent(this@SettingsActivity, ThemeSettingsActivity::class.java)) }
        val aboutbtn = findViewById<MaterialCardView>(R.id.aboutSetting)
        aboutbtn.setOnClickListener { startActivity(Intent(this@SettingsActivity, AboutSettingsActivity::class.java)) }
        ViewCompat.setOnApplyWindowInsetsListener(coord!!) { v: View, insets: WindowInsetsCompat ->
            val pB = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
            val tB = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            v.setPadding(0, tB, 0, pB)
            WindowInsetsCompat.CONSUMED
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
