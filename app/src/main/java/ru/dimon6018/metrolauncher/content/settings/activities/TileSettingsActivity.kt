package ru.dimon6018.metrolauncher.content.settings.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.slider.Slider
import ru.dimon6018.metrolauncher.Application.Companion.PREFS
import ru.dimon6018.metrolauncher.R
import ru.dimon6018.metrolauncher.helpers.utils.Utils

class TileSettingsActivity: AppCompatActivity() {

    private var alphaSlider: Slider? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(Utils.launcherAccentTheme())
        super.onCreate(savedInstanceState)
        setContentView(R.layout.launcher_settings_tiles)
        val coord = findViewById<CoordinatorLayout>(R.id.coordinator)
        Utils.applyWindowInsets(coord)
        alphaSlider = findViewById(R.id.alphaSlider)
        alphaSlider!!.addOnChangeListener(Slider.OnChangeListener { _: Slider?, value: Float, _: Boolean ->
            PREFS!!.setTileTransparency(value)
        })
    }

    override fun onResume() {
        super.onResume()
        alphaSlider!!.value = PREFS!!.getTilesTransparency
    }
}