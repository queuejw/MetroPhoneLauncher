package ru.dimon6018.metrolauncher.content.settings.activities

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.WindowCompat
import coil3.load
import coil3.request.placeholder
import com.google.android.material.button.MaterialButton
import com.google.android.material.textview.MaterialTextView
import ru.dimon6018.metrolauncher.Application.Companion.PREFS
import ru.dimon6018.metrolauncher.R
import ru.dimon6018.metrolauncher.content.settings.Reset
import ru.dimon6018.metrolauncher.helpers.ui.WPDialog
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.BRAND
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.BUILD
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.DEVICE
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.HARDWARE
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.MANUFACTURER
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.MODEL
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.PRODUCT
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.TIME
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.VERSION_CODE
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.VERSION_NAME
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.applyWindowInsets
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.launcherAccentTheme
import kotlin.system.exitProcess

class AboutSettingsActivity : AppCompatActivity() {

    private var main: CoordinatorLayout? = null
    private val caracalLink = "https://upload.wikimedia.org/wikipedia/commons/thumb/a/a3/Caracl_%2801%29%2C_Paris%2C_d%C3%A9cembre_2013.jpg/916px-Caracl_%2801%29%2C_Paris%2C_d%C3%A9cembre_2013.jpg"

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(launcherAccentTheme() )
        super.onCreate(savedInstanceState)
        setContentView(R.layout.launcher_settings_about)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val moreinfo = findViewById<MaterialTextView>(R.id.phoneinfo_more)
        val moreinfobtn = findViewById<MaterialButton>(R.id.moreInfobtn)
        val moreinfolayout = findViewById<LinearLayout>(R.id.moreinfoLayout)
        val shortPhoneInfo = findViewById<MaterialTextView>(R.id.phoneinfo)
        shortPhoneInfo.text = getString(R.string.phone_info, "$MANUFACTURER $PRODUCT", MODEL, VERSION_NAME)
        moreinfobtn.setOnClickListener {
            moreinfo.text = getString(R.string.phone_moreinfo, VERSION_NAME, VERSION_CODE, DEVICE, BRAND, MODEL, PRODUCT, HARDWARE, BUILD, TIME)
            moreinfobtn.visibility = View.GONE
            moreinfolayout.visibility = View.VISIBLE
        }
        val reset = findViewById<MaterialButton>(R.id.resetLauncher)
        reset.setOnClickListener {
            WPDialog(this).setTopDialog(true)
                    .setTitle(getString(R.string.reset_warning_title))
                    .setMessage(getString(R.string.reset_warning))
                    .setNegativeButton(getString(R.string.yes)) {
                        resetPart1()
                        WPDialog(this).dismiss()
                    }
                    .setPositiveButton(getString(R.string.no), null).show()
        }
        val restart = findViewById<MaterialButton>(R.id.restartLauncher)
        restart.setOnClickListener {
            exitProcess(0)
        }
        val crash = findViewById<MaterialButton>(R.id.crashLauncher)
        crash.setOnClickListener {
            // NullPointerException crash
            val crashElement: MaterialButton = findViewById(R.id.refresh)
            crashElement.visibility = View.GONE
        }
        main = findViewById(R.id.coordinator)
        main?.apply { applyWindowInsets(this) }

        val easterEggText = findViewById<MaterialTextView>(R.id.queuejw)
        val easterEggImg = findViewById<ImageView>(R.id.queuejwImg)
        easterEggText.setOnLongClickListener {
            easterEggImg.visibility = View.VISIBLE
            easterEggImg.load(caracalLink) {
              placeholder(R.drawable.ic_clock)
            }
            return@setOnLongClickListener true
        }
        easterEggImg.setOnClickListener {
            WPDialog(this).setTopDialog(true).setTitle("Meow meow").setMessage("Meow meow, meow?").setPositiveButton("meow", null).show()
        }
    }
    private fun resetPart1() {
        val intent = (Intent(this@AboutSettingsActivity, Reset::class.java))
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        finishAffinity()
        startActivity(intent)
    }
    private fun enterAnimation(exit: Boolean) {
        if(main == null || !PREFS!!.isTransitionAnimEnabled) {
            return
        }
        val animatorSet = AnimatorSet()
        if(exit) {
            animatorSet.playTogether(
                ObjectAnimator.ofFloat(main!!, "translationX", 0f, 300f),
                ObjectAnimator.ofFloat(main!!, "rotationY", 0f, 90f),
                ObjectAnimator.ofFloat(main!!, "alpha", 1f, 0f),
                ObjectAnimator.ofFloat(main!!, "scaleX", 1f, 0.5f),
                ObjectAnimator.ofFloat(main!!, "scaleY", 1f, 0.5f),
            )
        } else {
            animatorSet.playTogether(
                ObjectAnimator.ofFloat(main!!, "translationX", 300f, 0f),
                ObjectAnimator.ofFloat(main!!, "rotationY", 90f, 0f),
                ObjectAnimator.ofFloat(main!!, "alpha", 0f, 1f),
                ObjectAnimator.ofFloat(main!!, "scaleX", 0.5f, 1f),
                ObjectAnimator.ofFloat(main!!, "scaleY", 0.5f, 1f)
            )
        }
        animatorSet.setDuration(400)
        animatorSet.start()
    }

    override fun onResume() {
        enterAnimation(false)
        super.onResume()
    }

    override fun onPause() {
        enterAnimation(true)
        super.onPause()
    }
}
