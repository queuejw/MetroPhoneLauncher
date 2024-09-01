package ru.dimon6018.metrolauncher.helpers

import android.util.Log
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import ru.dimon6018.metrolauncher.Application.Companion.PREFS
import ru.dimon6018.metrolauncher.content.NewStart

class ItemTouchCallback(private val mAdapter: NewStart.NewStartAdapter) : ItemTouchHelper.Callback() {
    override fun getMovementFlags(recyclerView: RecyclerView,
                                  viewHolder: RecyclerView.ViewHolder): Int {
        val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN or
                ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        val swipeFlags = 0
        return makeMovementFlags(dragFlags, swipeFlags)
    }
    override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
        Log.d("moved", "move")
        if(!PREFS.isStartBlocked && mAdapter.isEditMode && viewHolder.itemViewType != mAdapter.spaceType) {
            mAdapter.onItemMove(viewHolder.bindingAdapterPosition, target.bindingAdapterPosition)
            return true
        } else {
            return false
        }
    }
    override fun isItemViewSwipeEnabled(): Boolean {
        return false
    }
    override fun isLongPressDragEnabled(): Boolean {
        return mAdapter.isEditMode
    }
    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, i: Int) {}
    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        super.clearView(recyclerView, viewHolder)
        mAdapter.onDragAndDropCompleted(viewHolder)
    }
}