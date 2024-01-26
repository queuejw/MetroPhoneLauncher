package ru.dimon6018.metrolauncher.content.oobe

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.WindowCompat
import ru.dimon6018.metrolauncher.Main
import ru.dimon6018.metrolauncher.R


class WelcomeActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.oobe)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val coordinatorLayout: CoordinatorLayout = findViewById(R.id.coordinator)
        Main.applyWindowInsets(coordinatorLayout)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                    .add(R.id.fragment_container_view, WelcomeFragment::class.java, null)
                    .setReorderingAllowed(true)
                    .commit()
        }
    }
}