package ru.queuejw.mpl.helpers.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * A receiver called when the launcher receives any changes in packages (apps).
 *
 * This receiver is only used in LauncherActivity and it is not called when the launcher is paused.
 */
open class PackageChangesReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != null && intent.data != null) {
            val packageAction: Int
            val isReplacing = intent.getBooleanExtra(Intent.EXTRA_REPLACING, false)
            val packageName = intent.data?.encodedSchemeSpecificPart ?: ""
            packageAction = if (isReplacing) {
                PACKAGE_UPDATED
            } else when (intent.action) {
                Intent.ACTION_PACKAGE_REPLACED -> PACKAGE_UPDATED
                Intent.ACTION_PACKAGE_ADDED -> PACKAGE_INSTALLED
                Intent.ACTION_PACKAGE_REMOVED, Intent.ACTION_PACKAGE_FULLY_REMOVED -> PACKAGE_REMOVED
                Intent.ACTION_PACKAGE_CHANGED -> PACKAGE_MISC
                else -> -1
            }
            // Receive intent from broadcast.
            if (packageAction != -1 && !packageName.contains(context.packageName)) {
                Log.d("Broadcaster", "send broadcast")
                Intent().apply {
                    putExtra("action", packageAction)
                    putExtra("package", packageName)
                    action = "ru.dimon6018.metrolauncher.PACKAGE_CHANGE_BROADCAST"
                }.also {
                    context.sendBroadcast(it)
                }
            }
        }
    }

    companion object {
        const val PACKAGE_REMOVED = 0
        const val PACKAGE_INSTALLED = 1
        const val PACKAGE_UPDATED = 2
        const val PACKAGE_MISC = 42
    }
}