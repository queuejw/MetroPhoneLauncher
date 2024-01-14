package ru.dimon6018.metrolauncher.content.settings

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.WindowCompat
import com.google.android.material.button.MaterialButton
import ru.dimon6018.metrolauncher.Application
import ru.dimon6018.metrolauncher.Application.Companion.BRAND
import ru.dimon6018.metrolauncher.Application.Companion.BUILD
import ru.dimon6018.metrolauncher.Application.Companion.DEVICE
import ru.dimon6018.metrolauncher.Application.Companion.HARDWARE
import ru.dimon6018.metrolauncher.Application.Companion.MODEL
import ru.dimon6018.metrolauncher.Application.Companion.PRODUCT
import ru.dimon6018.metrolauncher.Application.Companion.TIME
import ru.dimon6018.metrolauncher.Application.Companion.VERSION_CODE
import ru.dimon6018.metrolauncher.Application.Companion.VERSION_NAME
import ru.dimon6018.metrolauncher.Main.Companion.applyWindowInsets
import ru.dimon6018.metrolauncher.R
import ru.dimon6018.metrolauncher.helpers.WPDialog

class AboutSettingsActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(Application.launcherAccentTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.launcher_settings_about)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val moreinfo = findViewById<TextView>(R.id.phoneinfo_more)
        val coord: CoordinatorLayout = findViewById(R.id.coordinator)
        val moreinfobtn = findViewById<MaterialButton>(R.id.moreInfobtn)
        val moreinfolayout = findViewById<LinearLayout>(R.id.moreinfoLayout)
        moreinfo.text = getString(R.string.phone_moreinfo, VERSION_NAME, VERSION_CODE, DEVICE, BRAND, MODEL, PRODUCT, HARDWARE, BUILD, TIME)
        moreinfobtn.setOnClickListener {
            moreinfobtn.visibility = View.GONE
            moreinfolayout.visibility = View.VISIBLE
        }
        val reset = findViewById<MaterialButton>(R.id.resetLauncher)
        reset.setOnClickListener {
            WPDialog(this).setTopDialog(true)
                    .setTitle(getString(R.string.reset_warning_title))
                    .setMessage(getString(R.string.reset_warning))
                    .setNegativeButton(getString(R.string.yes)) { resetPart1() }
                    .setPositiveButton(getString(R.string.no), null).show()
        }
        applyWindowInsets(coord)
    }
    private fun resetPart1() {
        val intent = (Intent(this@AboutSettingsActivity, Reset::class.java))
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
    }
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}
