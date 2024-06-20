package ru.dimon6018.metrolauncher

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.marginStart
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.dimon6018.metrolauncher.Application.Companion.PREFS
import ru.dimon6018.metrolauncher.content.NewAllApps
import ru.dimon6018.metrolauncher.content.NewStart
import ru.dimon6018.metrolauncher.content.data.bsod.BSOD
import ru.dimon6018.metrolauncher.content.oobe.WelcomeActivity
import ru.dimon6018.metrolauncher.helpers.WPDialog
import ru.dimon6018.metrolauncher.helpers.receivers.PackageChangesReceiver
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.VERSION_CODE
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.accentColorFromPrefs
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.applyWindowInsets
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.launcherAccentTheme
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.launcherSurfaceColor
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.registerPackageReceiver
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.sendCrash
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.unregisterPackageReceiver
import kotlin.system.exitProcess


class Main : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var pagerAdapter: FragmentStateAdapter

    // bottom bar
    private lateinit var bottomView: FrameLayout
    private var bottomMainView: LinearLayout? = null
    private var bottomViewStartBtn: ImageView? = null
    private var bottomViewSearchBtn: ImageView? = null

    private var bottomViewSearchBarView: LinearLayout? = null



    private val packageReceiver = PackageChangesReceiver()

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(launcherAccentTheme())
        setAppTheme()
        super.onCreate(savedInstanceState)
        when(PREFS!!.launcherState) {
            0 -> {
                val intent = Intent(this, WelcomeActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                finishAffinity()
                startActivity(intent)
                return
            }
        }
        setContentView(R.layout.main_screen_laucnher)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        viewPager = findViewById(R.id.pager)
        val coordinatorLayout: CoordinatorLayout = findViewById(R.id.coordinator)
        lifecycleScope.launch(Dispatchers.Default) {
            pagerAdapter = WinAdapter(this@Main)
            if(PREFS!!.isWallpaperUsed) {
                window?.setBackgroundDrawable(
                    ContextCompat.getDrawable(
                        this@Main,
                        R.drawable.start_transparent
                    )
                )
                window?.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER)
            }
            if (PREFS!!.pref.getBoolean(
                    "updateInstalled",
                    false
                ) && PREFS!!.versionCode == VERSION_CODE
            ) {
                PREFS!!.setUpdateState(3)
            }
            withContext(Dispatchers.Main) {
                applyWindowInsets(coordinatorLayout)
                viewPager.apply {
                    adapter = pagerAdapter
                }
                setupNavigationBar()
                onBackPressedDispatcher.addCallback(this@Main, object: OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {
                        if (viewPager.currentItem != 0) {
                            viewPager.currentItem -= 1
                        }
                    }
                })
            }
        }
        otherTasks()
    }
    private fun otherTasks() {
        if (PREFS!!.pref.getBoolean("tip1Enabled", true)) {
            WPDialog(this@Main).setTopDialog(false)
                .setTitle(getString(R.string.tip))
                .setMessage(getString(R.string.tip1))
                .setPositiveButton(getString(android.R.string.ok), null)
                .show()
            PREFS!!.editor.putBoolean("tip1Enabled", false).apply()
        }
        if (PREFS!!.pref.getBoolean("app_crashed", false)) {
            lifecycleScope.launch(Dispatchers.IO) {
                delay(5000)
                PREFS!!.editor.putBoolean("app_crashed", false).apply()
                PREFS!!.editor.putInt("crashCounter", 0).apply()
                if (PREFS!!.isFeedbackEnabled) {
                    var pos = (BSOD.getData(this@Main).getDao().getBsodList().size) - 1
                    if(pos < 0) {
                        pos = 0
                    }
                    val text = BSOD.getData(this@Main).getDao().getBSOD(pos).log
                    runOnUiThread {
                        WPDialog(this@Main).setTopDialog(true)
                            .setTitle(getString(R.string.bsodDialogTitle))
                            .setMessage(getString(R.string.bsodDialogMessage))
                            .setNegativeButton(getString(R.string.bsodDialogDismiss), null)
                            .setPositiveButton(getString(R.string.bsodDialogSend)) {
                                sendCrash(text, this@Main)
                            }.show()
                    }
                }
            }
        }
    }
    private fun setupNavigationBar() {
        bottomView = findViewById(R.id.navigation)
        when(PREFS!!.navBarColor) {
            0 -> {
                bottomView.setBackgroundColor(ContextCompat.getColor(this, android.R.color.background_dark))
            }
            1 -> {
                bottomView.setBackgroundColor(ContextCompat.getColor(this, android.R.color.background_light))
            }
            2 -> {
                bottomView.setBackgroundColor(accentColorFromPrefs(this))
            }
            3 -> {
                bottomView.visibility = View.GONE
                return
            }
            else -> {
                bottomView.setBackgroundColor(launcherSurfaceColor(theme))
            }
        }
        if(!PREFS!!.isSearchBarEnabled) {
            bottomMainView = findViewById(R.id.navigation_main)
            bottomMainView?.visibility = View.VISIBLE
            bottomViewStartBtn = findViewById(R.id.navigation_start_btn)
            bottomViewSearchBtn = findViewById(R.id.navigation_search_btn)
            bottomViewStartBtn?.setImageDrawable(when(PREFS!!.navBarIconValue) {
                0 -> {
                    ContextCompat.getDrawable(this, R.drawable.ic_os_windows_8)
                }
                1 -> {
                    ContextCompat.getDrawable(this, R.drawable.ic_os_windows)
                }
                2 -> {
                    ContextCompat.getDrawable(this, R.drawable.ic_os_android)
                }
                else -> {
                    ContextCompat.getDrawable(this, R.drawable.ic_os_windows_8)
                }
            })
            if(PREFS!!.navBarColor == 1 || PREFS!!.isLightThemeUsed) {
                bottomViewStartBtn?.setColorFilter(ContextCompat.getColor(this, android.R.color.black))
                bottomViewSearchBtn?.setColorFilter(ContextCompat.getColor(this, android.R.color.black))
            } else {
                bottomViewStartBtn?.setColorFilter(ContextCompat.getColor(this, android.R.color.white))
                bottomViewSearchBtn?.setColorFilter(ContextCompat.getColor(this, android.R.color.white))
            }
            bottomViewStartBtn?.setOnClickListener {
                viewPager.currentItem = 0
            }
            bottomViewSearchBtn?.setOnClickListener {
                viewPager.currentItem = 1
            }
        } else {
            bottomViewSearchBarView = findViewById(R.id.navigation_searchBar)
            bottomViewSearchBarView?.visibility = View.VISIBLE
        }
    }
    private fun setAppTheme() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return
        }
        if(PREFS!!.isLightThemeUsed) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }
    }
    override fun onResume() {
        super.onResume()
        if (PREFS!!.isPrefsChanged()) {
            PREFS!!.setPrefsChanged(false)
            exitProcess(0)
        }
        registerPackageReceiver(this, packageReceiver)
    }
    override fun onStop() {
        unregisterPackageReceiver(this, packageReceiver)
        super.onStop()
    }
    class WinAdapter(fragment: FragmentActivity) : FragmentStateAdapter(fragment) {
        override fun getItemCount(): Int = 2
        override fun createFragment(position: Int): Fragment {
            return if (position == 1) NewAllApps() else NewStart()
        }
    }
}