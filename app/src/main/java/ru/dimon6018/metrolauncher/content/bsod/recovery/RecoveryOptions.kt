package ru.dimon6018.metrolauncher.content.bsod.recovery

import android.Manifest
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import ru.dimon6018.metrolauncher.Application.Companion.PREFS
import ru.dimon6018.metrolauncher.R
import ru.dimon6018.metrolauncher.content.settings.SettingsActivity
import ru.dimon6018.metrolauncher.databinding.RecoveryOptionsScreenBinding
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.downloadUpdate

class RecoveryOptions : AppCompatActivity() {

    private lateinit var binding: RecoveryOptionsScreenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = RecoveryOptionsScreenBinding.inflate(layoutInflater)
        setTheme(R.style.bsod)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.launcherSettingsCard.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
        binding.systemSettingsCard.setOnClickListener {
            startActivity(Intent(Settings.ACTION_SETTINGS))
        }
        binding.openBrowserCard.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://google.com/")))
        }
        binding.launcherRefreshCard.setOnClickListener {
            if (checkStoragePermissions()) {
                if (areNotificationsEnabled(NotificationManagerCompat.from(this))) {
                    PREFS.reset()
                    downloadUpdate(this)
                } else {
                    Toast.makeText(
                        this,
                        getString(R.string.allow_notifications_recovery_error),
                        Toast.LENGTH_LONG
                    ).show()
                    openSettings()
                }
            } else {
                getPermission()
            }
        }
    }

    private fun openSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", packageName, null)
        intent.setData(uri)
        startActivity(intent)
    }

    private fun areNotificationsEnabled(noman: NotificationManagerCompat) = when {
        noman.areNotificationsEnabled().not() -> false
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
            noman.notificationChannels.firstOrNull { channel ->
                channel.importance == NotificationManager.IMPORTANCE_NONE
            } == null
        }

        else -> true
    }

    private fun checkStoragePermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            val write =
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            val read =
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            read == PackageManager.PERMISSION_GRANTED && write == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun getPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).setData(
                Uri.parse(String.format("package:%s", packageName))
            )
            startActivity(intent)
        } else {
            ActivityCompat.requestPermissions(
                this, arrayOf(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ),
                1507
            )
        }
    }
}