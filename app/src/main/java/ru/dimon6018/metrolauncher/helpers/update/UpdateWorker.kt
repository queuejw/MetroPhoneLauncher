package ru.dimon6018.metrolauncher.helpers.update

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.drawable.Icon
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity.DOWNLOAD_SERVICE
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import ru.dimon6018.metrolauncher.Application.Companion.PREFS
import ru.dimon6018.metrolauncher.Application.Companion.isUpdateDownloading
import ru.dimon6018.metrolauncher.R
import ru.dimon6018.metrolauncher.content.data.Prefs
import ru.dimon6018.metrolauncher.content.settings.activities.UpdateActivity
import ru.dimon6018.metrolauncher.content.settings.activities.UpdateActivity.Companion.URL
import ru.dimon6018.metrolauncher.content.settings.activities.UpdateActivity.Companion.URL_BETA_FILE
import ru.dimon6018.metrolauncher.content.settings.activities.UpdateActivity.Companion.URL_RELEASE_FILE
import ru.dimon6018.metrolauncher.content.settings.activities.UpdateActivity.Companion.deleteUpdateFile
import ru.dimon6018.metrolauncher.content.settings.activities.UpdateActivity.Companion.downloadXml
import ru.dimon6018.metrolauncher.content.settings.activities.UpdateActivity.Companion.isUpdateAvailable
import java.io.IOException
import java.util.concurrent.TimeUnit

private const val CHAN_ID = "MPL-updates"

class UpdateWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
    private fun buildNotificationN(context: Context): NotificationCompat.Builder {
        val icon = IconCompat.createWithResource(context, R.drawable.ic_download)
        val intent = Intent(Intent.ACTION_MAIN)
                .setClass(context, UpdateActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            NotificationCompat.Builder(context, CHAN_ID)
                    .setSmallIcon(icon)
                    .setContentTitle(context.getString(R.string.app_name))
                    .setShowWhen(true)
                    .setCategory(Notification.CATEGORY_RECOMMENDATION)
                    .setContentText(context.getString(R.string.update_is_available))
                    .setContentIntent(PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE))
                    .setAutoCancel(true)
        } else {
            NotificationCompat.Builder(context, CHAN_ID)
                .setContentTitle(context.getString(R.string.app_name))
                .setShowWhen(true)
                .setCategory(Notification.CATEGORY_RECOMMENDATION)
                .setContentText(context.getString(R.string.update_is_available))
                .setContentIntent(PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE))
                .setAutoCancel(true)
        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    fun buildNotificationO(context: Context): Notification.Builder {
        val icon = Icon.createWithResource(context, R.drawable.ic_download)
        val intent = Intent(Intent.ACTION_MAIN)
                .setClass(context, UpdateActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        return Notification.Builder(context, CHAN_ID)
                .setSmallIcon(icon)
                .setContentTitle(context.getString(R.string.app_name))
                .setShowWhen(true)
                .setCategory(Notification.CATEGORY_RECOMMENDATION)
                .setContentText(context.getString(R.string.update_is_available))
                .setContentIntent(
                        PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE))
                .setAutoCancel(true)
    }
    override fun doWork(): Result {
        val context = applicationContext
        val prefs = Prefs(context)
        val state: Result = try {
            downloadXml(URL)
            if(isUpdateAvailable()) {
                if(PREFS.isAutoUpdateEnabled) {
                    val name = if(UpdateDataParser.isBeta == true) "MPL BETA" else "MPL"
                    val link = if(UpdateDataParser.isBeta == true) URL_BETA_FILE else URL_RELEASE_FILE
                    downloadFile(name, link, context)
                } else {
                    prefs.updateState = 6
                    val noman = ContextCompat.getSystemService(context, NotificationManager::class.java)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        val builder = buildNotificationO(context)
                        noman?.notify(1, builder.build())
                    } else {
                        val builder = buildNotificationN(context)
                        noman?.notify(1, builder.build())
                    }
                }
            } else {
                prefs.updateState = 3
            }
            Result.success()
        } catch (_: Exception) {
            Result.failure()
        }
        scheduleWork(context)
        return state
    }
    @SuppressLint("Range")
    private fun downloadFile(fileName: String, url: String, context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                deleteUpdateFile(context)
            } catch (e: IOException) {
                Log.e("Background Update", "Error: $e")
                PREFS.updateState = 5
                cancel()
                return@launch
            }
            try {
                val request = DownloadManager.Request(Uri.parse(url))
                request.setDescription(context.getString(R.string.update_notification))
                request.setTitle(fileName)
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN)
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "MPL_update.apk")
                val manager = context.getSystemService(DOWNLOAD_SERVICE) as DownloadManager
                val downloadId = manager.enqueue(request)
                isUpdateDownloading = true
                PREFS.updateState = 2
                val q = DownloadManager.Query()
                q.setFilterById(downloadId)
                var cursor: Cursor?
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
                while (isUpdateDownloading) {
                    cursor = manager.query(q)
                    if (cursor != null && cursor.moveToFirst()) {
                        val downloaded = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                        val total = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                        val progress: Int = ((downloaded * 100L / total)).toInt()
                        PREFS.updateProgressLevel = progress
                        if (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_SUCCESSFUL) {
                            isUpdateDownloading = false
                            PREFS.updateState = 4
                        }
                        if (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_FAILED) {
                            isUpdateDownloading = false
                            PREFS.updateState = 5
                        }
                        cursor.close()
                    } else {
                        cursor.close()
                        isUpdateDownloading = false
                        PREFS.updateState = 0
                    }
                }
            } catch (e: Exception) {
                Log.e("Background Update", "Error: $e")
                isUpdateDownloading = false
                PREFS.updateState = 5
            }
        }
    }
    companion object {
        fun setupNotificationChannels(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val noman = context.getSystemService(NotificationManager::class.java)
                val channel = NotificationChannel(CHAN_ID,
                        context.getString(R.string.notification_channel_name),
                        NotificationManager.IMPORTANCE_DEFAULT)
                channel.setSound(Uri.EMPTY, Notification.AUDIO_ATTRIBUTES_DEFAULT)
                channel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                noman.createNotificationChannel(channel)
            }
        }
        fun scheduleWork(context: Context) {
            Log.i("backgroundWork", "scheduleWork")
            val time = 390L
            val workFoodRequest: OneTimeWorkRequest = OneTimeWorkRequest.Builder(UpdateWorker::class.java)
                    .addTag("UPDATE_WORK")
                    .setInitialDelay(time, TimeUnit.MINUTES)
                    .build()
            WorkManager.getInstance(context).enqueue(workFoodRequest)
        }
        fun stopWork(context: Context) {
            Log.i("backgroundWork", "stopWork")
            WorkManager.getInstance(context).cancelAllWorkByTag("UPDATE_WORK")
        }
    }
}