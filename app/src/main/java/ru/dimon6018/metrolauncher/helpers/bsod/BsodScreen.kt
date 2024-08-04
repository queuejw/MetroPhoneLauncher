package ru.dimon6018.metrolauncher.helpers.bsod

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.WindowCompat
import com.google.android.material.textview.MaterialTextView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.dimon6018.metrolauncher.Application.Companion.PREFS
import ru.dimon6018.metrolauncher.Main
import ru.dimon6018.metrolauncher.R
import ru.dimon6018.metrolauncher.content.data.bsod.BSOD
import ru.dimon6018.metrolauncher.helpers.bsod.recovery.Recovery
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.ANDROID_VERSION
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.BRAND
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.MODEL
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.VERSION_NAME
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.applyWindowInsets
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.saveError

class BsodScreen : AppCompatActivity() {

    private var db: BSOD? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        var counter = PREFS!!.prefs.getInt("crashCounter", 0)
        counter += 1
        PREFS!!.prefs.edit().putBoolean("app_crashed", true).apply()
        PREFS!!.prefs.edit().putInt("crashCounter", counter).apply()
        setTheme(R.style.bsod)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.bsod)
        CoroutineScope(Dispatchers.IO).launch {
            db = BSOD.getData(this@BsodScreen)
            val model = "Model: $MODEL\n"
            val brand = "Brand: $BRAND\n"
            val name = "MPL Ver: $VERSION_NAME\n"
            val android = "Android Version: $ANDROID_VERSION\n"
            val code = intent.extras?.getString("errorCode")
            val errCode = "\nIf vou call a support person. aive them this info:\n" +
                    "Stop code: $code"
            val error = "Your launcher ran into a problem and needs to restart. We're just collecting some error info, and then we'll restart for you.\n " + model + brand + android + name + intent.extras?.getString("stacktrace") + errCode
            Log.e("BSOD", error)
            saveError(error, db!!)
            if(PREFS!!.bsodOutputEnabled) {
                withContext(Dispatchers.Main) {
                    val errorTextView = findViewById<MaterialTextView>(R.id.bsodDetailsText)
                    errorTextView.text = error
                }
            }
        }
        if (counter >= 3) {
            val intent = Intent(this, Recovery::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.putExtra("stacktrace", intent.extras?.getString("stacktrace"))
            finishAffinity()
            startActivity(intent)
        }
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val layout: ConstraintLayout = findViewById(R.id.bsodLayout)
        applyWindowInsets(layout)

    }
    override fun onStart() {
        super.onStart()
        Handler(Looper.getMainLooper()).postDelayed({ restartApplication() }, 3000)
    }
    private fun restartApplication() {
        val intent = Intent(this, Main::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }
}