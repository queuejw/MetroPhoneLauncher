package ru.dimon6018.metrolauncher.helpers.bsod

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import ru.dimon6018.metrolauncher.BuildConfig
import ru.dimon6018.metrolauncher.Main
import ru.dimon6018.metrolauncher.R
import ru.dimon6018.metrolauncher.content.data.Prefs

class BsodScreen : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.bsod)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.bsod)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val layout: ConstraintLayout = findViewById(R.id.bsodLayout)
        ViewCompat.setOnApplyWindowInsetsListener(layout) { view, insets ->
            val paddingBottom = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
            val paddingTop = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            view.setPadding(0, paddingTop, 0, paddingBottom)
            WindowInsetsCompat.CONSUMED
        }
        val errorSmile: TextView = findViewById(R.id.bsodSmile)
        errorSmile.text = getString(R.string.bsod)
    }

    override fun onStart() {
        super.onStart()
        var errorText: TextView
        val context = this
        object : Thread() {
            override fun run() {
                try {
                    sleep(5000)
                    val model = "Model: " + Build.MODEL + "\n"
                    val name = "MPL Ver: " + BuildConfig.VERSION_NAME + "\n"
                    val code = intent.extras?.getString("errorCode")
                    val errCode = "\nIf vou call a support person. aive them this info:\n" +
                            "Stop code: $code"
                    val error = "Your launcher ran into a problem and needs to restart. We're just\n" +
                            "collecting some error info, and then we'll restart for you.\n " + model + name + intent.extras?.getString("stacktrace") + errCode
                    Log.e("BSOD", error)
                    Prefs(context).editor.putString("crash_report", error).apply()
                    Prefs(context).editor.putBoolean("app_crashed", true).apply()
                    runOnUiThread {
                        errorText = findViewById(R.id.bsodLog)
                        errorText.text = error
                    }
                } catch (e: InterruptedException) {
                    Log.e("BSOD", e.toString())
                }
            }
        }.start()
        object : Thread() {
            override fun run() {
                try {
                    sleep(15000)
                    runOnUiThread {
                        restartApplication()
                    }
                } catch (e: InterruptedException) {
                    Log.e("BSOD", e.toString())
                }
            }
        }.start()
    }
    private fun restartApplication() {
        val intent = Intent(this, Main::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }
}