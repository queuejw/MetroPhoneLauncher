package ru.queuejw.mpl.content.oobe.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import kotlin.system.exitProcess

class RestartFragment: Fragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        exitProcess(0)
    }
}