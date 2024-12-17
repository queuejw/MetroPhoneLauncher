package ru.queuejw.mpl.content.bsod

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
import ru.queuejw.mpl.Application.Companion.PREFS
import ru.queuejw.mpl.Main
import ru.queuejw.mpl.R
import ru.queuejw.mpl.content.bsod.recovery.Recovery
import ru.queuejw.mpl.content.data.bsod.BSOD
import ru.queuejw.mpl.helpers.utils.Utils

class BsodScreen : AppCompatActivity() {

    private var db: BSOD? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        var counter = PREFS.prefs.getInt("crashCounter", 0)
        counter += 1
        PREFS.prefs.edit().putBoolean("app_crashed", true).apply()
        PREFS.prefs.edit().putInt("crashCounter", counter).apply()
        setTheme(R.style.bsod)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.bsod_screen)
        CoroutineScope(Dispatchers.IO).launch {
            db = BSOD.getData(this@BsodScreen)
            val model = "\nModel: ${Utils.MODEL}\n"
            val brand = "Brand: ${Utils.BRAND}\n"
            val mplVerCode = "MPL Ver Code: ${Utils.VERSION_CODE}\n"
            val mplVerName = "MPL Ver: ${Utils.VERSION_NAME}\n"
            val android = "Android Version: ${Utils.ANDROID_VERSION}\n\n"
            val code = intent.extras?.getString("errorCode")
            val errCode = "\nIf vou call a support person. aive them this info:\n" +
                    "Stop code: $code"
            val error =
                "Your launcher ran into a problem and needs to restart. We're just collecting some error info, and then we'll restart for you.\n $model$brand$android$mplVerCode$mplVerName" + intent.extras?.getString(
                    "stacktrace"
                ) + errCode
            Log.e("BSOD", error)
            Utils.saveError(error, db!!)
            if (PREFS.bsodOutputEnabled) {
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
        Utils.applyWindowInsets(layout)

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