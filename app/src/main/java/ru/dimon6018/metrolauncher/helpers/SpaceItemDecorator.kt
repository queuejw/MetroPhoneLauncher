@file:JvmName("SpaceItemDecorator")

package ru.dimon6018.metrolauncher.helpers

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

/**
 * Created by Jorge Martín on 2/6/17.
 */

class SpaceItemDecorator(val left: Int,
                         val top: Int,
                         val right: Int,
                         val bottom: Int): RecyclerView.ItemDecoration() {


    constructor(rect: android.graphics.Rect): this(rect.left, rect.top, rect.right, rect.bottom)

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        outRect.left = this.left
        outRect.top = this.top
        outRect.right = this.right
        outRect.bottom = this.bottom
    }
}