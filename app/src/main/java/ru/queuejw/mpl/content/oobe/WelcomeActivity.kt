package ru.queuejw.mpl.content.oobe

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.WindowCompat
import androidx.fragment.app.commit
import ru.queuejw.mpl.Application.Companion.PREFS
import ru.queuejw.mpl.R
import ru.queuejw.mpl.content.oobe.fragments.ConfigureFragment
import ru.queuejw.mpl.content.oobe.fragments.WelcomeFragment
import ru.queuejw.mpl.databinding.OobeMainScreenBinding
import ru.queuejw.mpl.helpers.utils.Utils.Companion.applyWindowInsets

class WelcomeActivity : AppCompatActivity() {

    private lateinit var binding: OobeMainScreenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        when (PREFS.appTheme) {
            0 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            1 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            2 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
        super.onCreate(savedInstanceState)
        binding = OobeMainScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        applyWindowInsets(binding.root)
        if (PREFS.launcherState != 2) {
            supportFragmentManager.commit {
                replace(R.id.fragment_container_view, WelcomeFragment(), "oobe")
            }
        } else {
            supportFragmentManager.commit {
                replace(R.id.fragment_container_view, ConfigureFragment(), "oobe")
            }
        }
        WindowCompat.setDecorFitsSystemWindows(window, false)
    }

    fun setText(newText: String) {
        binding.appbarTextView.text = newText
    }
}