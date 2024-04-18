package ru.dimon6018.metrolauncher.content.oobe.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import coil3.load
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import ru.dimon6018.metrolauncher.R
import ru.dimon6018.metrolauncher.content.oobe.WelcomeActivity


class AdFragment: Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.oobe_fragment_ad, container, false)
        val back: MaterialButton = view.findViewById(R.id.back)
        val next: MaterialButton = view.findViewById(R.id.next)
        val open: MaterialButton = view.findViewById(R.id.openN11)
        val screenshotsN11: MaterialButton = view.findViewById(R.id.screenshotsAdButtonNeko11)
        WelcomeActivity.setText(requireActivity(), getString(R.string.advertisement))
        back.setOnClickListener {
            requireActivity().supportFragmentManager.commit {
                replace(R.id.fragment_container_view, ConfigureFragment(), "oobe")
            }
        }
        next.setOnClickListener {
            requireActivity().supportFragmentManager.commit {
                replace(R.id.fragment_container_view, AppsFragment(), "oobe")
            }
        }
        open.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/queuejw/neko11")))
        }
        screenshotsN11.setOnClickListener {
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
        return view
    }
}