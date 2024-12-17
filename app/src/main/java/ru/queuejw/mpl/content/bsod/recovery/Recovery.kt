package ru.queuejw.mpl.content.bsod.recovery

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import ru.queuejw.mpl.Main
import ru.queuejw.mpl.R
import ru.queuejw.mpl.databinding.RecoveryMainScreenBinding

class Recovery : AppCompatActivity() {

    private lateinit var binding: RecoveryMainScreenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = RecoveryMainScreenBinding.inflate(layoutInflater)
        setTheme(R.style.bsod)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.restartButton.setOnClickListener {
            val intent = Intent(this, Main::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
        binding.advancedOptionsButton.setOnClickListener {
            startActivity(Intent(this, RecoveryOptions::class.java))
        }
    }
}