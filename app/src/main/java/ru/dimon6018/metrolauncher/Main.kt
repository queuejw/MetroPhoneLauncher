package ru.dimon6018.metrolauncher

import android.Manifest
import android.app.UiModeManager
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomnavigation.BottomNavigationView
import ru.dimon6018.metrolauncher.content.AllApps
import ru.dimon6018.metrolauncher.content.Start
import ru.dimon6018.metrolauncher.content.data.Prefs
import ru.dimon6018.metrolauncher.content.data.bsod.BSOD
import ru.dimon6018.metrolauncher.content.settings.BSODadapter.Companion.sendCrash
import ru.dimon6018.metrolauncher.helpers.WPDialog
import ru.dimon6018.metrolauncher.helpers.update.UpdateWorker.Companion.setupNotificationChannels

class Main : AppCompatActivity() {

    private val permCode = 123

    private lateinit var viewPager: ViewPager2
    private lateinit var pagerAdapter: FragmentStateAdapter

    private val handler: Handler = Handler(Looper.getMainLooper())
    private val runnable: Runnable = Runnable {
        Log.i("runnable", "clear bsod")
        checkCrashes()
    }
    private var prefs: Prefs? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(Application.launcherAccentTheme)
        setAppTheme()
        super.onCreate(savedInstanceState)
        prefs = Prefs(this)
        setContentView(R.layout.main_screen_laucnher)
        //      checkPermissions()
        initViews()
        setupNavigationBar()
    }

    private fun checkPermissions() {
        val permissions = arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE
        )
        val missingPermissions = ArrayList<String>()

        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(this, permission)
                    != PackageManager.PERMISSION_GRANTED
            ) {
                missingPermissions.add(permission)
            }
        }
        if (missingPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                    this,
                    missingPermissions.toTypedArray(),
                    permCode
            )
        } else {
            // Разрешения уже предоставлены
            // Можете выполнять операции с файлами и медиафайлами
        }
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == permCode) {
            for (result in grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    // Если хотя бы одно разрешение не было предоставлено
                    finish()
                    return
                }
            }
            // Все разрешения предоставлены
            // Можете выполнять операции с файлами и медиафайлами
        }
    }
    private fun checkCrashes() {
        if (prefs!!.pref.getBoolean("app_crashed", false)) {
            prefs!!.editor.putBoolean("app_crashed", false).apply()
            prefs!!.editor.putInt("crashCounter", 0).apply()
            if(prefs!!.isFeedbackEnabled) {
                Thread {
                    val db = BSOD.getData(this)
                    val pos = (db.getDao().getBsodList().size) - 1
                    val text = db.getDao().getBSOD(pos).log
                    runOnUiThread {
                        WPDialog(this).setTopDialog(true)
                                .setTitle(getString(R.string.bsodDialogTitle))
                                .setMessage(getString(R.string.bsodDialogMessage))
                                .setNegativeButton(getString(R.string.bsodDialogDismiss), null)
                                .setPositiveButton(getString(R.string.bsodDialogSend)) {
                                    sendCrash(text, this)
                                }.show()
                    }
                }.start()
            }
        }
    }
    override fun onLowMemory() {
        super.onLowMemory()
        WPDialog(this).setTopDialog(true)
                .setTitle("Not enough memory")
                .setMessage("This may degrade the performance of your phone. Try closing unnecessary applications")
                .setPositiveButton(getString(android.R.string.ok), null)
                .show()
    }

    private fun initViews() {
        viewPager = findViewById(R.id.pager)
        pagerAdapter = NumberAdapter(this)
        viewPager.adapter = pagerAdapter
        val coordinatorLayout: CoordinatorLayout = findViewById(R.id.coordinator)
        applyWindowInsets(coordinatorLayout)
    }

    private fun setupNavigationBar() {
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.navigation)

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
        val uiModeManager = getSystemService(UI_MODE_SERVICE) as UiModeManager
        val isLightThemeUsed = Prefs(this).isLightThemeUsed

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            uiModeManager.setApplicationNightMode(if (isLightThemeUsed) UiModeManager.MODE_NIGHT_NO else UiModeManager.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(if (isLightThemeUsed) AppCompatDelegate.MODE_NIGHT_NO else AppCompatDelegate.MODE_NIGHT_YES)
        }
    }

    override fun onStart() {
        super.onStart()
        //move this to oobe in future
        if(!prefs!!.pref.getBoolean("channelConfigured", false)) {
            setupNotificationChannels(this)
            prefs!!.editor.putBoolean("channelConfigured", true).apply()
        }
    }
    override fun onResume() {
        super.onResume()
        if (Prefs.isAccentChanged) {
            Prefs.isAccentChanged = false
            recreate()
        }
        handler.postDelayed(runnable, 5000)
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (viewPager.currentItem != 0) {
            viewPager.currentItem -= 1
        } else {
            super.onBackPressed()
        }
    }
    companion object {
         fun applyWindowInsets(target: View) {
            ViewCompat.setOnApplyWindowInsetsListener(target) { view, insets ->
                val paddingBottom = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
                val paddingTop = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
                view.setPadding(0, paddingTop, 0, paddingBottom)
                WindowInsetsCompat.CONSUMED
            }
        }
    }
    class NumberAdapter(fragment: FragmentActivity) : FragmentStateAdapter(fragment) {
        override fun getItemCount(): Int = 2

        override fun createFragment(position: Int): Fragment {
            return if (position == 1)  AllApps() else Start()
        }
    }
}