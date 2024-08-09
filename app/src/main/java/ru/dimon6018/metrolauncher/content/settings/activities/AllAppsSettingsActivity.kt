package ru.dimon6018.metrolauncher.content.settings.activities

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import ru.dimon6018.metrolauncher.Application.Companion.PREFS
import ru.dimon6018.metrolauncher.R
import ru.dimon6018.metrolauncher.databinding.LauncherSettingsAllappsBinding
import ru.dimon6018.metrolauncher.helpers.utils.Utils
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.applyWindowInsets

class AllAppsSettingsActivity: AppCompatActivity() {

    private lateinit var binding: LauncherSettingsAllappsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(Utils.launcherAccentTheme())
        binding = LauncherSettingsAllappsBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.settingsInclude.settingsBtnSwitch.apply {
            isChecked = PREFS!!.isSettingsBtnEnabled
            text = if(PREFS!!.isSettingsBtnEnabled) getString(R.string.on) else getString(R.string.off)
            setOnCheckedChangeListener { _, isChecked ->
                PREFS!!.setAllAppsSettingsBtn(isChecked)
                PREFS!!.isPrefsChanged = true
                text = if(PREFS!!.isSettingsBtnEnabled) getString(R.string.on) else getString(R.string.off)
            }
        }
        binding.settingsInclude.alphabetSwitch.apply {
            isChecked = PREFS!!.isAlphabetEnabled
            text = if(PREFS!!.isAlphabetEnabled) getString(R.string.on) else getString(R.string.off)
            setOnCheckedChangeListener { _, isChecked ->
                PREFS!!.setAlphabetActive(isChecked)
                PREFS!!.isPrefsChanged = true
                text = if(PREFS!!.isAlphabetEnabled) getString(R.string.on) else getString(R.string.off)
            }
        }
        binding.settingsInclude.allAppsBackgroundSwitch.apply {
            isChecked = PREFS!!.isAllAppsBackgroundEnabled
            text = if(PREFS!!.isAllAppsBackgroundEnabled) getString(R.string.on) else getString(R.string.off)
            setOnCheckedChangeListener { _, isChecked ->
                PREFS!!.setAllAppsBackground(isChecked)
                PREFS!!.isPrefsChanged = true
                text = if(PREFS!!.isAllAppsBackgroundEnabled) getString(R.string.on) else getString(R.string.off)
            }
        }
        binding.settingsInclude.disableAllAppsSwitch.apply {
            isChecked = PREFS!!.isAllAppsEnabled
            if(PREFS!!.isAllAppsEnabled) getString(R.string.on) else getString(R.string.off)
            setOnCheckedChangeListener { _, isChecked ->
                PREFS!!.setAllAppsAvailability(isChecked)
                PREFS!!.isPrefsChanged = true
                text = if(PREFS!!.isAllAppsEnabled) getString(R.string.on) else getString(R.string.off)
            }
        }
        applyWindowInsets(binding.root)
    }
    private fun enterAnimation(exit: Boolean) {
        if(!PREFS!!.isTransitionAnimEnabled) {
            return
        }
        val main = binding.root
        val animatorSet = AnimatorSet()
        if(exit) {
            animatorSet.playTogether(
                ObjectAnimator.ofFloat(main, "translationX", 0f, 300f),
                ObjectAnimator.ofFloat(main, "rotationY", 0f, 90f),
                ObjectAnimator.ofFloat(main, "alpha", 1f, 0f),
                ObjectAnimator.ofFloat(main, "scaleX", 1f, 0.5f),
                ObjectAnimator.ofFloat(main, "scaleY", 1f, 0.5f),
            )
        } else {
            animatorSet.playTogether(
                ObjectAnimator.ofFloat(main, "translationX", 300f, 0f),
                ObjectAnimator.ofFloat(main, "rotationY", 90f, 0f),
                ObjectAnimator.ofFloat(main, "alpha", 0f, 1f),
                ObjectAnimator.ofFloat(main, "scaleX", 0.5f, 1f),
                ObjectAnimator.ofFloat(main, "scaleY", 0.5f, 1f)
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