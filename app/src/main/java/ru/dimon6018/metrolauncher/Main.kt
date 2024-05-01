package ru.dimon6018.metrolauncher

import android.Manifest
import android.app.WallpaperManager
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.dimon6018.metrolauncher.Application.Companion.PREFS
import ru.dimon6018.metrolauncher.content.NewAllApps
import ru.dimon6018.metrolauncher.content.NewStart
import ru.dimon6018.metrolauncher.content.data.bsod.BSOD
import ru.dimon6018.metrolauncher.content.oobe.WelcomeActivity
import ru.dimon6018.metrolauncher.helpers.WPDialog
import ru.dimon6018.metrolauncher.helpers.receivers.PackageChangesReceiver
import ru.dimon6018.metrolauncher.helpers.utils.Utils
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.VERSION_CODE
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.accentColorFromPrefs
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.applyWindowInsets
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.launcherAccentTheme
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.launcherSurfaceColor
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.saveError
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.sendCrash
import kotlin.system.exitProcess


class Main : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var pagerAdapter: FragmentStateAdapter
    private lateinit var bottomNavigationView: BottomNavigationView

    private val packageReceiver = PackageChangesReceiver()

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(launcherAccentTheme())
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setAppTheme()
        setContentView(R.layout.main_screen_laucnher)
        when(PREFS!!.launcherState) {
            0 -> {
                val intent = Intent(this, WelcomeActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                finishAffinity()
                startActivity(intent)
                return
            }
        }
        viewPager = findViewById(R.id.pager)
        val coordinatorLayout: CoordinatorLayout = findViewById(R.id.coordinator)
        applyWindowInsets(coordinatorLayout)
        lifecycleScope.launch(Dispatchers.Default) {
            pagerAdapter = WinAdapter(this@Main)
            if(PREFS!!.isWallpaperUsed) {
                try {
                    if(checkStoragePermissions()) {
                        val wallpaperManager = WallpaperManager.getInstance(this@Main)
                        val bmp = wallpaperManager.drawable
                        runOnUiThread {
                            coordinatorLayout.background = bmp
                        }
                    } else {
                        runOnUiThread {
                            permsDialog()
                            getPermission()
                        }
                    }
                } catch (e: Exception) {
                    Log.e("Start", e.toString())
                    saveError(e.toString(), BSOD.getData(this@Main))
                }
            } else {
                runOnUiThread {
                    coordinatorLayout.background = null
                }
            }
            runOnUiThread {
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
        Utils.registerPackageReceiver(this, packageReceiver)
        otherTasks()
    }
    private fun permsDialog() {
        WPDialog(this).setTopDialog(false)
            .setTitle(getString(R.string.tip))
            .setMessage(getString(R.string.permissionsError))
            .setPositiveButton(getString(android.R.string.ok), null).show()
    }
    private fun checkStoragePermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            val write =
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            val read =
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            read == PackageManager.PERMISSION_GRANTED && write == PackageManager.PERMISSION_GRANTED
        }
    }
    private fun getPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).setData(Uri.parse(String.format("package:%s", packageName)))
            startActivity(intent)
        } else {
            ActivityCompat.requestPermissions(
                this, arrayOf(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ),
                1507
            )
        }
    }
    private fun otherTasks() {
        if (PREFS!!.pref.getBoolean(
                "updateInstalled",
                false
            ) && PREFS!!.versionCode == VERSION_CODE
        ) {
            PREFS!!.setUpdateState(3)
        }
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
                    val dbCall = BSOD.getData(this@Main).getDao()
                    val pos = (dbCall.getBsodList().size) - 1
                    val text = dbCall.getBSOD(pos).log
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
        bottomNavigationView = findViewById(R.id.navigation)
        when(PREFS!!.navBarColor) {
            0 -> {
                bottomNavigationView.setBackgroundColor(ContextCompat.getColor(this, android.R.color.background_dark))
            }
            1 -> {
                bottomNavigationView.setBackgroundColor(ContextCompat.getColor(this, android.R.color.background_light))
            }
            2 -> {
                bottomNavigationView.setBackgroundColor(accentColorFromPrefs(this))
            }
            3 -> {
                bottomNavigationView.visibility = View.GONE
                return
            }
            else -> {
                bottomNavigationView.setBackgroundColor(launcherSurfaceColor(theme))
            }
        }
        bottomNavigationView.selectedItemId = R.id.start_apps
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.start_win -> {
                    viewPager.currentItem = 0
                    true
                }
                R.id.start_apps -> {
                    viewPager.currentItem = 1
                    true
                }
                else -> false
            }
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
    }
    override fun onStop() {
        Utils.unregisterPackageReceiver(this, packageReceiver)
        super.onStop()
    }
    class WinAdapter(fragment: FragmentActivity) : FragmentStateAdapter(fragment) {
        override fun getItemCount(): Int = 2
        override fun createFragment(position: Int): Fragment {
            return if (position == 1) NewAllApps() else NewStart()
        }
    }
}