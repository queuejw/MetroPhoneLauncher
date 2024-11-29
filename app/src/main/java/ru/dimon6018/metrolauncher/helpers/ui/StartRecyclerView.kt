package ru.dimon6018.metrolauncher.helpers.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.view.ViewParent
import ru.dimon6018.metrolauncher.helpers.utils.Utils

// RecyclerView which is used by tiles on the start screen
class StartRecyclerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MetroRecyclerView(context, attrs, defStyleAttr) {

    var isUpdateEnabled = false

    private val overlayPaint by lazy {
        Paint().apply {
            color = Utils.launcherBackgroundColor(context.theme)
        }
    }

    private val clearPaint by lazy {
        Paint().apply {
            xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        }
    }

    private val tempRectF by lazy {
        RectF()
    }
    private val tempMatrix by lazy {
        Matrix()
    }

    override fun dispatchDraw(canvas: Canvas) {
        if (isUpdateEnabled) update(canvas)
        super.dispatchDraw(canvas)
    }

    private fun update(canvas: Canvas) {
        val saveCount = canvas.saveLayer(0f, 0f, width.toFloat(), height.toFloat(), overlayPaint)
        try {
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), overlayPaint)

            for (i in 0 until childCount) {
                val child = getChildAt(i)
                if (getChildViewHolder(child)?.itemViewType == -1) continue

                tempMatrix.reset()
                child.getMatrixTransform(tempMatrix)

                tempRectF.set(0f, 0f, child.width.toFloat(), child.height.toFloat())
                tempMatrix.mapRect(tempRectF)

                canvas.drawRect(tempRectF, clearPaint)
            }
        } finally {
            canvas.restoreToCount(saveCount)
        }
    }

    private fun View.getMatrixTransform(outMatrix: Matrix) {
        outMatrix.set(matrix)
        outMatrix.postTranslate(left.toFloat(), top.toFloat())

        var parentView: ViewParent? = parent
        while (parentView is View) {
            val parentMatrix = Matrix().apply {
                set(parentView.matrix)
                postTranslate(parentView.left.toFloat(), parentView.top.toFloat())
            }
            outMatrix.postConcat(parentMatrix)
            parentView = parentView.parent
        }
    }
}