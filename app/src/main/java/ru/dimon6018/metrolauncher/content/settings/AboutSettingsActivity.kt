package ru.dimon6018.metrolauncher.content.settings

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
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

class AboutSettingsActivity : AppCompatActivity() {

    private var coord: CoordinatorLayout? = null

    private var mpl_n = BuildConfig.VERSION_NAME
    private var mpl_c = BuildConfig.VERSION_CODE
    private var model = Build.MODEL
    private var brand = Build.BRAND
    private var device = Build.DEVICE
    private var product = Build.PRODUCT
    private var hardware = Build.HARDWARE
    private var display = Build.DISPLAY
    private var time = Build.TIME

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(Application.getLauncherAccentTheme())
        super.onCreate(savedInstanceState)
        setContentView(R.layout.launcher_settings_about)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val moreinfo = findViewById<TextView>(R.id.phoneinfo_more)
        coord = findViewById(R.id.coordinator)
        val moreinfo_btn = findViewById<MaterialButton>(R.id.moreInfobtn)
        val moreinfo_layout = findViewById<LinearLayout>(R.id.moreinfoLayout)
        moreinfo.text = getString(R.string.phone_moreinfo, mpl_n, mpl_c, device, brand, model, product, hardware, display, time)
        moreinfo_btn.setOnClickListener {
            moreinfo_btn.visibility = View.GONE
            moreinfo_layout.visibility = View.VISIBLE
        }
        ViewCompat.setOnApplyWindowInsetsListener(coord!!) { v: View, insets: WindowInsetsCompat ->
            val pB = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
            val tB = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            v.setPadding(0, tB, 0, pB)
            WindowInsetsCompat.CONSUMED
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        window.decorView.findViewById<View>(android.R.id.content).startAnimation(AnimationUtils.loadAnimation(this, R.anim.back_flip_anim))
        finish()
    }
}
