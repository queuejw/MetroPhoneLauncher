package ru.dimon6018.metrolauncher.content.settings.activities

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Build
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.WindowCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.color.DynamicColors
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textview.MaterialTextView
import ru.dimon6018.metrolauncher.Application
import ru.dimon6018.metrolauncher.Application.Companion.PREFS
import ru.dimon6018.metrolauncher.R
import ru.dimon6018.metrolauncher.content.data.Prefs
import ru.dimon6018.metrolauncher.helpers.ui.WPDialog
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.accentName
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.applyWindowInsets
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.launcherAccentColor
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.launcherAccentTheme

class ThemeSettingsActivity : AppCompatActivity() {
    private lateinit var themeMenu: MaterialCardView
    private lateinit var chooseThemeBtn: MaterialButton
    private lateinit var chooseAccentBtn: MaterialCardView
    private lateinit var accentNameTextView: MaterialTextView
    private lateinit var light: MaterialTextView
    private lateinit var dark: MaterialTextView
    private lateinit var accentTip: MaterialTextView

    private var tileImg: ImageView? = null

    private lateinit var wallpaperSwitch: MaterialSwitch
    private lateinit var wallpaperTransparentTilesSwitch: MaterialSwitch
    private lateinit var moreTilesSwitch: MaterialSwitch
    private lateinit var dynamicColorSwitch: MaterialSwitch
    private lateinit var lockDesktopSwitch: MaterialSwitch
    private lateinit var pinAppsToStartSwitch: MaterialSwitch

