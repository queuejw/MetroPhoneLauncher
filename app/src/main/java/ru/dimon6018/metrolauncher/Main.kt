package ru.dimon6018.metrolauncher

import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.drawable.Icon
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.AutoCompleteTextView
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.collection.ArrayMap
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.WindowCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.textview.MaterialTextView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.dimon6018.metrolauncher.Application.Companion.PREFS
import ru.dimon6018.metrolauncher.Application.Companion.isAppOpened
import ru.dimon6018.metrolauncher.content.NewAllApps
import ru.dimon6018.metrolauncher.content.NewStart
import ru.dimon6018.metrolauncher.content.data.apps.App
import ru.dimon6018.metrolauncher.content.data.bsod.BSOD
import ru.dimon6018.metrolauncher.content.oobe.WelcomeActivity
import ru.dimon6018.metrolauncher.content.settings.SettingsActivity
import ru.dimon6018.metrolauncher.helpers.IconPackManager
import ru.dimon6018.metrolauncher.helpers.WPDialog
import ru.dimon6018.metrolauncher.helpers.receivers.PackageChangesReceiver
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.VERSION_CODE
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.accentColorFromPrefs
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.applyWindowInsets
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.launcherAccentColor
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.launcherAccentTheme
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.launcherSurfaceColor
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.recompressIcon
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.registerPackageReceiver
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.sendCrash
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.setUpApps
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.unregisterPackageReceiver
import java.util.Locale
import kotlin.system.exitProcess


