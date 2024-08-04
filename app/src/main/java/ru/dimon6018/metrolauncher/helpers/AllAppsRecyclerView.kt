package ru.dimon6018.metrolauncher.helpers

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.recyclerview.widget.RecyclerView

class AllAppsRecyclerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : RecyclerView(context, attrs, defStyle) {

    var isScrollEnabled = true

    override fun onTouchEvent(e: MotionEvent): Boolean {
        performClick()
        return isScrollEnabled && super.onTouchEvent(e)
    }

    override fun onInterceptTouchEvent(e: MotionEvent): Boolean {
        return isScrollEnabled && super.onInterceptTouchEvent(e)
    }
    override fun performClick(): Boolean {
        return super.performClick()
    }
}