package ru.dimon6018.metrolauncher.content.settings

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
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.textview.MaterialTextView
import ru.dimon6018.metrolauncher.Application
import ru.dimon6018.metrolauncher.Application.Companion.PREFS
import ru.dimon6018.metrolauncher.Main
import ru.dimon6018.metrolauncher.R
import ru.dimon6018.metrolauncher.content.data.Prefs
import kotlin.system.exitProcess

class ThemeSettingsActivity : AppCompatActivity() {
    private var themeMenu: MaterialCardView? = null
    private var chooseThemeBtn: MaterialButton? = null
    private var chooseAccentBtn: MaterialCardView? = null
    private var accentNameTextView: MaterialTextView? = null
    private var light: TextView? = null
    private var dark: TextView? = null
    private var accentTip: TextView? = null
    private var context: Context? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(Application.launcherAccentTheme())
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
        val accentTip: TextView = findViewById(R.id.accentTip)
        val textFinal = getString(R.string.settings_theme_accent_title_part2) + " " + getString(R.string.settings_theme_accent_title_part1) + " " + getString(R.string.settings_theme_accent_title_part3)
        val spannable: Spannable = SpannableString(textFinal)
        spannable.setSpan(ForegroundColorSpan(Application.launcherAccentColor(theme)), textFinal.indexOf(getString(R.string.settings_theme_accent_title_part1)),textFinal.indexOf(getString(R.string.settings_theme_accent_title_part1)) + getString(R.string.settings_theme_accent_title_part1).length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        accentTip.setText(spannable, TextView.BufferType.SPANNABLE)
        val moreTilesSwitch: MaterialSwitch = findViewById(R.id.moreTilesSwitch)
        val fastIconsSwitch: MaterialSwitch = findViewById(R.id.fastIcons)
        val wallpaperSwitch: MaterialSwitch = findViewById(R.id.wallpaperShowSwtich)
        accentNameTextView!!.text = Application.accentName(this)
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
            restoreThemeButtonsAndApplyChanges()
        }
        dark!!.setOnClickListener {
            PREFS!!.useLightTheme(false)
            restoreThemeButtonsAndApplyChanges()
        }
        chooseAccentBtn!!.setOnClickListener { AccentDialog.display(supportFragmentManager) }
        fastIconsSwitch.setChecked(PREFS!!.isFastIconsEnabled)
        fastIconsSwitch.text = if(PREFS!!.isFastIconsEnabled) getString(R.string.on) else getString(R.string.off)
        fastIconsSwitch.setOnCheckedChangeListener { _, isChecked ->
            PREFS!!.setFastIcons(isChecked)
            fastIconsSwitch.text = if(isChecked) getString(R.string.on) else getString(R.string.off)
            fastIconsSwitch.setChecked(isChecked)
        }
        moreTilesSwitch.setChecked(PREFS!!.isMoreTilesEnabled)
        moreTilesSwitch.text = if(PREFS!!.isMoreTilesEnabled) getString(R.string.on) else getString(R.string.off)
        wallpaperSwitch.setChecked(PREFS!!.isWallpaperUsed)
        wallpaperSwitch.text = if(PREFS!!.isWallpaperUsed) getString(R.string.on) else getString(R.string.off)
        moreTilesSwitch.setOnCheckedChangeListener { _, isChecked ->
            PREFS!!.setMoreTilesPref(isChecked)
            moreTilesSwitch.text = if(isChecked) getString(R.string.on) else getString(R.string.off)
            moreTilesSwitch.setChecked(isChecked)
            Prefs.isPrefsChanged = true
        }
        wallpaperSwitch.setOnCheckedChangeListener { _, isChecked ->
            PREFS!!.setWallpaper(isChecked)
            wallpaperSwitch.text = if(isChecked) getString(R.string.on) else getString(R.string.off)
            wallpaperSwitch.setChecked(isChecked)
            Prefs.isPrefsChanged = true
        }
       Main.applyWindowInsets(cord)
    }

    private fun restoreThemeButtonsAndApplyChanges() {
        chooseThemeBtn!!.visibility = View.VISIBLE
        themeMenu!!.visibility = View.GONE
        setAppTheme()
    }
    private fun setAppTheme() {
        if (Prefs(this).isLightThemeUsed) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                exitProcess(0)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                exitProcess(0)
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
