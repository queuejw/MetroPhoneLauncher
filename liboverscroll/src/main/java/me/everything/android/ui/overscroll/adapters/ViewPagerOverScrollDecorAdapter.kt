package me.everything.android.ui.overscroll.adapters

import android.view.View
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import me.everything.android.ui.overscroll.HorizontalOverScrollBounceEffectDecorator

/**
 * An adapter to enable over-scrolling over object of [ViewPager]
 *
 * @see HorizontalOverScrollBounceEffectDecorator
 */
class ViewPagerOverScrollDecorAdapter(private val mViewPager: ViewPager) :
    IOverScrollDecoratorAdapter, OnPageChangeListener {
    private var mLastPagerPosition: Int = 0
    private var mLastPagerScrollOffset: Float

    init {
        mViewPager.addOnPageChangeListener(this)

        mLastPagerPosition = mViewPager.currentItem
        mLastPagerScrollOffset = 0f
    }

    override val view: View
        get() = mViewPager

    override val isInAbsoluteStart: Boolean
        get() = mLastPagerPosition == 0 &&
                mLastPagerScrollOffset == 0f

    override val isInAbsoluteEnd: Boolean
        get() = mLastPagerPosition == mViewPager.adapter!!.count - 1 &&
                mLastPagerScrollOffset == 0f

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
        mLastPagerPosition = position
        mLastPagerScrollOffset = positionOffset
    }

    override fun onPageSelected(position: Int) {
    }

    override fun onPageScrollStateChanged(state: Int) {
    }
}
