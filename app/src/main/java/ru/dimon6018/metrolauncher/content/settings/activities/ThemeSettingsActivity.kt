package ru.dimon6018.metrolauncher.content.settings.activities

import android.content.Context
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
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.accentName
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.applyWindowInsets
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.launcherAccentColor
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.launcherAccentTheme

class ThemeSettingsActivity : AppCompatActivity() {
    private var themeMenu: MaterialCardView? = null
    private var chooseThemeBtn: MaterialButton? = null
    private var chooseAccentBtn: MaterialCardView? = null
    private var accentNameTextView: MaterialTextView? = null
    private var light: MaterialTextView? = null
    private var dark: MaterialTextView? = null
    private var accentTip: MaterialTextView? = null
    private var context: Context? = null
    private var tileImg: ImageView? = null

    private var wallpaperSwitch: MaterialSwitch? = null
    private var wallpaperTransparentTilesSwitch: MaterialSwitch? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(launcherAccentTheme())
        super.onCreate(savedInstanceState)
        setContentView(R.layout.launcher_settings_theme)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val cord: CoordinatorLayout = findViewById(R.id.coordinator)
        context = this
        chooseThemeBtn = findViewById(R.id.chooseTheme)
        accentTip = findViewById(R.id.accentTip)
        chooseAccentBtn = findViewById(R.id.chooseAccent)
        accentNameTextView = findViewById(R.id.choosedAccentName)
        themeMenu = findViewById(R.id.chooseThemeMenu)
        light = findViewById(R.id.chooseLight)
        dark = findViewById(R.id.chooseDark)
        tileImg = findViewById(R.id.moreTilesImage)
        accentTip = findViewById(R.id.accentTip)
        val textFinal = getString(R.string.settings_theme_accent_title_part2) + " " + getString(R.string.settings_theme_accent_title_part1) + " " + getString(R.string.settings_theme_accent_title_part3)
        val spannable: Spannable = SpannableString(textFinal)
        spannable.setSpan(ForegroundColorSpan(launcherAccentColor(theme)), textFinal.indexOf(getString(R.string.settings_theme_accent_title_part1)),textFinal.indexOf(getString(R.string.settings_theme_accent_title_part1)) + getString(R.string.settings_theme_accent_title_part1).length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        accentTip?.setText(spannable, TextView.BufferType.SPANNABLE)
        val moreTilesSwitch: MaterialSwitch = findViewById(R.id.moreTilesSwitch)
        wallpaperSwitch = findViewById(R.id.wallpaperShowSwtich)
        val pinAppsToStartSwitch: MaterialSwitch = findViewById(R.id.newAppsToStartSwitch)
        accentNameTextView!!.text = accentName(this)
        val themeButtonLabel: String = if (PREFS!!.isLightThemeUsed) {
            getString(R.string.light)
        } else {
            getString(R.string.dark)
        }
        chooseThemeBtn!!.text = themeButtonLabel
        chooseThemeBtn!!.setOnClickListener {
            chooseThemeBtn!!.visibility = View.GONE
            themeMenu!!.visibility = View.VISIBLE
        }
        light!!.setOnClickListener {
            PREFS!!.useLightTheme(true)
            PREFS!!.setPrefsChanged(true)
            restoreThemeButtonsAndApplyChanges()
        }
        dark!!.setOnClickListener {
            PREFS!!.useLightTheme(false)
            PREFS!!.setPrefsChanged(true)
            restoreThemeButtonsAndApplyChanges()
        }
        chooseAccentBtn!!.setOnClickListener { AccentDialog.display(supportFragmentManager) }
        moreTilesSwitch.isChecked = PREFS!!.isMoreTilesEnabled
        moreTilesSwitch.text = if(PREFS!!.isMoreTilesEnabled) getString(R.string.on) else getString(R.string.off)
        pinAppsToStartSwitch.isChecked = PREFS!!.pinNewApps
        pinAppsToStartSwitch.text = if(PREFS!!.pinNewApps) getString(R.string.on) else getString(R.string.off)
        wallpaperTransparentTilesSwitch = findViewById(R.id.wallpaperTransparentTilesSwtich)
        //wallpaper switches
        wallpaperSwitch?.setOnCheckedChangeListener { _, check ->
            PREFS!!.setWallpaper(check)
            PREFS!!.setPrefsChanged(true)
            refreshWallpaperSwitches()
        }
        wallpaperTransparentTilesSwitch?.setOnCheckedChangeListener { _, check ->
            PREFS!!.setTransparentTiles(check)
            PREFS!!.setPrefsChanged(true)
            refreshWallpaperSwitches()
        }
        //wallpaper switches end
        moreTilesSwitch.setOnCheckedChangeListener { _, isChecked ->
            PREFS!!.setMoreTilesPref(isChecked)
            moreTilesSwitch.text = if(isChecked) getString(R.string.on) else getString(R.string.off)
            moreTilesSwitch.setChecked(isChecked)
            PREFS!!.setPrefsChanged(true)
            setImg()
        }
        pinAppsToStartSwitch.setOnCheckedChangeListener { _, isChecked ->
            PREFS!!.setPinNewApps(isChecked)
            pinAppsToStartSwitch.text = if(isChecked) getString(R.string.on) else getString(R.string.off)
            pinAppsToStartSwitch.setChecked(isChecked)
        }
        val dynamicColorSwitch: MaterialSwitch = findViewById(R.id.dynamicColorSwtich)
        dynamicColorSwitch.setChecked(PREFS!!.accentColor == 20)
        dynamicColorSwitch.text = if(PREFS!!.accentColor == 20) getString(R.string.on) else getString(R.string.off)
        dynamicColorSwitch.setOnCheckedChangeListener { _, isChecked ->
            if(DynamicColors.isDynamicColorAvailable()) {
                if (isChecked) {
                    PREFS!!.accentColor = 20
                } else {
                    PREFS!!.accentColor = PREFS!!.pref.getInt("previous_accent_color", 5)
                }
                dynamicColorSwitch.text =
                    if (isChecked) getString(R.string.on) else getString(R.string.off)
                dynamicColorSwitch.setChecked(isChecked)
                recreate()
            } else {
                Snackbar.make(dynamicColorSwitch, getString(R.string.dynamicColor_error), Snackbar.LENGTH_LONG).show()
            }
        }
        val lockDesktopSwitch: MaterialSwitch = findViewById(R.id.blockStartSwitch)
        lockDesktopSwitch.isChecked = PREFS!!.isStartBlocked
        lockDesktopSwitch.text = if(PREFS!!.isStartBlocked) getString(R.string.on) else getString(R.string.off)
        lockDesktopSwitch.setOnCheckedChangeListener { _, isChecked ->
            PREFS!!.blockStartScreen(isChecked)
            lockDesktopSwitch.text = if(isChecked) getString(R.string.on) else getString(R.string.off)
        }
        setImg()
       applyWindowInsets(cord)
    }

