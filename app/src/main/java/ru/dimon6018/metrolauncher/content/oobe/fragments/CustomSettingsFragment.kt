package ru.dimon6018.metrolauncher.content.oobe.fragments

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.dimon6018.metrolauncher.R
import ru.dimon6018.metrolauncher.content.oobe.WelcomeActivity

class CustomSettingsFragment: Fragment() {

    private var main: View? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        WelcomeActivity.setText(requireActivity(), getString(R.string.configureCustomPhone))
        val view = inflater.inflate(R.layout.oobe_fragment_custom_prefs, container, false)
        main = view
        val back: MaterialButton = view.findViewById(R.id.back)
        val next: MaterialButton = view.findViewById(R.id.next)
        WelcomeActivity.setText(requireActivity(), getString(R.string.configurePhone))
        back.setOnClickListener {
            lifecycleScope.launch {
                enterAnimation(true)
                delay(200)
                requireActivity().supportFragmentManager.commit {
                    replace(R.id.fragment_container_view, ConfigureFragment(), "oobe")
                }
            }
        }
        next.setOnClickListener {
            lifecycleScope.launch {
                enterAnimation(true)
                delay(200)
                requireActivity().supportFragmentManager.commit {
                    replace(R.id.fragment_container_view, AdFragment(), "oobe")
                }
            }
        }
        return view
    }
    private fun enterAnimation(exit: Boolean) {
        if(main == null) {
            return
        }
        val animatorSet = AnimatorSet()
        if(exit) {
            animatorSet.playTogether(
                ObjectAnimator.ofFloat(main!!, "translationX", 0f, -1000f),
                ObjectAnimator.ofFloat(main!!, "alpha", 1f, 0f),
            )
        } else {
            animatorSet.playTogether(
                ObjectAnimator.ofFloat(main!!, "translationX", 1000f, 0f),
                ObjectAnimator.ofFloat(main!!, "alpha", 0f, 1f),
            )
        }
        animatorSet.setDuration(300)
        animatorSet.start()
    }

    override fun onResume() {
        enterAnimation(false)
        super.onResume()
    }
}