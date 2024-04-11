package ru.dimon6018.metrolauncher

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.core.graphics.toColor
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.dimon6018.metrolauncher.Application.Companion.PREFS
import ru.dimon6018.metrolauncher.content.NewAllApps
import ru.dimon6018.metrolauncher.content.NewStart
import ru.dimon6018.metrolauncher.content.data.Prefs
import ru.dimon6018.metrolauncher.content.data.bsod.BSOD
import ru.dimon6018.metrolauncher.content.oobe.WelcomeActivity
import ru.dimon6018.metrolauncher.content.settings.BSODadapter.Companion.sendCrash
import ru.dimon6018.metrolauncher.helpers.WPDialog

class Main : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var pagerAdapter: FragmentStateAdapter
    private lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(Application.launcherAccentTheme())
        super.onCreate(savedInstanceState)
        setAppTheme()
        setContentView(R.layout.main_screen_laucnher)
        viewPager = findViewById(R.id.pager)
        when(PREFS!!.launcherState) {
            0 -> {
                val intent = Intent(this, WelcomeActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                finishAffinity()
                startActivity(intent)
                return
            }
        }
        val coordinatorLayout: CoordinatorLayout = findViewById(R.id.coordinator)
        lifecycleScope.launch(Dispatchers.Main) {
            pagerAdapter = NumberAdapter(this@Main)
            applyWindowInsets(coordinatorLayout)
            runOnUiThread {
                viewPager.adapter = pagerAdapter
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
    }
    fun openAllApps() {
        bottomNavigationView.selectedItemId = R.id.start_apps
    }
    fun openStart() {
        bottomNavigationView.selectedItemId = R.id.start_win
    }
    fun hideNavBar() {
        bottomNavigationView.visibility = View.GONE
    }
    fun showNavBar() {
        bottomNavigationView.visibility = View.VISIBLE
    }
    private fun otherTasks() {
        if(PREFS!!.pref.getBoolean("updateInstalled", false) && PREFS!!.versionCode == Application.VERSION_CODE) {
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
            CoroutineScope(Dispatchers.IO).launch {
                delay(5000)
                PREFS!!.editor.putBoolean("app_crashed", false).apply()
                PREFS!!.editor.putInt("crashCounter", 0).apply()
                if (PREFS!!.isFeedbackEnabled) {
                    val dbCall = BSOD.getData(this@Main).getDao()
                    val pos = (dbCall.getBsodList().size) - 1
                    val text = dbCall.getBSOD(pos).log
                    runOnUiThread {
                        viewPager.visibility = View.VISIBLE
                        WPDialog(this@Main).setTopDialog(true)
                                .setTitle(getString(R.string.bsodDialogTitle))
                                .setMessage(getString(R.string.bsodDialogMessage))
                                .setNegativeButton(getString(R.string.bsodDialogDismiss), null)
                                .setOnDismissListener {
                                    viewPager.visibility = View.VISIBLE
                                }
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
                bottomNavigationView.setBackgroundColor(Application.accentColorFromPrefs(this))
            }
            3 -> {
                bottomNavigationView.visibility = View.GONE
                return
            }
            else -> {
                bottomNavigationView.setBackgroundColor(Application.launcherSurfaceColor(theme))
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

    override fun onStart() {
        super.onStart()
        otherTasks()
    }
    override fun onResume() {
        super.onResume()
        if (Prefs.isPrefsChanged) {
            Prefs.isPrefsChanged = false
            recreate()
            return
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
            return if (position == 1) NewAllApps() else NewStart()
        }
    }
}