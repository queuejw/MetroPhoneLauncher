package ru.dimon6018.metrolauncher.content.oobe

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import ru.dimon6018.metrolauncher.Application
import ru.dimon6018.metrolauncher.Main
import ru.dimon6018.metrolauncher.R

class AlmostDoneFragment: Fragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.oobe_fragment_almostdone, container, false)
        val next: MaterialButton = view.findViewById(R.id.next)
        WelcomeActivity.setText(requireActivity(), getString(R.string.welcomeAlmostDone))
        next.setOnClickListener {
            Application.PREFS!!.setLauncherState(1)
            requireActivity().startActivity(Intent(activity, Main::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
        }
        return view
    }
}