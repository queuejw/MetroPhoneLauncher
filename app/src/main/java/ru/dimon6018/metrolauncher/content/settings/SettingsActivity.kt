package ru.dimon6018.metrolauncher.content.settings

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import leakcanary.LeakCanary
import ru.dimon6018.metrolauncher.Application
import ru.dimon6018.metrolauncher.Application.Companion.PREFS
import ru.dimon6018.metrolauncher.R
import ru.dimon6018.metrolauncher.content.settings.activities.AboutSettingsActivity
import ru.dimon6018.metrolauncher.content.settings.activities.AllAppsSettingsActivity
import ru.dimon6018.metrolauncher.content.settings.activities.AnimationSettingsActivity
import ru.dimon6018.metrolauncher.content.settings.activities.ExperimentsSettingsActivity
import ru.dimon6018.metrolauncher.content.settings.activities.FeedbackSettingsActivity
import ru.dimon6018.metrolauncher.content.settings.activities.IconSettingsActivity
import ru.dimon6018.metrolauncher.content.settings.activities.NavBarSettingsActivity
import ru.dimon6018.metrolauncher.content.settings.activities.ThemeSettingsActivity
import ru.dimon6018.metrolauncher.content.settings.activities.TileSettingsActivity
import ru.dimon6018.metrolauncher.content.settings.activities.UpdateActivity
import ru.dimon6018.metrolauncher.content.settings.activities.WeatherSettingsActivity
import ru.dimon6018.metrolauncher.databinding.LauncherSettingsMainBinding
import ru.dimon6018.metrolauncher.helpers.ui.WPDialog
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.accentName
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.applyWindowInsets
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.launcherAccentTheme
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.setViewInteractAnimation
import kotlin.random.Random
import kotlin.system.exitProcess

class SettingsActivity : AppCompatActivity() {

