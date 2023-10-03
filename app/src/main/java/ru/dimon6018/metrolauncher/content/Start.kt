package ru.dimon6018.metrolauncher.content

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.OnLongClickListener
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.MotionEventCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.arasthel.spannedgridlayoutmanager.SpanSize
import com.arasthel.spannedgridlayoutmanager.SpannedGridLayoutManager
import ru.dimon6018.metrolauncher.Main
import ru.dimon6018.metrolauncher.R
import ru.dimon6018.metrolauncher.content.Start.StartAdapter.NormalItemViewHolder
import ru.dimon6018.metrolauncher.content.data.Prefs
import ru.dimon6018.metrolauncher.helpers.AbstractDataProvider
import ru.dimon6018.metrolauncher.helpers.ItemTouchCallback
import ru.dimon6018.metrolauncher.helpers.ItemTouchHelperAdapter
import ru.dimon6018.metrolauncher.helpers.ItemTouchHelperViewHolder
import ru.dimon6018.metrolauncher.helpers.OnStartDragListener
import ru.dimon6018.metrolauncher.helpers.SpaceItemDecorator


class Start : Fragment(), OnStartDragListener {
    private var mRecyclerView: RecyclerView? = null
    private var mItemTouchHelper: ItemTouchHelper? = null
    private var mSpannedLayoutManager: SpannedGridLayoutManager? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.main_screen_content, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mRecyclerView = requireView().findViewById(R.id.start_apps_tiles)
        //adapter
        val adapter = StartAdapter(dataProvider, this)
        mRecyclerView?.addItemDecoration(SpaceItemDecorator(5, 5, 5, 5))
        mSpannedLayoutManager = SpannedGridLayoutManager(SpannedGridLayoutManager.Orientation.VERTICAL, 4)
        mSpannedLayoutManager?.spanSizeLookup = SpannedGridLayoutManager.SpanSizeLookup { position ->
                 when (dataProvider.getItem(position).tileSize) {
                    0 -> { SpanSize(1, 1) }
                    1 -> { SpanSize(2, 2) }
                    2 ->  { SpanSize(4, 2) }
                    else -> { SpanSize(1, 1) }
                }
        }
        mSpannedLayoutManager?.itemOrderIsStable = true
        mRecyclerView?.layoutManager = mSpannedLayoutManager
        mRecyclerView?.adapter = adapter
        val callback: ItemTouchHelper.Callback = ItemTouchCallback(adapter)
        mItemTouchHelper = ItemTouchHelper(callback)
        mItemTouchHelper!!.attachToRecyclerView(mRecyclerView)
    }

    override fun onDestroyView() {
        if (mRecyclerView != null) {
            mRecyclerView!!.itemAnimator = null
            mRecyclerView!!.layoutManager = null
            mRecyclerView!!.adapter = null
            mRecyclerView = null
        }
        super.onDestroyView()
    }

    val dataProvider: AbstractDataProvider
        get() = (requireActivity() as Main).dataProvider

    override fun onStartDrag(viewHolder: RecyclerView.ViewHolder) {
        mItemTouchHelper!!.startDrag(viewHolder)
    }

    inner class StartAdapter(private val mProvider: AbstractDataProvider, private val mDragStartListener: OnStartDragListener) : RecyclerView.Adapter<NormalItemViewHolder>(), ItemTouchHelperAdapter {
        override fun onItemMove(fromPosition: Int, toPosition: Int) {
            Log.i("adapter", "onMoveItem(fromPosition = $fromPosition, toPosition = $toPosition)")
            val item1 = mProvider.getItem(fromPosition)
            val item2 = mProvider.getItem(toPosition)
            Prefs(context).setPos(item1.getPackage(), toPosition)
            Prefs(context).setPos(item2.getPackage(), fromPosition)
            mProvider.moveItem(fromPosition, toPosition)
            notifyItemMoved(fromPosition, toPosition)
            trySaveAllPositions()
        }

        override fun onItemDismiss(position: Int) {}

        inner class NormalItemViewHolder(v: View) : RecyclerView.ViewHolder(v), ItemTouchHelperViewHolder {
            val mContainer: FrameLayout
            val mDragHandle: View
            val mTextView: TextView
            val mAppIcon: ImageView

            init {
                mContainer = v.findViewById(R.id.container)
                mDragHandle = v.findViewById(R.id.drag_handle)
                mTextView = v.findViewById(android.R.id.text1)
                mAppIcon = v.findViewById(android.R.id.icon1)
            }

            override fun onItemSelected() {}
            override fun onItemClear() {}
        }

        init {
            Log.e("Adapter", "Adapter Loaded")
        }

        override fun getItemId(position: Int): Long {
            return mProvider.getItem(position).id
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NormalItemViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val v = inflater.inflate(R.layout.tile, parent, false)
            return NormalItemViewHolder(v)
        }

        override fun onBindViewHolder(holder: NormalItemViewHolder, position: Int) {
            val item = mProvider.getItem(position)
            // set text
            if (item.tileSize != 0) {
                holder.mTextView.text = item.text
            }
            holder.mAppIcon.setImageDrawable(item.drawable)

            holder.mAppIcon.setOnClickListener {
                val intent = context!!.packageManager.getLaunchIntentForPackage((item.`package` as String))
                intent!!.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
            holder.mDragHandle.setOnLongClickListener {
                val wp = WPDialog(activity)
                wp.setTitle(item.text)
                        .setMessage("What do you want to do?")
                        .setTopDialog(true)
                        .setPositiveButton("resize") {
                            when (item.tileSize) {
                                0 -> Prefs(context).setTileSize(item.getPackage(), 1)
                                1 -> Prefs(context).setTileSize(item.getPackage(), 2)
                                2 -> Prefs(context).setTileSize(item.getPackage(), 0)
                            }
                            notifyItemChanged(position)
                            wp.dismiss()
                        }
                        .setNegativeButton("remove") {
                            Prefs(context).removeApp(item.`package`)
                            mProvider.removeItem(position)
                            notifyItemRemoved(position)
                            wp.dismiss()
                        }
                        .setNeutralButton("move") {
                            mDragStartListener.onStartDrag(holder)
                        }
                        .show()
                false
            }
        }
        private fun trySaveAllPositions() {
            var allItemsCount = itemCount
            while(allItemsCount != 0) {
                val pos = allItemsCount - 1
                mRecyclerView?.adapter?.getItemId(pos)
                val item = dataProvider.getItem(pos)
                allItemsCount -= 1
                Log.i("savePos", "Saving item pos. Item " + item.text + ". New pos: " + pos + " . (Old pos: " + item.tilePos)
                Prefs(context).setPos(item.`package`, pos)
            }
        }
        private fun trySaveAllPositionsExp() {
            var allItemsCount = itemCount
            var currentItem = 0
            var nextitem = currentItem + 1;
            while(allItemsCount != 0) {
                allItemsCount -= 1
                currentItem += 1
                nextitem += 1
            }
        }
        override fun getItemCount(): Int {
            return mProvider.count
        }
    }
}