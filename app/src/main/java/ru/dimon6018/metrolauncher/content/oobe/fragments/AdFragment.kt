package ru.dimon6018.metrolauncher.content.oobe.fragments

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.lifecycle.lifecycleScope
import coil3.load
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.dimon6018.metrolauncher.R
import ru.dimon6018.metrolauncher.content.oobe.WelcomeActivity
import ru.dimon6018.metrolauncher.databinding.OobeFragmentAdBinding


class AdFragment: Fragment() {

    private var _binding: OobeFragmentAdBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        _binding = OobeFragmentAdBinding.inflate(inflater, container, false)
        (requireActivity() as WelcomeActivity).setText(getString(R.string.advertisement))
        binding.back.setOnClickListener {
            lifecycleScope.launch {
                enterAnimation(true)
                delay(200)
                requireActivity().supportFragmentManager.commit {
                    replace(R.id.fragment_container_view, ConfigureFragment(), "oobe")
                }
            }
        }
        binding.next.setOnClickListener {
            lifecycleScope.launch {
                enterAnimation(true)
                delay(200)
                requireActivity().supportFragmentManager.commit {
                    replace(R.id.fragment_container_view, AppsFragment(), "oobe")
                }
            }
        }
        binding.openN11.setOnClickListener {
            lifecycleScope.launch {
                enterAnimation(true)
                delay(200)
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://github.com/queuejw/neko11")
                    )
                )
            }
        }
        binding.screenshotsAdButtonNeko11.setOnClickListener {
            val dialog = MaterialAlertDialogBuilder(requireActivity())
            val dialogView: View = inflater.inflate(R.layout.ad, null)
            dialog.setView(dialogView)
            dialog.setPositiveButton(getString(android.R.string.ok), null)
            val adImg1 = dialogView.findViewById<ImageView>(R.id.ad_img1)
            val adImg2 = dialogView.findViewById<ImageView>(R.id.ad_img2)
            val adImg3 = dialogView.findViewById<ImageView>(R.id.ad_img3)
            adImg1.load("https://raw.githubusercontent.com/queuejw/Neko11/neko11-stable/screenshots/photo_2024-01-16_16-49-28.jpg")
            adImg2.load("https://raw.githubusercontent.com/queuejw/Neko11/neko11-stable/screenshots/photo_2024-01-16_16-56-34%20(3).jpg")
            adImg3.load("https://raw.githubusercontent.com/queuejw/Neko11/neko11-stable/screenshots/photo_2024-01-16_16-56-34.jpg")
            dialog.show()
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

    override fun onPause() {
        enterAnimation(true)
        super.onPause()
    }
}