package ru.dimon6018.metrolauncher.helpers.bsod.recovery

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import ru.dimon6018.metrolauncher.Main
import ru.dimon6018.metrolauncher.R

class Recovery: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.bsod)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.recovery)
        val restartBtn = findViewById<MaterialButton>(R.id.restartRecovery)
        val advanced = findViewById<MaterialButton>(R.id.advancedRecovery)
        restartBtn.setOnClickListener {
            val intent = Intent(this, Main::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
        advanced.setOnClickListener {
            startActivity(Intent(this, RecoveryOptions::class.java))
        }
    }
}