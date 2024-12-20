package ru.queuejw.mpl.content.oobe

import android.os.Bundle
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.WindowCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.queuejw.mpl.Application.Companion.PREFS
import ru.queuejw.mpl.R
import ru.queuejw.mpl.content.oobe.fragments.AlmostDoneFragment
import ru.queuejw.mpl.content.oobe.fragments.AppsFragment
import ru.queuejw.mpl.content.oobe.fragments.ConfigureFragment
import ru.queuejw.mpl.content.oobe.fragments.CustomSettingsFragment
import ru.queuejw.mpl.content.oobe.fragments.IconPackFragment
import ru.queuejw.mpl.content.oobe.fragments.RestartFragment
import ru.queuejw.mpl.content.oobe.fragments.TermsOfUseFragment
import ru.queuejw.mpl.content.oobe.fragments.WelcomeFragment
import ru.queuejw.mpl.databinding.OobeMainScreenBinding
import ru.queuejw.mpl.helpers.utils.Utils.Companion.applyWindowInsets

class OOBEActivity : AppCompatActivity() {

    private lateinit var binding: OobeMainScreenBinding
    private val slideDp by lazy { this.resources.getDimensionPixelSize(R.dimen.oobe_bottom_bar_slide_dp).toFloat() }

    var nextFragment = 1
    var previousFragment = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        when (PREFS.appTheme) {
            0 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            1 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            2 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
        super.onCreate(savedInstanceState)
        binding = OobeMainScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setUi()
        blockBottomBarButton(false)
        setFragment(0)
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {}
        })
    }
    private fun setUi() {
        binding.next.setOnClickListener {
            animateBottomBar(true)
            setFragment(nextFragment)
        }
        binding.previous.setOnClickListener {
            animateBottomBar(true)
            setFragment(previousFragment)
        }
        applyWindowInsets(binding.root)
        WindowCompat.setDecorFitsSystemWindows(window, false)
    }
    fun setFragment(value: Int) {
        binding.oobeView.animate().translationX(-500f).alpha(0.15f).setDuration(200).setInterpolator(
            DecelerateInterpolator()).withEndAction {
            binding.oobeView.apply {
                translationX = 500f
            }
            supportFragmentManager.commit {
                replace(binding.fragmentContainerView.id, getCurrentFragment(value), "oobe")
            }
            binding.oobeView.animate().translationX(0f).alpha(1f).setDuration(200).setInterpolator(
                DecelerateInterpolator()).start()
        }.start()
    }
    private fun getCurrentFragment(value: Int): Fragment {
        return when (value) {
            1 -> TermsOfUseFragment()
            2 -> ConfigureFragment()
            3 -> AppsFragment()
            4 -> CustomSettingsFragment()
            5 -> IconPackFragment()
            6 -> AlmostDoneFragment()
            7 -> RestartFragment()
            else -> WelcomeFragment()
        }
    }
    fun animateBottomBar(slideDown: Boolean) {
        if (slideDown) {
            binding.bottomBar.animate().translationY(slideDp).alpha(0f).setDuration(125).start()
        } else {
            binding.bottomBar.animate().translationY(0f).alpha(1f).setDuration(125).start()
        }
    }
    fun animateBottomBarFromFragment() {
        lifecycleScope.launch {
            delay(100)
            animateBottomBar(false)
        }
    }
    fun updateNextButtonText(text: String) {
        binding.next.text = text
    }
    fun updatePreviousButtonText(text: String) {
        binding.previous.text = text
    }
    fun enableAllButtons() {
        binding.next.apply {
            visibility = View.VISIBLE
            isActivated = true
        }
        binding.previous.apply {
            visibility = View.VISIBLE
            isActivated = true
        }
    }
    fun blockBottomBarButton(next: Boolean) {
        if (next) binding.next.apply {
            visibility = View.INVISIBLE
            isActivated = false
        }
        else {
            binding.previous.apply {
                visibility = View.INVISIBLE
                isActivated = false
            }
        }
    }
    fun setText(newText: String) {
        binding.appbarTextView.text = newText
    }
}