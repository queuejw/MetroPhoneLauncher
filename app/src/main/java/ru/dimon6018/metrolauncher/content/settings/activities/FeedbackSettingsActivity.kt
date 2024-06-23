package ru.dimon6018.metrolauncher.content.settings.activities

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.textview.MaterialTextView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.dimon6018.metrolauncher.Application.Companion.PREFS
import ru.dimon6018.metrolauncher.R
import ru.dimon6018.metrolauncher.content.data.Prefs
import ru.dimon6018.metrolauncher.content.data.bsod.BSOD
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.applyWindowInsets
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.launcherAccentTheme

class FeedbackSettingsActivity: AppCompatActivity()  {

    private var setCrashLogLimitBtn: MaterialButton? = null
    private var main: CoordinatorLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(launcherAccentTheme())
        super.onCreate(savedInstanceState)
        setContentView(R.layout.launcher_settings_feedback)
        val db = BSOD.getData(this)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val info: MaterialButton = findViewById(R.id.showBsodInfo)
        info.setOnClickListener {
            startActivity(Intent(this, FeedbackBsodListActivity::class.java))
        }
        val delInfo: MaterialButton = findViewById(R.id.deleteBsodInfo)
        delInfo.setOnClickListener {
           lifecycleScope.launch(Dispatchers.IO) {
                db.clearAllTables()
           }.start()
        }
        val feedbackSwitch = findViewById<MaterialSwitch>(R.id.sendFeedbackSwitch)
        feedbackSwitch.setChecked(PREFS!!.isFeedbackEnabled)
        feedbackSwitch.text = if(PREFS!!.isFeedbackEnabled) getString(R.string.on) else getString(R.string.off)
        feedbackSwitch.setOnCheckedChangeListener { _, isChecked ->
            PREFS!!.setFeedback(isChecked)
            feedbackSwitch.text = if(isChecked) getString(R.string.on) else getString(R.string.off)
        }
        setCrashLogLimitBtn = findViewById(R.id.setCrashLogLimitBtn)
        val chooseBsodInfoLimitCard = findViewById<MaterialCardView>(R.id.chooseBsodInfoLimit)
        setButtonText(PREFS!!)
        setCrashLogLimitBtn!!.setOnClickListener {
            chooseBsodInfoLimitCard.visibility = View.VISIBLE
            setCrashLogLimitBtn!!.visibility = View.GONE
        }
        val save1bsod: MaterialTextView = findViewById(R.id.save1bsod)
        save1bsod.setOnClickListener {
            PREFS!!.setMaxCrashLogs(0)
            chooseBsodInfoLimitCard.visibility = View.GONE
            setCrashLogLimitBtn!!.visibility = View.VISIBLE
            setButtonText(PREFS!!)
        }
        val save5bsod: MaterialTextView = findViewById(R.id.save5bsod)
        save5bsod.setOnClickListener {
            PREFS!!.setMaxCrashLogs(1)
            chooseBsodInfoLimitCard.visibility = View.GONE
            setCrashLogLimitBtn!!.visibility = View.VISIBLE
            setButtonText(PREFS!!)
        }
        val save10bsod: MaterialTextView = findViewById(R.id.save10bsod)
        save10bsod.setOnClickListener {
            PREFS!!.setMaxCrashLogs(2)
            chooseBsodInfoLimitCard.visibility = View.GONE
            setCrashLogLimitBtn!!.visibility = View.VISIBLE
            setButtonText(PREFS!!)
        }
        val saveallbsods: MaterialTextView = findViewById(R.id.saveallbsods)
        saveallbsods.setOnClickListener {
            PREFS!!.setMaxCrashLogs(3)
            chooseBsodInfoLimitCard.visibility = View.GONE
            setCrashLogLimitBtn!!.visibility = View.VISIBLE
            setButtonText(PREFS!!)
        }
        main = findViewById(R.id.coordinator)
        main?.apply { applyWindowInsets(this) }
    }
    private fun setButtonText(prefs: Prefs) {
        setCrashLogLimitBtn!!.text = when(prefs.getMaxCrashLogs()) {
            0 -> getString(R.string.feedback_limit_0)
            1 -> getString(R.string.feedback_limit_1)
            2 -> getString(R.string.feedback_limit_2)
            3 -> getString(R.string.feedback_limit_3)
            else -> getString(R.string.feedback_limit_0)
        }
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