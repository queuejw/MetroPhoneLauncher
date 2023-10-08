package ru.dimon6018.metrolauncher

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.elevation.SurfaceColors
import ru.dimon6018.metrolauncher.content.AllApps
import ru.dimon6018.metrolauncher.content.Start
import ru.dimon6018.metrolauncher.content.data.DataProviderFragment
import ru.dimon6018.metrolauncher.helpers.AbstractDataProvider

class Main : AppCompatActivity() {

    private var viewPager: ViewPager2? = null
    private var pagerAdapter: FragmentStateAdapter? = null

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_screen_laucnher)
        window.navigationBarColor = SurfaceColors.SURFACE_2.getColor(this)
        val navbar = findViewById<BottomNavigationView>(R.id.navigation)
        supportFragmentManager.beginTransaction()
                .add(DataProviderFragment(), FRAGMENT_TAG_DATA_PROVIDER)
                .commit()
        viewPager = findViewById(R.id.pager)
        pagerAdapter = NumberAdapter(this)
        viewPager?.adapter = pagerAdapter
        navbar.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.start_win -> {
                    viewPager?.currentItem = 0
                    true
                }
                R.id.start_apps -> {
                    viewPager?.currentItem = 1
                    true
                }
                else -> false
            }
        }
    }
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (viewPager!!.currentItem != 0) {
            viewPager!!.currentItem = viewPager!!.currentItem - 1
        } else {
            super.onBackPressed()
        }
    }

    val dataProvider: AbstractDataProvider
        get() {
            val fragment = supportFragmentManager.findFragmentByTag(FRAGMENT_TAG_DATA_PROVIDER)
            return (fragment as DataProviderFragment?)!!.dataProvider
        }

    companion object {
        private const val FRAGMENT_TAG_DATA_PROVIDER = "data provider"
    }
    class NumberAdapter(fragment: FragmentActivity) : FragmentStateAdapter(fragment) {
        override fun getItemCount(): Int = 2

        override fun createFragment(position: Int): Fragment {
            return if (position == 1) AllApps() else Start()
        }
    }
}