    private var isDialogEnabled = true
    private var isEnter = false
    private lateinit var binding: LauncherSettingsMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(launcherAccentTheme())
        setAppTheme()
        super.onCreate(savedInstanceState)
        binding = LauncherSettingsMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        confAnim()
        setOnClickers()
        applyWindowInsets(binding.root)
        prepareMessage()
        prepareTip()
    }

    private fun prepareMessage() {
        if(!PREFS!!.prefs.getBoolean("tipSettingsEnabled", true) && Random.nextFloat() < 0.1 && PREFS!!.prefs.getBoolean("messageEnabled", true)) {
            WPDialog(this).apply {
                setTopDialog(true)
                setTitle(getString(R.string.developer))
                setMessage(getString(R.string.dev_p1))
                setPositiveButton(getString(R.string.no), null)
                setNegativeButton(getString(R.string.yes)) {
                    donateDialog()
                }
                setNeutralButton(getString(R.string.not_show_again)) {
                    PREFS!!.prefs.edit().putBoolean("messageEnabled", false).apply()
                    dismiss()
                }
                show()
            }
        }
    }
    private fun donateDialog() {
        WPDialog(this).apply {
            setTopDialog(true)
            setTitle(getString(R.string.developer))
            setMessage(getString(R.string.dev_p2))
            setPositiveButton(getString(R.string.hide)) {
                PREFS!!.prefs.edit().putBoolean("messageEnabled", false).apply()
                dismiss()
            }
            setNegativeButton(getString(R.string.support)) {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://donationalerts.com/r/queuejw")))
                dismiss()
            }
            show()
        }
    }
    private fun prepareTip() {
        if(PREFS!!.prefs.getBoolean("tipSettingsEnabled", true)) {
            WPDialog(this).setTopDialog(true)
                .setTitle(getString(R.string.tip))
                .setMessage(getString(R.string.tipSettings))
                .setPositiveButton(getString(android.R.string.ok), null)
                .show()
            PREFS!!.prefs.edit().putBoolean("tipSettingsEnabled", false).apply()
        }
    }
    private fun setOnClickers() {
        binding.settingsInclude.themeSetting.setOnClickListener {
            if(!isEnter) {
                lifecycleScope.launch(Dispatchers.Main) {
                    isEnter = true
                    if (PREFS!!.isTransitionAnimEnabled) {
                        startAnim()
                    }
                    startActivity(Intent(this@SettingsActivity, ThemeSettingsActivity::class.java))
                }
            }
        }
        binding.settingsInclude.allAppsSetting.setOnClickListener {
            if(!isEnter) {
                lifecycleScope.launch {
                    isEnter = true
                    if (PREFS!!.isTransitionAnimEnabled) {
                        startAnim()
                    }
                    startActivity(Intent(this@SettingsActivity, AllAppsSettingsActivity::class.java))
                }
            }
        }
        binding.settingsInclude.tilesSetting.setOnClickListener {
            if(!isEnter) {
                lifecycleScope.launch {
                    isEnter = true
                    if (PREFS!!.isTransitionAnimEnabled) {
                        startAnim()
                    }
                    startActivity(Intent(this@SettingsActivity, TileSettingsActivity::class.java))
                }
            }
        }
        binding.settingsInclude.aboutSetting.setOnClickListener {
            if(!isEnter) {
                lifecycleScope.launch {
                    isEnter = true
                    if (PREFS!!.isTransitionAnimEnabled) {
                        startAnim()
                    }
                    startActivity(Intent(this@SettingsActivity, AboutSettingsActivity::class.java))
                }
            }
        }
        binding.settingsInclude.feedbackSetting.setOnClickListener {
            if(!isEnter) {
                lifecycleScope.launch {
                    isEnter = true
                    if (PREFS!!.isTransitionAnimEnabled) {
                        startAnim()
                    }
                    startActivity(Intent(this@SettingsActivity, FeedbackSettingsActivity::class.java))
                }
            }
        }
        binding.settingsInclude.updatesSetting.setOnClickListener {
            if(!isEnter) {
                lifecycleScope.launch {
                    isEnter = true
                    if (PREFS!!.isTransitionAnimEnabled) {
                        startAnim()
                    }
                    startActivity(Intent(this@SettingsActivity, UpdateActivity::class.java))
                }
            }
        }
        binding.settingsInclude.navbarSetting.setOnClickListener {
            if(!isEnter) {
                lifecycleScope.launch {
                    isEnter = true
                    if (PREFS!!.isTransitionAnimEnabled) {
                        startAnim()
                    }
                    startActivity(Intent(this@SettingsActivity, NavBarSettingsActivity::class.java))
                }
            }
        }
        binding.settingsInclude.weatherSetting.setOnClickListener {
            if(!isEnter) {
                lifecycleScope.launch {
                    isEnter = true
                    if (PREFS!!.isTransitionAnimEnabled) {
                        startAnim()
                    }
                    startActivity(Intent(this@SettingsActivity, WeatherSettingsActivity::class.java))
                }
            }
        }
        binding.settingsInclude.iconsSetting.setOnClickListener {
            if(!isEnter) {
                lifecycleScope.launch {
                    isEnter = true
                    if (PREFS!!.isTransitionAnimEnabled) {
                        startAnim()
                    }
                    startActivity(Intent(this@SettingsActivity, IconSettingsActivity::class.java))
                }
            }
        }
        binding.settingsInclude.expSetting.setOnClickListener {
            if(!isEnter) {
                lifecycleScope.launch {
                    isEnter = true
                    if (PREFS!!.isTransitionAnimEnabled) {
                        startAnim()
                    }
                    startActivity(
                        Intent(this@SettingsActivity, ExperimentsSettingsActivity::class.java))
                }
            }
        }
        binding.settingsInclude.leaks.setOnClickListener {
            if(!isEnter) {
                lifecycleScope.launch {
                    isEnter = true
                    if (PREFS!!.isTransitionAnimEnabled) {
                        startAnim()
                    }
                    startActivity(LeakCanary.newLeakDisplayActivityIntent())
                }
            }
        }
        binding.settingsInclude.animSetting.setOnClickListener {
            if(!isEnter) {
                lifecycleScope.launch {
                    isEnter = true
                    if (PREFS!!.isTransitionAnimEnabled) {
                        startAnim()
                    }
                    startActivity(Intent(this@SettingsActivity, AnimationSettingsActivity::class.java))
                }
            }
        }
    }
    private fun confAnim() {
        setViewInteractAnimation(binding.settingsInclude.themeSetting)
        setViewInteractAnimation(binding.settingsInclude.allAppsSetting)
        setViewInteractAnimation(binding.settingsInclude.tilesSetting)
        setViewInteractAnimation(binding.settingsInclude.iconsSetting)
        setViewInteractAnimation(binding.settingsInclude.animSetting)
        setViewInteractAnimation(binding.settingsInclude.feedbackSetting)
        setViewInteractAnimation(binding.settingsInclude.weatherSetting)
        setViewInteractAnimation(binding.settingsInclude.updatesSetting)
        setViewInteractAnimation(binding.settingsInclude.navbarSetting)
        setViewInteractAnimation(binding.settingsInclude.aboutSetting)
        setViewInteractAnimation(binding.settingsInclude.leaks)
        setViewInteractAnimation(binding.settingsInclude.expSetting)
    }
    private suspend fun startAnim() {
        if(PREFS!!.isTransitionAnimEnabled) {
            setupAnim(binding.settingsInclude.themeSetting, 200)
            setupAnim(binding.settingsInclude.allAppsSetting, 210)
            setupAnim(binding.settingsInclude.tilesSetting, 220)
            setupAnim(binding.settingsInclude.iconsSetting, 225)
            setupAnim(binding.settingsInclude.animSetting, 230)
            setupAnim(binding.settingsInclude.feedbackSetting, 235)
            setupAnim(binding.settingsInclude.weatherSetting, 240)
            setupAnim(binding.settingsInclude.updatesSetting, 245)
            setupAnim(binding.settingsInclude.navbarSetting, 250)
            setupAnim(binding.settingsInclude.aboutSetting, 255)
            setupAnim(binding.settingsInclude.leaks, 260)
            setupAnim(binding.settingsInclude.expSetting, 265)
            setupAnim(binding.root, 280)
            isEnter = false
            CoroutineScope(Dispatchers.Main).launch {
                delay(500)
                binding.settingsInclude.themeSetting.alpha = 1f
                binding.settingsInclude.allAppsSetting.alpha = 1f
                binding.settingsInclude.tilesSetting.alpha = 1f
                binding.settingsInclude.iconsSetting.alpha = 1f
                binding.settingsInclude.animSetting.alpha = 1f
                binding.settingsInclude.feedbackSetting.alpha = 1f
                binding.settingsInclude.weatherSetting.alpha = 1f
                binding.settingsInclude.updatesSetting.alpha = 1f
                binding.settingsInclude.navbarSetting.alpha = 1f
                binding.settingsInclude.aboutSetting.alpha = 1f
                binding.settingsInclude.leaks.alpha = 1f
                binding.settingsInclude.expSetting.alpha = 1f
                binding.root.alpha = 1f
                cancel()
            }
            delay(150)
        } else {
            isEnter = false
        }
    }
    private fun hideViews() {
        if(PREFS!!.isTransitionAnimEnabled) {
            hideAnim(binding.settingsInclude.themeSetting)
            hideAnim(binding.settingsInclude.allAppsSetting)
            hideAnim(binding.settingsInclude.tilesSetting)
            hideAnim(binding.settingsInclude.iconsSetting)
            hideAnim(binding.settingsInclude.animSetting)
            hideAnim(binding.settingsInclude.feedbackSetting)
            hideAnim(binding.settingsInclude.weatherSetting)
            hideAnim(binding.settingsInclude.updatesSetting)
            hideAnim(binding.settingsInclude.navbarSetting)
            hideAnim(binding.settingsInclude.aboutSetting)
            hideAnim(binding.settingsInclude.leaks)
            hideAnim(binding.settingsInclude.expSetting)
            hideAnim(binding.root)
        }
    }
    private fun setupAnim(view: View?, duration: Long) {
        if(view == null || !PREFS!!.isTransitionAnimEnabled) {
            return
        }
        val animatorSet = AnimatorSet()
        if(!isEnter) {
            animatorSet.playTogether(
                ObjectAnimator.ofFloat(view, "translationX", -600f, 0f),
                ObjectAnimator.ofFloat(view, "rotationY", -90f, 0f),
                ObjectAnimator.ofFloat(view, "alpha", 0f, 1f)
            )
        } else {
            animatorSet.playTogether(
                ObjectAnimator.ofFloat(view, "translationX", 0f, -600f),
                ObjectAnimator.ofFloat(view, "rotationY", 0f, -90f),
                ObjectAnimator.ofFloat(view, "alpha", 1f, 0f)
            )
        }
        animatorSet.setDuration(duration)
        animatorSet.start()
    }
    private fun hideAnim(view: View?) {
        if(view == null || !PREFS!!.isTransitionAnimEnabled) {
            return
        }
        ObjectAnimator.ofFloat(view, "alpha", 1f, 0f).start()
    }
    private fun setAppTheme() {
        if (PREFS!!.isLightThemeUsed) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                (application as Application).setNightMode()
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                (application as Application).setNightMode()
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
        }
    }
    private fun isHomeApp(): Boolean {
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_HOME)
        val res = packageManager.resolveActivity(intent, 0)
        return res!!.activityInfo != null && (packageName
                == res.activityInfo.packageName)
    }
    override fun onResume() {
        super.onResume()
        binding.settingsInclude.themeSub.text = accentName(this)
        binding.settingsInclude.navbarSub.text = when (PREFS!!.navBarColor) {
            0 -> getString(R.string.always_dark)
            1 -> getString(R.string.always_light)
            2 -> getString(R.string.matches_accent_color)
            3 -> getString(R.string.hide_navbar)
            4 -> getString(R.string.auto)
            else -> getString(R.string.navigation_bar_2)
        }
        try {
            binding.settingsInclude.iconsSub.text =
                if (PREFS!!.iconPackPackage == "null") getString(R.string.iconPackNotSelectedSub) else packageManager.getApplicationLabel(
                    packageManager.getApplicationInfo(PREFS!!.iconPackPackage!!, 0)
                )
        } catch (e: Exception) {
            binding.settingsInclude.iconsSub.text = getString(R.string.iconPackNotSelectedSub)
        }
        if(!isHomeApp() && isDialogEnabled && Random.nextFloat() < 0.25) {
            isDialogEnabled = false
            WPDialog(this).setTopDialog(false)
                .setTitle(getString(R.string.tip))
                .setMessage(getString(R.string.setAsDefaultLauncher))
                .setNegativeButton(getString(R.string.no), null)
                .setPositiveButton(getString(R.string.yes)) {
                    startActivity(Intent(Settings.ACTION_HOME_SETTINGS))
                }.show()
        }
        if(PREFS!!.isPrefsChanged) {
            PREFS!!.isPrefsChanged = false
            Toast.makeText(this, getString(R.string.restart_required), Toast.LENGTH_SHORT).show()
            exitProcess(0)
        } else {
            lifecycleScope.launch {
                startAnim()
            }
        }
    }
    override fun onPause() {
        super.onPause()
        hideViews()
    }
}
