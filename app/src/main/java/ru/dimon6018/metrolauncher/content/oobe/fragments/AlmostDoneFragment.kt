package ru.dimon6018.metrolauncher.content.oobe.fragments

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import ru.dimon6018.metrolauncher.Application
import ru.dimon6018.metrolauncher.R
import ru.dimon6018.metrolauncher.content.oobe.WelcomeActivity
import kotlin.system.exitProcess

class AlmostDoneFragment: Fragment() {

    private var main: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        Application.PREFS!!.setLauncherState(2)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.oobe_fragment_almostdone, container, false)
        main = view
        val next: MaterialButton = view.findViewById(R.id.next)
        WelcomeActivity.setText(requireActivity(), getString(R.string.welcomeAlmostDone))
        next.setOnClickListener {
            Application.PREFS!!.setLauncherState(1)
            exitProcess(0)
        }
        return view
    }
    private fun enterAnimation() {
        if(main == null) {
            return
        }
        val animatorSet = AnimatorSet()
        animatorSet.playTogether(
                ObjectAnimator.ofFloat(main!!, "translationX", 1000f, 0f),
                ObjectAnimator.ofFloat(main!!, "alpha", 0f, 1f)
        )
        animatorSet.setDuration(300)
        animatorSet.start()
    }

    override fun onResume() {
        enterAnimation()
        super.onResume()
    }
}