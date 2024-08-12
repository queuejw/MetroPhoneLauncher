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
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
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
import ru.dimon6018.metrolauncher.databinding.LauncherSettingsUpdatesBinding
import ru.dimon6018.metrolauncher.helpers.ui.WPDialog
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

    private var db: BSOD? = null
    private var manager: DownloadManager? = null
    private var downloadId: Long? = null

    private var coroutineXmlScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
    private var coroutineErrorScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
    private var coroutineDownloadingScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
    private var mainDispatcher = Dispatchers.Main

    private lateinit var binding: LauncherSettingsUpdatesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(Utils.launcherAccentTheme())
        binding = LauncherSettingsUpdatesBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        init()
        refreshUi()
        setOnClickers()
        applyWindowInsets(binding.root)
        prepareTip()
    }
    private fun init() {
        db = BSOD.getData(this)
    }
    private fun setOnClickers() {
        binding.settingsInclude.AutoUpdateCheckBox.setOnCheckedChangeListener { _, isChecked ->
            PREFS!!.isAutoUpdateEnabled = isChecked
            refreshUi()
        }
        binding.settingsInclude.UpdateNotifyCheckBox.setOnCheckedChangeListener { _, isChecked ->
            PREFS!!.isUpdateNotificationEnabled = isChecked
            if (isChecked) {
                UpdateWorker.scheduleWork(this)
            } else {
                UpdateWorker.stopWork(this)
            }
            refreshUi()
        }
        binding.settingsInclude.updateInfo.setOnClickListener {
            WPDialog(this).setTopDialog(true)
                .setTitle(getString(R.string.details))
                .setMessage(getUpdateMessage())
                .setPositiveButton(getString(android.R.string.ok), null).show()
        }
        binding.settingsInclude.checkForUpdatesBtn.setOnClickListener {
            if(!checkStoragePermissions()) {
                PREFS!!.updateState = 5
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
                        PREFS!!.updateState = 5
                        refreshUi()
                        saveError(e.toString(), db!!)
                    }
                    return@setOnClickListener
                }
                6, 7 -> {
                    checkDownload()
                }
                else -> {
                    PREFS!!.updateState = 1
                    refreshUi()
                    checkForUpdates()
                }
            }
        }
        binding.settingsInclude.cancelButton.setOnClickListener {
            val ver = if (UpdateDataParser.verCode == null) {
                PREFS!!.versionCode
            } else {
                UpdateDataParser.verCode
            }
            if (ver == VERSION_CODE) {
                PREFS!!.updateState = 3
            } else {
                PREFS!!.updateState = 0
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
    }
    private fun prepareTip() {
        if(PREFS!!.prefs.getBoolean("tipSettingsUpdatesEnabled", true)) {
            WPDialog(this).setTopDialog(true)
                .setTitle(getString(R.string.tip))
                .setMessage(getString(R.string.tipSettingsUpdates))
                .setPositiveButton(getString(android.R.string.ok), null)
                .show()
            PREFS!!.prefs.edit().putBoolean("tipSettingsUpdatesEnabled", false).apply()
        }
    }
    private fun enterAnimation(exit: Boolean) {
        if (!PREFS!!.isTransitionAnimEnabled) {
            return
        }
        val main = binding.root
        val animatorSet = AnimatorSet().apply {
            playTogether(
                createObjectAnimator(main, "translationX", if (exit) 0f else -300f, if (exit) -300f else 0f),
                createObjectAnimator(main, "rotationY", if (exit) 0f else 90f, if (exit) 90f else 0f),
                createObjectAnimator(main, "alpha", if (exit) 1f else 0f, if (exit) 0f else 1f),
                createObjectAnimator(main, "scaleX", if (exit) 1f else 0.5f, if (exit) 0.5f else 1f),
                createObjectAnimator(main, "scaleY", if (exit) 1f else 0.5f, if (exit) 0.5f else 1f)
            )
            duration = 400
        }
        animatorSet.start()
    }
    private fun createObjectAnimator(target: Any, property: String, startValue: Float, endValue: Float): ObjectAnimator {
        return ObjectAnimator.ofFloat(target, property, startValue, endValue)
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
        binding.settingsInclude.AutoUpdateCheckBox.apply {
            isChecked = PREFS!!.isAutoUpdateEnabled
            isEnabled = PREFS!!.isUpdateNotificationEnabled
        }
        binding.settingsInclude.UpdateNotifyCheckBox.isChecked = PREFS!!.isUpdateNotificationEnabled
        when (PREFS!!.updateState) {
            1 -> {
                //checking for updates state
                binding.settingsInclude.checkForUpdatesBtn.visibility = View.GONE
                binding.settingsInclude.chekingUpdatesSub.apply {
                    visibility = View.VISIBLE
                    text = getString(R.string.checking_for_updates)
                }
                binding.settingsInclude.updateIndicator.visibility = View.GONE
                binding.settingsInclude.updateInfo.visibility = View.GONE
                binding.settingsInclude.cancelButton.visibility = View.VISIBLE
            }
            2 -> {
                // dowloading state
                binding.settingsInclude.chekingUpdatesSub.visibility = View.GONE
                binding.settingsInclude.checkForUpdatesBtn.visibility = View.GONE
                binding.settingsInclude.updateIndicator.visibility = View.VISIBLE
                binding.settingsInclude.cancelButton.visibility = View.VISIBLE
                val progressString = if(isUpdateDownloading) {
                    binding.settingsInclude.progress.progress = PREFS!!.updateProgressLevel
                    getString(R.string.preparing_to_install, PREFS!!.updateProgressLevel) + "%"
                } else {
                    getString(R.string.preparing_to_install, 0) + "%"
                }
                binding.settingsInclude.progessText.text = progressString
                binding.settingsInclude.updateInfo.visibility = View.GONE
            }
            3 -> {
                // up to date
                binding.settingsInclude.chekingUpdatesSub.apply {
                    visibility = View.VISIBLE
                    text = getString(R.string.up_to_date)
                }
                binding.settingsInclude.checkForUpdatesBtn.apply {
                    visibility = View.VISIBLE
                    text = getString(R.string.check_for_updates)
                }
                binding.settingsInclude.updateIndicator.visibility = View.GONE
                binding.settingsInclude.updateInfo.visibility = View.GONE
                binding.settingsInclude.cancelButton.visibility = View.GONE
            }
            4 -> {
                // ready to install
                binding.settingsInclude.chekingUpdatesSub.apply {
                    visibility = View.VISIBLE
                    text = getString(R.string.ready_to_install)
                }
                binding.settingsInclude.checkForUpdatesBtn.apply {
                    visibility = View.VISIBLE
                    text = getString(R.string.install)
                }
                binding.settingsInclude.updateIndicator.visibility = View.GONE
                binding.settingsInclude.updateInfo.visibility = View.VISIBLE
                binding.settingsInclude.cancelButton.visibility = View.VISIBLE
            }
            5 -> {
                // error
                binding.settingsInclude.chekingUpdatesSub.apply {
                    visibility = View.VISIBLE
                    text = getString(R.string.update_failed)
                }
                binding.settingsInclude.checkForUpdatesBtn.apply {
                    visibility = View.VISIBLE
                    text = getString(R.string.retry)
                }
                binding.settingsInclude.updateIndicator.visibility = View.GONE
                binding.settingsInclude.updateInfo.visibility = View.GONE
                binding.settingsInclude.cancelButton.visibility = View.GONE
            }
            6 -> {
                // ready for download
                binding.settingsInclude.chekingUpdatesSub.apply {
                    visibility = View.VISIBLE
                    text = getString(R.string.ready_to_download)
                }
                binding.settingsInclude.checkForUpdatesBtn.apply {
                    visibility = View.VISIBLE
                    text = getString(R.string.download)
                }
                binding.settingsInclude.updateIndicator.visibility = View.GONE
                binding.settingsInclude.updateInfo.visibility = View.VISIBLE
                binding.settingsInclude.cancelButton.visibility = View.VISIBLE
            }
            7 -> {
                // BETA is ready for download
                binding.settingsInclude.chekingUpdatesSub.apply {
                    visibility = View.VISIBLE
                    text = getString(R.string.ready_to_download_beta)
                }
                binding.settingsInclude.checkForUpdatesBtn.apply {
                    visibility = View.VISIBLE
                    text = getString(R.string.download_beta)
                }
                binding.settingsInclude.updateIndicator.visibility = View.GONE
                binding.settingsInclude.updateInfo.visibility = View.VISIBLE
                binding.settingsInclude.cancelButton.visibility = View.VISIBLE
            }
            8 -> {
                // current version is newer
                binding.settingsInclude.chekingUpdatesSub.apply {
                    visibility = View.VISIBLE
                    text = getString(R.string.update_failed_version_bigger_than_server)
                }
                binding.settingsInclude.checkForUpdatesBtn.apply {
                    visibility = View.VISIBLE
                    text = getString(R.string.retry)
                }
                binding.settingsInclude.updateIndicator.visibility = View.GONE
                binding.settingsInclude.updateInfo.visibility = View.GONE
                binding.settingsInclude.cancelButton.visibility = View.GONE
            }
            0 -> {
                // default
                binding.settingsInclude.checkForUpdatesBtn.apply {
                    visibility = View.VISIBLE
                    text = getString(R.string.check_for_updates)
                }
                binding.settingsInclude.chekingUpdatesSub.visibility = View.GONE
                binding.settingsInclude.updateIndicator.visibility = View.GONE
                binding.settingsInclude.updateInfo.visibility = View.GONE
                binding.settingsInclude.cancelButton.visibility = View.GONE
            }
        }
        if(!BuildConfig.UPDATES_ACITVE) {
            binding.settingsInclude.checkForUpdatesBtn.visibility = View.GONE
            binding.settingsInclude.chekingUpdatesSub.apply {
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
                PREFS!!.updateState = 5
                return@launch
            }
            if (UpdateDataParser.verCode == VERSION_CODE) {
                PREFS!!.updateState = 3
            } else if (VERSION_CODE > UpdateDataParser.verCode!!) {
                PREFS!!.updateState = 8
            } else if(UpdateDataParser.verCode!! > VERSION_CODE) {
                if (UpdateDataParser.isBeta == true) {
                    PREFS!!.updateState = 7
                } else {
                    PREFS!!.updateState = 6
                }
            }
            withContext(mainDispatcher) {
                binding.settingsInclude.progress.isIndeterminate = false
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
                PREFS!!.updateState = 5
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
                PREFS!!.updateState = 2
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
                        PREFS!!.updateProgressLevel = progress
                        withContext(mainDispatcher) {
                            binding.settingsInclude.progessText.text = progressString
                            if (isGreaterThanN) {
                                binding.settingsInclude.progress.setProgress(progress, true)
                            } else {
                                binding.settingsInclude.progress.progress = progress
                            }
                        }
                        if (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_SUCCESSFUL) {
                            isUpdateDownloading = false
                            PREFS!!.updateState = 4
                            withContext(mainDispatcher) {
                                refreshUi()
                            }
                        }
                        if (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_FAILED) {
                            isUpdateDownloading = false
                            PREFS!!.updateState = 5
                                withContext(mainDispatcher) {
                                refreshUi()
                            }
                        }
                        cursor.close()
                    } else {
                        cursor.close()
                        isUpdateDownloading = false
                        PREFS!!.updateState = 0
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
                PREFS!!.updateState = 5
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
            PREFS!!.updateState = 5
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