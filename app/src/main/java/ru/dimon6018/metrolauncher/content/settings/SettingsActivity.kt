package ru.dimon6018.metrolauncher.content.settings

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.WindowCompat
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textview.MaterialTextView
import leakcanary.LeakCanary
import ru.dimon6018.metrolauncher.Application
import ru.dimon6018.metrolauncher.Application.Companion.EXP_PREFS
import ru.dimon6018.metrolauncher.Application.Companion.PREFS
import ru.dimon6018.metrolauncher.R
import ru.dimon6018.metrolauncher.content.settings.activities.AboutSettingsActivity
import ru.dimon6018.metrolauncher.content.settings.activities.AllAppsSettingsActivity
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

class SettingsActivity : AppCompatActivity() {

    private var themeSub: MaterialTextView? = null
    private var navSub: MaterialTextView? = null
    private var iconsSub: MaterialTextView? = null

    private var isDialogEnabled = true
    private var animationEnabled = true
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
    }
    override fun onStart() {
        super.onStart()
        themeBtn = findViewById(R.id.themeSetting)
        themeBtn?.setOnClickListener {
            isEnter = true
            startAnim()
            startActivity(Intent(this@SettingsActivity, ThemeSettingsActivity::class.java))
        }
        allAppsBtn = findViewById(R.id.allAppsSetting)
        allAppsBtn?.setOnClickListener {
            isEnter = true
            startAnim()
            startActivity(Intent(this@SettingsActivity, AllAppsSettingsActivity::class.java))
        }
        tilesBtn = findViewById(R.id.tilesSetting)
        tilesBtn?.setOnClickListener {
            isEnter = true
            startAnim()
            startActivity(Intent(this@SettingsActivity, TileSettingsActivity::class.java))
        }
        aboutBtn = findViewById(R.id.aboutSetting)
        aboutBtn?.setOnClickListener {
            isEnter = true
            startAnim()
            startActivity(Intent(this@SettingsActivity, AboutSettingsActivity::class.java))
        }
        feedbackBtn = findViewById(R.id.feedbackSetting)
        feedbackBtn?.setOnClickListener {
            isEnter = true
            startAnim()
            startActivity(Intent(this@SettingsActivity, FeedbackSettingsActivity::class.java))
        }
        updateBtn = findViewById(R.id.updatesSetting)
        updateBtn?.setOnClickListener {
            isEnter = true
            startAnim()
            startActivity(Intent(this@SettingsActivity, UpdateActivity::class.java))
        }
        navBarBtn = findViewById(R.id.navbarSetting)
        navBarBtn?.setOnClickListener {
            isEnter = true
            startAnim()
            startActivity(Intent(this@SettingsActivity, NavBarSettingsActivity::class.java))
        }
        weatherBtm = findViewById(R.id.weatherSetting)
        weatherBtm?.setOnClickListener {
            isEnter = true
            startAnim()
            startActivity(Intent(this@SettingsActivity, WeatherSettingsActivity::class.java))
        }
        iconBtn = findViewById(R.id.iconsSetting)
        iconBtn?.setOnClickListener {
            isEnter = true
            startAnim()
            startActivity(Intent(this@SettingsActivity, IconSettingsActivity::class.java))
        }
        expBtn = findViewById(R.id.expSetting)
        expBtn?.setOnClickListener {
            isEnter = true
            startAnim()
            startActivity(Intent(this@SettingsActivity, ExperimentsSettingsActivity::class.java))
        }
        leaks = findViewById(R.id.leaks)
        leaks?.setOnClickListener {
            isEnter = true
            startAnim()
            startActivity(LeakCanary.newLeakDisplayActivityIntent())
        }
    }
    private fun startAnim() {
        if(animationEnabled && EXP_PREFS!!.getAnimationPref) {
            setupAnim(cord!!, 320)
            setupAnim(themeBtn!!, 300)
            setupAnim(allAppsBtn!!, 320)
            setupAnim(tilesBtn!!, 340)
            setupAnim(iconBtn!!, 360)
            setupAnim(feedbackBtn!!, 380)
            setupAnim(weatherBtm!!, 400)
            setupAnim(updateBtn!!, 420)
            setupAnim(navBarBtn!!, 440)
            setupAnim(aboutBtn!!, 460)
            setupAnim(leaks!!, 480)
            setupAnim(expBtn!!, 500)
            animationEnabled = false
            isEnter = false
        }
    }
    private fun setupAnim(view: View?, duration: Long) {
        if(view == null) {
            return
        }
        val animatorSet = AnimatorSet()
        if(!isEnter) {
            animatorSet.playTogether(
                ObjectAnimator.ofFloat(view, "rotationY", 30f, 0f),
                ObjectAnimator.ofFloat(view, "rotation", 10f, 0f),
                ObjectAnimator.ofFloat(view, "alpha", 0f, 1f)
            )
        } else {
            animatorSet.playTogether(
                ObjectAnimator.ofFloat(view, "rotationY", 30f, 0f),
                ObjectAnimator.ofFloat(view, "rotation", -10f, 0f),
                ObjectAnimator.ofFloat(view, "alpha", 0f, 1f)
            )
        }
        animatorSet.setDuration(duration)
        animatorSet.start()
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
        startAnim()
    }
    override fun onPause() {
        super.onPause()
        animationEnabled = true
    }
}
