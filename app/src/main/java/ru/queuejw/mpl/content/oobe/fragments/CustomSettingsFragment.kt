package ru.queuejw.mpl.content.oobe.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import ru.queuejw.mpl.R
import ru.queuejw.mpl.content.oobe.OOBEActivity
import ru.queuejw.mpl.databinding.OobeFragmentCustomPrefsBinding

class CustomSettingsFragment : Fragment() {

    private var _binding: OobeFragmentCustomPrefsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = OobeFragmentCustomPrefsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as OOBEActivity).apply {
            nextFragment = 3
            previousFragment = 2
            enableAllButtons()
            updateNextButtonText(this.getString(R.string.next))
            updatePreviousButtonText(this.getString(R.string.back))
            animateBottomBarFromFragment()
            setText(getString(R.string.configureCustomPhone))
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}