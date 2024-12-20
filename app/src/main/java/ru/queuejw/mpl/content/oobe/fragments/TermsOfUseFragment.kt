package ru.queuejw.mpl.content.oobe.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import ru.queuejw.mpl.R
import ru.queuejw.mpl.content.oobe.OOBEActivity
import ru.queuejw.mpl.databinding.OobeFragmentTermsofuseBinding

class TermsOfUseFragment: Fragment() {

    private var _binding: OobeFragmentTermsofuseBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = OobeFragmentTermsofuseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as OOBEActivity).apply {
            nextFragment = 2
            previousFragment = 0
            setText(getString(R.string.terms_of_use_label))
            enableAllButtons()
            updateNextButtonText(this.getString(R.string.accept_label))
            updatePreviousButtonText(this.getString(R.string.reject_label))
            animateBottomBarFromFragment()
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}