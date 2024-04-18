package ru.dimon6018.metrolauncher.content.settings.activities

import android.os.Bundle
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.WindowCompat
import com.google.android.material.radiobutton.MaterialRadioButton
import ru.dimon6018.metrolauncher.Application.Companion.PREFS
import ru.dimon6018.metrolauncher.Application.Companion.applyWindowInsets
import ru.dimon6018.metrolauncher.R
import ru.dimon6018.metrolauncher.content.data.Prefs

class NavBarSettingsActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.launcher_settings_navbar)
        val radio: RadioGroup = findViewById(R.id.navbarRadioGroup)
        val dark: MaterialRadioButton = findViewById(R.id.alwaysDark)
        val light: MaterialRadioButton = findViewById(R.id.alwaysLight)
    //    val accent: MaterialRadioButton = findViewById(R.id.byTheme)
        val hidden: MaterialRadioButton = findViewById(R.id.hidden)
        val auto: MaterialRadioButton = findViewById(R.id.auto)
        radio.setOnCheckedChangeListener { _, checkedId ->
           when(checkedId) {
               dark.id -> {
                   PREFS!!.setNavBarSetting(0)
               }
               light.id -> {
                   PREFS!!.setNavBarSetting(1)
               }
       //        accent.id -> {
       //            PREFS!!.setNavBarSetting(2)
         //      }
               hidden.id -> {
                   PREFS!!.setNavBarSetting(3)
               }
               auto.id -> {
                   PREFS!!.setNavBarSetting(4)
               }
           }
            PREFS!!.setPrefsChanged(true)
        }
        when(PREFS!!.navBarColor) {
            0 -> {
                dark.isChecked = true
                light.isChecked = false
                //accent.isChecked = false
                hidden.isChecked = false
                auto.isChecked = false
            }
            1 -> {
                dark.isChecked = false
                light.isChecked = true
            //    accent.isChecked = false
                hidden.isChecked = false
                auto.isChecked = false
            }
            2 -> {
                dark.isChecked = false
                light.isChecked = false
            //    accent.isChecked = true
                hidden.isChecked = false
                auto.isChecked = false
            }
            3 -> {
                dark.isChecked = false
                light.isChecked = false
            //    accent.isChecked = false
                hidden.isChecked = true
                auto.isChecked = false
            }
            4 -> {
                dark.isChecked = false
                light.isChecked = false
          //      accent.isChecked = false
                hidden.isChecked = false
                auto.isChecked = true
            }
        }
        val coord: CoordinatorLayout = findViewById(R.id.coordinator)
        applyWindowInsets(coord)
    }
}