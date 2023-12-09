package ru.dimon6018.metrolauncher.content.settings

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import ir.alirezabdn.wp7progress.WP10ProgressBar
import ru.dimon6018.metrolauncher.R
import ru.dimon6018.metrolauncher.content.Start.Companion.db
import ru.dimon6018.metrolauncher.content.data.Prefs
import ru.dimon6018.metrolauncher.content.oobe.WelcomeActivity

class Reset : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.reset)
        val progressBar: WP10ProgressBar = findViewById(R.id.progressBar)
        progressBar.setIndicatorRadius(5)
        progressBar.showProgressBar()
        val frame: FrameLayout = findViewById(R.id.frameReset)
        ViewCompat.setOnApplyWindowInsetsListener(frame) { v: View, insets: WindowInsetsCompat ->
            val pB = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
            val tB = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            v.setPadding(0, tB, 0, pB)
            WindowInsetsCompat.CONSUMED
        }
        resetPart2()
    }
    private fun resetPart2() {
        Thread {
            db!!.clearAllTables()
            Prefs(this).reset()
            runOnUiThread {
                val intent = (Intent(this, WelcomeActivity::class.java))
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
            }
        }.start()
    }
}