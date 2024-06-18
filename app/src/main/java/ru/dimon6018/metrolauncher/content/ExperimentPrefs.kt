package ru.dimon6018.metrolauncher.content

import android.content.Context
import android.content.SharedPreferences

class ExperimentPrefs(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(FILE_NAME_EXP, 0)

    fun setAnimationPref(value: Boolean) {
        prefs.edit().putBoolean(ANIMATIONS, value).apply()
    }
    val getAnimationPref: Boolean
        get() = prefs.getBoolean(ANIMATIONS, false)

    companion object {
        const val FILE_NAME_EXP = "ExperimentPrefs"
        const val ANIMATIONS = "animations"
    }
}