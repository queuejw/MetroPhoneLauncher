package ru.queuejw.mpl.content.oobe.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import ru.queuejw.mpl.R
import ru.queuejw.mpl.content.oobe.OOBEActivity
import ru.queuejw.mpl.databinding.OobeFragmentConfBinding

class ConfigureFragment : Fragment() {

    private var _binding: OobeFragmentConfBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = OobeFragmentConfBinding.inflate(inflater, container, false)
        (requireActivity() as OOBEActivity).apply {
            previousFragment = 1
            enableAllButtons()
            updateNextButtonText(this.getString(R.string.next))
            updatePreviousButtonText(this.getString(R.string.back))
            blockBottomBarButton(true)
            animateBottomBarFromFragment()
            setText(getString(R.string.configurePhone))
        }
        binding.custom.setOnClickListener {
            (requireActivity() as OOBEActivity).apply {
                nextFragment = 4
                animateBottomBar(true)
                setFragment(nextFragment)
            }
        }
        binding.recommended.setOnClickListener {
            (requireActivity() as OOBEActivity).apply {
                nextFragment = 3
                animateBottomBar(true)
                setFragment(nextFragment)
            }
        }
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
