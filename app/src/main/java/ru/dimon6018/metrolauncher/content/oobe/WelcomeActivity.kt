package ru.dimon6018.metrolauncher.content.oobe

import android.app.Activity
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.WindowCompat
import androidx.fragment.app.commit
import ru.dimon6018.metrolauncher.Application.Companion.PREFS
import ru.dimon6018.metrolauncher.Main
import ru.dimon6018.metrolauncher.R


class WelcomeActivity: AppCompatActivity() {

    private var textLabel: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.oobe)
        textLabel = findViewById(R.id.appbarTextView)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val coordinatorLayout: CoordinatorLayout = findViewById(R.id.coordinator)
        Main.applyWindowInsets(coordinatorLayout)
        if (savedInstanceState == null) {
            if(PREFS!!.launcherState != 2) {
                supportFragmentManager.commit {
                    replace(R.id.fragment_container_view, WelcomeFragment(), "oobe")
                }
            } else {
                supportFragmentManager.commit {
                    replace(R.id.fragment_container_view, ConfigureFragment(), "oobe")
                }
            }
        }
    }
    companion object {
        fun setText(activity: Activity, text: String) {
            activity.findViewById<TextView>(R.id.appbarTextView)!!.text = text
        }
    }
}