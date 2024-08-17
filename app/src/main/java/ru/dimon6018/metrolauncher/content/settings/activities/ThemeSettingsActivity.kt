package ru.dimon6018.metrolauncher.content.settings.activities

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
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
import androidx.core.view.WindowCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.color.DynamicColors
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.snackbar.Snackbar
import ru.dimon6018.metrolauncher.Application
import ru.dimon6018.metrolauncher.Application.Companion.PREFS
import ru.dimon6018.metrolauncher.R
import ru.dimon6018.metrolauncher.content.data.Prefs
import ru.dimon6018.metrolauncher.databinding.LauncherSettingsThemeBinding
import ru.dimon6018.metrolauncher.helpers.ui.WPDialog
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.accentName
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.applyWindowInsets
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.launcherAccentColor

class ThemeSettingsActivity : AppCompatActivity() {

    private lateinit var binding: LauncherSettingsThemeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LauncherSettingsThemeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setThemeText()
        configure()
        setImg()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        applyWindowInsets(binding.root)
        prepareTip()
    }

    private fun configure() {
        binding.settingsInclude.choosedAccentName.text = accentName(this)
        binding.settingsInclude.chooseTheme.apply {
            text = if (PREFS!!.isLightThemeUsed) getString(R.string.light) else getString(R.string.dark)
            setOnClickListener {
                visibility = View.GONE
                binding.settingsInclude.chooseThemeMenu.visibility = View.VISIBLE
            }
        }
        binding.settingsInclude.chooseLight.setOnClickListener {
            PREFS!!.isLightThemeUsed = true
            PREFS!!.isPrefsChanged = true
            restoreThemeButtonsAndApplyChanges()
        }
        binding.settingsInclude.chooseDark.setOnClickListener {
            PREFS!!.isLightThemeUsed = false
            PREFS!!.isPrefsChanged = true
            restoreThemeButtonsAndApplyChanges()
        }
        binding.settingsInclude.chooseAccent.setOnClickListener { AccentDialog.display(supportFragmentManager) }
        binding.settingsInclude.moreTilesSwitch.apply {
            isChecked = PREFS!!.isMoreTilesEnabled
            text = if(PREFS!!.isMoreTilesEnabled) getString(R.string.on) else getString(R.string.off)
            setOnCheckedChangeListener { _, isChecked ->
                PREFS!!.isMoreTilesEnabled = isChecked
                text = if (isChecked) getString(R.string.on) else getString(R.string.off)
                PREFS!!.isPrefsChanged = true
                setImg()
            }
        }
        binding.settingsInclude.newAppsToStartSwitch.apply {
            isChecked = PREFS!!.pinNewApps
            text = if(PREFS!!.pinNewApps) getString(R.string.on) else getString(R.string.off)
            setOnCheckedChangeListener { _, isChecked ->
                PREFS!!.pinNewApps = isChecked
                text = if (isChecked) getString(R.string.on) else getString(R.string.off)
            }
        }
        binding.settingsInclude.parallaxSwitch.apply {
            isChecked = PREFS!!.isParallaxEnabled
            text = if(PREFS!!.isParallaxEnabled) getString(R.string.on) else getString(R.string.off)
            setOnCheckedChangeListener { _, chk ->
                if (PREFS!!.isWallpaperUsed) {
                    PREFS!!.isParallaxEnabled = chk
                    PREFS!!.isPrefsChanged = true
                    text = if (chk) getString(R.string.on) else getString(R.string.off)
                } else {
                    if (chk) {
                        if(PREFS!!.isTransitionAnimEnabled) {
                            binding.root.animate().alpha(0.7f).setDuration(200).start()
                        }
                        isChecked = false
                        parallaxDialog(this@ThemeSettingsActivity, this)
                    }
                }
            }
        }
        binding.settingsInclude.wallpaperShowSwtich.apply {
            isChecked = PREFS!!.isWallpaperUsed
            text = if(PREFS!!.isWallpaperUsed) getString(R.string.on) else getString(R.string.off)
            setOnCheckedChangeListener { _, check ->
                PREFS!!.isWallpaperUsed = check
                PREFS!!.isPrefsChanged = true
                text = if(check) getString(R.string.on) else getString(R.string.off)
            }
        }
        binding.settingsInclude.dynamicColorSwtich.apply {
            if(!DynamicColors.isDynamicColorAvailable()) {
                isEnabled = false
            }
            setChecked(PREFS!!.accentColor == 20)
            text = if(PREFS!!.accentColor == 20) getString(R.string.on) else getString(R.string.off)
            setOnCheckedChangeListener { _, isChecked ->
                if (DynamicColors.isDynamicColorAvailable()) {
                    if (isChecked) {
                        PREFS!!.accentColor = 20
                    } else {
                        PREFS!!.accentColor = PREFS!!.prefs.getInt("previous_accent_color", 5)
                    }
                    recreate()
                } else {
                    Snackbar.make(
                        this,
                        getString(R.string.dynamicColor_error),
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            }
        }
        binding.settingsInclude.blockStartSwitch.apply {
            isChecked = PREFS!!.isStartBlocked
            text = if(PREFS!!.isStartBlocked) getString(R.string.on) else getString(R.string.off)
            setOnCheckedChangeListener { _, isChecked ->
                PREFS!!.isStartBlocked = isChecked
                text = if(isChecked) getString(R.string.on) else getString(R.string.off)
            }
        }
        binding.settingsInclude.coloredStrokeSwitch.apply {
            isChecked = PREFS!!.coloredStroke
            text = if(PREFS!!.coloredStroke) getString(R.string.on) else getString(R.string.off)
            setOnCheckedChangeListener { _, isChecked ->
                PREFS!!.coloredStroke = isChecked
                text = if(isChecked) getString(R.string.on) else getString(R.string.off)
                PREFS!!.isPrefsChanged = true
            }
        }
        setOrientationButtons()
    }

    private fun setThemeText() {
        val textFinal = getString(R.string.settings_theme_accent_title_part2) + " " + getString(R.string.settings_theme_accent_title_part1) + " " + getString(R.string.settings_theme_accent_title_part3)
        val spannable: Spannable = SpannableString(textFinal)
        spannable.setSpan(ForegroundColorSpan(launcherAccentColor(theme)), textFinal.indexOf(getString(R.string.settings_theme_accent_title_part1)),textFinal.indexOf(getString(R.string.settings_theme_accent_title_part1)) + getString(R.string.settings_theme_accent_title_part1).length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        binding.settingsInclude.accentTip.setText(spannable, TextView.BufferType.SPANNABLE)
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
        if (!PREFS!!.isTransitionAnimEnabled) {
            return
        }
        val main = binding.root
        val animatorSet = AnimatorSet().apply {
            playTogether(
                createObjectAnimator(main, "translationX", if (exit) 0f else -300f, if (exit) -300f else 0f),
                createObjectAnimator(main, "rotationY", if (exit) 0f else 90f, if (exit) 90f else 0f),
                createObjectAnimator(main, "alpha", if (exit) 1f else 0f, if (exit) 0f else 1f),
                createObjectAnimator(main, "scaleX", if (exit) 1f else 0.5f, if (exit) 0.5f else 1f),
                createObjectAnimator(main, "scaleY", if (exit) 1f else 0.5f, if (exit) 0.5f else 1f)
            )
            duration = 400
        }
        animatorSet.start()
    }
    private fun createObjectAnimator(target: Any, property: String, startValue: Float, endValue: Float): ObjectAnimator {
        return ObjectAnimator.ofFloat(target, property, startValue, endValue)
    }
    override fun onPause() {
        enterAnimation(true)
        super.onPause()
    }
    override fun onResume() {
        super.onResume()
        enterAnimation(false)
    }
    private fun setImg() {
        binding.settingsInclude.moreTilesImage.setImageResource(if(PREFS!!.isMoreTilesEnabled) R.mipmap.tiles_small else R.mipmap.tiles_default)
    }
    private fun restoreThemeButtonsAndApplyChanges() {
        binding.settingsInclude.chooseTheme.visibility = View.VISIBLE
        binding.settingsInclude.chooseThemeMenu.visibility = View.GONE
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
    fun setOrientationButtons() {
        val orientations = mapOf(
            "p" to Triple(true, false, false),
            "l" to Triple(false, true, false)
        )
        val (portrait, landscape, default) = orientations[PREFS!!.orientation] ?: Triple(false, false, true)
        binding.settingsInclude.portraitOrientation.isChecked = portrait
        binding.settingsInclude.landscapeOrientation.isChecked = landscape
        binding.settingsInclude.defaultOrientation.isChecked = default

        binding.settingsInclude.orientationRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            when(checkedId) {
                binding.settingsInclude.portraitOrientation.id -> {
                    PREFS!!.orientation = "p"
                }
                binding.settingsInclude.landscapeOrientation.id -> {
                    PREFS!!.orientation = "l"
                }
                binding.settingsInclude.defaultOrientation.id -> {
                    PREFS!!.orientation = "default"
                }
            }
            PREFS!!.isPrefsChanged = true
        }
    }

    fun parallaxDialog(context: Context, switch: MaterialSwitch) {
        WPDialog(context).apply {
            setTitle(getString(R.string.tip))
            setMessage(context.getString(R.string.parallax_warn))
            setPositiveButton(getString(R.string.yes)) {
                PREFS!!.isWallpaperUsed = true
                PREFS!!.isParallaxEnabled = true
                PREFS!!.isPrefsChanged = true
                switch.isChecked = true
                binding.settingsInclude.wallpaperShowSwtich.isChecked = true
                dismiss()
                switch.text = if (PREFS!!.isParallaxEnabled) getString(R.string.on) else getString(
                    R.string.off
                )
            }
            setNegativeButton(getString(android.R.string.cancel)) {
                dismiss()
            }
            setDismissListener {
                if(PREFS!!.isTransitionAnimEnabled) {
                    binding.root.animate().alpha(1f).setDuration(200).start()
                }
            }
            show()
        }
    }
}
