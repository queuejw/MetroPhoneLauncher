package ru.dimon6018.metrolauncher

import android.app.UiModeManager
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomnavigation.BottomNavigationView
import ru.dimon6018.metrolauncher.Application.getLauncherAccentTheme
import ru.dimon6018.metrolauncher.content.AllApps
import ru.dimon6018.metrolauncher.content.Start
import ru.dimon6018.metrolauncher.content.data.DataProviderFragment
import ru.dimon6018.metrolauncher.content.data.Prefs
import ru.dimon6018.metrolauncher.helpers.AbstractDataProvider

class Main : AppCompatActivity() {

    private var viewPager: ViewPager2? = null
    private var pagerAdapter: FragmentStateAdapter? = null
    private var coord: CoordinatorLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(getLauncherAccentTheme())
        setAppTheme()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_screen_laucnher)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val navbar = findViewById<BottomNavigationView>(R.id.navigation)
        supportFragmentManager.beginTransaction()
                .add(DataProviderFragment(), FRAGMENT_TAG_DATA_PROVIDER)
                .commit()
        viewPager = findViewById(R.id.pager)
        pagerAdapter = NumberAdapter(this)
        coord = findViewById(R.id.coordinator)
        viewPager?.adapter = pagerAdapter
        navbar.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.start_win -> {
                    viewPager?.currentItem = 0
                    true
                }
                R.id.start_apps -> {
                    viewPager?.currentItem = 1
                    true
                }
                else -> false
            }
        }
        ViewCompat.setOnApplyWindowInsetsListener(coord!!) { v: View, insets: WindowInsetsCompat ->
            val pB = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
            val tB = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            v.setPadding(0, tB, 0, pB)
            WindowInsetsCompat.CONSUMED
        }
    }
    private fun setAppTheme() {
        val uimanager = getSystemService(UI_MODE_SERVICE) as UiModeManager
        if(Prefs(this).isLightThemeUsed) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                uimanager.setApplicationNightMode(UiModeManager.MODE_NIGHT_NO)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                uimanager.setApplicationNightMode(UiModeManager.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if(Prefs.isAccentChanged) {
            Prefs.isAccentChanged = false
            recreate()
        }
    }
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (viewPager!!.currentItem != 0) {
            viewPager!!.currentItem -= 1
        } else {
            super.onBackPressed()
        }
    }
    val dataProvider: AbstractDataProvider
        get() {
            val fragment = supportFragmentManager.findFragmentByTag(FRAGMENT_TAG_DATA_PROVIDER)
            return (fragment as DataProviderFragment?)!!.dataProvider
        }

    companion object {
        private const val FRAGMENT_TAG_DATA_PROVIDER = "data provider"
    }
    class NumberAdapter(fragment: FragmentActivity) : FragmentStateAdapter(fragment) {
        override fun getItemCount(): Int = 2

        override fun createFragment(position: Int): Fragment {
            return if (position == 1) AllApps() else Start()
        }
    }
}
