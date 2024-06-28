package ru.dimon6018.metrolauncher.content.settings.activities

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.checkbox.MaterialCheckBox
import ru.dimon6018.metrolauncher.Application.Companion.PREFS
import ru.dimon6018.metrolauncher.R
import ru.dimon6018.metrolauncher.helpers.WPDialog
import ru.dimon6018.metrolauncher.helpers.utils.Utils
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.applyWindowInsets
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.isDevMode

class AnimationSettingsActivity: AppCompatActivity() {

    private var main: CoordinatorLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(Utils.launcherAccentTheme())
        super.onCreate(savedInstanceState)
        setContentView(R.layout.launcher_settings_animations)
        main = findViewById(R.id.coordinator)
        main?.apply { applyWindowInsets(this) }

        val tileAnim: MaterialCheckBox = findViewById(R.id.tilesAnimCheckbox)
        val liveTileAnim: MaterialCheckBox = findViewById(R.id.liveTilesAnimCheckbox)
        val allAppsAnim: MaterialCheckBox = findViewById(R.id.allAppsAnimCheckbox)
        val transitionAnim: MaterialCheckBox = findViewById(R.id.transitionAnimCheckbox)
        val alphabetAnim: MaterialCheckBox = findViewById(R.id.alphabetAnimCheckbox)
        val tilesScreenAnim: MaterialCheckBox = findViewById(R.id.tilesPhoneStartAnimCheckbox)
        val autoShutdownAnims: MaterialCheckBox = findViewById(R.id.autoShutdownAnimsCheckbox)
        tileAnim.isChecked = PREFS!!.isTilesAnimEnabled
        liveTileAnim.isChecked = PREFS!!.isLiveTilesAnimEnabled
        allAppsAnim.isChecked = PREFS!!.isAAllAppsAnimEnabled
        transitionAnim.isChecked = PREFS!!.isTransitionAnimEnabled
        alphabetAnim.isChecked = PREFS!!.isAlphabetAnimEnabled
        tilesScreenAnim.isChecked = PREFS!!.isTilesScreenAnimEnabled
        autoShutdownAnims.isChecked = PREFS!!.isAutoShutdownAnimEnabled

        tileAnim.setOnCheckedChangeListener { _, isChecked ->
            PREFS!!.setTilesAnim(isChecked)
        }
        liveTileAnim.setOnCheckedChangeListener { _, isChecked ->
            PREFS!!.setLiveTilesAnim(isChecked)
        }
        transitionAnim.setOnCheckedChangeListener { _, isChecked ->
            PREFS!!.setTransitionAnim(isChecked)
            PREFS!!.setPrefsChanged(true)
        }
        allAppsAnim.setOnCheckedChangeListener { _, isChecked ->
            PREFS!!.setAllAppsAnim(isChecked)
        }
        alphabetAnim.setOnCheckedChangeListener { _, isChecked ->
            PREFS!!.setAlphabetAnim(isChecked)
        }
        tilesScreenAnim.setOnCheckedChangeListener { _, isChecked ->
            PREFS!!.setTilesScreenAnim(isChecked)
        }
        autoShutdownAnims.setOnCheckedChangeListener { _, isChecked ->
            PREFS!!.setAutoShutdownAnim(isChecked)
        }
        if(isDevMode(this) && PREFS!!.isAutoShutdownAnimEnabled) {
            WPDialog(this).setTopDialog(true).setTitle(getString(R.string.tip)).setMessage(getString(R.string.animations_dev_mode)).setPositiveButton(getString(android.R.string.ok), null).show()
        }
    }
    private fun enterAnimation(exit: Boolean) {
        if(main == null || !PREFS!!.isTransitionAnimEnabled) {
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