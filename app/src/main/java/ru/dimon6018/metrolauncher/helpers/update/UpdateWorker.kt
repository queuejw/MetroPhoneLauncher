package ru.dimon6018.metrolauncher.helpers.update

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import ru.dimon6018.metrolauncher.R
import ru.dimon6018.metrolauncher.content.data.Prefs
import ru.dimon6018.metrolauncher.content.settings.UpdateActivity
import ru.dimon6018.metrolauncher.content.settings.UpdateActivity.Companion.URL
import ru.dimon6018.metrolauncher.content.settings.UpdateActivity.Companion.downloadXml
import ru.dimon6018.metrolauncher.content.settings.UpdateActivity.Companion.isUpdateAvailable
import java.util.concurrent.TimeUnit

private const val CHAN_ID = "MPL-updates"

class UpdateWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
    private fun buildNotificationN(context: Context): NotificationCompat.Builder {
        val icon = IconCompat.createWithResource(context, R.drawable.ic_download)
        val intent = Intent(Intent.ACTION_MAIN)
                .setClass(context, UpdateActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        return NotificationCompat.Builder(context, CHAN_ID)
                .setSmallIcon(icon)
                .setContentTitle(context.getString(R.string.app_name))
                .setShowWhen(true)
                .setCategory(Notification.CATEGORY_RECOMMENDATION)
                .setContentText(context.getString(R.string.update_is_available))
                .setContentIntent(PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE))
                .setAutoCancel(true)
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
                prefs.setUpdateState(6)
                val noman = context.getSystemService(NotificationManager::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val builder = buildNotificationO(context)
                    noman.notify(1, builder.build())
                } else {
                    val builder = buildNotificationN(context)
                    noman.notify(1, builder.build())
                }
            } else {
                prefs.setUpdateState(3)
            }
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
        scheduleWork(context)
        return state
    }
    companion object {
        @JvmStatic
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