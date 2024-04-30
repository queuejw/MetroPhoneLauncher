package ru.dimon6018.metrolauncher.helpers.bsod.recovery

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.card.MaterialCardView
import ru.dimon6018.metrolauncher.Application.Companion.PREFS
import ru.dimon6018.metrolauncher.R
import ru.dimon6018.metrolauncher.content.settings.SettingsActivity
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.downloadUpdate

class RecoveryOptions: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.bsod)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.recovery_options)
        val refresh: MaterialCardView = findViewById(R.id.refresh)
        val browser: MaterialCardView = findViewById(R.id.browser)
        val sysSett: MaterialCardView = findViewById(R.id.settingsSys)
        val lnhSett: MaterialCardView = findViewById(R.id.settingsLnch)
        lnhSett.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
        sysSett.setOnClickListener {
            startActivity(Intent(Settings.ACTION_SETTINGS))
        }
        browser.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://google.com/")))
        }
        refresh.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (!packageManager.canRequestPackageInstalls()) {
                    startActivityForResult(Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES)
                            .setData(Uri.parse(String.format("package:%s", packageName))), 1)
                }
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.MANAGE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.MANAGE_EXTERNAL_STORAGE), 1)
            }
            PREFS!!.reset()
            downloadUpdate(this)
        }
    }
}