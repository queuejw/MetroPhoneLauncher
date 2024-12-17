package ru.queuejw.mpl.content.settings.activities

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import com.google.android.material.bottomsheet.BottomSheetDialog
import ru.queuejw.mpl.Application.Companion.PREFS
import ru.queuejw.mpl.Application.Companion.customBoldFont
import ru.queuejw.mpl.Application.Companion.customFont
import ru.queuejw.mpl.R
import ru.queuejw.mpl.databinding.LauncherSettingsNavbarBinding
import ru.queuejw.mpl.databinding.SettingsNavbarIconChooseBinding
import ru.queuejw.mpl.helpers.utils.Utils.Companion.applyWindowInsets

class NavBarSettingsActivity : AppCompatActivity() {

    private lateinit var binding: LauncherSettingsNavbarBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = LauncherSettingsNavbarBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(binding.root)
        setNavBarColorRadioGroup()
        binding.settingsInclude.navbarRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            changeNavBarColor(checkedId)
            PREFS.isPrefsChanged = true
        }
        applyWindowInsets(binding.root)
        updateCurrentIcon()

        binding.settingsInclude.chooseStartIconBtn.setOnClickListener {
            iconsBottomSheet()
        }
        setupFont()
    }

    private fun setupFont() {
        customFont?.let {
            binding.settingsSectionLabel.typeface = it
            binding.settingsLabel.typeface = it
            binding.settingsInclude.navigationBar.typeface = it
            binding.settingsInclude.auto.typeface = it
            binding.settingsInclude.alwaysDark.typeface = it
            binding.settingsInclude.alwaysLight.typeface = it
            binding.settingsInclude.byTheme.typeface = it
            binding.settingsInclude.hidden.typeface = it
            binding.settingsInclude.additionalOptions.typeface = it
            binding.settingsInclude.currentIconText.typeface = it
            binding.settingsInclude.chooseStartIconBtn.typeface = it
            binding.settingsInclude.iconChangeLabel.typeface = it
        }
        customBoldFont?.let {
            binding.settingsLabel.typeface = it
        }
    }

    private fun setNavBarColorRadioGroup() {
        when (PREFS.navBarColor) {
            0 -> {
                binding.settingsInclude.alwaysDark.isChecked = true
                binding.settingsInclude.alwaysLight.isChecked = false
                binding.settingsInclude.byTheme.isChecked = false
                binding.settingsInclude.hidden.isChecked = false
                binding.settingsInclude.auto.isChecked = false
            }

            1 -> {
                binding.settingsInclude.alwaysDark.isChecked = false
                binding.settingsInclude.alwaysLight.isChecked = true
                binding.settingsInclude.byTheme.isChecked = false
                binding.settingsInclude.hidden.isChecked = false
                binding.settingsInclude.auto.isChecked = false
            }

            2 -> {
                binding.settingsInclude.alwaysDark.isChecked = false
                binding.settingsInclude.alwaysLight.isChecked = false
                binding.settingsInclude.byTheme.isChecked = true
                binding.settingsInclude.hidden.isChecked = false
                binding.settingsInclude.auto.isChecked = false
            }

            3 -> {
                binding.settingsInclude.alwaysDark.isChecked = false
                binding.settingsInclude.alwaysLight.isChecked = false
                binding.settingsInclude.byTheme.isChecked = false
                binding.settingsInclude.hidden.isChecked = true
                binding.settingsInclude.auto.isChecked = false
            }

            4 -> {
                binding.settingsInclude.alwaysDark.isChecked = false
                binding.settingsInclude.alwaysLight.isChecked = false
                binding.settingsInclude.byTheme.isChecked = false
                binding.settingsInclude.hidden.isChecked = false
                binding.settingsInclude.auto.isChecked = true
            }
        }
    }

    private fun updateCurrentIcon() {
        binding.settingsInclude.currentStartIcon.setImageDrawable(
            when (PREFS.navBarIconValue) {
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
    }

    private fun enterAnimation(exit: Boolean) {
        if (!PREFS.isTransitionAnimEnabled) return

        val main = binding.root
        val animatorSet = AnimatorSet().apply {
            playTogether(
                createObjectAnimator(
                    main,
                    "translationX",
                    if (exit) 0f else -300f,
                    if (exit) -300f else 0f
                ),
                createObjectAnimator(
                    main,
                    "rotationY",
                    if (exit) 0f else 90f,
                    if (exit) 90f else 0f
                ),
                createObjectAnimator(main, "alpha", if (exit) 1f else 0f, if (exit) 0f else 1f),
                createObjectAnimator(
                    main,
                    "scaleX",
                    if (exit) 1f else 0.5f,
                    if (exit) 0.5f else 1f
                ),
                createObjectAnimator(main, "scaleY", if (exit) 1f else 0.5f, if (exit) 0.5f else 1f)
            )
            duration = 400
        }
        animatorSet.start()
    }

    private fun createObjectAnimator(
        target: Any,
        property: String,
        startValue: Float,
        endValue: Float
    ): ObjectAnimator {
        return ObjectAnimator.ofFloat(target, property, startValue, endValue)
    }

    override fun onResume() {
        enterAnimation(false)
        super.onResume()
    }

    override fun onPause() {
        enterAnimation(true)
        super.onPause()
    }

    fun changeNavBarColor(checkedId: Int) {
        when (checkedId) {
            binding.settingsInclude.alwaysDark.id -> {
                PREFS.navBarColor = 0
            }

            binding.settingsInclude.alwaysLight.id -> {
                PREFS.navBarColor = 1
            }

            binding.settingsInclude.byTheme.id -> {
                PREFS.navBarColor = 2
            }

            binding.settingsInclude.hidden.id -> {
                PREFS.navBarColor = 3
            }

            binding.settingsInclude.auto.id -> {
                PREFS.navBarColor = 4
            }
        }
    }

    fun iconsBottomSheet() {
        val bottomSheet = BottomSheetDialog(this)
        val bBidding = SettingsNavbarIconChooseBinding.inflate(LayoutInflater.from(this))
        bottomSheet.setContentView(bBidding.root)
        bottomSheet.dismissWithAnimation = true
        bBidding.icon0.setOnClickListener {
            PREFS.navBarIconValue = 1
            updateCurrentIcon()
            PREFS.isPrefsChanged = true
            bottomSheet.dismiss()
        }
        bBidding.icon1.setOnClickListener {
            PREFS.navBarIconValue = 0
            updateCurrentIcon()
            PREFS.isPrefsChanged = true
            bottomSheet.dismiss()
        }
        bBidding.icon2.setOnClickListener {
            PREFS.navBarIconValue = 2
            updateCurrentIcon()
            PREFS.isPrefsChanged = true
            bottomSheet.dismiss()
        }
        bottomSheet.show()
    }
}