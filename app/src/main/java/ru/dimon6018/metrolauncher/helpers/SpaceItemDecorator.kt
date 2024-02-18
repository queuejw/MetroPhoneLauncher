package ru.dimon6018.metrolauncher.helpers

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

/**
 * Created by Jorge Martín on 2/6/17.
 */

class SpaceItemDecorator(private val left: Int,
                         private val top: Int,
                         private val right: Int,
                         private val bottom: Int): RecyclerView.ItemDecoration() {


    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        outRect.left = this.left
        outRect.top = this.top
        outRect.right = this.right
        outRect.bottom = this.bottom
    }
}