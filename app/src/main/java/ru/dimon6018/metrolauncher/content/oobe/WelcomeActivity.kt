package ru.dimon6018.metrolauncher.content.oobe

import android.app.Activity
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.WindowCompat
import androidx.fragment.app.commit
import com.google.android.material.textview.MaterialTextView
import ru.dimon6018.metrolauncher.Application.Companion.PREFS
import ru.dimon6018.metrolauncher.R
import ru.dimon6018.metrolauncher.content.oobe.fragments.ConfigureFragment
import ru.dimon6018.metrolauncher.content.oobe.fragments.WelcomeFragment
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.applyWindowInsets


class WelcomeActivity: AppCompatActivity() {

    private lateinit var main: CoordinatorLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.oobe)
        main = findViewById(R.id.coordinator)
        applyWindowInsets(main)
        if (PREFS!!.launcherState != 2) {
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
    companion object {
        fun setText(activity: Activity, text: String) {
            activity.findViewById<MaterialTextView>(R.id.appbarTextView)!!.text = text
        }
    }
}