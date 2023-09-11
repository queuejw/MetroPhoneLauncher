package ru.dimon6018.metrolauncher.content

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.MotionEventCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
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
    private var mLayoutManager: StaggeredGridLayoutManager? = null
    private var mItemTouchHelper: ItemTouchHelper? = null
    private var mSpannedLayoutManager: SpannedGridLayoutManager? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.main_screen_content, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mRecyclerView = requireView().findViewById(R.id.start_apps_tiles)
        mLayoutManager = StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL)

        //adapter
        val adapter = StartAdapter(dataProvider, this)
        mSpannedLayoutManager = SpannedGridLayoutManager(SpannedGridLayoutManager.Orientation.VERTICAL, 4)
        mSpannedLayoutManager?.spanSizeLookup = SpannedGridLayoutManager.SpanSizeLookup { position ->
                 when (dataProvider.getItem(position).tileSize) {
                    0 -> {
                        SpanSize(1, 1)
                    }
                    1 -> {
                        SpanSize(2, 2)
                    }
                    2 ->  {
                        SpanSize(4, 2)
                    }
                    else -> {
                        SpanSize(1, 1)
                    }
                }
        }
        mRecyclerView?.layoutManager = mSpannedLayoutManager
        mRecyclerView?.addItemDecoration(SpaceItemDecorator(5, 5, 5, 5))
        mRecyclerView?.adapter = adapter
        mRecyclerView?.setHasFixedSize(false)
        val callback: ItemTouchHelper.Callback = ItemTouchCallback(adapter)
        mItemTouchHelper = ItemTouchHelper(callback)
        mItemTouchHelper!!.attachToRecyclerView(mRecyclerView)
    }

    override fun onDestroyView() {
        if (mRecyclerView != null) {
            mRecyclerView!!.itemAnimator = null
            mRecyclerView!!.adapter = null
            mRecyclerView = null
        }
        mLayoutManager = null
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
            val b = mProvider.count
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
        //    setHasStableIds(true)
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
            if(item.tileSize != 0) {
                holder.mTextView.text = item.text
            }
            holder.mAppIcon.setImageDrawable(item.drawable)
            holder.itemView.setOnTouchListener { v: View?, event: MotionEvent? ->
                if (MotionEventCompat.getActionMasked(event) ==
                        MotionEvent.ACTION_DOWN) {
                    mDragStartListener.onStartDrag(holder)
                }
                false
            }
            holder.mAppIcon.setOnClickListener { v: View? ->
                Log.i("holder", "clicked")
                when (item.tileSize) {
                    0 -> Prefs(context).setTileSize(item.getPackage(), 1)
                    1 -> Prefs(context).setTileSize(item.getPackage(), 2)
                    2 -> Prefs(context).setTileSize(item.getPackage(), 0)
                }
                notifyItemChanged(position)
            }
        }
        private fun trySaveAllPositions() {
            var allItemsCount = itemCount;
            while(allItemsCount != 0) {
                val pos = allItemsCount - 1
                val item = dataProvider.getItem(pos)
                allItemsCount -= 1
                Log.i("savePos", "Saving item pos. Item " + item.text + ". New pos: " + pos + " . (Old pos: " + item.tilePos)
                Prefs(context).setPos(item.`package`, pos)
            }
        }
        override fun getItemCount(): Int {
            return mProvider.count
        }
    }
}