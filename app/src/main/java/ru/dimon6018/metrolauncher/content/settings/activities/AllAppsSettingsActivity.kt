package ru.dimon6018.metrolauncher.content.settings.activities

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.materialswitch.MaterialSwitch
import ru.dimon6018.metrolauncher.Application.Companion.PREFS
import ru.dimon6018.metrolauncher.R
import ru.dimon6018.metrolauncher.helpers.utils.Utils
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.applyWindowInsets

class AllAppsSettingsActivity: AppCompatActivity() {

    private var main: CoordinatorLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(Utils.launcherAccentTheme())
        super.onCreate(savedInstanceState)
        setContentView(R.layout.launcher_settings_allapps)
        main = findViewById(R.id.coordinator)
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
        main = findViewById(R.id.coordinator)
        main?.apply { applyWindowInsets(this) }
    }
    private fun enterAnimation(exit: Boolean) {
        if(main == null) {
            return
        }
        val animatorSet = AnimatorSet()
        if(exit) {
            animatorSet.playTogether(
                ObjectAnimator.ofFloat(main!!, "translationX", 0f, 300f),
                ObjectAnimator.ofFloat(main!!, "rotationY", 0f, 90f),
                ObjectAnimator.ofFloat(main!!, "alpha", 1f, 0f),
                ObjectAnimator.ofFloat(main!!, "scaleX", 1f, 0.5f),
                ObjectAnimator.ofFloat(main!!, "scaleY", 1f, 0.5f),
            )
        } else {
            animatorSet.playTogether(
                ObjectAnimator.ofFloat(main!!, "translationX", 300f, 0f),
                ObjectAnimator.ofFloat(main!!, "rotationY", 90f, 0f),
                ObjectAnimator.ofFloat(main!!, "alpha", 0f, 1f),
                ObjectAnimator.ofFloat(main!!, "scaleX", 0.5f, 1f),
                ObjectAnimator.ofFloat(main!!, "scaleY", 0.5f, 1f)
            )
        }
        animatorSet.setDuration(400)
        animatorSet.start()
    }

    override fun onResume() {
        enterAnimation(false)
        super.onResume()
    }

    override fun onPause() {
        enterAnimation(true)
        super.onPause()
    }
}