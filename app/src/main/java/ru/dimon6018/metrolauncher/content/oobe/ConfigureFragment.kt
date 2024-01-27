package ru.dimon6018.metrolauncher.content.oobe

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.google.android.material.button.MaterialButton
import ru.dimon6018.metrolauncher.R

class ConfigureFragment: Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.oobe_fragment_conf, container, false)
        val back: MaterialButton = view.findViewById(R.id.back)
        WelcomeActivity.setText(requireActivity(), getString(R.string.configurePhone))
        back.setOnClickListener {
            requireActivity().supportFragmentManager.commit {
                replace(R.id.fragment_container_view, WelcomeFragment(), "oobe")
            }
        }
        return view
    }
}
