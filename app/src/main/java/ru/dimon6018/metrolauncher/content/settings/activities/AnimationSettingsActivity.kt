package ru.dimon6018.metrolauncher.content.settings.activities

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import ru.dimon6018.metrolauncher.Application.Companion.PREFS
import ru.dimon6018.metrolauncher.R
import ru.dimon6018.metrolauncher.databinding.LauncherSettingsAnimationsBinding
import ru.dimon6018.metrolauncher.helpers.ui.WPDialog
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.applyWindowInsets
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.isDevMode

class AnimationSettingsActivity: AppCompatActivity() {

    private lateinit var binding: LauncherSettingsAnimationsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = LauncherSettingsAnimationsBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        applyWindowInsets(binding.root)
        setupLayout()
        if(isDevMode(this) && PREFS.isAutoShutdownAnimEnabled) {
            WPDialog(this).setTopDialog(true).setTitle(getString(R.string.tip)).setMessage(getString(R.string.animations_dev_mode)).setPositiveButton(getString(android.R.string.ok), null).show()
        }
    }
    private fun setupLayout() {
        binding.settingsInclude.tilesAnimCheckbox.apply {
            isChecked = PREFS.isTilesAnimEnabled
            setOnCheckedChangeListener { _, isChecked ->
                PREFS.isTilesAnimEnabled = isChecked
            }
        }
        binding.settingsInclude.liveTilesAnimCheckbox.apply {
            isChecked = PREFS.isLiveTilesAnimEnabled
            setOnCheckedChangeListener { _, isChecked ->
                PREFS.isLiveTilesAnimEnabled = isChecked
            }
        }
        binding.settingsInclude.allAppsAnimCheckbox.apply {
            isChecked = PREFS.isAAllAppsAnimEnabled
            setOnCheckedChangeListener { _, isChecked ->
                PREFS.isAAllAppsAnimEnabled = isChecked
            }
        }
        binding.settingsInclude.transitionAnimCheckbox.apply {
            isChecked = PREFS.isTransitionAnimEnabled
            setOnCheckedChangeListener { _, isChecked ->
                PREFS.isTransitionAnimEnabled = isChecked
                PREFS.isPrefsChanged = true
            }
        }
        binding.settingsInclude.alphabetAnimCheckbox.apply {
            isChecked = PREFS.isAlphabetAnimEnabled
            setOnCheckedChangeListener { _, isChecked ->
                PREFS.isAlphabetAnimEnabled = isChecked
            }
        }
        binding.settingsInclude.tilesPhoneStartAnimCheckbox.apply {
            isChecked = PREFS.isTilesScreenAnimEnabled
            setOnCheckedChangeListener { _, isChecked ->
                PREFS.isTilesScreenAnimEnabled = isChecked
            }
        }
        binding.settingsInclude.autoShutdownAnimsCheckbox.apply {
            isChecked = PREFS.isAutoShutdownAnimEnabled
            setOnCheckedChangeListener { _, isChecked ->
                PREFS.isAutoShutdownAnimEnabled = isChecked
            }
        }
    }
    private fun enterAnimation(exit: Boolean) {
        if (!PREFS.isTransitionAnimEnabled) {
            return
        }
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