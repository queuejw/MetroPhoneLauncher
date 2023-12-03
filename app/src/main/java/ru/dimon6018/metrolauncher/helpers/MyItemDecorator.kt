package ru.dimon6018.metrolauncher.helpers

import android.content.Context
import android.graphics.Canvas
import androidx.recyclerview.widget.RecyclerView

class MyItemDecorator(private val context: Context) : RecyclerView.ItemDecoration() {
    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDraw(c, parent, state)
        parent.setBackgroundColor(context.getColor(android.R.color.transparent))
        c.drawColor(context.getColor(android.R.color.transparent))
    }
}