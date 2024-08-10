package ru.dimon6018.metrolauncher.content.settings.activities

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.slider.Slider
import ru.dimon6018.metrolauncher.Application.Companion.PREFS
import ru.dimon6018.metrolauncher.R
import ru.dimon6018.metrolauncher.databinding.LauncherSettingsNavbarBinding
import ru.dimon6018.metrolauncher.databinding.NavbarIconChooseBinding
import ru.dimon6018.metrolauncher.helpers.utils.Utils
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.applyWindowInsets

class NavBarSettingsActivity: AppCompatActivity() {

    private lateinit var binding: LauncherSettingsNavbarBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(Utils.launcherAccentTheme())
        binding = LauncherSettingsNavbarBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(binding.root)
        when(PREFS!!.navBarColor) {
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
        binding.settingsInclude.navbarRadioGroup.setOnCheckedChangeListener { _, checkedId ->
           when(checkedId) {
               binding.settingsInclude.alwaysDark.id -> {
                   PREFS!!.navBarColor = 0
               }
               binding.settingsInclude.alwaysLight.id -> {
                   PREFS!!.navBarColor = 1
               }
               binding.settingsInclude.byTheme.id -> {
                   PREFS!!.navBarColor = 2
              }
               binding.settingsInclude.hidden.id -> {
                   PREFS!!.navBarColor = 3
               }
               binding.settingsInclude.auto.id -> {
                   PREFS!!.navBarColor = 4
               }
           }
            PREFS!!.isPrefsChanged = true
        }
        applyWindowInsets(binding.root)
        updateCurrentIcon()

        binding.settingsInclude.chooseStartIconBtn.setOnClickListener {
            val bottomSheet = BottomSheetDialog(this)
            val bBidding = NavbarIconChooseBinding.inflate(LayoutInflater.from(this))
            bottomSheet.setContentView(bBidding.root)
            bottomSheet.dismissWithAnimation = true
            bBidding.icon0.setOnClickListener {
                PREFS!!.navBarIconValue = 1
                updateCurrentIcon()
                PREFS!!.isPrefsChanged = true
                bottomSheet.dismiss()
            }
            bBidding.icon1.setOnClickListener {
                PREFS!!.navBarIconValue = 0
                updateCurrentIcon()
                PREFS!!.isPrefsChanged = true
                bottomSheet.dismiss()
            }
            bBidding.icon2.setOnClickListener {
                PREFS!!.navBarIconValue = 2
                updateCurrentIcon()
                PREFS!!.isPrefsChanged = true
                bottomSheet.dismiss()
            }
            bottomSheet.show()
        }
        binding.settingsInclude.searchBarSwitch.apply {
            isChecked = PREFS!!.isSearchBarEnabled
            text = if(PREFS!!.isSearchBarEnabled) getString(R.string.on) else getString(R.string.off)
            setOnCheckedChangeListener { _, isChecked ->
                PREFS!!.isSearchBarEnabled = isChecked
                text = if(isChecked) getString(R.string.on) else getString(R.string.off)
                PREFS!!.isPrefsChanged = true
            }
        }
        binding.settingsInclude.maxResultsSlider.apply {
            value = PREFS!!.maxResultsSearchBar.toFloat()
            addOnChangeListener(Slider.OnChangeListener { _: Slider?, value: Float, _: Boolean ->
                PREFS!!.maxResultsSearchBar = value.toInt()
            })
        }
    }
    private fun updateCurrentIcon() {
        binding.settingsInclude.currentStartIcon.setImageDrawable(when(PREFS!!.navBarIconValue) {
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
    }
    private fun enterAnimation(exit: Boolean) {
        if(!PREFS!!.isTransitionAnimEnabled) {
            return
        }
        val main = binding.root
        val animatorSet = AnimatorSet()
        if(exit) {
            animatorSet.playTogether(
                ObjectAnimator.ofFloat(main, "translationX", 0f, 300f),
                ObjectAnimator.ofFloat(main, "rotationY", 0f, 90f),
                ObjectAnimator.ofFloat(main, "alpha", 1f, 0f),
                ObjectAnimator.ofFloat(main, "scaleX", 1f, 0.5f),
                ObjectAnimator.ofFloat(main, "scaleY", 1f, 0.5f),
            )
        } else {
            animatorSet.playTogether(
                ObjectAnimator.ofFloat(main, "translationX", 300f, 0f),
                ObjectAnimator.ofFloat(main, "rotationY", 90f, 0f),
                ObjectAnimator.ofFloat(main, "alpha", 0f, 1f),
                ObjectAnimator.ofFloat(main, "scaleX", 0.5f, 1f),
                ObjectAnimator.ofFloat(main, "scaleY", 0.5f, 1f)
            )
        }
        animatorSet.setDuration(400)
        animatorSet.start()
    }
    override fun onResume() {
        enterAnimation(false)
        super.onResume()
    }

    override fun onPause() {
        enterAnimation(true)
        super.onPause()
    }
}