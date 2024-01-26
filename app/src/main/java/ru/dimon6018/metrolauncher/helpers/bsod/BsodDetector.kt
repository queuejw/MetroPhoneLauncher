package ru.dimon6018.metrolauncher.helpers.bsod

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.util.Log
import kotlin.system.exitProcess

class BsodDetector : Thread.UncaughtExceptionHandler {
    override fun uncaughtException(t: Thread, e: Throwable) {
        Log.e("BSOD", "Detected critical error. See: ${e.stackTraceToString()}")
        openErrorActivity(e)
    }
    private fun openErrorActivity(e: Throwable) {
        val intent = Intent(cntxt, BsodScreen::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.putExtra("stacktrace",e.stackTraceToString())
        intent.putExtra("errorCode", e.toString())
        cntxt!!.startActivity(intent)
        exitProcess(1)
    }
    companion object {
        @SuppressLint("StaticFieldLeak")
        var cntxt: Context? = null
        fun setContext(context: Context?) {
            cntxt = context
        }
    }
}