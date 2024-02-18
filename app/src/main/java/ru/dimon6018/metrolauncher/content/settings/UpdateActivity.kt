package ru.dimon6018.metrolauncher.content.settings

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.DownloadManager
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.google.android.material.button.MaterialButton
import com.google.android.material.checkbox.MaterialCheckBox
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.dimon6018.metrolauncher.Application
import ru.dimon6018.metrolauncher.Application.Companion.PREFS
import ru.dimon6018.metrolauncher.Application.Companion.isUpdateDownloading
import ru.dimon6018.metrolauncher.Main
import ru.dimon6018.metrolauncher.R
import ru.dimon6018.metrolauncher.content.data.bsod.BSOD
import ru.dimon6018.metrolauncher.content.data.bsod.BSODEntity
import ru.dimon6018.metrolauncher.helpers.WPDialog
import ru.dimon6018.metrolauncher.helpers.update.UpdateDataParser
import ru.dimon6018.metrolauncher.helpers.update.UpdateWorker
import java.io.File
import java.io.IOException
import java.net.URL
import java.util.Calendar
import java.util.Date


class UpdateActivity: AppCompatActivity() {
    private var check: MaterialButton? = null
    private var checkingSub: TextView? = null
    private var autoUpdateCheckBox: MaterialCheckBox? = null
    private var updateNotificationCheckBox: MaterialCheckBox? = null

    private var progressLayout: LinearLayout? = null
    private var progressText: TextView? = null
    private var cancelDownload: TextView? = null
    private var updateDetails: TextView? = null
    private var progressBar: ProgressBar? = null

    private val time: Date = Calendar.getInstance().time

