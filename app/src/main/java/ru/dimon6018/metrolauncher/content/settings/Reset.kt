package ru.dimon6018.metrolauncher.content.settings

import android.content.Intent
import android.os.Bundle
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.dimon6018.metrolauncher.Application
import ru.dimon6018.metrolauncher.Main
import ru.dimon6018.metrolauncher.R
import ru.dimon6018.metrolauncher.content.data.AppData
import ru.dimon6018.metrolauncher.content.data.Prefs
import ru.dimon6018.metrolauncher.content.data.bsod.BSOD
import ru.dimon6018.metrolauncher.content.oobe.WelcomeActivity

class Reset : AppCompatActivity() {

    private var frame: FrameLayout? = null
    private var dbApps: AppData? = null
    private var dbBsod: BSOD? = null
    private var intent: Intent? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.reset)
        dbApps = AppData.getAppData(this)
        dbBsod = BSOD.getData(this)
        frame = findViewById(R.id.frameReset)
        Main.applyWindowInsets(frame!!)
        intent = Intent(this, WelcomeActivity::class.java)
        resetPart2()
    }
    private fun resetPart2() {
        CoroutineScope(Dispatchers.Default).launch {
            dbApps!!.clearAllTables()
            dbBsod!!.clearAllTables()
            Prefs(this@Reset).reset()
        }.invokeOnCompletion {
            val oobe = Runnable {
                Application.PREFS!!.setLauncherState(0)
                intent!!.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                finishAffinity()
                startActivity(intent)
            }
            frame!!.postDelayed(oobe, 3000)
        }
    }
}