    override fun onResume() {
        super.onResume()
        refreshWallpaperSwitches()
    }
    private fun refreshWallpaperSwitches() {
        wallpaperSwitch?.isChecked = PREFS!!.isWallpaperUsed
        wallpaperSwitch?.text = if(PREFS!!.isWallpaperUsed) getString(R.string.on) else getString(R.string.off)
        wallpaperTransparentTilesSwitch?.isChecked = PREFS!!.isTilesTransparent
        wallpaperTransparentTilesSwitch?.text = if(PREFS!!.isTilesTransparent) getString(R.string.on) else getString(R.string.off)
    }
    private fun setImg() {
        tileImg?.setImageResource(if(PREFS!!.isMoreTilesEnabled) R.mipmap.tiles_small else R.mipmap.tiles_default)
    }
    private fun restoreThemeButtonsAndApplyChanges() {
        chooseThemeBtn!!.visibility = View.VISIBLE
        themeMenu!!.visibility = View.GONE
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
            PREFS!!.setPrefsChanged(true)
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
                requireActivity().recreate()
            }
            val green = view.findViewById<ImageView>(R.id.choose_color_green)
            green.setOnClickListener {
                prefs.accentColor = 1
                dismiss()
                requireActivity().recreate()
            }
            val emerald = view.findViewById<ImageView>(R.id.choose_color_emerald)
            emerald.setOnClickListener {
                prefs.accentColor = 2
                dismiss()
                requireActivity().recreate()
            }
            val cyan = view.findViewById<ImageView>(R.id.choose_color_cyan)
            cyan.setOnClickListener {
                prefs.accentColor = 3
                dismiss()
                requireActivity().recreate()
            }
            val teal = view.findViewById<ImageView>(R.id.choose_color_teal)
            teal.setOnClickListener {
                prefs.accentColor = 4
                dismiss()
                requireActivity().recreate()
            }
            val cobalt = view.findViewById<ImageView>(R.id.choose_color_cobalt)
            cobalt.setOnClickListener {
                prefs.accentColor = 5
                dismiss()
                requireActivity().recreate()
            }
            val indigo = view.findViewById<ImageView>(R.id.choose_color_indigo)
            indigo.setOnClickListener {
                prefs.accentColor = 6
                dismiss()
                requireActivity().recreate()
            }
            val violet = view.findViewById<ImageView>(R.id.choose_color_violet)
            violet.setOnClickListener {
                prefs.accentColor = 7
                dismiss()
                requireActivity().recreate()
            }
            val pink = view.findViewById<ImageView>(R.id.choose_color_pink)
            pink.setOnClickListener {
                prefs.accentColor = 8
                dismiss()
                requireActivity().recreate()
            }
            val magenta = view.findViewById<ImageView>(R.id.choose_color_magenta)
            magenta.setOnClickListener {
                prefs.accentColor = 9
                dismiss()
                requireActivity().recreate()
            }
            val crimson = view.findViewById<ImageView>(R.id.choose_color_crimson)
            crimson.setOnClickListener {
                prefs.accentColor = 10
                dismiss()
                requireActivity().recreate()
            }
            val red = view.findViewById<ImageView>(R.id.choose_color_red)
            red.setOnClickListener {
                prefs.accentColor = 11
                dismiss()
                requireActivity().recreate()
            }
            val orange = view.findViewById<ImageView>(R.id.choose_color_orange)
            orange.setOnClickListener {
                prefs.accentColor = 12
                dismiss()
                requireActivity().recreate()
            }
            val amber = view.findViewById<ImageView>(R.id.choose_color_amber)
            amber.setOnClickListener {
                prefs.accentColor = 13
                dismiss()
                requireActivity().recreate()
            }
            val yellow = view.findViewById<ImageView>(R.id.choose_color_yellow)
            yellow.setOnClickListener {
                prefs.accentColor = 14
                dismiss()
                requireActivity().recreate()
            }
            val brown = view.findViewById<ImageView>(R.id.choose_color_brown)
            brown.setOnClickListener {
                prefs.accentColor = 15
                dismiss()
                requireActivity().recreate()
            }
            val olive = view.findViewById<ImageView>(R.id.choose_color_olive)
            olive.setOnClickListener {
                prefs.accentColor = 16
                dismiss()
                requireActivity().recreate()
            }
            val steel = view.findViewById<ImageView>(R.id.choose_color_steel)
            steel.setOnClickListener {
                prefs.accentColor = 17
                dismiss()
                requireActivity().recreate()
            }
            val mauve = view.findViewById<ImageView>(R.id.choose_color_mauve)
            mauve.setOnClickListener {
                prefs.accentColor = 18
                dismiss()
                requireActivity().recreate()
            }
            val taupe = view.findViewById<ImageView>(R.id.choose_color_taupe)
            taupe.setOnClickListener {
                prefs.accentColor = 19
                dismiss()
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