    private var main: CoordinatorLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(launcherAccentTheme())
        super.onCreate(savedInstanceState)
        setContentView(R.layout.launcher_settings_theme)
        init()
        setThemeText()
        configure()
        setImg()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        main = findViewById(R.id.coordinator)
        main?.apply { applyWindowInsets(this) }
        prepareTip()
    }

    private fun configure() {
        accentNameTextView.text = accentName(this)
        chooseThemeBtn.text = if (PREFS!!.isLightThemeUsed) getString(R.string.light) else getString(R.string.dark)
        chooseThemeBtn.setOnClickListener {
            chooseThemeBtn.visibility = View.GONE
            themeMenu.visibility = View.VISIBLE
        }
        light.setOnClickListener {
            PREFS!!.useLightTheme(true)
            PREFS!!.isPrefsChanged = true
            restoreThemeButtonsAndApplyChanges()
        }
        dark.setOnClickListener {
            PREFS!!.useLightTheme(false)
            PREFS!!.isPrefsChanged = true
            restoreThemeButtonsAndApplyChanges()
        }
        chooseAccentBtn.setOnClickListener { AccentDialog.display(supportFragmentManager) }
        moreTilesSwitch.isChecked = PREFS!!.isMoreTilesEnabled
        moreTilesSwitch.text = if(PREFS!!.isMoreTilesEnabled) getString(R.string.on) else getString(R.string.off)
        pinAppsToStartSwitch.isChecked = PREFS!!.pinNewApps
        pinAppsToStartSwitch.text = if(PREFS!!.pinNewApps) getString(R.string.on) else getString(R.string.off)
        wallpaperTransparentTilesSwitch = findViewById(R.id.wallpaperTransparentTilesSwtich)
        //wallpaper switches
        wallpaperSwitch.setOnCheckedChangeListener { _, check ->
            PREFS!!.setWallpaper(check)
            PREFS!!.isPrefsChanged = true
            refreshWallpaperSwitches()
        }
        wallpaperTransparentTilesSwitch.setOnCheckedChangeListener { _, check ->
            PREFS!!.setTransparentTiles(check)
            PREFS!!.isPrefsChanged = true
            refreshWallpaperSwitches()
        }
        //wallpaper switches end
        moreTilesSwitch.setOnCheckedChangeListener { _, isChecked ->
            PREFS!!.setMoreTilesPref(isChecked)
            moreTilesSwitch.text = if(isChecked) getString(R.string.on) else getString(R.string.off)
            PREFS!!.isPrefsChanged = true
            setImg()
        }
        pinAppsToStartSwitch.setOnCheckedChangeListener { _, isChecked ->
            PREFS!!.setPinNewApps(isChecked)
            pinAppsToStartSwitch.text = if(isChecked) getString(R.string.on) else getString(R.string.off)
            pinAppsToStartSwitch.setChecked(isChecked)
        }
        dynamicColorSwitch.setChecked(PREFS!!.accentColor == 20)
        dynamicColorSwitch.text = if(PREFS!!.accentColor == 20) getString(R.string.on) else getString(R.string.off)
        dynamicColorSwitch.setOnCheckedChangeListener { _, isChecked ->
            if(DynamicColors.isDynamicColorAvailable()) {
                if (isChecked) {
                    PREFS!!.accentColor = 20
                } else {
                    PREFS!!.accentColor = PREFS!!.prefs.getInt("previous_accent_color", 5)
                }
                dynamicColorSwitch.text =
                    if (isChecked) getString(R.string.on) else getString(R.string.off)
                recreate()
            } else {
                Snackbar.make(dynamicColorSwitch, getString(R.string.dynamicColor_error), Snackbar.LENGTH_LONG).show()
            }
        }
        lockDesktopSwitch.isChecked = PREFS!!.isStartBlocked
        lockDesktopSwitch.text = if(PREFS!!.isStartBlocked) getString(R.string.on) else getString(R.string.off)
        lockDesktopSwitch.setOnCheckedChangeListener { _, isChecked ->
            PREFS!!.blockStartScreen(isChecked)
            lockDesktopSwitch.text = if(isChecked) getString(R.string.on) else getString(R.string.off)
        }
    }

    private fun setThemeText() {
        val textFinal = getString(R.string.settings_theme_accent_title_part2) + " " + getString(R.string.settings_theme_accent_title_part1) + " " + getString(R.string.settings_theme_accent_title_part3)
        val spannable: Spannable = SpannableString(textFinal)
        spannable.setSpan(ForegroundColorSpan(launcherAccentColor(theme)), textFinal.indexOf(getString(R.string.settings_theme_accent_title_part1)),textFinal.indexOf(getString(R.string.settings_theme_accent_title_part1)) + getString(R.string.settings_theme_accent_title_part1).length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        accentTip.setText(spannable, TextView.BufferType.SPANNABLE)
    }

    private fun init() {
        chooseThemeBtn = findViewById(R.id.chooseTheme)
        accentTip = findViewById(R.id.accentTip)
        chooseAccentBtn = findViewById(R.id.chooseAccent)
        accentNameTextView = findViewById(R.id.choosedAccentName)
        themeMenu = findViewById(R.id.chooseThemeMenu)
        light = findViewById(R.id.chooseLight)
        dark = findViewById(R.id.chooseDark)
        tileImg = findViewById(R.id.moreTilesImage)
        accentTip = findViewById(R.id.accentTip)
        moreTilesSwitch = findViewById(R.id.moreTilesSwitch)
        wallpaperSwitch = findViewById(R.id.wallpaperShowSwtich)
        dynamicColorSwitch = findViewById(R.id.dynamicColorSwtich)
        pinAppsToStartSwitch = findViewById(R.id.newAppsToStartSwitch)
        lockDesktopSwitch = findViewById(R.id.blockStartSwitch)
    }
    private fun prepareTip() {
        if(PREFS!!.prefs.getBoolean("tipSettingsThemeEnabled", true)) {
            WPDialog(this).setTopDialog(true)
                .setTitle(getString(R.string.tip))
                .setMessage(getString(R.string.tipSettingsTheme))
                .setPositiveButton(getString(android.R.string.ok), null)
                .show()
            PREFS!!.prefs.edit().putBoolean("tipSettingsThemeEnabled", false).apply()
        }
    }
    private fun enterAnimation(exit: Boolean) {
        if(main == null || !PREFS!!.isTransitionAnimEnabled) {
            return
        }
        val animatorSet = AnimatorSet()
        if(exit) {
            animatorSet.playTogether(
                ObjectAnimator.ofFloat(main!!, "translationX", 0f, 300f),
                ObjectAnimator.ofFloat(main!!, "rotationY", 0f, 90f),
                ObjectAnimator.ofFloat(main!!, "alpha", 1f, 0f),
                ObjectAnimator.ofFloat(main!!, "scaleX", 1f, 0.5f),
                ObjectAnimator.ofFloat(main!!, "scaleY", 1f, 0.5f),
            )
        } else {
            animatorSet.playTogether(
                ObjectAnimator.ofFloat(main!!, "translationX", 300f, 0f),
                ObjectAnimator.ofFloat(main!!, "rotationY", 90f, 0f),
                ObjectAnimator.ofFloat(main!!, "alpha", 0f, 1f),
                ObjectAnimator.ofFloat(main!!, "scaleX", 0.5f, 1f),
                ObjectAnimator.ofFloat(main!!, "scaleY", 0.5f, 1f)
            )
        }
        animatorSet.setDuration(400)
        animatorSet.start()
    }
    override fun onPause() {
        enterAnimation(true)
        super.onPause()
    }
    override fun onResume() {
        super.onResume()
        enterAnimation(false)
        refreshWallpaperSwitches()
    }
    private fun refreshWallpaperSwitches() {
        wallpaperSwitch.isChecked = PREFS!!.isWallpaperUsed
        wallpaperSwitch.text = if(PREFS!!.isWallpaperUsed) getString(R.string.on) else getString(R.string.off)
        wallpaperTransparentTilesSwitch.isChecked = PREFS!!.isTilesTransparent
        wallpaperTransparentTilesSwitch.text = if(PREFS!!.isTilesTransparent) getString(R.string.on) else getString(R.string.off)
    }
    private fun setImg() {
        tileImg?.setImageResource(if(PREFS!!.isMoreTilesEnabled) R.mipmap.tiles_small else R.mipmap.tiles_default)
    }
    private fun restoreThemeButtonsAndApplyChanges() {
        chooseThemeBtn.visibility = View.VISIBLE
        themeMenu.visibility = View.GONE
        setAppTheme()
    }
    private fun setAppTheme() {
        if (Prefs(this).isLightThemeUsed) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                (application as Application).setNightMode()
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                (application as Application).setNightMode()
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)

            }
        }
    }
    class AccentDialog : DialogFragment() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setStyle(STYLE_NORMAL, R.style.AppTheme_FullScreenDialog)
        }

        override fun onStart() {
            super.onStart()
            val dialog = dialog
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.MATCH_PARENT
            dialog?.setTitle("ACCENT")
            dialog?.window!!.setLayout(width, height)
        }

        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
            super.onCreateView(inflater, container, savedInstanceState)
            return inflater.inflate(R.layout.accent_dialog, container, false)
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            val prefs = Prefs(requireContext())
            val lime = view.findViewById<ImageView>(R.id.choose_color_lime)
            val back = view.findViewById<FrameLayout>(R.id.back_accent_menu)
            back.setOnClickListener { dismiss() }
            lime.setOnClickListener {
                prefs.accentColor = 0
                dismiss()
                PREFS!!.isPrefsChanged = true
                requireActivity().recreate()
            }
            val green = view.findViewById<ImageView>(R.id.choose_color_green)
            green.setOnClickListener {
                prefs.accentColor = 1
                dismiss()
                PREFS!!.isPrefsChanged = true
                requireActivity().recreate()
            }
            val emerald = view.findViewById<ImageView>(R.id.choose_color_emerald)
            emerald.setOnClickListener {
                prefs.accentColor = 2
                dismiss()
                PREFS!!.isPrefsChanged = true
                requireActivity().recreate()
            }
            val cyan = view.findViewById<ImageView>(R.id.choose_color_cyan)
            cyan.setOnClickListener {
                prefs.accentColor = 3
                dismiss()
                PREFS!!.isPrefsChanged = true
                requireActivity().recreate()
            }
            val teal = view.findViewById<ImageView>(R.id.choose_color_teal)
            teal.setOnClickListener {
                prefs.accentColor = 4
                dismiss()
                PREFS!!.isPrefsChanged = true
                requireActivity().recreate()
            }
            val cobalt = view.findViewById<ImageView>(R.id.choose_color_cobalt)
            cobalt.setOnClickListener {
                prefs.accentColor = 5
                dismiss()
                PREFS!!.isPrefsChanged = true
                requireActivity().recreate()
            }
            val indigo = view.findViewById<ImageView>(R.id.choose_color_indigo)
            indigo.setOnClickListener {
                prefs.accentColor = 6
                dismiss()
                PREFS!!.isPrefsChanged = true
                requireActivity().recreate()
            }
            val violet = view.findViewById<ImageView>(R.id.choose_color_violet)
            violet.setOnClickListener {
                prefs.accentColor = 7
                dismiss()
                PREFS!!.isPrefsChanged = true
                requireActivity().recreate()
            }
            val pink = view.findViewById<ImageView>(R.id.choose_color_pink)
            pink.setOnClickListener {
                prefs.accentColor = 8
                dismiss()
                PREFS!!.isPrefsChanged = true
                requireActivity().recreate()
            }
            val magenta = view.findViewById<ImageView>(R.id.choose_color_magenta)
            magenta.setOnClickListener {
                prefs.accentColor = 9
                dismiss()
                PREFS!!.isPrefsChanged = true
                requireActivity().recreate()
            }
            val crimson = view.findViewById<ImageView>(R.id.choose_color_crimson)
            crimson.setOnClickListener {
                prefs.accentColor = 10
                dismiss()
                PREFS!!.isPrefsChanged = true
                requireActivity().recreate()
            }
            val red = view.findViewById<ImageView>(R.id.choose_color_red)
            red.setOnClickListener {
                prefs.accentColor = 11
                dismiss()
                PREFS!!.isPrefsChanged = true
                requireActivity().recreate()
            }
            val orange = view.findViewById<ImageView>(R.id.choose_color_orange)
            orange.setOnClickListener {
                prefs.accentColor = 12
                dismiss()
                PREFS!!.isPrefsChanged = true
                requireActivity().recreate()
            }
            val amber = view.findViewById<ImageView>(R.id.choose_color_amber)
            amber.setOnClickListener {
                prefs.accentColor = 13
                dismiss()
                PREFS!!.isPrefsChanged = true
                requireActivity().recreate()
            }
            val yellow = view.findViewById<ImageView>(R.id.choose_color_yellow)
            yellow.setOnClickListener {
                prefs.accentColor = 14
                dismiss()
                PREFS!!.isPrefsChanged = true
                requireActivity().recreate()
            }
            val brown = view.findViewById<ImageView>(R.id.choose_color_brown)
            brown.setOnClickListener {
                prefs.accentColor = 15
                dismiss()
                PREFS!!.isPrefsChanged = true
                requireActivity().recreate()
            }
            val olive = view.findViewById<ImageView>(R.id.choose_color_olive)
            olive.setOnClickListener {
                prefs.accentColor = 16
                dismiss()
                PREFS!!.isPrefsChanged = true
                requireActivity().recreate()
            }
            val steel = view.findViewById<ImageView>(R.id.choose_color_steel)
            steel.setOnClickListener {
                prefs.accentColor = 17
                dismiss()
                PREFS!!.isPrefsChanged = true
                requireActivity().recreate()
            }
            val mauve = view.findViewById<ImageView>(R.id.choose_color_mauve)
            mauve.setOnClickListener {
                prefs.accentColor = 18
                dismiss()
                PREFS!!.isPrefsChanged = true
                requireActivity().recreate()
            }
            val taupe = view.findViewById<ImageView>(R.id.choose_color_taupe)
            taupe.setOnClickListener {
                prefs.accentColor = 19
                dismiss()
                PREFS!!.isPrefsChanged = true
                requireActivity().recreate()
            }
        }
        companion object {
            private const val TAG = "accentD"
            fun display(fragmentManager: FragmentManager?): AccentDialog {
                val accentDialog = AccentDialog()
                accentDialog.show(fragmentManager!!, TAG)
                return accentDialog
            }
        }
    }
}
