package ru.queuejw.mpl.content.oobe.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import ru.queuejw.mpl.R
import ru.queuejw.mpl.content.oobe.OOBEActivity

class IconPackFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (requireActivity() as OOBEActivity).apply {
            nextFragment = 6
            previousFragment = 3
            setText(this.getString(R.string.icon_packs_label))
            enableAllButtons()
            updateNextButtonText(this.getString(R.string.next))
            updatePreviousButtonText(this.getString(R.string.back))
            animateBottomBarFromFragment()
        }
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}