package ru.dimon6018.metrolauncher

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.widget.AutoCompleteTextView
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView.OnEditorActionListener
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.WindowCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.textview.MaterialTextView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.dimon6018.metrolauncher.Application.Companion.PREFS
import ru.dimon6018.metrolauncher.Application.Companion.isAppOpened
import ru.dimon6018.metrolauncher.content.NewAllApps
import ru.dimon6018.metrolauncher.content.NewStart
import ru.dimon6018.metrolauncher.content.data.app.App
import ru.dimon6018.metrolauncher.content.data.bsod.BSOD
import ru.dimon6018.metrolauncher.content.oobe.WelcomeActivity
import ru.dimon6018.metrolauncher.content.settings.SettingsActivity
import ru.dimon6018.metrolauncher.databinding.MainScreenLaucnherBinding
import ru.dimon6018.metrolauncher.helpers.IconPackManager
import ru.dimon6018.metrolauncher.helpers.disklru.CacheUtils.Companion.closeDiskCache
import ru.dimon6018.metrolauncher.helpers.disklru.CacheUtils.Companion.initDiskCache
import ru.dimon6018.metrolauncher.helpers.disklru.CacheUtils.Companion.loadIconFromDiskCache
import ru.dimon6018.metrolauncher.helpers.disklru.CacheUtils.Companion.saveIconToDiskCache
import ru.dimon6018.metrolauncher.helpers.receivers.PackageChangesReceiver
import ru.dimon6018.metrolauncher.helpers.ui.WPDialog
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.VERSION_CODE
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.accentColorFromPrefs
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.applyWindowInsets
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.isDevMode
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.launcherAccentColor
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.launcherAccentTheme
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.launcherSurfaceColor
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.registerPackageReceiver
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.sendCrash
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.setUpApps
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.sortApps
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.unregisterPackageReceiver
import java.util.Locale
import kotlin.system.exitProcess

class Main : AppCompatActivity() {

    private lateinit var pagerAdapter: FragmentStateAdapter
    private lateinit var mainViewModel: MainViewModel
    private val iconPackManager: IconPackManager by lazy {
        IconPackManager(this)
    }
    private val packageReceiver: PackageChangesReceiver by lazy {
        PackageChangesReceiver()
    }
    private val defaultIconSize: Int by lazy {
        resources.getDimensionPixelSize(R.dimen.tile_default_size)
    }

    private var searchAdapter: SearchAdapter? = null
    private var filteredList: MutableList<App>? = null

    private var bottomViewReady = false
    private var searching = false

