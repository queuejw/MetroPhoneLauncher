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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.dimon6018.metrolauncher.Application
import ru.dimon6018.metrolauncher.R
import ru.dimon6018.metrolauncher.content.data.tile.TileData
import ru.dimon6018.metrolauncher.content.oobe.WelcomeActivity
import ru.dimon6018.metrolauncher.databinding.OobeFragmentWelcomeBinding
import ru.dimon6018.metrolauncher.helpers.update.UpdateWorker
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.generatePlaceholder

class WelcomeFragment : Fragment() {

    private var placeholderCoroutine = CoroutineScope(Dispatchers.IO)

    private var _binding: OobeFragmentWelcomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        _binding = OobeFragmentWelcomeBinding.inflate(inflater, container, false)
        (requireActivity() as WelcomeActivity).setText(getString(R.string.welcomePhone))
        binding.next.setOnClickListener {
            lifecycleScope.launch {
                enterAnimation(true)
                delay(200)
                requireActivity().supportFragmentManager.commit {
                    replace(R.id.fragment_container_view, ConfigureFragment(), "oobe")
                }
            }
        }
        if(!Application.PREFS.prefs.getBoolean("channelConfigured", false)) {
            UpdateWorker.setupNotificationChannels(requireActivity())
            Application.PREFS.prefs.edit().putBoolean("channelConfigured", true).apply()
        }
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        generatePlaceholders()
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    private fun generatePlaceholders() {
        placeholderCoroutine.launch {
            Application.PREFS.prefs.edit().putBoolean("placeholdersGenerated", true).apply()
            generatePlaceholder(TileData.getTileData(requireContext()).getTileDao(), 84)
            cancel()
        }
    }
    private fun enterAnimation(exit: Boolean) {
        val animatorSet = AnimatorSet()
        val main = binding.root
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

    override fun onStop() {
        placeholderCoroutine.cancel()
        super.onStop()
    }
}