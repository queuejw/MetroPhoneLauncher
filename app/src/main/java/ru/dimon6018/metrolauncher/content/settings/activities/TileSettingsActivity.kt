package ru.dimon6018.metrolauncher.content.settings.activities

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.slider.Slider
import ru.dimon6018.metrolauncher.Application.Companion.PREFS
import ru.dimon6018.metrolauncher.databinding.LauncherSettingsTilesBinding
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.applyWindowInsets

class TileSettingsActivity: AppCompatActivity() {

    private lateinit var binding: LauncherSettingsTilesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = LauncherSettingsTilesBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        initView()
        applyWindowInsets(binding.root)
    }
    private fun initView() {
        binding.settingsInclude.alphaSlider.apply {
            value = PREFS!!.tilesTransparency
            addOnChangeListener(Slider.OnChangeListener { _: Slider?, value: Float, _: Boolean ->
                PREFS!!.tilesTransparency = value
                PREFS!!.isPrefsChanged = true
            })
        }
    }
    override fun onResume() {
        super.onResume()
        enterAnimation(false)
    }
    private fun enterAnimation(exit: Boolean) {
        if (!PREFS!!.isTransitionAnimEnabled) {
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
    override fun onPause() {
        enterAnimation(true)
        super.onPause()
    }
}