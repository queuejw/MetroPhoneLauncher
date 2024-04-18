package ru.dimon6018.metrolauncher.content.oobe.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.google.android.material.button.MaterialButton
import ru.dimon6018.metrolauncher.Application.Companion.PREFS
import ru.dimon6018.metrolauncher.R
import ru.dimon6018.metrolauncher.content.oobe.WelcomeActivity

class ConfigureFragment: Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.oobe_fragment_conf, container, false)
        val back: MaterialButton = view.findViewById(R.id.back)
        val custom: MaterialButton = view.findViewById(R.id.custom)
        val recommended: MaterialButton = view.findViewById(R.id.recommended)
        WelcomeActivity.setText(requireActivity(), getString(R.string.configurePhone))
        PREFS!!.setLauncherState(2)
        back.setOnClickListener {
            requireActivity().supportFragmentManager.commit {
                replace(R.id.fragment_container_view, WelcomeFragment(), "oobe")
            }
        }
        custom.setOnClickListener {
            requireActivity().supportFragmentManager.commit {
                replace(R.id.fragment_container_view, CustomSettingsFragment(), "oobe")
            }
        }
        recommended.setOnClickListener {
            requireActivity().supportFragmentManager.commit {
                replace(R.id.fragment_container_view, AdFragment(), "oobe")
            }
        }
        return view
    }
}
