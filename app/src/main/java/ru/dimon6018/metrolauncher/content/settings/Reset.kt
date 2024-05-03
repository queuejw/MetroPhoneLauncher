package ru.dimon6018.metrolauncher.content.settings

import android.content.Intent
import android.os.Bundle
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.dimon6018.metrolauncher.Application
import ru.dimon6018.metrolauncher.R
import ru.dimon6018.metrolauncher.content.data.Prefs
import ru.dimon6018.metrolauncher.content.data.apps.AppData
import ru.dimon6018.metrolauncher.content.data.bsod.BSOD
import ru.dimon6018.metrolauncher.content.oobe.WelcomeActivity
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.applyWindowInsets

class Reset : AppCompatActivity() {

    private var dbApps: AppData? = null
    private var dbBsod: BSOD? = null
    private var intent: Intent? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.reset)
        dbApps = AppData.getAppData(this)
        dbBsod = BSOD.getData(this)
        val frame = findViewById<FrameLayout>(R.id.frameReset)
        applyWindowInsets(frame)
        intent = Intent(this, WelcomeActivity::class.java)
        resetPart2()
    }
    private fun resetPart2() {
        lifecycleScope.launch(Dispatchers.IO) {
            dbApps!!.clearAllTables()
            dbBsod!!.clearAllTables()
            Prefs(this@Reset).reset()
            delay(3000)
        }.invokeOnCompletion {
            Application.PREFS!!.setLauncherState(0)
            intent!!.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            finishAffinity()
            startActivity(intent)
        }
    }
}