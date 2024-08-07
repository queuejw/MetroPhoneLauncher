package ru.dimon6018.metrolauncher.content.settings

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textview.MaterialTextView
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
import ru.dimon6018.metrolauncher.helpers.ui.WPDialog
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.accentName
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.applyWindowInsets
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.launcherAccentTheme
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.setViewInteractAnimation
import kotlin.system.exitProcess

class SettingsActivity : AppCompatActivity() {

    private lateinit var themeSub: MaterialTextView
    private lateinit var navSub: MaterialTextView
    private lateinit var iconsSub: MaterialTextView

    private var isDialogEnabled = true
    private var isEnter = false

    private lateinit var themeBtn: MaterialCardView
    private lateinit var allAppsBtn: MaterialCardView
    private lateinit var tilesBtn: MaterialCardView
    private lateinit var aboutBtn: MaterialCardView
    private lateinit var feedbackBtn: MaterialCardView
    private lateinit var updateBtn: MaterialCardView
    private lateinit var navBarBtn: MaterialCardView
    private lateinit var weatherBtm: MaterialCardView
    private lateinit var iconBtn: MaterialCardView
    private lateinit var expBtn: MaterialCardView
    private lateinit var leaks: MaterialCardView
    private lateinit var animsBtn: MaterialCardView
    private lateinit var cord: CoordinatorLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(launcherAccentTheme())
        setAppTheme()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.launcher_settings_main)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        cord = findViewById(R.id.coordinator)
        themeSub = findViewById(R.id.theme_sub)
        iconsSub = findViewById(R.id.icons_sub)
        navSub = findViewById(R.id.navbar_sub)
        applyWindowInsets(cord)
        themeBtn = findViewById(R.id.themeSetting)
        themeBtn.setOnClickListener {
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
        allAppsBtn = findViewById(R.id.allAppsSetting)
        allAppsBtn.setOnClickListener {
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
        tilesBtn = findViewById(R.id.tilesSetting)
        tilesBtn.setOnClickListener {
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
        aboutBtn = findViewById(R.id.aboutSetting)
        aboutBtn.setOnClickListener {
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
        feedbackBtn = findViewById(R.id.feedbackSetting)
        feedbackBtn.setOnClickListener {
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
        updateBtn = findViewById(R.id.updatesSetting)
        updateBtn.setOnClickListener {
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
        navBarBtn = findViewById(R.id.navbarSetting)
        navBarBtn.setOnClickListener {
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
        weatherBtm = findViewById(R.id.weatherSetting)
        weatherBtm.setOnClickListener {
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
        iconBtn = findViewById(R.id.iconsSetting)
        iconBtn.setOnClickListener {
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
        expBtn = findViewById(R.id.expSetting)
        expBtn.setOnClickListener {
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
        leaks = findViewById(R.id.leaks)
        leaks.setOnClickListener {
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
        animsBtn = findViewById(R.id.animSetting)
        animsBtn.setOnClickListener {
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
        confAnim()
    }
    private fun confAnim() {
        setViewInteractAnimation(themeBtn)
        setViewInteractAnimation(allAppsBtn)
        setViewInteractAnimation(tilesBtn)
        setViewInteractAnimation(iconBtn)
        setViewInteractAnimation(animsBtn)
        setViewInteractAnimation(feedbackBtn)
        setViewInteractAnimation(weatherBtm)
        setViewInteractAnimation(updateBtn)
        setViewInteractAnimation(navBarBtn)
        setViewInteractAnimation(aboutBtn)
        setViewInteractAnimation(leaks)
        setViewInteractAnimation(expBtn)
    }
    private suspend fun startAnim() {
        if(PREFS!!.isTransitionAnimEnabled) {
            setupAnim(themeBtn, 200)
            setupAnim(allAppsBtn, 210)
            setupAnim(tilesBtn, 220)
            setupAnim(iconBtn, 225)
            setupAnim(animsBtn, 230)
            setupAnim(feedbackBtn, 235)
            setupAnim(weatherBtm, 240)
            setupAnim(updateBtn, 245)
            setupAnim(navBarBtn, 250)
            setupAnim(aboutBtn, 255)
            setupAnim(leaks, 260)
            setupAnim(expBtn, 265)
            setupAnim(cord, 280)
            isEnter = false
            CoroutineScope(Dispatchers.Main).launch {
                delay(500)
                themeBtn.alpha = 1f
                allAppsBtn.alpha = 1f
                tilesBtn.alpha = 1f
                iconBtn.alpha = 1f
                animsBtn.alpha = 1f
                feedbackBtn.alpha = 1f
                weatherBtm.alpha = 1f
                updateBtn.alpha = 1f
                navBarBtn.alpha = 1f
                aboutBtn.alpha = 1f
                leaks.alpha = 1f
                expBtn.alpha = 1f
                cord.alpha = 1f
                cancel()
            }
            delay(150)
        } else {
            isEnter = false
        }
    }
    private fun hideViews() {
        if(PREFS!!.isTransitionAnimEnabled) {
            hideAnim(themeBtn)
            hideAnim(allAppsBtn)
            hideAnim(tilesBtn)
            hideAnim(iconBtn)
            hideAnim(animsBtn)
            hideAnim(feedbackBtn)
            hideAnim(weatherBtm)
            hideAnim(updateBtn)
            hideAnim(navBarBtn)
            hideAnim(aboutBtn)
            hideAnim(leaks)
            hideAnim(expBtn)
            hideAnim(cord)
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
        themeSub.text = accentName(this)
        navSub.text = when (PREFS!!.navBarColor) {
            0 -> getString(R.string.always_dark)
            1 -> getString(R.string.always_light)
            2 -> getString(R.string.matches_accent_color)
            3 -> getString(R.string.hide_navbar)
            4 -> getString(R.string.auto)
            else -> getString(R.string.navigation_bar_2)
        }
        try {
            iconsSub.text =
                if (PREFS!!.iconPackPackage == "null") getString(R.string.iconPackNotSelectedSub) else packageManager.getApplicationLabel(
                    packageManager.getApplicationInfo(PREFS!!.iconPackPackage!!, 0)
                )
        } catch (e: Exception) {
            iconsSub.text = getString(R.string.iconPackNotSelectedSub)
        }
        if(!isHomeApp() && isDialogEnabled) {
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
