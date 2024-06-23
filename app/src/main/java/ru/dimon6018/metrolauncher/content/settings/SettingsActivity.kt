package ru.dimon6018.metrolauncher.content.settings

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textview.MaterialTextView
import kotlinx.coroutines.Dispatchers
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
import ru.dimon6018.metrolauncher.helpers.WPDialog
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.accentName
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.applyWindowInsets
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.launcherAccentTheme
import kotlin.system.exitProcess

class SettingsActivity : AppCompatActivity() {

    private var themeSub: MaterialTextView? = null
    private var navSub: MaterialTextView? = null
    private var iconsSub: MaterialTextView? = null

    private var isDialogEnabled = true
    private var isEnter = false

    private var themeBtn: MaterialCardView? = null
    private var allAppsBtn: MaterialCardView? = null
    private var tilesBtn: MaterialCardView? = null
    private var aboutBtn: MaterialCardView? = null
    private var feedbackBtn: MaterialCardView? = null
    private var updateBtn: MaterialCardView? = null
    private var navBarBtn: MaterialCardView? = null
    private var weatherBtm: MaterialCardView? = null
    private var iconBtn: MaterialCardView? = null
    private var expBtn: MaterialCardView? = null
    private var leaks: MaterialCardView? = null
    private var animsBtn: MaterialCardView? = null

