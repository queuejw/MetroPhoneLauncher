package ru.dimon6018.metrolauncher

import android.Manifest
import android.app.UiModeManager
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
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
import ru.dimon6018.metrolauncher.helpers.WPDialog

class Main : AppCompatActivity() {

    private val permCode = 123

    private lateinit var viewPager: ViewPager2
    private lateinit var pagerAdapter: FragmentStateAdapter
    private lateinit var coordinatorLayout: CoordinatorLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(Application.launcherAccentTheme)
        setAppTheme()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_screen_laucnher)
        //      checkPermissions()
        initViews()
        setupNavigationBar()
        applyWindowInsets()
        checkCrashes()
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
                    Toast.makeText(
                            this,
                            "Необходимо предоставить все разрешения для работы приложения",
                            Toast.LENGTH_SHORT
                    ).show()

                    // Закрываем приложение
                    finish()
                    return
                }
            }

            // Все разрешения предоставлены
            // Можете выполнять операции с файлами и медиафайлами
        }
    }

    private fun checkCrashes() {
        if (Prefs(this).pref.getBoolean("app_crashed", true)) {
            Prefs(this).editor.putBoolean("app_crashed", false).apply()
            WPDialog(this).setTopDialog(true)
                    .setTitle(getString(R.string.bsodDialogTitle))
                    .setMessage(getString(R.string.bsodDialogMessage))
                    .setNegativeButton(getString(R.string.bsodDialogDismiss), null)
                    .setPositiveButton(getString(R.string.bsodDialogSend)) {
                    }.show()
        }
    }

    private fun initViews() {
        viewPager = findViewById(R.id.pager)
        pagerAdapter = NumberAdapter(this)
        coordinatorLayout = findViewById(R.id.coordinator)
        viewPager.adapter = pagerAdapter
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

    private fun applyWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(coordinatorLayout) { view, insets ->
            val paddingBottom = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
            val paddingTop = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            view.setPadding(0, paddingTop, 0, paddingBottom)
            WindowInsetsCompat.CONSUMED
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

    override fun onResume() {
        super.onResume()
        if (Prefs.isAccentChanged) {
            Prefs.isAccentChanged = false
            recreate()
        }
        if (viewPager.currentItem == 1) {
            AllApps.checkApps()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (viewPager.currentItem != 0) {
            viewPager.currentItem -= 1
        } else {
            super.onBackPressed()
        }
    }

    class NumberAdapter(fragment: FragmentActivity) : FragmentStateAdapter(fragment) {
        override fun getItemCount(): Int = 2

        override fun createFragment(position: Int): Fragment {
            return if (position == 1) AllApps() else Start()
        }
    }
}