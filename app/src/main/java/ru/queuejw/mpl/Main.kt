package ru.queuejw.mpl

import android.content.Intent
import android.content.res.Configuration
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.queuejw.mpl.Application.Companion.PREFS
import ru.queuejw.mpl.content.AllApps
import ru.queuejw.mpl.content.Start
import ru.queuejw.mpl.content.data.app.App
import ru.queuejw.mpl.content.data.bsod.BSOD
import ru.queuejw.mpl.content.oobe.OOBEActivity
import ru.queuejw.mpl.databinding.LauncherMainScreenBinding
import ru.queuejw.mpl.helpers.disklru.CacheUtils
import ru.queuejw.mpl.helpers.disklru.DiskLruCache
import ru.queuejw.mpl.helpers.iconpack.IconPackManager
import ru.queuejw.mpl.helpers.receivers.PackageChangesReceiver
import ru.queuejw.mpl.helpers.ui.WPDialog
import ru.queuejw.mpl.helpers.utils.Utils

/**
 * Main application screen (tiles, apps)
 * @see ru.queuejw.mpl.Application
 * @see Start
 * @see AllApps
 */
class Main : AppCompatActivity() {

    // 8.0
    private lateinit var pagerAdapter: FragmentStateAdapter
    private lateinit var mainViewModel: MainViewModel

    private val iconPackManager: IconPackManager by lazy { IconPackManager(this) }
    private val packageReceiver: PackageChangesReceiver by lazy { PackageChangesReceiver() }
    private val defaultIconSize: Int by lazy { resources.getDimensionPixelSize(R.dimen.tile_small) }

    private val accentColor: Int by lazy { Utils.launcherAccentColor(this@Main.theme) }
    private val onSurfaceColor: Int by lazy { Utils.launcherOnSurfaceColor(this@Main.theme) }

    private lateinit var binding: LauncherMainScreenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        isDarkMode = resources.getBoolean(R.bool.isDark) && PREFS.appTheme != 2
        when (PREFS.appTheme) {
            0 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            1 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            2 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
        handleDevMode()
        super.onCreate(savedInstanceState)

        // If MPL has never run before, open OOBE
        if (PREFS.launcherState == 0) {
            runOOBE()
            return
        }

        binding = LauncherMainScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupUI()
        lifecycleScope.launch(Dispatchers.Default) {
            otherTasks()
            initializeData()
            withContext(Dispatchers.Main) {
                setupNavigationBar()
                setupViewPager()
                setupBackPressedDispatcher()
            }
        }
    }

    /**
     * Turn off animations if developer mode is enabled to prevent some animation issues
     * @see onCreate
     * @see disableAnims
     */
    private fun handleDevMode() {
        if (Utils.isDevMode(this) && PREFS.isAutoShutdownAnimEnabled) {
            disableAnims()
        }
    }

    private fun runOOBE() {
        val intent = Intent(this, OOBEActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        finishAffinity()
        startActivity(intent)
    }

    /**
     * Called when the application is started.
     * Updates the icon cache, application list and checks the application version
     * @see onCreate
     */
    private suspend fun initializeData() {
        pagerAdapter = WinAdapter(this@Main)
        setMainViewModel()
        checkUpdate()
    }

    /**
     * Configures the user interface according to the settings
     */
    private fun setupUI() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        ViewCompat.setOnApplyWindowInsetsListener(binding.mainBottomBar.navigationFrame) { view, insets ->
            val systemBarInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updatePadding(
                bottom = systemBarInsets.bottom,
                left = systemBarInsets.left,
                right = systemBarInsets.right,
            )
            insets
        }
        isLandscape = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        Utils.registerPackageReceiver(this, packageReceiver)
    }

    private fun checkUpdate() {
        if (PREFS.prefs.getBoolean(
                "updateInstalled",
                false
            ) && PREFS.versionCode == Utils.VERSION_CODE
        ) PREFS.updateState = 3
    }

    private fun disableAnims() {
        PREFS.apply {
            isAAllAppsAnimEnabled = false
            isTransitionAnimEnabled = false
            isLiveTilesAnimEnabled = false
            isTilesAnimEnabled = false
        }
    }

