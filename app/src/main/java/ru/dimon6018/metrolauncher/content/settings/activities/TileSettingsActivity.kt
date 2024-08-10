package ru.dimon6018.metrolauncher.content.settings.activities

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.slider.Slider
import ru.dimon6018.metrolauncher.Application.Companion.PREFS
import ru.dimon6018.metrolauncher.databinding.LauncherSettingsTilesBinding
import ru.dimon6018.metrolauncher.helpers.utils.Utils
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.applyWindowInsets

class TileSettingsActivity: AppCompatActivity() {

    private lateinit var binding: LauncherSettingsTilesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(Utils.launcherAccentTheme())
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
    override fun onPause() {
        enterAnimation(true)
        super.onPause()
    }
}