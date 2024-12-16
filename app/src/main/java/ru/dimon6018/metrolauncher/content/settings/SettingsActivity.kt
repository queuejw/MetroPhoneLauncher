package ru.dimon6018.metrolauncher.content.settings

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textview.MaterialTextView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import leakcanary.LeakCanary
import ru.dimon6018.metrolauncher.Application.Companion.PREFS
import ru.dimon6018.metrolauncher.Application.Companion.customBoldFont
import ru.dimon6018.metrolauncher.Application.Companion.customFont
import ru.dimon6018.metrolauncher.Application.Companion.customLightFont
import ru.dimon6018.metrolauncher.R
import ru.dimon6018.metrolauncher.content.settings.activities.AboutSettingsActivity
import ru.dimon6018.metrolauncher.content.settings.activities.AllAppsSettingsActivity
import ru.dimon6018.metrolauncher.content.settings.activities.AnimationSettingsActivity
import ru.dimon6018.metrolauncher.content.settings.activities.ExperimentsSettingsActivity
import ru.dimon6018.metrolauncher.content.settings.activities.FeedbackSettingsActivity
import ru.dimon6018.metrolauncher.content.settings.activities.FontsSettingsActivity
import ru.dimon6018.metrolauncher.content.settings.activities.IconSettingsActivity
import ru.dimon6018.metrolauncher.content.settings.activities.NavBarSettingsActivity
import ru.dimon6018.metrolauncher.content.settings.activities.ThemeSettingsActivity
import ru.dimon6018.metrolauncher.content.settings.activities.TileSettingsActivity
import ru.dimon6018.metrolauncher.content.settings.activities.UpdateActivity
import ru.dimon6018.metrolauncher.databinding.LauncherSettingsMainBinding
import ru.dimon6018.metrolauncher.helpers.ui.WPDialog
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.accentName
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.applyWindowInsets
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.setViewInteractAnimation
import kotlin.random.Random

class SettingsActivity : AppCompatActivity() {

    private var isDialogEnabled = true
    private var isEnter = false

    private lateinit var binding: LauncherSettingsMainBinding
    private var job: Job? = null
    private val viewList: List<Pair<View, Long>> by lazy {
        listOf(
            binding.settings to 100,
            binding.settingsLabel to 150,
            binding.settingsInclude.themeSetting to 200,
            binding.settingsInclude.allAppsSetting to 205,
            binding.settingsInclude.tilesSetting to 210,
            binding.settingsInclude.iconsSetting to 215,
            binding.settingsInclude.fontSetting to 215,
            binding.settingsInclude.animSetting to 225,
            binding.settingsInclude.feedbackSetting to 225,
            binding.settingsInclude.updatesSetting to 225,
            binding.settingsInclude.navbarSetting to 225,
            binding.settingsInclude.aboutSetting to 225,
            binding.settingsInclude.leaks to 225,
            binding.settingsInclude.expSetting to 225
        )
    }
    private val regularTextViewList: List<MaterialTextView> by lazy {
        listOf(
            binding.settingsLabel,
            binding.settingsInclude.startThemeLabel,
            binding.settingsInclude.allAppsListLabel,
            binding.settingsInclude.tilesLabel,
            binding.settingsInclude.iconPacksLabel,
            binding.settingsInclude.fontsLabel,
            binding.settingsInclude.animationsLabel,
            binding.settingsInclude.feedbackLabel,
            binding.settingsInclude.updatesLabel,
            binding.settingsInclude.navigationLabel,
            binding.settingsInclude.aboutLabel,
            binding.settingsInclude.leackcanaryLabel,
            binding.settingsInclude.expLabel
        )
    }
    private val lightTextViewList: List<MaterialTextView> by lazy {
        listOf(
            binding.settings,
            binding.settingsInclude.themeSub,
            binding.settingsInclude.allAppsSub,
            binding.settingsInclude.tilesSettingSub,
            binding.settingsInclude.iconsSub,
            binding.settingsInclude.fontsSub,
            binding.settingsInclude.animationsSub,
            binding.settingsInclude.feedbackSub,
            binding.settingsInclude.updatesSub,
            binding.settingsInclude.navbarSub,
            binding.settingsInclude.aboutSub,
            binding.settingsInclude.leakcanarySub,
            binding.settingsInclude.expSub
        )
    }
    private var dialogActivated = false

