package ru.dimon6018.metrolauncher.content.settings

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.checkbox.MaterialCheckBox
import ru.dimon6018.metrolauncher.BuildConfig
import ru.dimon6018.metrolauncher.Main
import ru.dimon6018.metrolauncher.R
import ru.dimon6018.metrolauncher.content.data.Prefs

class UpdateActivity: AppCompatActivity() {
    private var check: MaterialButton? = null
    private var checkingSub: TextView? = null
    private var autoUpdate: MaterialCheckBox? = null
    private var prefs: Prefs? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.launcher_settings_updates)
        val coord = findViewById<CoordinatorLayout>(R.id.coordinator)
        Main.applyWindowInsets(coord)
        prefs = Prefs(this)
        check = findViewById(R.id.checkForUpdatesBtn)
        checkingSub = findViewById(R.id.cheking_updates_sub)
        autoUpdate = findViewById(R.id.AutoUpdateCheckBox)
        refreshUi()
        autoUpdate!!.setOnCheckedChangeListener { _, isChecked ->
            prefs!!.setAutoUpdate(isChecked)
        }
        check!!.setOnClickListener {
            prefs!!.setUpdateCheck(true)
            refreshUi()
            checkForUpdates()
        }
    }
    private fun refreshUi() {
        autoUpdate!!.isChecked = prefs!!.isAutoUpdateEnabled
        if(prefs!!.isUpdateCheck) {
            check!!.visibility = View.GONE
            checkingSub!!.visibility = View.VISIBLE
        } else {
            check!!.visibility = View.VISIBLE
            checkingSub!!.visibility = View.GONE
        }
    }
    private fun checkForUpdates() {
        object : Thread() {
            override fun run() {
                try {
                    sleep(5000)
                    prefs!!.setUpdateCheck(false)
                    runOnUiThread {
                        refreshUi()
                    }
                } catch (e: InterruptedException) {
                    Log.e("CheckForUpdates", e.toString())
                }
            }
        }.start()
    }
}