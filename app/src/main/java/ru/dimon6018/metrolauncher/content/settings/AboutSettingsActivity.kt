package ru.dimon6018.metrolauncher.content.settings

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import ru.dimon6018.metrolauncher.Application
import ru.dimon6018.metrolauncher.BuildConfig
import ru.dimon6018.metrolauncher.R
import ru.dimon6018.metrolauncher.helpers.WPDialog

class AboutSettingsActivity : AppCompatActivity() {

    private var coord: CoordinatorLayout? = null

    private var n = BuildConfig.VERSION_NAME
    private var c = BuildConfig.VERSION_CODE
    private var model = Build.MODEL
    private var brand = Build.BRAND
    private var device = Build.DEVICE
    private var product = Build.PRODUCT
    private var hardware = Build.HARDWARE
    private var display = Build.DISPLAY
    private var time = Build.TIME

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(Application.launcherAccentTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.launcher_settings_about)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val moreinfo = findViewById<TextView>(R.id.phoneinfo_more)
        coord = findViewById(R.id.coordinator)
        val moreinfobtn = findViewById<MaterialButton>(R.id.moreInfobtn)
        val moreinfolayout = findViewById<LinearLayout>(R.id.moreinfoLayout)
        moreinfo.text = getString(R.string.phone_moreinfo, n, c, device, brand, model, product, hardware, display, time)
        moreinfobtn.setOnClickListener {
            moreinfobtn.visibility = View.GONE
            moreinfolayout.visibility = View.VISIBLE
        }
        val reset = findViewById<MaterialButton>(R.id.resetLauncher)
        reset.setOnClickListener {
            WPDialog(this).setTopDialog(true)
                    .setTitle(getString(R.string.reset_warning_title))
                    .setMessage(getString(R.string.reset_warning))
                    .setNegativeButton(getString(android.R.string.yes)) { resetPart1() }
                    .setPositiveButton(getString(android.R.string.no), null).show()
        }
        ViewCompat.setOnApplyWindowInsetsListener(coord!!) { v: View, insets: WindowInsetsCompat ->
            val pB = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
            val tB = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            v.setPadding(0, tB, 0, pB)
            WindowInsetsCompat.CONSUMED
        }
    }
    private fun resetPart1() {
        val intent = (Intent(this@AboutSettingsActivity, Reset::class.java))
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
    }
    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}
