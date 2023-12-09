package ru.dimon6018.metrolauncher.content.oobe

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import ru.dimon6018.metrolauncher.R

class WelcomeActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.oobe_part1)
    }
}