package ru.queuejw.mpl.content.oobe.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.queuejw.mpl.Application
import ru.queuejw.mpl.R
import ru.queuejw.mpl.content.oobe.OOBEActivity
import ru.queuejw.mpl.databinding.OobeFragmentWelcomeBinding
import ru.queuejw.mpl.helpers.update.UpdateWorker

class WelcomeFragment : Fragment() {

    private var _binding: OobeFragmentWelcomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = OobeFragmentWelcomeBinding.inflate(inflater, container, false)
        if (!Application.PREFS.prefs.getBoolean("channelConfigured", false)) {
            UpdateWorker.setupNotificationChannels(requireActivity())
            Application.PREFS.prefs.edit().putBoolean("channelConfigured", true).apply()
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as OOBEActivity).apply {
            nextFragment = 1
            previousFragment = 0
            blockBottomBarButton(false)
            (requireActivity() as OOBEActivity).setText(getString(R.string.welcomePhone))
            updateNextButtonText(this.getString(R.string.next))
            updatePreviousButtonText(this.getString(R.string.back))
            lifecycleScope.launch {
                delay(200)
                animateBottomBarFromFragment()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}