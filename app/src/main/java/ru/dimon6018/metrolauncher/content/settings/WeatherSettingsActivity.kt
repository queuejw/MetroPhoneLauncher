package ru.dimon6018.metrolauncher.content.settings

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import ru.dimon6018.metrolauncher.Main
import ru.dimon6018.metrolauncher.R

class WeatherSettingsActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.launcher_settings_weather)
        val coord = findViewById<CoordinatorLayout>(R.id.coordinator)
        Main.applyWindowInsets(coord)
    }
}