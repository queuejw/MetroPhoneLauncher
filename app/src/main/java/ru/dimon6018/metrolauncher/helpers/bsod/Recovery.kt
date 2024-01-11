package ru.dimon6018.metrolauncher.helpers.bsod

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.google.android.material.button.MaterialButton
import ru.dimon6018.metrolauncher.Main
import ru.dimon6018.metrolauncher.R

class Recovery: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.bsod)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.recovery)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val restartBtn = findViewById<MaterialButton>(R.id.restartRecovery)
        restartBtn.setOnClickListener {
            val intent = Intent(this, Main::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }
}