    override fun onCreate(savedInstanceState: Bundle?) {
        when (PREFS.appTheme) {
            0 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            1 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            2 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
        super.onCreate(savedInstanceState)
        binding = LauncherSettingsMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        confTouchAnim()
        setOnClickers()
        applyWindowInsets(binding.root)
        checkHome()
        prepareMessage()
        prepareTip()
        setupFont()
    }

    private fun setupFont() {
        regularTextViewList.forEach {
            customFont?.let { font ->
                it.typeface = font
            }
        }
        lightTextViewList.forEach {
            if (PREFS.customLightFontPath != null) {
                customLightFont?.let { font ->
                    it.typeface = font
                }
            } else {
                customFont?.let { font ->
                    it.typeface = font
                }
            }
        }
        customBoldFont?.let {
            binding.settings.typeface = it
        }
    }

    private fun checkHome() {
        if (!isHomeApp() && isDialogEnabled && Random.nextFloat() < 0.2 && !dialogActivated) {
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
        if (!PREFS.prefs.getBoolean(
                "tipSettingsEnabled",
                true
            ) && Random.nextFloat() < 0.05 && PREFS.prefs.getBoolean("messageEnabled", true)
        ) {
            dialogActivated = true
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
                    PREFS.prefs.edit().putBoolean("messageEnabled", false).apply()
                    dismiss()
                }
                setDismissListener {
                    dialogActivated = false
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
                PREFS.prefs.edit().putBoolean("messageEnabled", false).apply()
                dismiss()
            }
            setNegativeButton(getString(R.string.support)) {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://donationalerts.com/r/queuejw")
                    )
                )
                dismiss()
            }
            show()
        }
    }

    private fun prepareTip() {
        if (PREFS.prefs.getBoolean("tipSettingsEnabled", true)) {
            WPDialog(this).setTopDialog(true)
                .setTitle(getString(R.string.tip))
                .setMessage(getString(R.string.tipSettings))
                .setPositiveButton(getString(android.R.string.ok), null)
                .show()
            PREFS.prefs.edit().putBoolean("tipSettingsEnabled", false).apply()
        }
    }

    private fun setOnClickers() {
        setClickListener(
            binding.settingsInclude.themeSetting,
            Intent(this@SettingsActivity, ThemeSettingsActivity::class.java)
        )
        setClickListener(
            binding.settingsInclude.allAppsSetting,
            Intent(this@SettingsActivity, AllAppsSettingsActivity::class.java)
        )
        setClickListener(
            binding.settingsInclude.tilesSetting,
            Intent(this@SettingsActivity, TileSettingsActivity::class.java)
        )
        setClickListener(
            binding.settingsInclude.aboutSetting,
            Intent(this@SettingsActivity, AboutSettingsActivity::class.java)
        )
        setClickListener(
            binding.settingsInclude.feedbackSetting,
            Intent(this@SettingsActivity, FeedbackSettingsActivity::class.java)
        )
        setClickListener(
            binding.settingsInclude.updatesSetting,
            Intent(this@SettingsActivity, UpdateActivity::class.java)
        )
        setClickListener(
            binding.settingsInclude.navbarSetting,
            Intent(this@SettingsActivity, NavBarSettingsActivity::class.java)
        )
        setClickListener(
            binding.settingsInclude.iconsSetting,
            Intent(this@SettingsActivity, IconSettingsActivity::class.java)
        )
        setClickListener(
            binding.settingsInclude.expSetting,
            Intent(this@SettingsActivity, ExperimentsSettingsActivity::class.java)
        )
        setClickListener(binding.settingsInclude.leaks, LeakCanary.newLeakDisplayActivityIntent())
        setClickListener(
            binding.settingsInclude.animSetting,
            Intent(this@SettingsActivity, AnimationSettingsActivity::class.java)
        )
        setClickListener(
            binding.settingsInclude.fontSetting,
            Intent(this@SettingsActivity, FontsSettingsActivity::class.java)
        )
    }

    private fun setClickListener(view: View, intent: Intent) {
        view.setOnClickListener {
            if (!isEnter) {
                isEnter = true
                job?.cancel()
                job = lifecycleScope.launch {
                    if (PREFS.isTransitionAnimEnabled) startAnim()
                    startActivity(intent)
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
        if (!PREFS.isTransitionAnimEnabled) return
        val animatorSet = AnimatorSet().apply {
            playTogether(
                ObjectAnimator.ofFloat(
                    view,
                    "alpha",
                    if (isEnter) 1f else 0f,
                    if (isEnter) 0f else 1f
                ),
                ObjectAnimator.ofFloat(
                    view,
                    "translationX",
                    if (isEnter) 0f else -400f,
                    if (isEnter) -500f else 0f
                ),
                ObjectAnimator.ofFloat(
                    view,
                    "rotationY",
                    if (isEnter) 0f else -90f,
                    if (isEnter) -90f else 0f
                )
            )
            duration = dur
        }
        animatorSet.start()
    }

    private fun confTouchAnim() {
        viewList.forEach {
            setViewInteractAnimation(it.first)
        }
    }

    private suspend fun startAnim() {
        if (PREFS.isTransitionAnimEnabled) {
            setupAnimations()
            CoroutineScope(Dispatchers.Main).launch {
                delay(200)
                isEnter = false
                viewList.forEach { (view, _) ->
                    view.alpha = 1f
                }
                cancel()
            }
            delay(200)
        } else {
            isEnter = false
        }
    }

    private fun hideViews() {
        if (PREFS.isTransitionAnimEnabled) {
            viewList.forEach { (view, _) ->
                hideAnim(view)
            }
        }
    }

    private fun hideAnim(view: View?) {
        if (view == null || !PREFS.isTransitionAnimEnabled) {
            return
        }
        ObjectAnimator.ofFloat(view, "alpha", 1f, 0f).start()
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
        binding.settingsInclude.navbarSub.text = when (PREFS.navBarColor) {
            0 -> getString(R.string.always_dark)
            1 -> getString(R.string.always_light)
            2 -> getString(R.string.matches_accent_color)
            3 -> getString(R.string.hide_navbar)
            4 -> getString(R.string.auto)
            else -> getString(R.string.navigation_bar_2)
        }
        binding.settingsInclude.fontsSub.text = runCatching {
            if (!PREFS.customFontInstalled) getString(R.string.fonts_tip) else PREFS.customFontName
        }.getOrElse {
            getString(R.string.fonts_tip)
        }
        binding.settingsInclude.iconsSub.text = runCatching {
            if (PREFS.iconPackPackage == "null") getString(R.string.iconPackNotSelectedSub)
            else packageManager.getApplicationLabel(
                packageManager.getApplicationInfo(
                    PREFS.iconPackPackage!!,
                    0
                )
            )
        }.getOrElse { getString(R.string.iconPackNotSelectedSub) }

        if (PREFS.isPrefsChanged) {
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
        finishAffinity()
        val componentName = Intent(this, this::class.java).component
        val intent = Intent.makeRestartActivityTask(componentName)
        startActivity(intent)
        PREFS.isPrefsChanged = false
        Runtime.getRuntime().exit(0)
    }

    override fun onPause() {
        super.onPause()
        hideViews()
    }
}
