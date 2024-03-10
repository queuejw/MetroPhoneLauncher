package ru.dimon6018.metrolauncher.content.oobe

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.dimon6018.metrolauncher.Application
import ru.dimon6018.metrolauncher.R
import ru.dimon6018.metrolauncher.content.data.AppData
import ru.dimon6018.metrolauncher.content.data.AppEntity
import ru.dimon6018.metrolauncher.helpers.update.UpdateWorker

class WelcomeFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.oobe_fragment_welcome, container, false)
        val next: MaterialButton = view.findViewById(R.id.next)
        WelcomeActivity.setText(requireActivity(), getString(R.string.welcomePhone))
        next.setOnClickListener {
            requireActivity().supportFragmentManager.commit {
                replace(R.id.fragment_container_view, ConfigureFragment(), "oobe")
            }
        }
        generatePlaceholders()
        if(!Application.PREFS!!.pref.getBoolean("channelConfigured", false)) {
            UpdateWorker.setupNotificationChannels(requireActivity())
            Application.PREFS!!.editor.putBoolean("channelConfigured", true).apply()
        }
        return view
    }
    private fun generatePlaceholders() {
        CoroutineScope(Dispatchers.IO).launch {
            Application.PREFS!!.editor.putBoolean("placeholdersGenerated", true).apply()
            val size = 100
            val dbCall = AppData.getAppData(requireContext()).getAppDao()
            for (i in 0..size) {
                val placeholder = AppEntity(i, (i + 1).toLong(), -1, true, "small", "", "")
                dbCall.insertItem(placeholder)
            }
        }
    }
}