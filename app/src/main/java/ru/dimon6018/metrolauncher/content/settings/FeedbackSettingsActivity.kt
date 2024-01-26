package ru.dimon6018.metrolauncher.content.settings

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.WindowCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.materialswitch.MaterialSwitch
import ru.dimon6018.metrolauncher.Application
import ru.dimon6018.metrolauncher.Application.Companion.PREFS
import ru.dimon6018.metrolauncher.Main
import ru.dimon6018.metrolauncher.R
import ru.dimon6018.metrolauncher.content.data.Prefs
import ru.dimon6018.metrolauncher.content.data.bsod.BSOD

class FeedbackSettingsActivity: AppCompatActivity()  {

    private var setCrashLogLimitBtn: MaterialButton? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(Application.launcherAccentTheme())
        super.onCreate(savedInstanceState)
        setContentView(R.layout.launcher_settings_feedback)
        val db = BSOD.getData(this)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val coord: CoordinatorLayout = findViewById(R.id.coordinator)
        Main.applyWindowInsets(coord)
        val info: MaterialButton = findViewById(R.id.showBsodInfo)
        info.setOnClickListener {
            startActivity(Intent(this, FeedbackBsodListActivity::class.java))
        }
        val delInfo: MaterialButton = findViewById(R.id.deleteBsodInfo)
        delInfo.setOnClickListener {
           Thread {
                db.clearAllTables()
           }.start()
        }
        val feedbackSwitch = findViewById<MaterialSwitch>(R.id.sendFeedbackSwitch)
        feedbackSwitch.setChecked(PREFS!!.isFeedbackEnabled)
        feedbackSwitch.text = if(PREFS!!.isFeedbackEnabled) getString(R.string.on) else getString(R.string.off)
        feedbackSwitch.setOnCheckedChangeListener { _, isChecked ->
            PREFS!!.setFeedback(isChecked)
            feedbackSwitch.text = if(isChecked) getString(R.string.on) else getString(R.string.off)
            feedbackSwitch.setChecked(isChecked)
        }
        setCrashLogLimitBtn = findViewById(R.id.setCrashLogLimitBtn)
        val chooseBsodInfoLimitCard = findViewById<MaterialCardView>(R.id.chooseBsodInfoLimit)
        setButtonText(PREFS!!)
        setCrashLogLimitBtn!!.setOnClickListener {
            chooseBsodInfoLimitCard.visibility = View.VISIBLE
            setCrashLogLimitBtn!!.visibility = View.GONE
        }
        val save1bsod: TextView = findViewById(R.id.save1bsod)
        save1bsod.setOnClickListener {
            PREFS!!.setMaxCrashLogs(0)
            chooseBsodInfoLimitCard.visibility = View.GONE
            setCrashLogLimitBtn!!.visibility = View.VISIBLE
            setButtonText(PREFS!!)
        }
        val save5bsod: TextView = findViewById(R.id.save5bsod)
        save5bsod.setOnClickListener {
            PREFS!!.setMaxCrashLogs(1)
            chooseBsodInfoLimitCard.visibility = View.GONE
            setCrashLogLimitBtn!!.visibility = View.VISIBLE
            setButtonText(PREFS!!)
        }
        val save10bsod: TextView = findViewById(R.id.save10bsod)
        save10bsod.setOnClickListener {
            PREFS!!.setMaxCrashLogs(2)
            chooseBsodInfoLimitCard.visibility = View.GONE
            setCrashLogLimitBtn!!.visibility = View.VISIBLE
            setButtonText(PREFS!!)
        }
        val saveallbsods: TextView = findViewById(R.id.saveallbsods)
        saveallbsods.setOnClickListener {
            PREFS!!.setMaxCrashLogs(3)
            chooseBsodInfoLimitCard.visibility = View.GONE
            setCrashLogLimitBtn!!.visibility = View.VISIBLE
            setButtonText(PREFS!!)
        }
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
}