package ru.queuejw.mpl.helpers.ui

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.recyclerview.widget.RecyclerView

/**
 * RecyclerView that can block scrolling
 */
open class MetroRecyclerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : RecyclerView(context, attrs, defStyle) {

    var isScrollEnabled = true

    override fun onTouchEvent(e: MotionEvent): Boolean {
        if (e.action == MotionEvent.ACTION_DOWN) {
            performClick()
        }
        return isScrollEnabled && super.onTouchEvent(e)
    }

    override fun onInterceptTouchEvent(e: MotionEvent): Boolean {
        return isScrollEnabled && super.onInterceptTouchEvent(e)
    }

    override fun performClick(): Boolean {
        return super.performClick()
    }
}