    private var db: BSOD? = null
    private var manager: DownloadManager? = null
    private var downloadId: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.launcher_settings_updates)
        val coord = findViewById<CoordinatorLayout>(R.id.coordinator)
        Main.applyWindowInsets(coord)
        db = BSOD.getData(this)
        progressLayout = findViewById(R.id.updateIndicator)
        progressText = findViewById(R.id.progessText)
        progressBar = findViewById(R.id.progress)
        check = findViewById(R.id.checkForUpdatesBtn)
        checkingSub = findViewById(R.id.cheking_updates_sub)
        autoUpdateCheckBox = findViewById(R.id.AutoUpdateCheckBox)
        updateNotificationCheckBox = findViewById(R.id.UpdateNotifyCheckBox)
        updateDetails = findViewById(R.id.updateInfo)
        cancelDownload = findViewById(R.id.cancel_button)
        refreshUi()
        autoUpdateCheckBox!!.setOnCheckedChangeListener { _, isChecked ->
            PREFS!!.setAutoUpdate(isChecked)
        }
        updateNotificationCheckBox!!.setOnCheckedChangeListener { _, isChecked ->
            PREFS!!.setUpdateNotification(isChecked)
            if (isChecked) {
                UpdateWorker.scheduleWork(this)
            } else {
                UpdateWorker.stopWork(this)
            }
        }
        updateDetails!!.setOnClickListener {
            WPDialog(this).setTopDialog(true)
                    .setTitle(getString(R.string.details))
                    .setMessage(getUpdateMessage())
                    .setPositiveButton(getString(android.R.string.ok), null).show()
        }
        check!!.setOnClickListener {
            if(PREFS!!.updateState == 4) {
                try {
                    val file = File(Environment.getExternalStorageDirectory().toString() + "/Download/", "MPL_update.apk")
                    val uri = FileProvider.getUriForFile(this, applicationContext.packageName + ".provider", file)
                    PREFS!!.setVersionCode(UpdateDataParser.verCode!!)
                    openFile(uri, this)
                } catch (e: Exception) {
                    Log.i("InstallAPK", "error: $e")
                    PREFS!!.setUpdateState(5)
                    refreshUi()
                    saveError(e.toString())
                }
                return@setOnClickListener
            }
            else if (PREFS!!.updateState == 7) {
                CoroutineScope(Dispatchers.IO).launch {
                    checkDownload()
                }
            }
            else if (PREFS!!.updateState == 6) {
                CoroutineScope(Dispatchers.IO).launch {
                    checkDownload()
                }
            } else {
                PREFS!!.setUpdateState(1)
                refreshUi()
                CoroutineScope(Dispatchers.IO).launch {
                checkForUpdates()
                }
            }
        }
        cancelDownload!!.setOnClickListener {
            PREFS!!.setUpdateState(0)
            if(manager != null) {
                isUpdateDownloading = false
                manager!!.remove(downloadId!!)
                try {
                    val file = File(Environment.getExternalStorageDirectory().toString() + "/Download/", "MPL_update.apk")
                    if (file.exists()) {
                        file.delete()
                    }
                } catch (e: IOException) {
                    saveError(e.toString())
                    refreshUi()
                }
            }
            refreshUi()
        }
    }
    private fun getUpdateMessage(): String {
        return if(UpdateDataParser.updateMsg == null) {
            PREFS!!.updateMessage
        } else {
            UpdateDataParser.updateMsg!!
        }
    }
    override fun onStart() {
        super.onStart()
        if(PREFS!!.pref.getBoolean("permsDialogUpdateScreenEnabled", true)) {
            WPDialog(this).setTopDialog(true)
                    .setTitle(getString(R.string.perms_req))
                    .setMessage(getString(R.string.perms_req_tip))
                    .setNegativeButton(getString(R.string.yes)) {
                        checkPerms()
                        WPDialog(this).dismiss()
                        return@setNegativeButton
                    }
                    .setNeutralButton(getString(R.string.hide)) {
                        WPDialog(this).dismiss()
                        hideDialog()
                    }
                    .setPositiveButton(getString(R.string.no), null).show()
        }
    }
    private fun hideDialog() {
        PREFS!!.editor.putBoolean("permsDialogUpdateScreenEnabled", false).apply()
    }
    private fun checkPerms() {
        hideDialog()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!packageManager.canRequestPackageInstalls()) {
                startActivityForResult(Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES)
                        .setData(Uri.parse(String.format("package:%s", packageName))), 1)
            }
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.MANAGE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.MANAGE_EXTERNAL_STORAGE), 1)
        }
    }
    private fun refreshUi() {
        autoUpdateCheckBox?.isChecked = PREFS!!.isAutoUpdateEnabled
        updateNotificationCheckBox?.isChecked = PREFS!!.isUpdateNotificationEnabled
        when (PREFS!!.updateState) {
            1 -> {
                check!!.visibility = View.GONE
                checkingSub!!.visibility = View.VISIBLE
                progressLayout!!.visibility = View.GONE
                checkingSub!!.text = getString(R.string.checking_for_updates)
                updateDetails!!.visibility = View.GONE
            }
            2 -> {
                checkingSub!!.visibility = View.GONE
                check!!.visibility = View.GONE
                progressLayout!!.visibility = View.VISIBLE
                if(isUpdateDownloading) {
                    Thread {
                        while (isUpdateDownloading) {
                            val progressString = getString(R.string.preparing_to_install, PREFS!!.updateProgressLevel) + "%"
                            runOnUiThread {
                                progressText!!.text = progressString
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                    progressBar!!.setProgress(PREFS!!.updateProgressLevel, true)
                                } else {
                                    progressBar!!.progress = PREFS!!.updateProgressLevel
                                }
                            }
                        }
                    }.start()
                } else {
                    val progressString = getString(R.string.preparing_to_install, 0) + "%"
                    progressText!!.text = progressString
                }
                updateDetails!!.visibility = View.GONE
            }
            3 -> {
                checkingSub!!.visibility = View.VISIBLE
                checkingSub!!.text = getString(R.string.up_to_date)
                check!!.visibility = View.VISIBLE
                check!!.text = getString(R.string.check_for_updates)
                progressLayout!!.visibility = View.GONE
                updateDetails!!.visibility = View.GONE
            }
            4 -> {
                checkingSub!!.visibility = View.VISIBLE
                checkingSub!!.text = getString(R.string.ready_to_install)
                check!!.visibility = View.VISIBLE
                check!!.text = getString(R.string.install)
                progressLayout!!.visibility = View.GONE
                updateDetails!!.visibility = View.VISIBLE
            }
            5 -> {
                checkingSub!!.visibility = View.VISIBLE
                checkingSub!!.text = getString(R.string.update_failed)
                check!!.visibility = View.VISIBLE
                progressLayout!!.visibility = View.GONE
                check!!.text = getString(R.string.retry)
                updateDetails!!.visibility = View.GONE
            }
            6 -> {
                checkingSub!!.visibility = View.VISIBLE
                checkingSub!!.text = getString(R.string.ready_to_download)
                check!!.visibility = View.VISIBLE
                progressLayout!!.visibility = View.GONE
                check!!.text = getString(R.string.download)
                updateDetails!!.visibility = View.VISIBLE
            }
            7 -> {
                checkingSub!!.visibility = View.VISIBLE
                checkingSub!!.text = getString(R.string.ready_to_download_beta)
                check!!.visibility = View.VISIBLE
                progressLayout!!.visibility = View.GONE
                check!!.text = getString(R.string.download_beta)
                updateDetails!!.visibility = View.VISIBLE
            }
            0 -> {
                check!!.visibility = View.VISIBLE
                check!!.text = getString(R.string.check_for_updates)
                checkingSub!!.visibility = View.GONE
                progressLayout!!.visibility = View.GONE
                updateDetails!!.visibility = View.GONE
            }
        }
    }

    private fun checkForUpdates() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                downloadXml(URL)
                checkUpdateInfo()
                runOnUiThread {
                    refreshUi()
                }
            } catch (e: Exception) {
                Log.e("CheckForUpdates", e.toString())
            }
        }
    }
    private fun checkUpdateInfo() {
        CoroutineScope(Dispatchers.IO).launch {
            if (UpdateDataParser.verCode!! == Application.VERSION_CODE) {
                Log.i("CheckForUpdates", "up-to-date")
                PREFS!!.setUpdateState(3)
                runOnUiThread {
                    refreshUi()
                }
            } else {
                if (UpdateDataParser.isBeta == true) PREFS!!.setUpdateState(7) else PREFS!!.setUpdateState(3)
                runOnUiThread {
                    progressBar!!.isIndeterminate = false
                    refreshUi()
                }
            }
        }
    }
    private fun checkDownload() {
        if (UpdateDataParser.isBeta == true) {
            Log.i("CheckForUpdates", "download beta")
            runOnUiThread {
                refreshUi()
                downloadFile("MPL Beta", URL_BETA)
            }
        } else {
            Log.i("CheckForUpdates", "download release")
            runOnUiThread {
                refreshUi()
                downloadFile("MPL", URL_RELEASE)
            }
        }
    }
    private fun saveError(e: String) {
        CoroutineScope(Dispatchers.IO).launch {
            Log.e("UpdateService", e)
            val entity = BSODEntity()
            entity.date = time.toString()
            entity.log = e
            val pos: Int
            when (PREFS!!.getMaxCrashLogs()) {
                0 -> {
                    db!!.clearAllTables()
                    pos = db!!.getDao().getBsodList().size
                }
                1 -> {
                    if (db!!.getDao().getBsodList().size >= 5) {
                        db!!.clearAllTables()
                    }
                    pos = db!!.getDao().getBsodList().size
                }
                2 -> {
                    if (db!!.getDao().getBsodList().size >= 10) {
                        db!!.clearAllTables()
                    }
                    pos = db!!.getDao().getBsodList().size
                }
                3 -> {
                    pos = db!!.getDao().getBsodList().size
                }
                else -> {
                    pos = db!!.getDao().getBsodList().size
                }
            }
            entity.pos = pos
            db!!.getDao().insertLog(entity)
        }
    }
    @SuppressLint("Range")
    private fun downloadFile(fileName: String, url: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val file = File(Environment.getExternalStorageDirectory().toString() + "/Download/", "MPL_update.apk")
                if (file.exists()) {
                    file.delete()
                }
            } catch (e: IOException) {
                saveError(e.toString())
                PREFS!!.setUpdateState(5)
                refreshUi()
                runOnUiThread {
                    WPDialog(this@UpdateActivity).setTopDialog(true)
                            .setTitle(getString(R.string.error))
                            .setMessage("Не получается продолжить процесс обновления из-за ошибки. Информация об ошибке была сохранена.")
                            .setPositiveButton(getString(android.R.string.ok), null).show()
                }
                return@launch
            }
            PREFS!!.setUpdateState(2)
            runOnUiThread {
                refreshUi()
            }
            try {
                val request = DownloadManager.Request(Uri.parse(url))
                request.setDescription(getString(R.string.update_notification))
                request.setTitle(fileName)
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN)
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "MPL_update.apk")
                manager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
                downloadId = manager!!.enqueue(request)
                isUpdateDownloading = true
                val q = DownloadManager.Query()
                q.setFilterById(downloadId!!)
                while (isUpdateDownloading) {
                    val cursor = manager!!.query(q)
                    cursor.moveToFirst()
                    val downloaded = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                    val total = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                    if (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_SUCCESSFUL) {
                        isUpdateDownloading = false
                        PREFS!!.setUpdateState(4)
                        runOnUiThread {
                            refreshUi()
                        }
                    }
                    if (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_FAILED) {
                        isUpdateDownloading = false
                        PREFS!!.setUpdateState(5)
                        runOnUiThread {
                            refreshUi()
                        }
                    }
                    val progress: Int = ((downloaded * 100L / total)).toInt()
                    val progressString = getString(R.string.preparing_to_install, progress) + "%"
                    Log.i("download", "current: $progress")
                    PREFS!!.setUpdateProgressLevel(progress)
                    runOnUiThread {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            progressBar!!.setProgress(PREFS!!.updateProgressLevel, true)
                        } else {
                            progressBar!!.progress = PREFS!!.updateProgressLevel
                        }
                        progressText!!.text = progressString
                    }
                    cursor.close()
                }
            } catch (e: Exception) {
                saveError(e.toString())
                downloadId?.let { manager?.remove(it) }
                isUpdateDownloading = false
                PREFS!!.setUpdateState(5)
                runOnUiThread {
                    refreshUi()
                }
            }
        }
    }
    companion object {
        const val URL: String = "https://raw.githubusercontent.com/queuejw/mpl_updates/main/update.xml"
        const val URL_BETA: String = "https://github.com/queuejw/mpl_updates/releases/download/beta/MPL-beta.apk"
        const val URL_RELEASE: String = "https://github.com/queuejw/mpl_updates/releases/download/release/MPL.apk"

        fun downloadXml(link: String) {
            Log.i("CheckForUpdates", "download xml")
            try {
                URL(link).openStream().use { input ->
                    Log.i("CheckForUpdates", "start parsing")
                    val parser = UpdateDataParser()
                    parser.parse(input)
                    input.close()
                }
            } catch (e: Exception) {
                Log.i("CheckForUpdates", "something went wrong: $e")
            }
        }
        fun isUpdateAvailable(): Boolean {
            val boolean: Boolean = if (UpdateDataParser.verCode == Application.VERSION_CODE) {
                Log.i("CheckForUpdates", "up-to-date")
                false
            } else {
                Log.i("CheckForUpdates", "Update Available")
                true
            }
            return boolean
        }
        fun openFile(fileUri: Uri, activity: Activity) {
            try {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.setDataAndType(fileUri, activity.contentResolver.getType(fileUri))
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                activity.startActivity(intent)
            } catch (e: Exception) {
                Log.e("installAPK", e.toString())
            }
        }
    }
}