    private var cord: CoordinatorLayout? = null

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
        applyWindowInsets(cord!!)
        themeBtn = findViewById(R.id.themeSetting)
        themeBtn?.setOnClickListener {
            lifecycleScope.launch(Dispatchers.Main) {
                isEnter = true
                if(PREFS!!.isTransitionAnimEnabled) {
                    startAnim()
                    delay(100)
                }
                startActivity(Intent(this@SettingsActivity, ThemeSettingsActivity::class.java))
            }
        }
        allAppsBtn = findViewById(R.id.allAppsSetting)
        allAppsBtn?.setOnClickListener {
            lifecycleScope.launch {
                isEnter = true
                if(PREFS!!.isTransitionAnimEnabled) {
                    startAnim()
                    delay(100)
                }
                startActivity(Intent(this@SettingsActivity, AllAppsSettingsActivity::class.java))
            }
        }
        tilesBtn = findViewById(R.id.tilesSetting)
        tilesBtn?.setOnClickListener {
            lifecycleScope.launch {
                isEnter = true
                if(PREFS!!.isTransitionAnimEnabled) {
                    startAnim()
                    delay(100)
                }
                startActivity(Intent(this@SettingsActivity, TileSettingsActivity::class.java))
            }
        }
        aboutBtn = findViewById(R.id.aboutSetting)
        aboutBtn?.setOnClickListener {
            lifecycleScope.launch {
                isEnter = true
                if(PREFS!!.isTransitionAnimEnabled) {
                    startAnim()
                    delay(200)
                }
                startActivity(Intent(this@SettingsActivity, AboutSettingsActivity::class.java))
            }
        }
        feedbackBtn = findViewById(R.id.feedbackSetting)
        feedbackBtn?.setOnClickListener {
            lifecycleScope.launch {
                isEnter = true
                if(PREFS!!.isTransitionAnimEnabled) {
                    startAnim()
                    delay(200)
                }
                startActivity(Intent(this@SettingsActivity, FeedbackSettingsActivity::class.java))
            }
        }
        updateBtn = findViewById(R.id.updatesSetting)
        updateBtn?.setOnClickListener {
            lifecycleScope.launch {
                isEnter = true
                if(PREFS!!.isTransitionAnimEnabled) {
                    startAnim()
                    delay(200)
                }
                startActivity(Intent(this@SettingsActivity, UpdateActivity::class.java))
            }
        }
        navBarBtn = findViewById(R.id.navbarSetting)
        navBarBtn?.setOnClickListener {
            lifecycleScope.launch {
                isEnter = true
                if(PREFS!!.isTransitionAnimEnabled) {
                    startAnim()
                    delay(200)
                }
                startActivity(Intent(this@SettingsActivity, NavBarSettingsActivity::class.java))
            }
        }
        weatherBtm = findViewById(R.id.weatherSetting)
        weatherBtm?.setOnClickListener {
            lifecycleScope.launch {
                isEnter = true
                if(PREFS!!.isTransitionAnimEnabled) {
                    startAnim()
                    delay(200)
                }
                startActivity(Intent(this@SettingsActivity, WeatherSettingsActivity::class.java))
            }
        }
        iconBtn = findViewById(R.id.iconsSetting)
        iconBtn?.setOnClickListener {
            lifecycleScope.launch {
                isEnter = true
                if(PREFS!!.isTransitionAnimEnabled) {
                    startAnim()
                    delay(200)
                }
                startActivity(Intent(this@SettingsActivity, IconSettingsActivity::class.java))
            }
        }
        expBtn = findViewById(R.id.expSetting)
        expBtn?.setOnClickListener {
            lifecycleScope.launch {
                isEnter = true
                if(PREFS!!.isTransitionAnimEnabled) {
                    startAnim()
                    delay(200)
                }
                startActivity(Intent(this@SettingsActivity, ExperimentsSettingsActivity::class.java))
            }
        }
        leaks = findViewById(R.id.leaks)
        leaks?.setOnClickListener {
            lifecycleScope.launch {
                isEnter = true
                if(PREFS!!.isTransitionAnimEnabled) {
                    startAnim()
                    delay(200)
                }
                startActivity(LeakCanary.newLeakDisplayActivityIntent())
            }
        }
        animsBtn = findViewById(R.id.animSetting)
        animsBtn?.setOnClickListener {
            lifecycleScope.launch {
                isEnter = true
                if(PREFS!!.isTransitionAnimEnabled) {
                    startAnim()
                    delay(200)
                }
                startActivity(Intent(this@SettingsActivity, AnimationSettingsActivity::class.java))
            }
        }
        hideViews()
    }
    private fun startAnim() {
        if(PREFS!!.isTransitionAnimEnabled) {
            setupAnim(themeBtn!!, 300)
            setupAnim(allAppsBtn!!, 320)
            setupAnim(tilesBtn!!, 340)
            setupAnim(iconBtn!!, 360)
            setupAnim(animsBtn!!, 380)
            setupAnim(feedbackBtn!!, 400)
            setupAnim(weatherBtm!!, 420)
            setupAnim(updateBtn!!, 440)
            setupAnim(navBarBtn!!, 460)
            setupAnim(aboutBtn!!, 480)
            setupAnim(leaks!!, 490)
            setupAnim(expBtn!!, 500)
            isEnter = false
        }
    }
    private fun hideViews() {
        if(PREFS!!.isTransitionAnimEnabled) {
            hideAnim(themeBtn!!)
            hideAnim(allAppsBtn!!)
            hideAnim(tilesBtn!!)
            hideAnim(iconBtn!!)
            hideAnim(animsBtn!!)
            hideAnim(feedbackBtn!!)
            hideAnim(weatherBtm!!)
            hideAnim(updateBtn!!)
            hideAnim(navBarBtn!!)
            hideAnim(aboutBtn!!)
            hideAnim(leaks!!)
            hideAnim(expBtn!!)
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
        themeSub?.text = accentName(this)
        navSub?.text = when (PREFS!!.navBarColor) {
            0 -> getString(R.string.always_dark)
            1 -> getString(R.string.always_light)
            2 -> getString(R.string.matches_accent_color)
            3 -> getString(R.string.hide_navbar)
            4 -> getString(R.string.auto)
            else -> getString(R.string.navigation_bar_2)
        }
        try {
            iconsSub?.text =
                if (PREFS!!.iconPackPackage == "null") getString(R.string.iconPackNotSelectedSub) else packageManager.getApplicationLabel(
                    packageManager.getApplicationInfo(PREFS!!.iconPackPackage!!, 0)
                )
        } catch (e: Exception) {
            iconsSub?.text = getString(R.string.iconPackNotSelectedSub)
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
        if(PREFS!!.isPrefsChanged()) {
            PREFS!!.setPrefsChanged(false)
            Toast.makeText(this, getString(R.string.restart_required), Toast.LENGTH_LONG).show()
            exitProcess(0)
        } else {
            startAnim()
        }
    }
    override fun onStop() {
        hideViews()
        super.onStop()
    }
}
