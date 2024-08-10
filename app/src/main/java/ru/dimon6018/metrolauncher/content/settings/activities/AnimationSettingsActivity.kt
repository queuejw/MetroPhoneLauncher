package ru.dimon6018.metrolauncher.content.settings.activities

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import ru.dimon6018.metrolauncher.Application.Companion.PREFS
import ru.dimon6018.metrolauncher.R
import ru.dimon6018.metrolauncher.databinding.LauncherSettingsAnimationsBinding
import ru.dimon6018.metrolauncher.helpers.ui.WPDialog
import ru.dimon6018.metrolauncher.helpers.utils.Utils
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.applyWindowInsets
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.isDevMode

class AnimationSettingsActivity: AppCompatActivity() {

    private lateinit var binding: LauncherSettingsAnimationsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(Utils.launcherAccentTheme())
        binding = LauncherSettingsAnimationsBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        applyWindowInsets(binding.root)
        setupLayout()
        if(isDevMode(this) && PREFS!!.isAutoShutdownAnimEnabled) {
            WPDialog(this).setTopDialog(true).setTitle(getString(R.string.tip)).setMessage(getString(R.string.animations_dev_mode)).setPositiveButton(getString(android.R.string.ok), null).show()
        }
    }
    private fun setupLayout() {
        binding.settingsInclude.tilesAnimCheckbox.apply {
            isChecked = PREFS!!.isTilesAnimEnabled
            setOnCheckedChangeListener { _, isChecked ->
                PREFS!!.isTilesAnimEnabled = isChecked
            }
        }
        binding.settingsInclude.liveTilesAnimCheckbox.apply {
            isChecked = PREFS!!.isLiveTilesAnimEnabled
            setOnCheckedChangeListener { _, isChecked ->
                PREFS!!.isLiveTilesAnimEnabled = isChecked
            }
        }
        binding.settingsInclude.allAppsAnimCheckbox.apply {
            isChecked = PREFS!!.isAAllAppsAnimEnabled
            setOnCheckedChangeListener { _, isChecked ->
                PREFS!!.isAAllAppsAnimEnabled = isChecked
            }
        }
        binding.settingsInclude.transitionAnimCheckbox.apply {
            isChecked = PREFS!!.isTransitionAnimEnabled
            setOnCheckedChangeListener { _, isChecked ->
                PREFS!!.isTransitionAnimEnabled = isChecked
                PREFS!!.isPrefsChanged = true
            }
        }
        binding.settingsInclude.alphabetAnimCheckbox.apply {
            isChecked = PREFS!!.isAlphabetAnimEnabled
            setOnCheckedChangeListener { _, isChecked ->
                PREFS!!.isAlphabetEnabled = isChecked
            }
        }
        binding.settingsInclude.tilesPhoneStartAnimCheckbox.apply {
            isChecked = PREFS!!.isTilesScreenAnimEnabled
            setOnCheckedChangeListener { _, isChecked ->
                PREFS!!.isTilesScreenAnimEnabled = isChecked
            }
        }
        binding.settingsInclude.autoShutdownAnimsCheckbox.apply {
            PREFS!!.isAutoShutdownAnimEnabled
            setOnCheckedChangeListener { _, isChecked ->
                PREFS!!.isAutoShutdownAnimEnabled = isChecked
            }
        }
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