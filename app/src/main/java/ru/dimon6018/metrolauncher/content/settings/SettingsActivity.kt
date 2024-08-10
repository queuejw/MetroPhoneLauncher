package ru.dimon6018.metrolauncher.content.settings

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
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
    private var job: Job? = null
    private val viewList: List<Pair<View, Long>> by lazy {
        listOf(
            binding.settings to 200,
            binding.settingsLabel to 250,
            binding.settingsInclude.themeSetting to 300,
            binding.settingsInclude.allAppsSetting to 310,
            binding.settingsInclude.tilesSetting to 320,
            binding.settingsInclude.iconsSetting to 330,
            binding.settingsInclude.animSetting to 340,
            binding.settingsInclude.feedbackSetting to 350,
            binding.settingsInclude.weatherSetting to 360,
            binding.settingsInclude.updatesSetting to 370,
            binding.settingsInclude.navbarSetting to 380,
            binding.settingsInclude.aboutSetting to 390,
            binding.settingsInclude.leaks to 400,
            binding.settingsInclude.expSetting to 410
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(launcherAccentTheme())
        setAppTheme()
        super.onCreate(savedInstanceState)
        binding = LauncherSettingsMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        confTouchAnim()
        setOnClickers()
        applyWindowInsets(binding.root)
        prepareMessage()
        prepareTip()
        checkHome()
    }
    private fun checkHome() {
        if(!isHomeApp() && isDialogEnabled && Random.nextFloat() < 0.2) {
            isDialogEnabled = false
            WPDialog(this).setTopDialog(false)
                .setTitle(getString(R.string.tip))
                .setMessage(getString(R.string.setAsDefaultLauncher))
                .setNegativeButton(getString(R.string.no), null)
                .setPositiveButton(getString(R.string.yes)) {
                    startActivity(Intent(Settings.ACTION_HOME_SETTINGS))
                }.show()
        }
    }
    private fun prepareMessage() {
        if(!PREFS!!.prefs.getBoolean("tipSettingsEnabled", true) && Random.nextFloat() < 0.08 && PREFS!!.prefs.getBoolean("messageEnabled", true)) {
            WPDialog(this).apply {
                setTopDialog(true)
                setTitle(getString(R.string.developer))
                setMessage(getString(R.string.dev_p1))
                setPositiveButton(getString(R.string.no), null)
                setNegativeButton(getString(R.string.yes)) {
                    donateDialog()
                    dismiss()
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
        setClickListener(binding.settingsInclude.themeSetting, Intent(this@SettingsActivity, ThemeSettingsActivity::class.java))
        setClickListener(binding.settingsInclude.allAppsSetting, Intent(this@SettingsActivity, AllAppsSettingsActivity::class.java))
        setClickListener(binding.settingsInclude.tilesSetting, Intent(this@SettingsActivity, TileSettingsActivity::class.java))
        setClickListener(binding.settingsInclude.aboutSetting, Intent(this@SettingsActivity, AboutSettingsActivity::class.java))
        setClickListener(binding.settingsInclude.feedbackSetting, Intent(this@SettingsActivity, FeedbackSettingsActivity::class.java))
        setClickListener(binding.settingsInclude.updatesSetting, Intent(this@SettingsActivity, UpdateActivity::class.java))
        setClickListener(binding.settingsInclude.navbarSetting, Intent(this@SettingsActivity, NavBarSettingsActivity::class.java))
        setClickListener(binding.settingsInclude.weatherSetting, Intent(this@SettingsActivity, WeatherSettingsActivity::class.java))
        setClickListener(binding.settingsInclude.iconsSetting, Intent(this@SettingsActivity, IconSettingsActivity::class.java))
        setClickListener(binding.settingsInclude.expSetting, Intent(this@SettingsActivity, ExperimentsSettingsActivity::class.java))
        setClickListener(binding.settingsInclude.leaks, LeakCanary.newLeakDisplayActivityIntent())
        setClickListener(binding.settingsInclude.animSetting, Intent(this@SettingsActivity, AnimationSettingsActivity::class.java))
    }
    private fun setClickListener(view: View, intent: Intent) {
        view.setOnClickListener {
            if (!isEnter) {
                isEnter = true
                job?.cancel()
                job = lifecycleScope.launch {
                    if (PREFS!!.isTransitionAnimEnabled) {
                        startAnim()
                    }
                    startActivity(intent)
                }
                job?.invokeOnCompletion {
                    isEnter = false
                }
            }
        }
    }
    private fun setupAnimations() {
        viewList.forEach { (view, duration) ->
            setupAnimForViews(view, duration)
        }
    }
    private fun setupAnimForViews(view: View, dur: Long) {
        if (!PREFS!!.isTransitionAnimEnabled) {
            return
        }
        val animatorSet = AnimatorSet().apply {
            playTogether(
                ObjectAnimator.ofFloat(view, "alpha", if (isEnter) 1f else 0f, if (isEnter) 0f else 1f),
                ObjectAnimator.ofFloat(view, "translationX", if (isEnter) 0f else -200f, if (isEnter) -200f else 0f),
                ObjectAnimator.ofFloat(view, "rotationY", if (isEnter) 0f else -90f, if (isEnter) -90f else 0f)
            )
            duration = dur
        }
        animatorSet.start()
    }
    private fun confTouchAnim() {
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
        if (PREFS!!.isTransitionAnimEnabled) {
            setupAnimations()
            isEnter = false
            CoroutineScope(Dispatchers.Main).launch {
                delay(350)
                viewList.forEach { (view, _) ->
                    view.alpha = 1f
                }
                cancel()
            }
            delay(350)
        } else {
            isEnter = false
        }
    }
    private fun hideViews() {
        if (PREFS!!.isTransitionAnimEnabled) {
            viewList.forEach { (view, _) ->
                hideAnim(view)
            }
        }
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
        binding.settingsInclude.iconsSub.text = runCatching {
            if (PREFS!!.iconPackPackage == "null") getString(R.string.iconPackNotSelectedSub)
            else packageManager.getApplicationLabel(packageManager.getApplicationInfo(PREFS!!.iconPackPackage!!, 0))
        }.getOrElse { getString(R.string.iconPackNotSelectedSub) }

        if(PREFS!!.isPrefsChanged) {
            job?.cancel()
            restartDialog()
        } else {
            startAnimWithLifecycle()
        }
    }

    private fun restartDialog() {
        WPDialog(this).apply {
            setTopDialog(true)
            setTitle(getString(R.string.settings_app_title))
            setMessage(getString(R.string.restart_required))
            setPositiveButton(getString(R.string.restart)) {
                dismiss()
                restartApp()
            }
            setNegativeButton(getString(R.string.later)) {
                dismiss()
                startAnimWithLifecycle()
            }
            setDismissListener {
                startAnimWithLifecycle()
            }
            show()
        }
    }
    private fun startAnimWithLifecycle() {
        lifecycleScope.launch {
            startAnim()
        }
    }
    private fun restartApp() {
        PREFS!!.isPrefsChanged = false
        exitProcess(0)
    }

    override fun onPause() {
        super.onPause()
        hideViews()
    }
}
