package ru.dimon6018.metrolauncher.content.settings

import android.content.Intent
import android.os.Bundle
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.dimon6018.metrolauncher.Application.Companion.PREFS
import ru.dimon6018.metrolauncher.R
import ru.dimon6018.metrolauncher.content.data.tile.TileData
import ru.dimon6018.metrolauncher.content.data.bsod.BSOD
import ru.dimon6018.metrolauncher.content.oobe.WelcomeActivity
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.applyWindowInsets
import kotlin.system.exitProcess

class Reset : AppCompatActivity() {

    private var dbApps: TileData? = null
    private var dbBsod: BSOD? = null
    private var intent: Intent? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.reset)
        dbApps = TileData.getTileData(this)
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
            PREFS!!.reset()
            PREFS!!.setLauncherState(0)
            delay(3000)
        }.invokeOnCompletion {
            exitProcess(0)
        }
    }
}