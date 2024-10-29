package ru.dimon6018.metrolauncher.content.settings.activities

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import ru.dimon6018.metrolauncher.Application.Companion.PREFS
import ru.dimon6018.metrolauncher.Application.Companion.customBoldFont
import ru.dimon6018.metrolauncher.Application.Companion.customFont
import ru.dimon6018.metrolauncher.R
import ru.dimon6018.metrolauncher.databinding.LauncherSettingsAllappsBinding
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.applyWindowInsets

class AllAppsSettingsActivity: AppCompatActivity() {

    private lateinit var binding: LauncherSettingsAllappsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = LauncherSettingsAllappsBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.settingsInclude.settingsBtnSwitch.apply {
            isChecked = PREFS.isSettingsBtnEnabled
            text = if(PREFS.isSettingsBtnEnabled) getString(R.string.on) else getString(R.string.off)
            setOnCheckedChangeListener { _, isChecked ->
                PREFS.isSettingsBtnEnabled = isChecked
                PREFS.isPrefsChanged = true
                text = if(PREFS.isSettingsBtnEnabled) getString(R.string.on) else getString(R.string.off)
            }
        }
        binding.settingsInclude.alphabetSwitch.apply {
            isChecked = PREFS.isAlphabetEnabled
            text = if(PREFS.isAlphabetEnabled) getString(R.string.on) else getString(R.string.off)
            setOnCheckedChangeListener { _, isChecked ->
                PREFS.isAlphabetEnabled = isChecked
                PREFS.isPrefsChanged = true
                text = if(PREFS.isAlphabetEnabled) getString(R.string.on) else getString(R.string.off)
            }
        }
        binding.settingsInclude.disableAllAppsSwitch.apply {
            isChecked = PREFS.isAllAppsEnabled
            text = if(PREFS.isAllAppsEnabled) getString(R.string.on) else getString(R.string.off)
            setOnCheckedChangeListener { _, isChecked ->
                PREFS.isAllAppsEnabled = isChecked
                PREFS.isPrefsChanged = true
                text = if(PREFS.isAllAppsEnabled) getString(R.string.on) else getString(R.string.off)
            }
        }
        binding.settingsInclude.keyboardWhenSearchingSwitch.apply {
            isChecked = PREFS.showKeyboardWhenSearching
            text = if(PREFS.showKeyboardWhenSearching) getString(R.string.on) else getString(R.string.off)
            setOnCheckedChangeListener { _, isChecked ->
                PREFS.showKeyboardWhenSearching = isChecked
                text = if(PREFS.showKeyboardWhenSearching) getString(R.string.on) else getString(R.string.off)
            }
        }
        binding.settingsInclude.keyboardWhenAllAppsOpened.apply {
            isChecked = PREFS.showKeyboardWhenOpeningAllApps
            text = if(PREFS.showKeyboardWhenOpeningAllApps) getString(R.string.on) else getString(R.string.off)
            setOnCheckedChangeListener { _, isChecked ->
                PREFS.showKeyboardWhenOpeningAllApps = isChecked
                text = if(PREFS.showKeyboardWhenOpeningAllApps) getString(R.string.on) else getString(R.string.off)
            }
        }
        binding.settingsInclude.allAppsKeyboardActionSwitch.apply {
            isChecked = PREFS.allAppsKeyboardActionEnabled
            text = if(PREFS.allAppsKeyboardActionEnabled) getString(R.string.on) else getString(R.string.off)
            setOnCheckedChangeListener { _, isChecked ->
                PREFS.allAppsKeyboardActionEnabled = isChecked
                text = if(PREFS.allAppsKeyboardActionEnabled) getString(R.string.on) else getString(R.string.off)
            }
        }
        applyWindowInsets(binding.root)
        setupFont()
    }
    private fun setupFont() {
        customFont?.let {
            binding.settingsSectionLabel.typeface = it
            binding.settingsLabel.typeface = it
            binding.settingsInclude.settingsBtnSwitch.typeface = it
            binding.settingsInclude.alphabetSwitch.typeface = it
            binding.settingsInclude.disableAllAppsSwitch.typeface = it
            binding.settingsInclude.keyboardWhenSearchingSwitch.typeface = it
            binding.settingsInclude.keyboardWhenAllAppsOpened.typeface = it
            binding.settingsInclude.settingsButtonLabel.typeface = it
            binding.settingsInclude.settingsButtonLabelSwitch.typeface = it
            binding.settingsInclude.alphabetSettingLabel.typeface = it
            binding.settingsInclude.alphabetSettingLabelSwitch.typeface = it
            binding.settingsInclude.alphabetSettingKeyboardLabel.typeface = it
            binding.settingsInclude.additionalOptions.typeface = it
            binding.settingsInclude.showScreenAllAppsLabel.typeface = it
            binding.settingsInclude.autoSearchLabel.typeface = it
            binding.settingsInclude.allAppsKeyboardActionLabel.typeface = it
            binding.settingsInclude.allAppsKeyboardActionSwitch.typeface = it
        }
        customBoldFont?.let {
            binding.settingsLabel.typeface = it
        }
    }
    private fun enterAnimation(exit: Boolean) {
        if (!PREFS.isTransitionAnimEnabled) return
        val main = binding.root
        val animatorSet = AnimatorSet().apply {
            playTogether(
                createObjectAnimator(main, "translationX", if (exit) 0f else -300f, if (exit) -300f else 0f),
                createObjectAnimator(main, "rotationY", if (exit) 0f else 90f, if (exit) 90f else 0f),
                createObjectAnimator(main, "alpha", if (exit) 1f else 0f, if (exit) 0f else 1f),
                createObjectAnimator(main, "scaleX", if (exit) 1f else 0.5f, if (exit) 0.5f else 1f),
                createObjectAnimator(main, "scaleY", if (exit) 1f else 0.5f, if (exit) 0.5f else 1f)
            )
            duration = 400
        }
        animatorSet.start()
    }
    private fun createObjectAnimator(target: Any, property: String, startValue: Float, endValue: Float): ObjectAnimator {
        return ObjectAnimator.ofFloat(target, property, startValue, endValue)
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