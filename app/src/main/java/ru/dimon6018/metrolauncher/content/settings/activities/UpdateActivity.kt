package ru.dimon6018.metrolauncher.content.settings.activities

import android.Manifest
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.widget.ContentLoadingProgressBar
import com.google.android.material.button.MaterialButton
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.textview.MaterialTextView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.dimon6018.metrolauncher.Application.Companion.PREFS
import ru.dimon6018.metrolauncher.Application.Companion.isUpdateDownloading
import ru.dimon6018.metrolauncher.BuildConfig
import ru.dimon6018.metrolauncher.R
import ru.dimon6018.metrolauncher.content.data.bsod.BSOD
import ru.dimon6018.metrolauncher.helpers.WPDialog
import ru.dimon6018.metrolauncher.helpers.update.UpdateDataParser
import ru.dimon6018.metrolauncher.helpers.update.UpdateWorker
import ru.dimon6018.metrolauncher.helpers.utils.Utils
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.VERSION_CODE
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.applyWindowInsets
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.saveError
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

class UpdateActivity: AppCompatActivity() {
    private lateinit var check: MaterialButton
    private lateinit var checkingSub: MaterialTextView
    private lateinit var autoUpdateCheckBox: MaterialCheckBox
    private lateinit var updateNotificationCheckBox: MaterialCheckBox

    private lateinit var progressLayout: LinearLayout
    private lateinit var progressText: MaterialTextView
    private lateinit var cancelDownload: MaterialTextView
    private lateinit var updateDetails: MaterialTextView
    private lateinit var progressBar: ContentLoadingProgressBar

    private var db: BSOD? = null
    private var manager: DownloadManager? = null
    private var downloadId: Long? = null

    private var coroutineXmlScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
    private var coroutineErrorScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
    private var coroutineDownloadingScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
    private var mainDispatcher = Dispatchers.Main

