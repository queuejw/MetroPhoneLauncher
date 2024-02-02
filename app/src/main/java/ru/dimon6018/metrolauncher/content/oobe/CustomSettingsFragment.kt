package ru.dimon6018.metrolauncher.content.oobe

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.google.android.material.button.MaterialButton
import ru.dimon6018.metrolauncher.R

class CustomSettingsFragment: Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        WelcomeActivity.setText(requireActivity(), getString(R.string.configureCustomPhone))
        val view = inflater.inflate(R.layout.oobe_fragment_custom_prefs, container, false)
        val back: MaterialButton = view.findViewById(R.id.back)
        val next: MaterialButton = view.findViewById(R.id.next)
        WelcomeActivity.setText(requireActivity(), getString(R.string.configurePhone))
        back.setOnClickListener {
            requireActivity().supportFragmentManager.commit {
                replace(R.id.fragment_container_view, ConfigureFragment(), "oobe")
            }
        }
        next.setOnClickListener {
            requireActivity().supportFragmentManager.commit {
                replace(R.id.fragment_container_view, AppsFragment(), "oobe")
            }
        }
        return view
    }
}