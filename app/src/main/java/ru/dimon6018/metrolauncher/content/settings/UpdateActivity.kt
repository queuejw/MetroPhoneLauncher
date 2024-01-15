package ru.dimon6018.metrolauncher.content.settings

import android.Manifest
import android.annotation.SuppressLint
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
import ru.dimon6018.metrolauncher.Application
import ru.dimon6018.metrolauncher.Application.Companion.isUpdateDownloading
import ru.dimon6018.metrolauncher.Main
import ru.dimon6018.metrolauncher.R
import ru.dimon6018.metrolauncher.content.data.Prefs
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
    private var prefs: Prefs? = null

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
        prefs = Prefs(this)
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
            prefs!!.setAutoUpdate(isChecked)
        }
        updateNotificationCheckBox!!.setOnCheckedChangeListener { _, isChecked ->
            prefs!!.setUpdateNotification(isChecked)
            if (isChecked) {
                UpdateWorker.scheduleWork(this)
            } else {
                UpdateWorker.stopWork(this)
            }
        }
        updateDetails!!.setOnClickListener {
            WPDialog(this).setTopDialog(true)
                    .setTitle("Подробности")
                    .setMessage(UpdateDataParser.updateMsg)
                    .setPositiveButton(getString(android.R.string.ok), null).show()
        }
        check!!.setOnClickListener {
            if(prefs!!.updateState == 4) {
                try {
                    val file = File(Environment.getExternalStorageDirectory().toString() + "/Download/", "MPL_update.apk")
                    val uri = FileProvider.getUriForFile(this, applicationContext.packageName + ".provider", file)
                    openFile(uri)
                } catch (e: Exception) {
                    Log.i("InstallAPK", "error: $e")
                    prefs!!.setUpdateState(5)
                    refreshUi()
                    saveError(e.toString())
                }
                return@setOnClickListener
            }
            if (prefs!!.updateState == 6) {
                Thread {
                    checkDownload()
                }.start()
            } else {
                prefs!!.setUpdateState(1)
                refreshUi()
                Thread {
                checkForUpdates()
                }.start()
            }
        }
        cancelDownload!!.setOnClickListener {
            prefs!!.setUpdateState(0)
            if(manager != null) {
                isUpdateDownloading = false
                manager!!.remove(downloadId!!)
            }
            refreshUi()
        }
    }

    override fun onStart() {
        super.onStart()
        if(prefs!!.pref.getBoolean("permsDialogUpdateScreenEnabled", true)) {
            WPDialog(this).setTopDialog(true)
                    .setTitle("Требуется разрешение")
                    .setMessage("Для работы обновлений необходимо выдать несколько разрешений. Продолжить?")
                    .setNegativeButton(getString(R.string.yes)) {
                        checkPerms()
                        return@setNegativeButton
                    }
                    .setNeutralButton(getString(R.string.hide)) {
                        hideDialog()
                        return@setNeutralButton
                    }
                    .setPositiveButton(getString(R.string.no), null).show()
        }
    }
    private fun hideDialog() {
        prefs!!.editor.putBoolean("permsDialogUpdateScreenEnabled", false).apply()
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
        autoUpdateCheckBox?.isChecked = prefs!!.isAutoUpdateEnabled
        updateNotificationCheckBox?.isChecked = prefs!!.isUpdateNotificationEnabled
        when (prefs!!.updateState) {
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
                            val progressString = getString(R.string.preparing_to_install, prefs!!.updateProgressLevel) + "%"
                            runOnUiThread {
                                progressText!!.text = progressString
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                    progressBar!!.setProgress(prefs!!.updateProgressLevel, true)
                                } else {
                                    progressBar!!.progress = prefs!!.updateProgressLevel
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
        object : Thread() {
            override fun run() {
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
        }.start()
    }
    private fun checkUpdateInfo() {
        Thread {
            if (UpdateDataParser.verCode!! >= Application.VERSION_CODE) {
                Log.i("CheckForUpdates", "up-to-date")
                prefs!!.setUpdateState(3)
                runOnUiThread {
                    refreshUi()
                }
                return@Thread
            }
            prefs!!.setUpdateState(6)
            runOnUiThread {
                progressBar!!.isIndeterminate = false
                refreshUi()
            }
        }.start()
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
        prefs!!.setUpdateState(2)
    }
    private fun saveError(e: String) {
        Thread {
            val entity = BSODEntity()
            entity.date = time.toString()
            entity.log = e
            val pos: Int
            when (prefs!!.getMaxCrashLogs()) {
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
        }.start()
    }
    @SuppressLint("Range")
    private fun downloadFile(fileName: String, url: String) {
        Thread {
            try {
                if (isFileExists("MPL_update.apk")) {
                    deleteFile("MPL_update.apk")
                }
            } catch (e: SecurityException) {
                saveError(e.toString())
                prefs!!.setUpdateState(5)
                refreshUi()
                runOnUiThread {
                    WPDialog(this).setTopDialog(true)
                            .setTitle("Требуется разрешение")
                            .setMessage("Для работы обновлений необходимо выдать несколько разрешений. Продолжить?")
                            .setNegativeButton(getString(R.string.yes)) { checkPerms() }
                            .setPositiveButton(getString(R.string.no), null).show()
                }
                return@Thread
            } catch (e: IOException) {
                saveError(e.toString())
                prefs!!.setUpdateState(5)
                refreshUi()
                runOnUiThread {
                    WPDialog(this).setTopDialog(true)
                            .setTitle("Что-то пошло не так")
                            .setMessage("Не получается продолжить процесс обновления из-за ошибки. Информация об ошибке была сохранена.")
                            .setPositiveButton(getString(android.R.string.ok), null).show()
                }
                return@Thread
            }
            prefs!!.setUpdateState(2)
            runOnUiThread {
                refreshUi()
            }
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
                    prefs!!.setUpdateState(4)
                    runOnUiThread {
                        refreshUi()
                    }
                }
                if (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_FAILED) {
                    isUpdateDownloading = false
                    prefs!!.setUpdateState(5)
                    runOnUiThread {
                        refreshUi()
                    }
                }
                val progress: Int = ((downloaded * 100L / total)).toInt()
                val progressString = getString(R.string.preparing_to_install, progress) + "%"
                Log.i("download", "current: $progress")
                prefs!!.setUpdateProgressLevel(progress)
                runOnUiThread {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        progressBar!!.setProgress(prefs!!.updateProgressLevel, true)
                    } else {
                        progressBar!!.progress = prefs!!.updateProgressLevel
                    }
                    progressText!!.text = progressString
                }
                cursor.close()
            }
        }.start()
    }
    private fun isFileExists(filename: String): Boolean {
        val folder1 = File(Environment.DIRECTORY_DOWNLOADS + filename)
        return folder1.exists()
    }
    override fun deleteFile(filename: String): Boolean {
        val folder1 = File(Environment.DIRECTORY_DOWNLOADS + filename)
        return folder1.delete()
    }
    private fun getMimeType(uri: Uri): String? {
        val resolver = contentResolver
        return resolver.getType(uri)
    }
    private fun openFile(fileUri: Uri) {
        try {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.setDataAndType(fileUri, getMimeType(fileUri))
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            startActivity(intent)
        } catch (e: Exception) {
            Log.e("instalAPK", e.toString())
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
                Log.i("CheckForUpdates-Background", "up-to-date")
                false
            } else {
                Log.i("CheckForUpdates-Background", "Uodate Available")
                true
            }
            return boolean
        }
    }
}