    private lateinit var binding: MainScreenLaucnherBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(launcherAccentTheme())
        setAppTheme()
        if (isDevMode(this) && PREFS!!.isAutoShutdownAnimEnabled) {
            //disabling animations if developer mode is enabled (to avoid problems)
            disableAnims()
        }
        super.onCreate(savedInstanceState)
        if (PREFS!!.launcherState == 0) {
            runOOBE()
            return
        }
        binding = MainScreenLaucnherBinding.inflate(layoutInflater)
        mainViewModel = ViewModelProvider(this)[MainViewModel::class.java]
        setContentView(binding.root)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        isLandscape = this.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        lifecycleScope.launch(Dispatchers.Default) {
            pagerAdapter = WinAdapter(this@Main)
            setMainViewModel()
            checkUpdate()
            withContext(Dispatchers.Main) {
                setupNavigationBar()
                setupViewPager()
                setupBackPressedDispatcher()
            }
            cancel("done")
        }
        applyWindowInsets(binding.coordinator)
        configureWallpaper()
        registerPackageReceiver(this, packageReceiver)
        otherTasks()
    }
    private fun runOOBE() {
        val intent = Intent(this, WelcomeActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        finishAffinity()
        startActivity(intent)
    }
    private fun checkUpdate() {
        if (PREFS!!.prefs.getBoolean(
                "updateInstalled",
                false
            ) && PREFS!!.versionCode == VERSION_CODE
        ) {
            PREFS!!.setUpdateState(3)
        }
    }

    private fun configureWallpaper() {
        if (PREFS!!.isWallpaperUsed) {
            window?.setBackgroundDrawable(
                ContextCompat.getDrawable(
                    this@Main,
                    R.drawable.start_transparent
                )
            )
            window?.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER)
        }
    }

    private fun disableAnims() {
        PREFS!!.apply {
            setAllAppsAnim(false)
            setAlphabetAnim(false)
            setTransitionAnim(false)
            setLiveTilesAnim(false)
            setTilesScreenAnim(false)
            setTilesAnim(false)
        }
    }

    private fun setupBackPressedDispatcher() {
        onBackPressedDispatcher.addCallback(
            this@Main,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (binding.pager.currentItem != 0) {
                        binding.pager.currentItem -= 1
                    } else {
                        if (searching && PREFS!!.isSearchBarEnabled) {
                            hideSearch()
                        }
                    }
                }
            })
    }

    private fun setupViewPager() {
        binding.pager.apply {
            adapter = pagerAdapter
        }
        binding.pager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            val black = if (!PREFS!!.isSearchBarEnabled && PREFS!!.navBarColor != 2)
                ContextCompat.getColor(this@Main, android.R.color.black) else null
            val white = if (!PREFS!!.isSearchBarEnabled && PREFS!!.navBarColor != 2)
                ContextCompat.getColor(this@Main, android.R.color.white) else null

            override fun onPageSelected(position: Int) {
                if (!PREFS!!.isSearchBarEnabled && PREFS!!.navBarColor != 2) {
                    if (position == 0) {
                        if (PREFS!!.navBarColor == 1 || PREFS!!.isLightThemeUsed) {
                            binding.navigationStartBtn.setColorFilter(launcherAccentColor(this@Main.theme))
                            binding.navigationSearchBtn.setColorFilter(black!!)
                        } else {
                            binding.navigationStartBtn.setColorFilter(launcherAccentColor(this@Main.theme))
                            binding.navigationSearchBtn.setColorFilter(white!!)
                        }
                    } else {
                        if (PREFS!!.navBarColor == 1 || PREFS!!.isLightThemeUsed) {
                            binding.navigationStartBtn.setColorFilter(black!!)
                            binding.navigationSearchBtn.setColorFilter(launcherAccentColor(this@Main.theme))
                        } else {
                            binding.navigationStartBtn.setColorFilter(white!!)
                            binding.navigationSearchBtn.setColorFilter(launcherAccentColor(this@Main.theme))
                        }
                    }
                }
                super.onPageSelected(position)
            }
        })
    }

    override fun onResume() {
        if (PREFS!!.isPrefsChanged) {
            PREFS!!.isPrefsChanged = false
            exitProcess(0)
        }
        super.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterPackageReceiver(this, packageReceiver)
    }

    private fun runApp(app: String) {
        isAppOpened = true
        when (app) {
            "ru.dimon6018.metrolauncher" -> {
                startActivity(Intent(this@Main, SettingsActivity::class.java))
            }

            else -> {
                startActivity(
                    Intent(this@Main.packageManager.getLaunchIntentForPackage(app)).addFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK
                    )
                )
            }
        }
    }

    fun configureViewPagerScroll(boolean: Boolean) {
        binding.pager.isUserInputEnabled = boolean
    }

    private suspend fun setMainViewModel() {
        val isCustomIconsInstalled = PREFS!!.iconPackPackage != "null"
        mainViewModel.setAppList(sortApps(setUpApps(this.packageManager, this)))
        var diskCache = initDiskCache(this)
        if (PREFS!!.iconPackChanged) {
            PREFS!!.iconPackChanged = false
            diskCache?.delete()
            diskCache?.close()
            diskCache = initDiskCache(this)
        }
        mainViewModel.getAppList().map {
            if (it.type != 1) {
                when (it.appPackage) {
                    this.packageName -> {
                        mainViewModel.addIconToCache(
                            this.packageName,
                            ContextCompat.getDrawable(this, R.drawable.ic_settings)
                                ?.toBitmap(defaultIconSize, defaultIconSize)
                        )
                    }

                    else -> {
                        withContext(Dispatchers.IO) {
                            val icon =
                                diskCache?.let { dc -> loadIconFromDiskCache(dc, it.appPackage!!) }
                            if (icon == null) {
                                generateIcon(it.appPackage!!, isCustomIconsInstalled)
                                saveIconToDiskCache(
                                    diskCache,
                                    it.appPackage!!,
                                    mainViewModel.getIconFromCache(it.appPackage!!)
                                )
                            } else {
                                mainViewModel.addIconToCache(it.appPackage!!, icon)
                            }
                        }
                    }
                }
            }
        }
        diskCache?.let { closeDiskCache(it) }
    }

    fun generateIcon(
        appPackage: String,
        isCustomIconsInstalled: Boolean
    ) {
        var icon = if (!isCustomIconsInstalled) this.packageManager.getApplicationIcon(appPackage)
        else iconPackManager.getIconPackWithName(PREFS!!.iconPackPackage)
            ?.getDrawableIconForPackage(appPackage, null)
        if (icon == null) {
            icon = this.packageManager.getApplicationIcon(appPackage)
        }
        mainViewModel.addIconToCache(appPackage, icon.toBitmap(defaultIconSize, defaultIconSize))
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        isLandscape = newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE
    }

    private fun otherTasks() {
        if (PREFS!!.prefs.getBoolean("tip1Enabled", true)) {
            WPDialog(this@Main).setTopDialog(false)
                .setTitle(getString(R.string.tip))
                .setMessage(getString(R.string.tip1))
                .setPositiveButton(getString(android.R.string.ok), null)
                .show()
            PREFS!!.prefs.edit().putBoolean("tip1Enabled", false).apply()
        }
        crashCheck()
    }
    private fun crashCheck() {
        if (PREFS!!.prefs.getBoolean("app_crashed", false)) {
            lifecycleScope.launch(Dispatchers.Default) {
                delay(5000)
                PREFS!!.prefs.edit().putBoolean("app_crashed", false).apply()
                PREFS!!.prefs.edit().putInt("crashCounter", 0).apply()
                if (PREFS!!.isFeedbackEnabled) {
                    var pos = (BSOD.getData(this@Main).getDao().getBsodList().size) - 1
                    if (pos < 0) {
                        pos = 0
                    }
                    val text = BSOD.getData(this@Main).getDao().getBSOD(pos).log
                    withContext(Dispatchers.Main) {
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
        if (bottomViewReady) {
            return
        }
        bottomViewReady = true
        val bottomView: FrameLayout = findViewById(R.id.navigation)
        when (PREFS!!.navBarColor) {
            0 -> {
                bottomView.setBackgroundColor(
                    ContextCompat.getColor(
                        this,
                        android.R.color.background_dark
                    )
                )
            }
            1 -> {
                bottomView.setBackgroundColor(
                    ContextCompat.getColor(
                        this,
                        android.R.color.background_light
                    )
                )
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
        configureBottomBar()
    }
    private fun configureBottomBar() {
        if (!PREFS!!.isSearchBarEnabled) {
            binding.navigationMain.visibility = View.VISIBLE
            binding.navigationStartBtn
            binding.navigationStartBtn.setImageDrawable(
                when (PREFS!!.navBarIconValue) {
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
                }
            )
            binding.navigationStartBtn.setOnClickListener {
                binding.pager.currentItem = 0
            }
            binding.navigationSearchBtn.setOnClickListener {
                if (PREFS!!.isAllAppsEnabled) {
                    binding.pager.currentItem = 1
                }
            }
        } else {
            binding.navigationSearchBar.visibility = View.VISIBLE
            searchAdapter = SearchAdapter(null)
            binding.searchBarRecyclerView.apply {
                layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
                adapter = searchAdapter
            }
            val editText = (binding.searchBar.editText as? AutoCompleteTextView)
            editText?.addTextChangedListener(object :
                TextWatcher {
                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    filterSearchText(s.toString(), mainViewModel.getAppList())
                }
                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
                override fun afterTextChanged(s: Editable) {}
            })
            editText?.setOnEditorActionListener(OnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_GO) {
                    if (filteredList != null && filteredList!!.isNotEmpty()) {
                        runApp(filteredList!!.first().appPackage!!)
                        editText.text.clear()
                        hideSearchResults()
                    }
                    return@OnEditorActionListener true
                }
                false
            })
        }
    }
    fun hideSearch() {
        hideSearchResults()
    }
    private fun hideSearchResults() {
        lifecycleScope.launch {
            searching = false
            binding.searchBarResults.apply {
                if (PREFS!!.isTransitionAnimEnabled) {
                    ObjectAnimator.ofFloat(this, "alpha", 1f, 0f).start()
                }
                visibility = View.GONE
            }
        }
    }
    private fun showSearchResults() {
        searching = true
        binding.searchBarResults.apply {
            if (PREFS!!.isTransitionAnimEnabled) {
                ObjectAnimator.ofFloat(this, "alpha", 0f, 1f).setDuration(100).start()
            }
            visibility = View.VISIBLE
        }
    }
    private fun filterSearchText(text: String, appList: List<App>) {
        var filteredList: MutableList<App> = ArrayList()
        val locale = Locale.getDefault()
        if (text.isEmpty()) {
            hideSearchResults()
        } else {
            showSearchResults()
        }
        val max = PREFS!!.maxResultsSearchBar
        for (element in appList) {
            if (element.appLabel!!.lowercase(locale).contains(text.lowercase(locale))) {
                if (filteredList.size >= max) {
                    break
                }
                filteredList.add(element)
            }
        }
        filteredList = sortApps(filteredList)
        searchAdapter?.setData(filteredList)
    }
    private fun setAppTheme() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return
        }
        if (PREFS!!.isLightThemeUsed) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }
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
    inner class SearchAdapter(private var dataList: MutableList<App>?): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
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
                holder.icon.setImageBitmap(mainViewModel.getIconFromCache(app.appPackage!!))
                holder.label.text = app.appLabel
            }
        }
        @SuppressLint("NotifyDataSetChanged")
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
    }
}