package ru.dimon6018.metrolauncher.content.settings.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import ru.dimon6018.metrolauncher.R
import ru.dimon6018.metrolauncher.helpers.utils.Utils
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.applyWindowInsets

class ExperimentsSettingsActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(Utils.launcherAccentTheme())
        super.onCreate(savedInstanceState)
        setContentView(R.layout.launcher_settings_experiments)
        val coord = findViewById<CoordinatorLayout>(R.id.coordinator)
        applyWindowInsets(coord)
    }
}