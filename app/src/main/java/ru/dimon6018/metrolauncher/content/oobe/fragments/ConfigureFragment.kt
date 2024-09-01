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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.dimon6018.metrolauncher.R
import ru.dimon6018.metrolauncher.content.oobe.WelcomeActivity
import ru.dimon6018.metrolauncher.databinding.OobeFragmentConfBinding

class ConfigureFragment: Fragment() {

    private var _binding: OobeFragmentConfBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        _binding = OobeFragmentConfBinding.inflate(inflater, container, false)
        (requireActivity() as WelcomeActivity).setText(getString(R.string.configurePhone))
        binding.back.setOnClickListener {
            lifecycleScope.launch {
                enterAnimation(true)
                delay(200)
                requireActivity().supportFragmentManager.commit {
                    replace(R.id.fragment_container_view, WelcomeFragment(), "oobe")
                }
            }
        }
        binding.custom.setOnClickListener {
            lifecycleScope.launch {
                enterAnimation(true)
                delay(200)
                requireActivity().supportFragmentManager.commit {
                    replace(R.id.fragment_container_view, CustomSettingsFragment(), "oobe")
                }
            }
        }
        binding.recommended.setOnClickListener {
            lifecycleScope.launch {
                enterAnimation(true)
                delay(200)
                requireActivity().supportFragmentManager.commit {
                    replace(R.id.fragment_container_view, AppsFragment(), "oobe")
                }
            }
        }
        return binding.root
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    private fun enterAnimation(exit: Boolean) {
        val main = binding.root
        val animatorSet = AnimatorSet()
        if(exit) {
            animatorSet.playTogether(
                ObjectAnimator.ofFloat(main, "translationX", 0f, -1000f),
                ObjectAnimator.ofFloat(main, "alpha", 1f, 0f),
            )
        } else {
            animatorSet.playTogether(
                ObjectAnimator.ofFloat(main, "translationX", 1000f, 0f),
                ObjectAnimator.ofFloat(main, "alpha", 0f, 1f),
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
