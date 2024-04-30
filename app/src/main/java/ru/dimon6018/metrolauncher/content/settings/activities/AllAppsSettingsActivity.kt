package ru.dimon6018.metrolauncher.content.settings.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.materialswitch.MaterialSwitch
import ru.dimon6018.metrolauncher.Application.Companion.PREFS
import ru.dimon6018.metrolauncher.R
import ru.dimon6018.metrolauncher.helpers.utils.Utils
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.applyWindowInsets

class AllAppsSettingsActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(Utils.launcherAccentTheme())
        super.onCreate(savedInstanceState)
        setContentView(R.layout.launcher_settings_allapps)
        val coord = findViewById<CoordinatorLayout>(R.id.coordinator)
        applyWindowInsets(coord)
        val settBtnSwitch: MaterialSwitch = findViewById(R.id.settingsBtnSwitch)
        val alphabetSwitch: MaterialSwitch = findViewById(R.id.alphabetSwitch)
        val backgroundSwitch: MaterialSwitch = findViewById(R.id.allAppsBackgroundSwitch)
        settBtnSwitch.isChecked = PREFS!!.isSettingsBtnEnabled
        settBtnSwitch.text = if(PREFS!!.isSettingsBtnEnabled) getString(R.string.on) else getString(R.string.off)
        alphabetSwitch.isChecked = PREFS!!.isAlphabetEnabled
        alphabetSwitch.text = if(PREFS!!.isAlphabetEnabled) getString(R.string.on) else getString(R.string.off)
        backgroundSwitch.isChecked = PREFS!!.isAllAppsBackgroundEnabled
        backgroundSwitch.text = if(PREFS!!.isAllAppsBackgroundEnabled) getString(R.string.on) else getString(R.string.off)
        alphabetSwitch.setOnCheckedChangeListener { _, isChecked ->
            PREFS!!.setAlphabetActive(isChecked)
            PREFS!!.setPrefsChanged(true)
            alphabetSwitch.setChecked(PREFS!!.isAlphabetEnabled)
            alphabetSwitch.text = if(PREFS!!.isAlphabetEnabled) getString(R.string.on) else getString(R.string.off)
        }
        settBtnSwitch.setOnCheckedChangeListener { _, isChecked ->
            PREFS!!.setAllAppsSettingsBtn(isChecked)
            PREFS!!.setPrefsChanged(true)
            settBtnSwitch.setChecked(PREFS!!.isSettingsBtnEnabled)
            settBtnSwitch.text = if(PREFS!!.isSettingsBtnEnabled) getString(R.string.on) else getString(R.string.off)
        }
        backgroundSwitch.setOnCheckedChangeListener { _, isChecked ->
            PREFS!!.setAllAppsBackground(isChecked)
            PREFS!!.setPrefsChanged(true)
            backgroundSwitch.setChecked(PREFS!!.isAllAppsBackgroundEnabled)
            backgroundSwitch.text = if(PREFS!!.isAllAppsBackgroundEnabled) getString(R.string.on) else getString(R.string.off)
        }
    }
}