    private lateinit var main: CoordinatorLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(Utils.launcherAccentTheme())
        super.onCreate(savedInstanceState)
        setContentView(R.layout.launcher_settings_updates)
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
        autoUpdateCheckBox.setOnCheckedChangeListener { _, isChecked ->
            PREFS!!.setAutoUpdate(isChecked)
            refreshUi()
        }
        updateNotificationCheckBox.setOnCheckedChangeListener { _, isChecked ->
            PREFS!!.setUpdateNotification(isChecked)
            if (isChecked) {
                UpdateWorker.scheduleWork(this)
            } else {
                UpdateWorker.stopWork(this)
            }
            refreshUi()
        }
        updateDetails.setOnClickListener {
            WPDialog(this).setTopDialog(true)
                .setTitle(getString(R.string.details))
                .setMessage(getUpdateMessage())
                .setPositiveButton(getString(android.R.string.ok), null).show()
        }
        check.setOnClickListener {
            if(!checkStoragePermissions()) {
                PREFS!!.setUpdateState(5)
                refreshUi()
                showPermsDialog()
                return@setOnClickListener
            }
            when(PREFS!!.updateState) {
                4 -> {
                    try {
                        val file = File(Environment.getExternalStorageDirectory().toString() + "/Download/", "MPL_update.apk")
                        val uri = FileProvider.getUriForFile(this, applicationContext.packageName + ".provider", file)
                        PREFS!!.prefs.edit().putBoolean("updateInstalled", true).apply()
                        openFile(uri, this)
                    } catch (e: Exception) {
                        Log.i("InstallAPK", "error: $e")
                        PREFS!!.setUpdateState(5)
                        refreshUi()
                        saveError(e.toString(), db!!)
                    }
                    return@setOnClickListener
                }
                6, 7 -> {
                    checkDownload()
                }
                else -> {
                    PREFS!!.setUpdateState(1)
                    refreshUi()
                    checkForUpdates()
                }
            }
        }
        cancelDownload.setOnClickListener {
            val ver = if (UpdateDataParser.verCode == null) {
                PREFS!!.versionCode
            } else {
                UpdateDataParser.verCode
            }
            if (ver == VERSION_CODE) {
                PREFS!!.setUpdateState(3)
            } else {
                PREFS!!.setUpdateState(0)
            }
            if (PREFS!!.updateState == 1) {
                return@setOnClickListener
            }
            isUpdateDownloading = false
            manager?.remove(downloadId!!)
            deleteUpdateFile(this)
            refreshUi()
        }
        if(PREFS!!.prefs.getBoolean("permsDialogUpdateScreenEnabled", true) && !checkStoragePermissions()) {
            showPermsDialog()
        }
        main = findViewById(R.id.coordinator)
        applyWindowInsets(main)
    }
    private fun enterAnimation(exit: Boolean) {
        if(!PREFS!!.isTransitionAnimEnabled) {
            return
        }
        val animatorSet = AnimatorSet()
        if(exit) {
            animatorSet.playTogether(
                ObjectAnimator.ofFloat(main, "translationX", 0f, 300f),
                ObjectAnimator.ofFloat(main, "rotationY", 0f, 90f),
                ObjectAnimator.ofFloat(main, "alpha", 1f, 0f),
                ObjectAnimator.ofFloat(main, "scaleX", 1f, 0.5f),
                ObjectAnimator.ofFloat(main, "scaleY", 1f, 0.5f),
            )
        } else {
            animatorSet.playTogether(
                ObjectAnimator.ofFloat(main, "translationX", 300f, 0f),
                ObjectAnimator.ofFloat(main, "rotationY", 90f, 0f),
                ObjectAnimator.ofFloat(main, "alpha", 0f, 1f),
                ObjectAnimator.ofFloat(main, "scaleX", 0.5f, 1f),
                ObjectAnimator.ofFloat(main, "scaleY", 0.5f, 1f)
            )
        }
        animatorSet.setDuration(400)
        animatorSet.start()
    }

    override fun onResume() {
        enterAnimation(false)
        super.onResume()
    }

    override fun onPause() {
        enterAnimation(true)
        super.onPause()
    }
    private fun showPermsDialog() {
        val dialog = WPDialog(this).setTopDialog(true)
            .setTitle(getString(R.string.perms_req))
            .setCancelable(true)
            .setMessage(getString(R.string.perms_req_tip))
        dialog.setNegativeButton(getString(R.string.yes)) {
            getPermission()
            WPDialog(this).dismiss()
            dialog.dismiss()
        }
            .setNeutralButton(getString(R.string.hide)) {
                hideDialogForever()
                dialog.dismiss()
            }
            .setPositiveButton(getString(R.string.no)){ dialog.dismiss()
            }
        dialog.show()
    }
    private fun getUpdateMessage(): String {
        return if(UpdateDataParser.updateMsg == null) {
            PREFS!!.updateMessage
        } else {
            UpdateDataParser.updateMsg!!
        }
    }
    override fun onDestroy() {
        coroutineXmlScope.cancel()
        coroutineErrorScope.cancel()
        super.onDestroy()
    }
    private fun hideDialogForever() {
        PREFS!!.prefs.edit().putBoolean("permsDialogUpdateScreenEnabled", false).apply()
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
            val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).setData(Uri.parse(String.format("package:%s", packageName)))
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
    private fun refreshUi() {
        autoUpdateCheckBox.isChecked = PREFS!!.isAutoUpdateEnabled
        updateNotificationCheckBox.isChecked = PREFS!!.isUpdateNotificationEnabled
        autoUpdateCheckBox.isEnabled = PREFS!!.isUpdateNotificationEnabled
        when (PREFS!!.updateState) {
            1 -> {
                //checking for updates state
                check.visibility = View.GONE
                checkingSub.visibility = View.VISIBLE
                progressLayout.visibility = View.GONE
                checkingSub.text = getString(R.string.checking_for_updates)
                updateDetails.visibility = View.GONE
                cancelDownload.visibility = View.VISIBLE
            }
            2 -> {
                // dowloading state
                checkingSub.visibility = View.GONE
                check.visibility = View.GONE
                progressLayout.visibility = View.VISIBLE
                cancelDownload.visibility = View.VISIBLE
                val progressString = if(isUpdateDownloading) {
                    progressBar.progress = PREFS!!.updateProgressLevel
                    getString(R.string.preparing_to_install, PREFS!!.updateProgressLevel) + "%"
                } else {
                    getString(R.string.preparing_to_install, 0) + "%"
                }
                progressText.text = progressString
                updateDetails.visibility = View.GONE
            }
            3 -> {
                // up to date
                checkingSub.visibility = View.VISIBLE
                checkingSub.text = getString(R.string.up_to_date)
                check.visibility = View.VISIBLE
                check.text = getString(R.string.check_for_updates)
                progressLayout.visibility = View.GONE
                updateDetails.visibility = View.GONE
                cancelDownload.visibility = View.GONE
            }
            4 -> {
                // ready to install
                checkingSub.visibility = View.VISIBLE
                checkingSub.text = getString(R.string.ready_to_install)
                check.visibility = View.VISIBLE
                check.text = getString(R.string.install)
                progressLayout.visibility = View.GONE
                updateDetails.visibility = View.VISIBLE
                cancelDownload.visibility = View.VISIBLE
            }
            5 -> {
                // error
                checkingSub.visibility = View.VISIBLE
                checkingSub.text = getString(R.string.update_failed)
                check.visibility = View.VISIBLE
                progressLayout.visibility = View.GONE
                check.text = getString(R.string.retry)
                updateDetails.visibility = View.GONE
                cancelDownload.visibility = View.GONE
            }
            6 -> {
                // ready for download
                checkingSub.visibility = View.VISIBLE
                checkingSub.text = getString(R.string.ready_to_download)
                check.visibility = View.VISIBLE
                progressLayout.visibility = View.GONE
                check.text = getString(R.string.download)
                updateDetails.visibility = View.VISIBLE
                cancelDownload.visibility = View.VISIBLE
            }
            7 -> {
                // BETA is ready for download
                checkingSub.visibility = View.VISIBLE
                checkingSub.text = getString(R.string.ready_to_download_beta)
                check.visibility = View.VISIBLE
                progressLayout.visibility = View.GONE
                check.text = getString(R.string.download_beta)
                updateDetails.visibility = View.VISIBLE
                cancelDownload.visibility = View.VISIBLE
            }
            8 -> {
                // current version is newer
                checkingSub.visibility = View.VISIBLE
                checkingSub.text = getString(R.string.update_failed_version_bigger_than_server)
                check.visibility = View.VISIBLE
                progressLayout.visibility = View.GONE
                check.text = getString(R.string.retry)
                updateDetails.visibility = View.GONE
                cancelDownload.visibility = View.GONE
            }
            0 -> {
                // default
                check.visibility = View.VISIBLE
                check.text = getString(R.string.check_for_updates)
                checkingSub.visibility = View.GONE
                progressLayout.visibility = View.GONE
                updateDetails.visibility = View.GONE
                cancelDownload.visibility = View.GONE
            }
        }
        if(!BuildConfig.UPDATES_ACITVE) {
            check.visibility = View.GONE
            checkingSub.apply {
                visibility = View.VISIBLE
                text = getString(R.string.updates_disabled)
            }
        }
    }
    private fun checkForUpdates() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                downloadXmlActivity()
                withContext(mainDispatcher) {
                    refreshUi()
                    checkUpdateInfo()
                }
            } catch (e: Exception) {
                Log.e("CheckForUpdates", e.toString())
                saveError(e.toString(), db!!)
                withContext(mainDispatcher) {
                    refreshUi()
                }
            }
            cancel()
        }
    }
    private fun checkUpdateInfo() {
        coroutineXmlScope.launch {
            if (UpdateDataParser.verCode == null) {
                PREFS!!.setUpdateState(5)
                return@launch
            }
            if (UpdateDataParser.verCode == VERSION_CODE) {
                PREFS!!.setUpdateState(3)
            } else if (VERSION_CODE > UpdateDataParser.verCode!!) {
                PREFS!!.setUpdateState(8)
            } else if(UpdateDataParser.verCode!! > VERSION_CODE) {
                if (UpdateDataParser.isBeta == true) {
                    PREFS!!.setUpdateState(7)
                } else {
                    PREFS!!.setUpdateState(6)
                }
            }
            withContext(mainDispatcher) {
                progressBar.isIndeterminate = false
                refreshUi()
            }
            cancel()
        }
    }
    private fun checkDownload() {
        if (UpdateDataParser.isBeta == true) {
            Log.i("CheckForUpdates", "download beta")
            downloadFile("MPL Beta", URL_BETA_FILE)
        } else {
            Log.i("CheckForUpdates", "download release")
            downloadFile("MPL", URL_RELEASE_FILE)
        }
    }
    @SuppressLint("Range")
    private fun downloadFile(fileName: String, url: String) {
        coroutineDownloadingScope.launch {
            try {
                deleteUpdateFile(this@UpdateActivity)
            } catch (e: IOException) {
                saveError(e.toString(), db!!)
                PREFS!!.setUpdateState(5)
                refreshUi()
                withContext(mainDispatcher) {
                    WPDialog(this@UpdateActivity).setTopDialog(true)
                        .setTitle(getString(R.string.error))
                        .setMessage(getString(R.string.downloading_error))
                        .setPositiveButton(getString(android.R.string.ok), null).show()
                }
                cancel()
                return@launch
            }
            try {
                val request = DownloadManager.Request(Uri.parse(url))
                request.setDescription(getString(R.string.update_notification))
                request.setTitle(fileName)
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN)
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "MPL_update.apk")
                manager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
                downloadId = manager?.enqueue(request)
                isUpdateDownloading = true
                PREFS!!.setUpdateState(2)
                withContext(mainDispatcher) {
                    refreshUi()
                }
                val q = DownloadManager.Query()
                q.setFilterById(downloadId!!)
                var cursor: Cursor?
                val isGreaterThanN = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
                while (isUpdateDownloading) {
                    cursor = manager!!.query(q)
                    if (cursor != null && cursor.moveToFirst()) {
                        val downloaded = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                        val total = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                        val progress: Int = ((downloaded * 100L / total)).toInt()
                        val progressString = getString(R.string.preparing_to_install, progress) + "%"
                        PREFS!!.setUpdateProgressLevel(progress)
                        withContext(mainDispatcher) {
                            progressText.text = progressString
                            if (isGreaterThanN) {
                                progressBar.setProgress(progress, true)
                            } else {
                                progressBar.progress = progress
                            }
                        }
                        if (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_SUCCESSFUL) {
                            isUpdateDownloading = false
                            PREFS!!.setUpdateState(4)
                            withContext(mainDispatcher) {
                                refreshUi()
                            }
                        }
                        if (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_FAILED) {
                            isUpdateDownloading = false
                            PREFS!!.setUpdateState(5)
                            withContext(mainDispatcher) {
                                refreshUi()
                            }
                        }
                        cursor.close()
                    } else {
                        cursor.close()
                        isUpdateDownloading = false
                        PREFS!!.setUpdateState(0)
                        withContext(mainDispatcher) {
                            this@UpdateActivity.recreate()
                        }
                    }
                }
            } catch (e: Exception) {
                saveError(e.toString(), db!!)
                if(downloadId != null) {
                    manager?.remove(downloadId!!)
                }
                isUpdateDownloading = false
                PREFS!!.setUpdateState(5)
                withContext(mainDispatcher) {
                    refreshUi()
                    WPDialog(this@UpdateActivity).setTopDialog(true)
                        .setTitle(getString(R.string.error))
                        .setMessage(getString(R.string.downloading_error))
                        .setPositiveButton(getString(android.R.string.ok), null).show()
                }
            }
            cancel()
        }
    }
    private suspend fun downloadXmlActivity() {
        Log.i("CheckForUpdates", "download xml")
        val url = URL(URL)
        val connection = withContext(Dispatchers.IO) {
            url.openConnection()
        } as HttpURLConnection
        connection.connectTimeout = 15000
        try {
            val input = connection.inputStream
            val parser = UpdateDataParser()
            parser.parse(input)
            withContext(Dispatchers.IO) {
                input.close()
            }
        } catch (e: Exception) {
            Log.e("CheckForUpdates", "something went wrong: $e")
            PREFS!!.setUpdateState(5)
            withContext(mainDispatcher) {
                refreshUi()
            }
        }
    }
    companion object {
        const val URL: String = "https://github.com/queuejw/mpl_updates/releases/download/release/update.xml"
        const val URL_BETA_FILE: String = "https://github.com/queuejw/mpl_updates/releases/download/release/MPL-beta.apk"
        const val URL_RELEASE_FILE: String = "https://github.com/queuejw/mpl_updates/releases/download/release/MPL.apk"

        fun downloadXml(link: String) {
            Log.i("CheckForUpdates", "download xml")
            val url = URL(link)
            try {
                val connection = url.openConnection() as HttpURLConnection
                connection.connectTimeout = 15000
                val input = connection.inputStream
                val parser = UpdateDataParser()
                parser.parse(input)
                input.close()
            } catch (e: Exception) {
                Log.e("CheckForUpdates", "something went wrong: $e")
            }
        }
        fun isUpdateAvailable(): Boolean {
            if(UpdateDataParser.verCode == null || VERSION_CODE > UpdateDataParser.verCode!! || !BuildConfig.UPDATES_ACITVE) {
                return false
            }
            val boolean: Boolean = if (UpdateDataParser.verCode == VERSION_CODE) {
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
        fun deleteUpdateFile(context: Context) {
            try {
                val file = File(Environment.getExternalStorageDirectory().toString() + "/Download/", "MPL_update.apk")
                val uri = FileProvider.getUriForFile(context, context.packageName + ".provider", file)
                context.contentResolver.delete(uri, null, null)
            } catch (e: IOException) {
                Log.e("Update", e.toString())
            }
        }
    }
}