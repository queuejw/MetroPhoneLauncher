package ru.dimon6018.metrolauncher.content.settings.activities

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import ru.dimon6018.metrolauncher.Application.Companion.PREFS
import ru.dimon6018.metrolauncher.Application.Companion.customBoldFont
import ru.dimon6018.metrolauncher.Application.Companion.customFont
import ru.dimon6018.metrolauncher.databinding.LauncherSettingsExperimentsBinding
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.applyWindowInsets

class ExperimentsSettingsActivity: AppCompatActivity() {

    private lateinit var binding: LauncherSettingsExperimentsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = LauncherSettingsExperimentsBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        applyWindowInsets(binding.root)
        setupFont()
    }
    private fun setupFont() {
        customFont?.let {
            binding.settingsInclude.expPlaceholder.typeface = it
            binding.settingsSectionLabel.typeface = it
            binding.settingsLabel.typeface = it
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