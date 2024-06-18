package ru.dimon6018.metrolauncher.content.settings.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.materialswitch.MaterialSwitch
import ru.dimon6018.metrolauncher.Application.Companion.EXP_PREFS
import ru.dimon6018.metrolauncher.R
import ru.dimon6018.metrolauncher.helpers.utils.Utils
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.applyWindowInsets

class ExperimentsSettingsActivity: AppCompatActivity() {

    private var anims: MaterialSwitch? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(Utils.launcherAccentTheme())
        super.onCreate(savedInstanceState)
        setContentView(R.layout.launcher_settings_experiments)
        val coord = findViewById<CoordinatorLayout>(R.id.coordinator)
        applyWindowInsets(coord)

        anims = findViewById(R.id.exp_allowAnims)
        anims?.isChecked = EXP_PREFS!!.getAnimationPref
        anims?.setOnCheckedChangeListener { _, isChecked ->
            EXP_PREFS!!.setAnimationPref(isChecked)
            anims?.isChecked = isChecked
        }
    }
}