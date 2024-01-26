package ru.dimon6018.metrolauncher

import android.app.UiModeManager
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.dimon6018.metrolauncher.Application.Companion.PREFS
import ru.dimon6018.metrolauncher.content.AllApps
import ru.dimon6018.metrolauncher.content.Start
import ru.dimon6018.metrolauncher.content.data.AppDao
import ru.dimon6018.metrolauncher.content.data.AppData
import ru.dimon6018.metrolauncher.content.data.AppEntity
import ru.dimon6018.metrolauncher.content.data.Prefs
import ru.dimon6018.metrolauncher.content.data.bsod.BSOD
import ru.dimon6018.metrolauncher.content.settings.BSODadapter.Companion.sendCrash
import ru.dimon6018.metrolauncher.helpers.WPDialog
import ru.dimon6018.metrolauncher.helpers.update.UpdateWorker.Companion.setupNotificationChannels

class Main : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var pagerAdapter: FragmentStateAdapter
    private lateinit var dbCall: AppDao

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(Application.launcherAccentTheme())
        setAppTheme()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_screen_laucnher)
        viewPager = findViewById(R.id.pager)
        val coordinatorLayout: CoordinatorLayout = findViewById(R.id.coordinator)
        CoroutineScope(Dispatchers.Default).launch {
            if (!PREFS!!.pref.getBoolean("placeholdersGenerated", false)) {
                dbCall = AppData.getAppData(this@Main).getAppDao()
                generatePlaceholders()
            }
            pagerAdapter = NumberAdapter(this@Main)
            applyWindowInsets(coordinatorLayout)
            runOnUiThread {
                viewPager.adapter = pagerAdapter
            }
        }
        setupNavigationBar()
    }
    private fun checkCrashes() {
        if (PREFS!!.pref.getBoolean("app_crashed", false)) {
            CoroutineScope(Dispatchers.Default).launch {
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
        if(!PREFS!!.pref.getBoolean("channelConfigured", false)) {
            setupNotificationChannels(this)
            PREFS!!.editor.putBoolean("channelConfigured", true).apply()
        }
        if(PREFS!!.pref.getBoolean("updateInstalled", false) && PREFS!!.versionCode == Application.VERSION_CODE) {
            PREFS!!.setUpdateState(3)
        }
    }
    private fun generatePlaceholders() {
        CoroutineScope(Dispatchers.IO).launch {
            PREFS!!.editor.putBoolean("placeholdersGenerated", true).apply()
            val size = 35
            var temp = 0
            while (temp != size) {
                val placeholder = AppEntity(temp, temp + 1, -1, true, "small", "", "")
                temp += 1
                dbCall.insertItem(placeholder)
            }
        }
    }
    override fun onResume() {
        super.onResume()
        if (Prefs.isAccentChanged) {
            Prefs.isAccentChanged = false
            recreate()
        }
        checkCrashes()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (viewPager.currentItem != 0) {
            viewPager.currentItem -= 1
        } else {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewPager.adapter = null
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