    /**
     * Creates OnBackPressedCallback, which is needed to move to the previous ViewPager screen by pressing/gesturing backwards.
     */
    private fun setupBackPressedDispatcher() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.mainPager.currentItem != 0) {
                    binding.mainPager.currentItem -= 1
                }
            }
        })
    }

    private fun setupViewPager() {
        binding.mainPager.apply {
            adapter = pagerAdapter
            registerOnPageChangeCallback(createPageChangeCallback())
        }
    }

    /**
     * Creates OnPageChangeCallback for some required actions
     */
    private fun createPageChangeCallback() = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)
            updateNavigationBarColors(position)
        }

        private fun updateNavigationBarColors(position: Int) {
            if (PREFS.navBarColor != 2) {
                val (startColor, searchColor) = when {
                    position == 0 -> accentColor to onSurfaceColor
                    else -> onSurfaceColor to accentColor
                }
                binding.mainBottomBar.navigationStartBtn.setColorFilter(startColor)
                binding.mainBottomBar.navigationSearchBtn.setColorFilter(searchColor)
            }
        }
    }

    override fun onResume() {
        // restart MPL if some settings have been changed
        if (PREFS.isPrefsChanged) restart()

        super.onResume()
    }

    private fun restart() {
        PREFS.isPrefsChanged = false
        val componentName = Intent(this, this::class.java).component
        val intent = Intent.makeRestartActivityTask(componentName)
        startActivity(intent)
        Runtime.getRuntime().exit(0)
    }

    override fun onDestroy() {
        super.onDestroy()
        Utils.unregisterPackageReceiver(this, packageReceiver)
    }

    fun configureViewPagerScroll(enabled: Boolean) {
        binding.mainPager.isUserInputEnabled = enabled
    }

    private suspend fun setMainViewModel() {
        mainViewModel = ViewModelProvider(this)[MainViewModel::class.java]
        val list = Utils.setUpApps(this)
        mainViewModel.setAppList(list)
        regenerateIcons(list)
    }


    private suspend fun regenerateIcons(appList: MutableList<App>) {
        val isCustomIconsInstalled = PREFS.iconPackPackage != "null"
        var diskCache = CacheUtils.initDiskCache(this)
        if (isCustomIconsInstalled) {
            checkIconPack(diskCache)
        }
        if (PREFS.iconPackChanged) {
            PREFS.iconPackChanged = false
            diskCache?.apply {
                delete()
                close()
            }
            diskCache = null
        }
        withContext(Dispatchers.IO) {
            if (diskCache == null) diskCache = CacheUtils.initDiskCache(this@Main)
            appList.forEach { app ->
                if (app.type != 1) {
                    val icon = CacheUtils.loadIconFromDiskCache(diskCache!!, app.appPackage!!)
                    if (icon == null) {
                        generateIcon(app.appPackage!!, isCustomIconsInstalled)
                        CacheUtils.saveIconToDiskCache(
                            diskCache,
                            app.appPackage!!,
                            mainViewModel.getIconFromCache(app.appPackage!!)
                        )
                    } else {
                        mainViewModel.addIconToCache(app.appPackage!!, icon)
                    }
                }
            }
            CacheUtils.closeDiskCache(diskCache!!)
        }
    }

    private fun checkIconPack(disk: DiskLruCache?): Boolean {
        return runCatching {
            packageManager.getApplicationInfo(PREFS.iconPackPackage!!, 0)
            true
        }.getOrElse {
            PREFS.iconPackPackage = "null"
            disk?.apply {
                delete()
                close()
            }
            false
        }
    }

    // Icon generation for cache
    fun generateIcon(appPackage: String, isCustomIconsInstalled: Boolean) {
        val icon = if (!isCustomIconsInstalled) {
            packageManager.getApplicationIcon(appPackage)
        } else {
            iconPackManager.getIconPackWithName(PREFS.iconPackPackage)
                ?.getDrawableIconForPackage(appPackage, null)
                ?: packageManager.getApplicationIcon(appPackage)
        }
        mainViewModel.addIconToCache(appPackage, icon.toBitmap(defaultIconSize, defaultIconSize))
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        isLandscape = newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE
    }

    private suspend fun otherTasks() {
        withContext(Dispatchers.Main) {
            if (PREFS.prefs.getBoolean("tip1Enabled", true)) {
                showTipDialog()
                PREFS.prefs.edit().putBoolean("tip1Enabled", false).apply()
            }
        }
        crashCheck()
    }

    private fun showTipDialog() {
        WPDialog(this@Main).apply {
            setTopDialog(false)
            setTitle(getString(R.string.tip))
            setMessage(getString(R.string.tip1))
            setPositiveButton(getString(android.R.string.ok), null)
            show()
        }
    }

    // If 5 seconds after the crash is successful, display an error message
    private suspend fun crashCheck() {
        if (PREFS.prefs.getBoolean("app_crashed", false)) {
            delay(5000)
            PREFS.prefs.edit().apply {
                putBoolean("app_crashed", false)
                putInt("crashCounter", 0)
                apply()
            }
            if (PREFS.isFeedbackEnabled) {
                handleCrashFeedback()
            }
        }
    }

    private suspend fun handleCrashFeedback() {
        val dao = BSOD.getData(this@Main).getDao()
        var pos = (dao.getBsodList().size) - 1
        if (pos < 0) pos = 0
        val text = dao.getBSOD(pos).log
        withContext(Dispatchers.Main) {
            WPDialog(this@Main).apply {
                setTopDialog(true)
                setTitle(getString(R.string.bsodDialogTitle))
                setMessage(getString(R.string.bsodDialogMessage))
                setNegativeButton(getString(R.string.bsodDialogDismiss), null)
                setPositiveButton(getString(R.string.bsodDialogSend)) {
                    Utils.sendCrash(text, this@Main)
                }
                show()
            }
        }
    }

    private fun setupNavigationBar() {
        binding.mainBottomBar.navigationFrame.setBackgroundColor(getNavBarColor())
        configureBottomBar()
    }

    private fun getNavBarColor(): Int {
        return when (PREFS.navBarColor) {
            0 -> ContextCompat.getColor(this, android.R.color.background_dark)
            1 -> ContextCompat.getColor(this, android.R.color.background_light)
            2 -> Utils.accentColorFromPrefs(this)
            3 -> {
                binding.mainBottomBar.navigationFrame.visibility = View.GONE
                return ContextCompat.getColor(this, android.R.color.transparent)
            }

            else -> Utils.launcherSurfaceColor(theme)
        }
    }

    private fun configureBottomBar() {
        setupNavigationBarButtons()
    }

    private fun setupNavigationBarButtons() {
        binding.mainBottomBar.navigationMain.visibility = View.VISIBLE
        binding.mainBottomBar.navigationStartBtn.apply {
            setImageDrawable(getNavBarIconDrawable())
            setOnClickListener { binding.mainPager.setCurrentItem(0, true) }
        }
        binding.mainBottomBar.navigationSearchBtn.setOnClickListener {
            if (PREFS.isAllAppsEnabled) binding.mainPager.setCurrentItem(1, true)
        }
    }

    private fun getNavBarIconDrawable(): Drawable? {
        return ContextCompat.getDrawable(
            this, when (PREFS.navBarIconValue) {
                0 -> R.drawable.ic_os_windows_8
                1 -> R.drawable.ic_os_windows
                2 -> R.drawable.ic_os_android
                else -> R.drawable.ic_os_windows_8
            }
        )
    }

    companion object {
        var isLandscape: Boolean = false
        var isDarkMode: Boolean = false
    }

    class WinAdapter(fragment: FragmentActivity) : FragmentStateAdapter(fragment) {

        override fun getItemCount(): Int = if (PREFS.isAllAppsEnabled) 2 else 1

        override fun createFragment(position: Int): Fragment {
            return when {
                !PREFS.isAllAppsEnabled -> Start()
                position == 1 -> AllApps()
                else -> Start()
            }
        }
    }
}
