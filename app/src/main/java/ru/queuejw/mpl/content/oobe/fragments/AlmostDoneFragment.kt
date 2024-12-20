package ru.queuejw.mpl.content.oobe.fragments

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import ru.queuejw.mpl.Application.Companion.PREFS
import ru.queuejw.mpl.R
import ru.queuejw.mpl.content.oobe.OOBEActivity
import ru.queuejw.mpl.databinding.OobeFragmentAlmostdoneBinding

class AlmostDoneFragment : Fragment() {

    private var _binding: OobeFragmentAlmostdoneBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        PREFS.launcherState = 2
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = OobeFragmentAlmostdoneBinding.inflate(inflater, container, false)
        (requireActivity() as OOBEActivity).apply {
            nextFragment = 7
            blockBottomBarButton(false)
            setText(getString(R.string.welcomeAlmostDone))
            animateBottomBarFromFragment()
        }
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun enterAnimation() {
        val main = binding.root
        val animatorSet = AnimatorSet()
        animatorSet.playTogether(
            ObjectAnimator.ofFloat(main, "translationX", 1000f, 0f),
            ObjectAnimator.ofFloat(main, "alpha", 0f, 1f)
        )
        animatorSet.setDuration(300)
        animatorSet.start()
    }

    override fun onResume() {
        enterAnimation()
        super.onResume()
    }
}