class Main : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var pagerAdapter: FragmentStateAdapter

    // bottom bar
    private lateinit var bottomView: FrameLayout
    private var bottomMainView: LinearLayout? = null
    private var bottomViewStartBtn: ImageView? = null
    private var bottomViewSearchBtn: ImageView? = null

    // search bar
    private var bottomViewSearchBarView: LinearLayout? = null
    private var bottomViewSearchBar: TextInputLayout? = null

    private var searchRecyclerView: RecyclerView? = null
    private var searchBarResultsLayout: MaterialCardView? = null
    private var appList: MutableList<App>? = null
    private val hashCache = ArrayMap<String, Icon?>()
    private var searchAdapter: SearchAdapter? = null

    private val packageReceiver = PackageChangesReceiver()

    private var black: Int? = null
    private var white: Int? = null
    private var bottomViewReady = false

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(launcherAccentTheme())
        setAppTheme()
        black = ContextCompat.getColor(this@Main, android.R.color.black)
        white = ContextCompat.getColor(this@Main, android.R.color.white)
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
        isLandscape = this.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
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
                viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                    override fun onPageSelected(position: Int) {
                        if(!PREFS!!.isSearchBarEnabled && PREFS!!.navBarColor != 2) {
                            if (position == 0) {
                                if(PREFS!!.navBarColor == 1 || PREFS!!.isLightThemeUsed) {
                                    bottomViewStartBtn?.setColorFilter(launcherAccentColor(this@Main.theme))
                                    bottomViewSearchBtn?.setColorFilter(black!!)
                                } else {
                                    bottomViewStartBtn?.setColorFilter(launcherAccentColor(this@Main.theme))
                                    bottomViewSearchBtn?.setColorFilter(white!!)
                                }
                            } else {
                                if(PREFS!!.navBarColor == 1 || PREFS!!.isLightThemeUsed) {
                                    bottomViewStartBtn?.setColorFilter(black!!)
                                    bottomViewSearchBtn?.setColorFilter(launcherAccentColor(this@Main.theme))
                                } else {
                                    bottomViewStartBtn?.setColorFilter(white!!)
                                    bottomViewSearchBtn?.setColorFilter(launcherAccentColor(this@Main.theme))
                                }
                            }
                        }
                        super.onPageSelected(position)
                    }
                })
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

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        isLandscape = newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE
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
        if(bottomViewReady) {
            return
        }
        bottomViewReady = true
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
            bottomViewStartBtn?.setOnClickListener {
                viewPager.currentItem = 0
            }
            bottomViewSearchBtn?.setOnClickListener {
                if(!PREFS!!.isAllAppsEnabled) {
                    return@setOnClickListener
                }
                viewPager.currentItem = 1
            }
        } else {
            bottomViewSearchBarView = findViewById(R.id.navigation_searchBar)
            bottomViewSearchBarView?.visibility = View.VISIBLE
            searchRecyclerView = findViewById(R.id.searchBarRecyclerView)
            searchBarResultsLayout = findViewById(R.id.searchBarResults)
            bottomViewSearchBar = findViewById(R.id.searchBar)
            appList = setUpApps(this.packageManager, this)
            val iconSize = this.resources.getDimensionPixelSize(R.dimen.iconAppsListSize)
            var iconManager: IconPackManager? = null
            var isCustomIconsInstalled = false
            if (PREFS!!.iconPackPackage != "null") {
                iconManager = IconPackManager()
                iconManager.setContext(this)
                isCustomIconsInstalled = true
            }
            appList?.forEach {
                if (it.type != 1) {
                    hashCache[it.appPackage] = generateIcon(it, iconSize, iconManager, isCustomIconsInstalled)
                }
            }
            searchAdapter = SearchAdapter(this, null)
            searchRecyclerView?.apply {
                layoutManager = LinearLayoutManager(this@Main, LinearLayoutManager.VERTICAL, false)
                adapter = searchAdapter
            }
            (bottomViewSearchBar!!.editText as? AutoCompleteTextView)?.addTextChangedListener(object :
                TextWatcher {
                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    filterSearchText(s.toString())
                }
                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
                override fun afterTextChanged(s: Editable) {}
            })
        }
    }
    private fun hideSearchResults() {
        lifecycleScope.launch {
            searchBarResultsLayout?.apply {
                (bottomViewSearchBar!!.editText as? AutoCompleteTextView)?.text?.clear()
                ObjectAnimator.ofFloat(this, "alpha", 1f, 0f).start()
            }
            searchBarResultsLayout?.visibility = View.GONE
        }
    }
    private fun showSearchResults() {
        searchBarResultsLayout?.visibility = View.VISIBLE
        searchBarResultsLayout?.apply {
            ObjectAnimator.ofFloat(this, "alpha", 0f, 1f).setDuration(100).start()
        }
    }
    private fun generateIcon(it: App, iconSize: Int, iconManager: IconPackManager?, isCustomIconsInstalled: Boolean): Icon? {
        var bmp = if (!isCustomIconsInstalled) recompressIcon(
            this.packageManager.getApplicationIcon(it.appPackage!!).toBitmap(iconSize, iconSize),
            75
        )
        else
            recompressIcon(
                iconManager?.getIconPackWithName(PREFS!!.iconPackPackage)
                    ?.getDrawableIconForPackage(it.appPackage, null)
                    ?.toBitmap(iconSize, iconSize), 75
            )
        if (bmp == null) {
            bmp = recompressIcon(
                this.packageManager.getApplicationIcon(it.appPackage!!).toBitmap(iconSize, iconSize),
                75
            )
        }
        return bmp
    }
    private fun filterSearchText(text: String) {
        if(appList == null) {
            return
        }
        val filteredList: ArrayList<App> = ArrayList()
        val locale = Locale.getDefault()
        if(text.isEmpty()) {
            hideSearchResults()
        } else {
            showSearchResults()
        }
        val max = PREFS!!.maxResultsSearchBar
        for(i in 0..<appList!!.size) {
            val item = appList!![i]
            if (item.appLabel!!.lowercase(locale).contains(text.lowercase(locale))) {
                if(filteredList.size >= max) {
                    break
                }
                filteredList.add(item)
            }
        }
        if (filteredList.isNotEmpty()) {
            searchAdapter?.setData(filteredList)
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
        if (PREFS!!.isPrefsChanged()) {
            PREFS!!.setPrefsChanged(false)
            exitProcess(0)
        }
        super.onResume()
        registerPackageReceiver(this, packageReceiver)
    }
    override fun onDestroy() {
        super.onDestroy()
        unregisterPackageReceiver(this, packageReceiver)
    }
    companion object {
        var isLandscape: Boolean = false
    }
    inner class WinAdapter(fragment: FragmentActivity) : FragmentStateAdapter(fragment) {

        override fun getItemCount(): Int = if(PREFS!!.isAllAppsEnabled) 2 else 1

        override fun createFragment(position: Int): Fragment {
            return if(!PREFS!!.isAllAppsEnabled) {
                NewStart()
            } else {
                if (position == 1)
                    NewAllApps()
                else
                    NewStart()
            }

        }
    }
    inner class SearchAdapter(private val context: Context, private var dataList: MutableList<App>?): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return AppSearchHolder(LayoutInflater.from(parent.context).inflate(R.layout.app, parent, false))
        }
        override fun getItemCount(): Int {
            return if(dataList != null) dataList!!.size else 0
        }
        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            if (dataList != null) {
                holder as AppSearchHolder
                val app = dataList!![position]
                try {
                    val bmp = hashCache[app.appPackage]
                    if (bmp != null) {
                        holder.icon.setImageIcon(bmp)
                    } else {
                        holder.icon.setImageDrawable(context.packageManager.getApplicationIcon(app.appPackage!!))
                    }
                } catch (e: Exception) {
                    Log.e("Main", e.toString())
                }
                holder.label.text = app.appLabel
            }
        }
        fun setData(new: MutableList<App>) {
            dataList = new
            notifyDataSetChanged()
    }
    inner class AppSearchHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val icon: ImageView = itemView.findViewById(R.id.app_icon)
        val label: MaterialTextView = itemView.findViewById(R.id.app_label)
        init {
            label.setTextColor(launcherSurfaceColor(theme))
            itemView.setOnClickListener {
                val app = dataList!![absoluteAdapterPosition]
                runApp(app.appPackage!!)
                }
            }
        }
        private fun runApp(app: String) {
            isAppOpened = true
            when (app) {
                "ru.dimon6018.metrolauncher" -> {
                    startActivity(Intent(this@Main, SettingsActivity::class.java))
                }
                else -> {
                    startActivity(Intent(this@Main.packageManager.getLaunchIntentForPackage(app)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                }
            }
        }
    }
}