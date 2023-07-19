package ru.dimon6018.metrolauncher

import android.os.Bundle
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.elevation.SurfaceColors
import ru.dimon6018.metrolauncher.content.AllApps
import ru.dimon6018.metrolauncher.content.Start

class MainActivity : FragmentActivity() {
    /**
     * The pager widget, which handles animation and allows swiping horizontally to access previous
     * and next wizard steps.
     */
    private var viewPager: ViewPager2? = null
    private var pagerAdapter: FragmentStateAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_screen_laucnher)
        window.navigationBarColor = SurfaceColors.SURFACE_2.getColor(this)
        val navbar = findViewById<BottomNavigationView>(R.id.navigation)
        val right = AnimationUtils.loadAnimation(this, R.anim.slide_left)
        val left = AnimationUtils.loadAnimation(this, R.anim.slide_right)
        viewPager = findViewById(R.id.pager)
        pagerAdapter = NumberAdapter(this)
        viewPager?.adapter = pagerAdapter
        navbar.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.start_win -> {
                    true
                }
                R.id.start_apps -> {
                    true
                }
                else -> false
            }
        }
    }
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (viewPager!!.currentItem == 0) {
            // If the user is currently looking at the first step, allow the system to handle the
            // Back button. This calls finish() on this activity and pops the back stack.
            super.onBackPressed()
        } else {
            // Otherwise, select the previous step.
            viewPager!!.currentItem = viewPager!!.currentItem - 1
        }
    }

    /**
     * A simple pager adapter that represents 5 ScreenSlidePageFragment objects, in
     * sequence.
     */
    class NumberAdapter(fragment: FragmentActivity) : FragmentStateAdapter(fragment) {
        override fun getItemCount(): Int = 2

        override fun createFragment(position: Int): Fragment {
            val fragment = if(position == 1) AllApps() else Start()
            return fragment
        }
    }
}