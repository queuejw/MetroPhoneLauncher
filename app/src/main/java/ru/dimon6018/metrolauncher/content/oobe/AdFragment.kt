package ru.dimon6018.metrolauncher.content.oobe

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import coil.load
import com.google.android.material.button.MaterialButton
import ru.dimon6018.metrolauncher.R

class AdFragment: Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.oobe_fragment_ad, container, false)
        val back: MaterialButton = view.findViewById(R.id.back)
        val next: MaterialButton = view.findViewById(R.id.next)
        val open: MaterialButton = view.findViewById(R.id.open)
        val adImg1 = view.findViewById<ImageView>(R.id.ad_img1)
        val adImg2 = view.findViewById<ImageView>(R.id.ad_img2)
        val adImg3 = view.findViewById<ImageView>(R.id.ad_img3)
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
        adImg1.load("https://github.com/queuejw/Neko11/blob/neko11-stable/screenshots/photo_2024-01-16_16-49-28.jpg") {
            crossfade(true)
            placeholder(R.drawable.ic_clock)
        }
        adImg2.load("https://github.com/queuejw/Neko11/blob/neko11-stable/screenshots/photo_2024-01-16_16-56-34%20(3).jpg") {
            crossfade(true)
            placeholder(R.drawable.ic_clock)
        }
        adImg3.load("https://github.com/queuejw/Neko11/blob/neko11-stable/screenshots/photo_2024-01-16_16-56-34.jpg") {
            crossfade(true)
            placeholder(R.drawable.ic_clock)
        }
        return view
    }
}